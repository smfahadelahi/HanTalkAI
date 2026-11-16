package com.hantalk.koreanai.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Thin wrapper around Android's on-device [SpeechRecognizer] for Korean.
 *
 * Speech recognition is a *system* service: it may be missing, it may have no
 * Korean pack installed, and it may be slow. Every one of those cases is
 * surfaced as [Phase.Unavailable] / [Phase.Error] with a readable [State.message]
 * so the voice screen can fall back to typing instead of breaking.
 */
class VoiceRecognizer(private val context: Context) {

    enum class Phase { Idle, Listening, Working, Done, Error, Unavailable }

    data class State(
        val phase: Phase = Phase.Idle,
        val partial: String = "",
        val text: String = "",
        /** 0f..1f microphone loudness, used to drive the orb animation. */
        val level: Float = 0f,
        val message: String? = null
    ) {
        val isListening: Boolean get() = phase == Phase.Listening
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    /** True when a speech service exists and the microphone permission is held. */
    fun canListen(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context) && hasPermission()

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /** Starts a single Korean utterance. Safe to call repeatedly. */
    fun start(locale: Locale = Locale.KOREAN) {
        if (!hasPermission()) {
            _state.value = State(
                phase = Phase.Unavailable,
                message = "Microphone permission is needed to listen."
            )
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = State(
                phase = Phase.Unavailable,
                message = "No speech service on this device — type your answer instead."
            )
            return
        }

        stopListening()
        val engine = recognizer ?: createRecognizer().also { recognizer = it }
        _state.value = State(phase = Phase.Listening)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1600L)
        }
        runCatching { engine?.startListening(intent) }
            .onFailure {
                _state.value = State(
                    phase = Phase.Error,
                    message = "Could not start the microphone. Try again."
                )
            }
    }

    fun stopListening() {
        runCatching { recognizer?.stopListening() }
    }

    fun cancel() {
        runCatching { recognizer?.cancel() }
        _state.value = State()
    }

    fun release() {
        runCatching { recognizer?.cancel() }
        runCatching { recognizer?.destroy() }
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer? =
        runCatching { SpeechRecognizer.createSpeechRecognizer(context) }
            .onFailure { _state.value = State(phase = Phase.Unavailable, message = "Speech unavailable.") }
            .getOrNull()
            ?.also { it.setRecognitionListener(listener) }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = _state.value.copy(phase = Phase.Listening, message = null)
        }

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) {
            // rmsdB is roughly -2..10; normalise into 0..1 for the animation.
            val level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            if (level > _state.value.level) _state.value = _state.value.copy(level = level)
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            _state.value = _state.value.copy(phase = Phase.Working, level = 0f)
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                    "I didn't catch that — tap the mic and try again."

                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                    "Microphone permission is needed to listen."

                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                    "The speech service needs a connection for this voice. You can type instead."

                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "The microphone is busy — try again."
                SpeechRecognizer.ERROR_AUDIO -> "Microphone problem — check that nothing else is using it."
                else -> "I couldn't hear that. Try again, a little closer to the mic."
            }
            _state.value = State(phase = Phase.Error, message = message)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            _state.value = State(
                phase = if (text.isBlank()) Phase.Error else Phase.Done,
                text = text,
                message = if (text.isBlank()) "Nothing was recognised. Try again." else null
            )
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotBlank()) {
                _state.value = _state.value.copy(partial = text, phase = Phase.Listening)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
