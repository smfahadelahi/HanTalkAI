package com.hantalk.koreanai.domain.text

/**
 * Small text utilities used by the pronunciation coach and the tutor.
 */
object Similarity {

    /** Classic Levenshtein edit distance over Unicode code points. */
    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)

        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                val deletion = previous[j] + 1
                val insertion = current[j - 1] + 1
                val substitution = previous[j - 1] + cost
                current[j] = minOf(deletion, insertion, substitution)
            }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[b.length]
    }

    /**
     * Similarity in `0.0..1.0`, where `1.0` means the strings are identical
     * after normalisation (whitespace, punctuation and case are ignored).
     */
    fun score(expected: String, actual: String): Double {
        val e = normalise(expected)
        val a = normalise(actual)
        if (e.isEmpty() && a.isEmpty()) return 1.0
        if (e.isEmpty() || a.isEmpty()) return 0.0
        val distance = levenshtein(e, a)
        return (1.0 - distance.toDouble() / maxOf(e.length, a.length)).coerceIn(0.0, 1.0)
    }

    /** Keeps Hangul, Latin letters and digits; lowercases the rest away. */
    fun normalise(text: String): String =
        text.trim().lowercase().filter { it.isLetterOrDigit() }

    /** A short, friendly verdict for a pronunciation attempt. */
    fun verdict(score: Double): String = when {
        score >= 0.95 -> "완벽해요! Perfect."
        score >= 0.80 -> "아주 좋아요! Very good."
        score >= 0.60 -> "좋아요. Keep practising."
        score >= 0.35 -> "거의 다 왔어요. Almost there."
        else -> "다시 한번 해 보세요. Try once more."
    }
}
