package com.hantalk.koreanai.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Small TextToSpeech wrapper that speaks Korean and reports when it is talking,
 * so the voice orb can animate along with the audio.
 *
 * TTS is best-effort: if the Korean voice is not installed the app keeps
 * working, [speak] silently does nothing and [isAvailable] stays false.
 */
class KoreanSpeaker(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    /** Speech requested before the engine finished starting up. */
    private var pending: String? = null

    init {
        tts = TextToSpeech(appContext) { status -> onInitialised(status) }
    }

    private fun onInitialised(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TextToSpeech init failed: $status")
            _isAvailable.value = false
            return
        }
        val engine = tts ?: return
        val result = engine.setLanguage(Locale.KOREAN)
        val ok = result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
        if (!ok) Log.w(TAG, "Korean TTS voice is not installed on this device")
        engine.setSpeechRate(0.92f)
        engine.setPitch(1.0f)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
        _isAvailable.value = ok
        val queued = pending
        pending = null
        if (ok && queued != null) speak(queued)
    }

    /** Speaks [text] in Korean, replacing anything already queued. */
    fun speak(text: String, rate: Float? = null) {
        val clean = text.filter { it.isLetterOrDigit() || it.isWhitespace() || it in "?!.," }
        if (clean.isBlank()) return
        // The engine starts asynchronously, so remember the first request.
        if (!_isAvailable.value) {
            pending = text
            return
        }
        val engine = tts ?: return
        if (rate != null) engine.setSpeechRate(rate)
        engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "hantalk-${clean.hashCode()}")
    }

    /** Speaks more slowly — used for first-time pronunciation help. */
    fun speakSlowly(text: String) = speak(text, rate = 0.7f)

    fun stop() {
        runCatching { tts?.stop() }
        _isSpeaking.value = false
    }

    fun shutdown() {
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        _isSpeaking.value = false
        _isAvailable.value = false
    }

    companion object {
        private const val TAG = "KoreanSpeaker"
    }
}
