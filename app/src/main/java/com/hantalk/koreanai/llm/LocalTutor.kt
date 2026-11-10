package com.hantalk.koreanai.llm

import com.hantalk.koreanai.domain.hangul.Hangul
import kotlin.math.abs

/**
 * A deterministic, zero-dependency Korean tutor.
 *
 * It is used (a) when no on-device model is installed and (b) as the automatic
 * fallback if the model fails to load. It answers the questions a beginner
 * actually asks — alphabet, batchim, romanisation, greetings, numbers — and
 * always stays in character.
 */
object LocalTutor {

    private data class Rule(val keywords: List<String>, val answer: (String) -> String)

    private val rules: List<Rule> = listOf(
        Rule(listOf("안녕", "hello", "hi ", "greeting", "인사")) {
            """
            |안녕하세요! 👋 Nice to meet you.
            |
            |**Greetings you can use right now**
            ||Korean|Romanisation|When|
            ||---|---|---|
            ||안녕하세요|annyeonghaseyo|Polite, any time|
            ||안녕|annyeong|Casual, close friends|
            ||안녕히 계세요|annyeonghi gyeseyo|You are leaving|
            ||안녕히 가세요|annyeonghi gaseyo|The other person is leaving|
            |
            |**자가소개 (self-introduction)**
            |저는 ___ 입니다. *jeoneun ___ imnida.* — “I am ___.”
            |
            |Try writing: 안녕하세요, 저는 민수입니다.
            """.trimMargin()
        },
        Rule(listOf("batchim", "받침", "final consonant", "종성")) {
            """
            |**받침 (batchim)** is the final consonant of a syllable block.
            |
            |A block is 초성 + 중성 (+ 종성):
            |**한** = ㅎ + ㅏ + ㄴ → h + a + n
            |**글** = ㄱ + ㅡ + ㄹ → g + eu + l
            |
            |Only 7 sounds are actually pronounced at the end: **ㄱ, ㄴ, ㄷ, ㄹ, ㅁ, ㅂ, ㅇ**.
            |So ㅅ, ㅆ, ㅈ, ㅊ, ㅌ, ㅎ all reduce to a ㄷ-like sound:
            |**옷** (clothes) → *ot*, **있어요** → *isseoyo*
            |
            |Quick check — does **밥** have a batchim? Yes: ㅂ. Does **나** have one? No.
            """.trimMargin()
        },
        Rule(listOf("alphabet", "한글", "hangul", "consonant", "vowel", "자모", "letter")) {
            """
            |**한글 in one screen**
            |
            |Consonants (14 basic): ㄱ ㄴ ㄷ ㄹ ㅁ ㅂ ㅅ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ
            |Vowels (10 basic): ㅏ ㅑ ㅓ ㅕ ㅗ ㅛ ㅜ ㅠ ㅡ ㅣ
            |
            |Korean is written in blocks, not in a line:
            |ㅎ + ㅏ + ㄴ → **한**
            |ㄱ + ㅡ + ㄹ → **글**
            |
            |Sounds that surprise English speakers:
            |• ㄹ is a light flap, between *r* and *l*.
            |• ㄱ/ㄷ/ㅂ/ㅈ are soft at the start of a word (closer to *k/t/p/ch*) and hard in the middle.
            |• ㅇ is silent at the start and *ng* at the end.
            |
            |Open the **Alphabet** tab to practise each letter.
            """.trimMargin()
        },
        Rule(listOf("number", "숫자", "sino", "native", "count")) {
            """
            |Korean has **two number systems** — this is the #1 beginner trap.
            |
            |**Sino-Korean** (money, dates, phone numbers)
            |1 일 · 2 이 · 3 삼 · 4 사 · 5 오 · 6 육 · 7 칠 · 8 팔 · 9 구 · 10 십
            |
            |**Native Korean** (counting things, age, hours)
            |1 하나 · 2 둘 · 3 셋 · 4 넷 · 5 다섯 · 6 여섯 · 7 일곱 · 8 여덟 · 9 아홉 · 10 열
            |
            |Say your age: 저는 스무 살이에요. (*jeoneun seumu sarieyo*) — “I'm 20.”
            |Say the price: 이거 얼마예요? (*igeo eolmayeyo?*) — “How much is this?”
            """.trimMargin()
        },
        Rule(listOf("roman", "pronounce", "발음", "how do i say")) {
            """
            |Give me a Korean word and I'll break it into jamo and romanise it — for example:
            |
            |**한국어** → 한 · 국 · 어 → *hangugeo*
            |**감사합니다** → *gamsahamnida*
            |
            |Two rules worth memorising:
            |1. A 받침 moves into the next syllable when that syllable starts with ㅇ:
            |   한국어 → 한구거 → *hangugeo*
            |2. ㅎ is weak between vowels: 좋아 → *joa*
            """.trimMargin()
        },
        Rule(listOf("grammar", "문법", "particle", "은/는", "이/가", "topic")) {
            """
            |**The particle starter pack**
            |
            ||Particle|Job|Example|
            ||---|---|---|
            ||은/는|topic — “as for …”|저**는** 학생이에요|
            ||이/가|subject|비**가** 와요|
            ||을/를|object|밥**을** 먹어요|
            ||에|time / direction|학교**에** 가요|
            ||에서|location of an action|집**에서** 공부해요|
            |
            |Pick 은/는 or 을/를 when the word ends in a consonant, 는/를 when it ends in a vowel:
            |밥 + 을 = 밥을, 나 + 를 = 나를
            |
            |Word order is **Subject – Object – Verb**: 저는 밥을 먹어요 = “I rice eat”.
            """.trimMargin()
        }),
        Rule(listOf("thank", "감사", "sorry", "미안", "polite", "존댓말")) {
            """
            |**Politeness you can use today**
            |
            ||Korean|Romanisation|Meaning|
            ||---|---|---|
            ||감사합니다|gamsahamnida|Thank you (formal)|
            ||고마워요|gomawoyo|Thanks (polite)|
            ||죄송합니다|joesonghamnida|I'm sorry (formal)|
            ||미안해요|mianhaeyo|Sorry (polite)|
            ||실례합니다|sillyehamnida|Excuse me|
            ||괜찮아요|gwaenchanayo|It's okay / I'm fine|
            |
            |Adding **-요** to a plain verb makes it polite. That single 요 carries a lot of goodwill.
            """.trimMargin()
        }),
        Rule(listOf("study", "learn", "how", "start", "beginner", "공부")) {
            """
            |**A 4-week plan that actually works**
            |
            |**Week 1 — read anything.** Learn 14 consonants + 10 vowels, then sound out shop signs.
            |**Week 2 — blocks.** Practise batchim pairs: 밥, 집, 옷, 강, 눈, 물.
            |**Week 3 — survival phrases.** 30 phrases beats 300 vocabulary words.
            |**Week 4 — output.** Write 3 sentences a day about your real day.
            |
            |Use the **Lessons** tab for the phrase sets, the **Alphabet** tab to drill letters,
            |and **Practice** to speak a phrase and get a score.
            |
            |화이팅! 💪
            """.trimMargin()
        }),
        Rule(listOf("translate", "meaning", "번역", "뜻")) {
            """
            |Paste the Korean and I'll break it down syllable by syllable:
            |
            |**안녕하세요** → 안 · 녕 · 하 · 세 · 요 → *annyeonghaseyo* → “hello”
            |
            |If you need a full translation, install the on-device model from the **AI Tutor** tab and ask me there —
            |it runs entirely on your phone with no internet.
            """.trimMargin()
        })
    )

    private val wordBank = setOf(
        "안녕", "감사", "사랑", "학교", "친구", "가족", "음식", "물", "밥", "김치",
        "서울", "한국", "사람", "시간", "오늘", "내일", "공부", "선생님", "학생",
        "고양이", "강아지", "하늘", "바다", "나무", "꽃", "책", "집", "차", "비"
    )

    /** Produces a reply for [prompt]. Never throws. */
    fun reply(prompt: String): String {
        val text = prompt.trim()
        if (text.isEmpty()) return "무엇이든 물어보세요! Ask me anything about Korean."

        val lower = text.lowercase()

        rules.firstOrNull { rule -> rule.keywords.any { lower.contains(it) } }
            ?.let { return it.answer(text) }

        // If the user typed Korean, give a jamo breakdown — that is the most
        // useful thing a rule-based tutor can do without a model.
        val koreanSyllables = text.filter { Hangul.isSyllable(it) }
        if (koreanSyllables.length >= 2) {
            val blocks = koreanSyllables.map { c ->
                val s = Hangul.decompose(c)
                if (s == null) c.toString() else "${c}(${s.jamoString()})"
            }.joinToString(" · ")

            return """
            |**Jamo breakdown**
            |$blocks
            |
            |**Romanisation** — ${Hangul.romanize(koreanSyllables.toString())}
            |**Syllables** — ${koreanSyllables.length}
            |
            |Tip: install the on-device model in the **AI Tutor** tab for full translations and free conversation.
            """.trimMargin()
        }

        val word = wordBank.firstOrNull { abs(it.length - lower.length) <= 2 }
        val topicHint = if (word != null) {
            "I noticed something close to **$word**. "
        } else ""

        return """
        |${topicHint}Here is a phrase you can use right away:
        |
        |**오늘 뭐 해요?** — *oneul mwo haeyo?* — “What are you doing today?”
        |
        |I can help with these topics without any model installed:
        |• 한글 alphabet and batchim
        |• numbers (sino and native)
        |• particles (은/는, 이/가, 을/를)
        |• greetings and polite endings
        |• jamo breakdown of any Korean word you type
        |
        |For open-ended conversation, install a local model in the **AI Tutor** tab — it runs offline on your phone.
        """.trimMargin()
    }
}
