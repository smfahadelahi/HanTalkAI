package com.hantalk.koreanai.llm

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.hantalk.koreanai.data.ModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

sealed interface EngineStatus {
    data object NoModel : EngineStatus
    data class Loading(val modelName: String) : EngineStatus
    data class Ready(val modelName: String) : EngineStatus
    data class Failed(val message: String) : EngineStatus
}

/**
 * Thin, null-safe wrapper around MediaPipe's on-device LLM.
 *
 * Design rules:
 *  • The model is loaded once; a lightweight session is created per request so
 *    the context window can never grow without bound.
 *  • Every MediaPipe call stays off the main thread.
 *  • Any native failure degrades to [fallback] instead of crashing the app.
 *  • The engine is optional: the app is fully usable without a model.
 */
class LlmEngine(
    private val context: Context,
    private val fallback: (String) -> String = LocalTutor::reply
) {
    @Volatile
    private var inference: LlmInference? = null

    @Volatile
    var status: EngineStatus = EngineStatus.NoModel
        private set

    val isReady: Boolean get() = inference != null

    val loadedModelName: String? get() = (status as? EngineStatus.Ready)?.modelName

    /** Loads [spec] from [ModelStore]. Safe to call repeatedly. */
    suspend fun load(spec: ModelSpec): Result<Unit> = withContext(Dispatchers.Default) {
        if (inference != null && (status as? EngineStatus.Ready)?.modelName == spec.displayName) {
            return@withContext Result.success(Unit)
        }

        status = EngineStatus.Loading(spec.displayName)

        val file: File = ModelStore.modelFile(context, spec)
        if (!file.exists() || file.length() < MIN_VALID_MODEL_BYTES) {
            status = EngineStatus.NoModel
            return@withContext Result.failure(
                IllegalStateException("Model file is missing. Download it first.")
            )
        }

        release()

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(file.absolutePath)
                .setMaxTokens(1024)
                .build()

            inference = LlmInference.createFromOptions(context, options)
            status = EngineStatus.Ready(spec.displayName)
            Log.i(TAG, "Loaded ${spec.displayName} (${file.length() / 1024 / 1024} MB)")
            Result.success(Unit)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load ${spec.id}", t)
            release()
            status = EngineStatus.Failed(t.message ?: "Could not start the on-device model.")
            Result.failure(t)
        }
    }

    /**
     * Streams a reply token-by-token. If no model is loaded — or generation
     * fails — emits the rule-based answer so the UI never dead-ends.
     */
    fun streamReply(prompt: String, systemPrompt: String = SYSTEM_PROMPT): Flow<String> {
        val engine = inference
        if (engine == null) {
            return flow { emit(fallback(prompt)) }.flowOn(Dispatchers.Default)
        }

        return callbackFlow {
            val accumulated = StringBuilder()
            var session: LlmInferenceSession? = null
            try {
                session = createSession(engine)
                session.addQueryChunk(systemPrompt)
                session.addQueryChunk(prompt)
                session.generateResponseAsync { partial, done ->
                    accumulated.append(partial)
                    trySend(accumulated.toString())
                    if (done) close()
                }
                awaitClose { runCatching { session?.close() } }
            } catch (t: Throwable) {
                Log.e(TAG, "Generation failed, falling back", t)
                trySend(fallback(prompt))
                runCatching { session?.close() }
                close()
            }
        }.flowOn(Dispatchers.Default)
    }

    /** Blocking single-shot reply, used by "explain this" style actions. */
    suspend fun reply(prompt: String, systemPrompt: String = SYSTEM_PROMPT): String =
        withContext(Dispatchers.Default) {
            val engine = inference ?: return@withContext fallback(prompt)
            var session: LlmInferenceSession? = null
            try {
                session = createSession(engine)
                session.addQueryChunk(systemPrompt)
                session.addQueryChunk(prompt)
                session.generateResponse()
            } catch (t: Throwable) {
                Log.e(TAG, "reply() failed", t)
                fallback(prompt)
            } finally {
                runCatching { session?.close() }
            }
        }

    private fun createSession(engine: LlmInference): LlmInferenceSession {
        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setTopK(40)
            .setTopP(0.95f)
            .setTemperature(0.7f)
            .build()
        return LlmInferenceSession.createFromOptions(engine, sessionOptions)
    }

    fun release() {
        runCatching { inference?.close() }
        inference = null
        status = EngineStatus.NoModel
    }

    companion object {
        private const val TAG = "LlmEngine"
        private const val MIN_VALID_MODEL_BYTES = 50L * 1024L * 1024L

        val SYSTEM_PROMPT: String = """
            You are HanTalk, a warm and patient Korean language tutor for English speakers.
            Rules:
            1. Answer in English, but always give Korean examples with romanisation.
            2. Keep answers short: at most 6 short lines or 3 bullet points.
            3. Use plain text only. No markdown tables and no emoji spam.
            4. If the learner writes Korean, gently correct mistakes and explain why.
            5. If you are unsure, say so instead of inventing grammar.
            6. Never mention that you are a language model.
        """.trimIndent()
    }
}
