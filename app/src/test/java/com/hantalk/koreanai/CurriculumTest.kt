package com.hantalk.koreanai

import com.hantalk.koreanai.domain.Curriculum
import com.hantalk.koreanai.domain.hangul.Hangul
import com.hantalk.koreanai.llm.LocalTutor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {

    @Test
    fun `lessons are non empty and have unique ids`() {
        assertTrue(Curriculum.lessons.size >= 6)
        val ids = Curriculum.lessons.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `every phrase has korean english and romanisation`() {
        Curriculum.allPhrases.forEach { phrase ->
            assertTrue("empty korean in ${phrase.english}", phrase.korean.isNotBlank())
            assertTrue("empty english in ${phrase.korean}", phrase.english.isNotBlank())
            assertTrue("empty roman in ${phrase.korean}", phrase.romanisation.isNotBlank())
            // Every Korean phrase should actually contain Hangul syllables.
            assertTrue("no hangul in ${phrase.korean}", Hangul.syllableCount(phrase.korean) > 0)
        }
    }

    @Test
    fun `romanisation of a phrase has no hangul left`() {
        Curriculum.allPhrases.forEach { phrase ->
            val roman = phrase.romanisation
            val leftover = roman.filter { Hangul.isSyllable(it) }
            assertTrue("hangul left in romanisation of ${phrase.korean}: $leftover", leftover.isEmpty())
        }
    }

    @Test
    fun `lookup by id works`() {
        assertNotNull(Curriculum.lesson("greetings"))
        assertEquals("인사", Curriculum.lesson("greetings")!!.titleKorean)
    }

    @Test
    fun `alphabet letters are all valid jamo`() {
        Curriculum.consonants.forEach {
            assertTrue("bad consonant ${it.jamo}", Hangul.isConsonantJamo(it.jamo.first()))
        }
        Curriculum.vowels.forEach {
            assertTrue("bad vowel ${it.jamo}", Hangul.isVowelJamo(it.jamo.first()))
        }
    }

    @Test
    fun `every letter has an example word`() {
        (Curriculum.consonants + Curriculum.vowels).forEach {
            assertNotNull("missing example for ${it.jamo}", Curriculum.letterExamples[it.jamo])
        }
    }

    @Test
    fun `local tutor always answers`() {
        val prompts = listOf(
            "", "hello", "batchim", "numbers", "grammar", "안녕하세요",
            "한국어를 배워요", "???", "a very long unrelated question about the weather"
        )
        prompts.forEach { prompt ->
            val reply = LocalTutor.reply(prompt)
            assertTrue("empty reply for '$prompt'", reply.isNotBlank())
        }
    }

    @Test
    fun `tutor explains hangul when asked about the alphabet`() {
        val reply = LocalTutor.reply("teach me the hangul alphabet")
        assertTrue(reply.contains("Consonants"))
    }

    @Test
    fun `tutor breaks down typed korean`() {
        val reply = LocalTutor.reply("한국어")
        assertTrue(reply.contains("Jamo breakdown"))
    }

    @Test
    fun `tutor does not loop on punctuation only`() {
        assertFalse(LocalTutor.reply("!!!").isBlank())
    }
}
