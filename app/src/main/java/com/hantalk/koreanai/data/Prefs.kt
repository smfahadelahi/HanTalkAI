package com.hantalk.koreanai.data

import android.content.Context

/** Tiny SharedPreferences wrapper — no DI framework needed for this size of app. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("hantalk_prefs", Context.MODE_PRIVATE)

    var activeModelId: String?
        get() = sp.getString(KEY_ACTIVE_MODEL, null)
        set(value) = sp.edit().putString(KEY_ACTIVE_MODEL, value).apply()

    var lastScreen: Int
        get() = sp.getInt(KEY_LAST_SCREEN, 0)
        set(value) = sp.edit().putInt(KEY_LAST_SCREEN, value).apply()

    /** Total number of phrases practised, shown on the home card. */
    var practiceCount: Int
        get() = sp.getInt(KEY_PRACTICE_COUNT, 0)
        set(value) = sp.edit().putInt(KEY_PRACTICE_COUNT, value).apply()

    var bestScore: Int
        get() = sp.getInt(KEY_BEST_SCORE, 0)
        set(value) = sp.edit().putInt(KEY_BEST_SCORE, value).apply()

    /** Consecutive days with at least one practice session. */
    var streakDays: Int
        get() = sp.getInt(KEY_STREAK, 0)
        set(value) = sp.edit().putInt(KEY_STREAK, value).apply()

    var lastPracticeDay: Long
        get() = sp.getLong(KEY_LAST_PRACTICE_DAY, 0L)
        set(value) = sp.edit().putLong(KEY_LAST_PRACTICE_DAY, value).apply()

    var learnedLetters: Set<String>
        get() = sp.getStringSet(KEY_LEARNED_LETTERS, emptySet()) ?: emptySet()
        set(value) = sp.edit().putStringSet(KEY_LEARNED_LETTERS, value).apply()

    fun toggleLetter(letter: String): Set<String> {
        val current = learnedLetters.toMutableSet()
        if (!current.add(letter)) current.remove(letter)
        learnedLetters = current
        return current
    }

    /**
     * Records a practice session and returns the new streak.
     * Uses epoch days so a session yesterday keeps the streak alive.
     */
    fun recordPractice(scorePercent: Int): Int {
        practiceCount += 1
        if (scorePercent > bestScore) bestScore = scorePercent

        val today = System.currentTimeMillis() / 86_400_000L
        val last = lastPracticeDay
        streakDays = when {
            last == 0L -> 1
            last == today -> streakDays
            last == today - 1 -> streakDays + 1
            else -> 1
        }
        lastPracticeDay = today
        return streakDays
    }

    private companion object {
        const val KEY_ACTIVE_MODEL = "active_model_id"
        const val KEY_LAST_SCREEN = "last_screen"
        const val KEY_PRACTICE_COUNT = "practice_count"
        const val KEY_BEST_SCORE = "best_score"
        const val KEY_STREAK = "streak_days"
        const val KEY_LAST_PRACTICE_DAY = "last_practice_day"
        const val KEY_LEARNED_LETTERS = "learned_letters"
    }
}
