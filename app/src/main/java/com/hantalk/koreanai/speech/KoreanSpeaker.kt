package com.hantalk.koreanai.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Small TextToSpeech wrapper that speaks Korean.
 *
 * TTS is best-effort: if the Korean engine is not installed the app keeps
 * working and [speak] silently does nothing.
 */
class KoreanSpeaker(context: Context) {

    private var ready = false
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.KOREAN)
                ready = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                if (!ready) Log.w(TAG, "Korean TTS voice is not installed on this device")
                tts?.setSpeechRate(0.9f)
                tts?.setPitch(1.0f)
            } else {
                Log.w(TAG, "TextToSpeech init failed: $status")
            }
        }
    }

    /** Speaks [text] in Korean, replacing anything already queued. */
    fun speak(text: String) {
        if (!ready) return
        val clean = text.filter { it.isLetterOrDigit() || it.isWhitespace() || it in "?!.," }
        if (clean.isBlank()) return
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "hantalk-$clean")
    }

    fun stop() {
        runCatching { tts?.stop() }
    }

    fun shutdown() {
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        ready = false
    }

    companion object {
        private const val TAG = "KoreanSpeaker"
    }
}
