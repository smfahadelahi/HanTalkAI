package com.hantalk.koreanai.domain.hangul

/**
 * A single decomposed Hangul syllable block: 초성 (initial), 중성 (medial), 종성 (final).
 * [finalJamo] is `null` when the syllable has no 받침 (batchim).
 */
data class JamoSyllable(
    val initial: Char,
    val medial: Char,
    val finalJamo: Char?
) {
    /** True when this syllable carries a 받침. */
    val hasBatchim: Boolean get() = finalJamo != null

    /** e.g. "ㄱ ㅏ" or "ㄱ ㅏ ㄴ" */
    fun jamoString(): String =
        if (finalJamo == null) "$initial $medial" else "$initial $medial $finalJamo"
}

/**
 * Pure-Kotlin 한글 helpers: syllable decomposition, composition and
 * Revised-Romanization-style romanisation.
 *
 * All functions are side-effect free so they can be unit tested on the JVM.
 */
object Hangul {

    private const val S_BASE = 0xAC00
    private const val L_BASE = 0x1100
    private const val V_BASE = 0x1161
    private const val T_BASE = 0x11A7

    const val L_COUNT = 19
    const val V_COUNT = 21
    const val T_COUNT = 28
    const val N_COUNT = V_COUNT * T_COUNT // 588
    const val S_COUNT = L_COUNT * N_COUNT // 11172

    /** 19 initial consonants, in Unicode order (U+1100 …). */
    val INITIALS: List<Char> = listOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    /** 21 medial vowels, in Unicode order (U+1161 …). */
    val MEDIALS: List<Char> = listOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ',
        'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )

    /** 28 trailing consonants: index 0 means "no 받침". */
    val FINALS: List<Char?> = listOf(
        null, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
        'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ',
        'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    private val INITIAL_ROMAN = listOf(
        "g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s",
        "ss", "", "j", "jj", "ch", "k", "t", "p", "h"
    )

    private val MEDIAL_ROMAN = listOf(
        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae",
        "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "ui", "i"
    )

    /** Romanisation of a syllable-final consonant in isolation. */
    private val FINAL_ROMAN = listOf(
        "", "k", "k", "k", "n", "n", "n", "t", "l", "k", "m",
        "l", "l", "l", "p", "l", "m", "p", "p", "t", "t",
        "ng", "t", "t", "k", "t", "p", "t"
    )

    /** Single representative sound of a final, used for resyllabification. */
    private val FINAL_TO_INITIAL = mapOf(
        'ㄱ' to 'ㄱ', 'ㄲ' to 'ㄲ', 'ㄳ' to 'ㄱ', 'ㄴ' to 'ㄴ', 'ㄵ' to 'ㄴ',
        'ㄶ' to 'ㄴ', 'ㄷ' to 'ㄷ', 'ㄹ' to 'ㄹ', 'ㄺ' to 'ㄹ', 'ㄻ' to 'ㄹ',
        'ㄼ' to 'ㄹ', 'ㄽ' to 'ㄹ', 'ㄾ' to 'ㄹ', 'ㄿ' to 'ㅍ', 'ㅀ' to 'ㄹ',
        'ㅁ' to 'ㅁ', 'ㅂ' to 'ㅂ', 'ㅄ' to 'ㅂ', 'ㅅ' to 'ㅅ', 'ㅆ' to 'ㅆ',
        'ㅈ' to 'ㅈ', 'ㅊ' to 'ㅊ', 'ㅋ' to 'ㅋ', 'ㅌ' to 'ㅌ',
        'ㅍ' to 'ㅍ', 'ㅎ' to 'ㅎ'
        // Note: final ㅇ is deliberately absent — it never moves into the next
        // syllable, it stays a nasal "ng" (학생이에요 → haksaengieyo).
    )

    /**
     * Nasalisation before ㄴ/ㅁ — the single most common sound change in
     * Korean, and the one every learner meets first (합니다 → hamnida).
     */
    private val NASALISE_BEFORE_NASAL = mapOf(
        'ㄱ' to 'ㅇ', 'ㄲ' to 'ㅇ', 'ㅋ' to 'ㅇ', 'ㄳ' to 'ㅇ', 'ㄺ' to 'ㅇ',
        'ㄷ' to 'ㄴ', 'ㅅ' to 'ㄴ', 'ㅆ' to 'ㄴ', 'ㅈ' to 'ㄴ', 'ㅊ' to 'ㄴ',
        'ㅌ' to 'ㄴ', 'ㅎ' to 'ㄴ',
        'ㅂ' to 'ㅁ', 'ㅍ' to 'ㅁ', 'ㅄ' to 'ㅁ', 'ㄼ' to 'ㅁ',
        'ㄵ' to 'ㄴ', 'ㄶ' to 'ㄴ', 'ㄻ' to 'ㅁ'
    )

