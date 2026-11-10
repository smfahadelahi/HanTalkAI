package com.hantalk.koreanai

import com.hantalk.koreanai.domain.hangul.Hangul
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HangulTest {

    @Test
    fun `decomposes 한 into h a n jamo`() {
        val s = Hangul.decompose('한')
        assertNotNull(s)
        assertEquals('ㅎ', s!!.initial)
        assertEquals('ㅏ', s.medial)
        assertEquals('ㄴ', s.finalJamo)
        assertTrue(s.hasBatchim)
    }

    @Test
    fun `decomposes a syllable with no batchim`() {
        val s = Hangul.decompose('나')
        assertNotNull(s)
        assertFalse(s!!.hasBatchim)
        assertNull(s.finalJamo)
    }

    @Test
    fun `returns null for non hangul`() {
        assertNull(Hangul.decompose('a'))
        assertNull(Hangul.decompose('1'))
        assertNull(Hangul.decompose(' '))
    }

    @Test
    fun `compose and decompose round trip for every syllable`() {
        // 0xAC00 .. 0xD7A3 is the full pre-composed syllable range.
        for (code in 0xAC00..0xD7A3) {
            val c = code.toChar()
            val s = Hangul.decompose(c)!!
            val rebuilt = Hangul.compose(s.initial, s.medial, s.finalJamo)
            assertEquals("round trip failed for $c", c, rebuilt)
        }
    }

    @Test
    fun `compose rejects invalid combinations`() {
        assertNull(Hangul.compose('가', 'ㅏ'))
        assertNull(Hangul.compose('ㄱ', 'a'))
    }

    @Test
    fun `counts syllables only`() {
        assertEquals(5, Hangul.syllableCount("안녕하세요"))
        assertEquals(2, Hangul.syllableCount("한글 123"))
    }

    @Test
    fun `romanises single syllables`() {
        assertEquals("han", Hangul.romanizeSyllable('한'))
        assertEquals("geul", Hangul.romanizeSyllable('글'))
        assertEquals("na", Hangul.romanizeSyllable('나'))
    }

    @Test
    fun `romanises words with liaison`() {
        // 한국어 -> 한구거 -> hangugeo
        assertEquals("hangugeo", Hangul.romanize("한국어"))
    }

    @Test
    fun `final ieung never moves to the next syllable`() {
        // 학생이에요 keeps the ng: haksaeng + ieyo
        assertEquals("jeoneun haksaengieyo", Hangul.romanize("저는 학생이에요"))
        assertEquals("saeng", Hangul.romanize("생"))
    }

    @Test
    fun `nasalisation before n or m`() {
        // 합니다 is pronounced hamnida, not hapnida
        assertEquals("gamsahamnida", Hangul.romanize("감사합니다"))
        assertEquals("bangapseumnida", Hangul.romanize("반갑습니다"))
        assertEquals("joesonghamnida", Hangul.romanize("죄송합니다"))
    }

    @Test
    fun `liaison and nasalisation combine in a sentence`() {
        assertEquals("je ireumeun sarayeyo", Hangul.romanize("제 이름은 사라예요"))
        assertEquals("jeoneun migugeseo wasseoyo", Hangul.romanize("저는 미국에서 왔어요"))
    }

    @Test
    fun `romanises simple greeting`() {
        assertEquals("annyeonghaseyo", Hangul.romanize("안녕하세요"))
    }

    @Test
    fun `romanise leaves latin text untouched`() {
        assertTrue(Hangul.romanize("hi 123").contains("hi"))
    }

    @Test
    fun `jamo string formats blocks`() {
        assertEquals("ㅎ ㅏ ㄴ", Hangul.decompose('한')!!.jamoString())
        assertEquals("ㄱ ㅏ", Hangul.decompose('가')!!.jamoString())
    }
}
