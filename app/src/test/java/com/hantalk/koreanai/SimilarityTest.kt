package com.hantalk.koreanai

import com.hantalk.koreanai.domain.text.Similarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimilarityTest {

    @Test
    fun `identical strings score one`() {
        assertEquals(1.0, Similarity.score("안녕하세요", "안녕하세요"), 0.0001)
    }

    @Test
    fun `whitespace and punctuation are ignored`() {
        assertEquals(1.0, Similarity.score("안녕하세요", " 안녕하세요! "), 0.0001)
    }

    @Test
    fun `empty input scores zero against a phrase`() {
        assertEquals(0.0, Similarity.score("안녕", ""), 0.0001)
    }

    @Test
    fun `one wrong character is penalised slightly`() {
        val score = Similarity.score("안녕하세요", "안녕하세여")
        assertTrue("expected high score but was $score", score > 0.7)
        assertTrue("expected below perfect but was $score", score < 1.0)
    }

    @Test
    fun `unrelated strings score low`() {
        val score = Similarity.score("안녕하세요", "감사합니다")
        assertTrue("expected low score but was $score", score < 0.4)
    }

    @Test
    fun `levenshtein handles classic cases`() {
        assertEquals(0, Similarity.levenshtein("abc", "abc"))
        assertEquals(3, Similarity.levenshtein("", "abc"))
        assertEquals(1, Similarity.levenshtein("kitten", "sitten"))
        assertEquals(3, Similarity.levenshtein("kitten", "sitting"))
    }

    @Test
    fun `verdict buckets never throw`() {
        for (i in 0..100) {
            assertTrue(Similarity.verdict(i / 100.0).isNotEmpty())
        }
    }
}