    /** True if [c] is a pre-composed Hangul syllable (가 … 힣). */
    fun isSyllable(c: Char): Boolean = c.code in S_BASE until (S_BASE + S_COUNT)

    /** True if [c] is a compatibility jamo consonant (ㄱ … ㅎ). */
    fun isConsonantJamo(c: Char): Boolean = c in INITIALS

    /** True if [c] is a compatibility jamo vowel (ㅏ … ㅣ). */
    fun isVowelJamo(c: Char): Boolean = c in MEDIALS

    /** True if [c] is a final-cluster jamo that is not a plain initial (e.g. ㄳ, ㄺ). */
    fun isClusterFinal(c: Char): Boolean = FINALS.contains(c) && c !in INITIALS

    /** Splits a pre-composed syllable into its three jamo. Returns `null` for non-Hangul. */
    fun decompose(c: Char): JamoSyllable? {
        if (!isSyllable(c)) return null
        val index = c.code - S_BASE
        val initial = INITIALS[index / N_COUNT]
        val medial = MEDIALS[(index % N_COUNT) / T_COUNT]
        val finalJamo = FINALS[index % T_COUNT]
        return JamoSyllable(initial, medial, finalJamo)
    }

    /**
     * Combines jamo into a pre-composed syllable.
     * Returns `null` when the combination is not a valid syllable.
     */
    fun compose(initial: Char, medial: Char, finalJamo: Char? = null): Char? {
        val l = INITIALS.indexOf(initial)
        val v = MEDIALS.indexOf(medial)
        val t = FINALS.indexOf(finalJamo)
        if (l < 0 || v < 0 || t < 0) return null
        return (S_BASE + (l * N_COUNT) + (v * T_COUNT) + t).toChar()
    }

    /** Number of pre-composed syllables in [text]. */
    fun syllableCount(text: String): Int = text.count { isSyllable(it) }

    /** Number of characters in [text] (Hangul syllables and everything else). */
    fun charCount(text: String): Int = text.trim().length

    /**
     * Syllable-level romanisation of a single syllable block, without
     * cross-syllable sound changes. Used by the alphabet lessons.
     */
    fun romanizeSyllable(c: Char): String {
        val s = decompose(c) ?: return c.toString()
        val l = INITIALS.indexOf(s.initial)
        val v = MEDIALS.indexOf(s.medial)
        val t = FINALS.indexOf(s.finalJamo)
        if (l < 0 || v < 0 || t < 0) return c.toString()
        return INITIAL_ROMAN[l] + MEDIAL_ROMAN[v] + FINAL_ROMAN[t]
    }

    /**
     * Romanises a whole word using Revised-Romanization-style rules plus the
     * most common liaison rule (받침 followed by a vowel-initial syllable moves
     * into the next syllable: 한국어 → 한구거 → hangugeo).
     *
     * This is a learning aid, not a phonology engine: it does not model every
     * nasalisation or tensification case.
     */
    fun romanize(text: String): String {
        val syllables = text.map { decompose(it) }
        val out = StringBuilder()

        // A final consonant moved into the NEXT syllable (한국어 → 한구거).
        var carriedInitial: Char? = null

        for (i in syllables.indices) {
            val s = syllables[i]
            if (s == null) {
                // Non-Hangul character: nothing can flow across it.
                carriedInitial = null
                out.append(text[i])
                continue
            }

            val initialChar = carriedInitial ?: s.initial
            carriedInitial = null

            var finalChar = s.finalJamo
            val next = syllables.getOrNull(i + 1)
            if (finalChar != null && next != null && next.initial == 'ㅇ' && next.finalJamo == null) {
                // Liaison: the final becomes the next syllable's initial.
                val moved = FINAL_TO_INITIAL[finalChar]
                if (moved == 'ㅎ') {
                    // ㅎ blends away before a vowel: 좋아 → joa
                    finalChar = null
                } else if (moved != null) {
                    carriedInitial = moved
                    finalChar = null
                }
            } else if (finalChar != null && next != null &&
                (next.initial == 'ㄴ' || next.initial == 'ㅁ')
            ) {
                // Nasalisation: 합니다 → hamnida, 국물 → gungmul
                finalChar = NASALISE_BEFORE_NASAL[finalChar] ?: finalChar
            }

            val l = INITIALS.indexOf(initialChar)
            val v = MEDIALS.indexOf(s.medial)
            val t = FINALS.indexOf(finalChar)
            if (l < 0 || v < 0 || t < 0) {
                out.append(text[i])
            } else {
                out.append(INITIAL_ROMAN[l]).append(MEDIAL_ROMAN[v]).append(FINAL_ROMAN[t])
            }
        }
        return out.toString()
    }

    /** Uppercase-first romanisation, handy for labels. */
    fun romanizeTitle(text: String): String {
        val r = romanize(text)
        return r.replaceFirstChar { it.uppercaseChar() }
    }
}
