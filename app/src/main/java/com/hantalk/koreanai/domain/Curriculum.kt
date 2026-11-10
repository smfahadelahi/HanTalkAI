package com.hantalk.koreanai.domain

import com.hantalk.koreanai.domain.hangul.Hangul

/** One teachable phrase. [roman] may be empty and will be derived. */
data class Phrase(
    val korean: String,
    val english: String,
    val roman: String = ""
) {
    val romanisation: String
        get() = roman.ifBlank { Hangul.romanize(korean) }
}

/** A themed lesson containing ordered phrases. */
data class Lesson(
    val id: String,
    val title: String,
    val titleKorean: String,
    val emoji: String,
    val blurb: String,
    val phrases: List<Phrase>
)

/** A single hangul letter with its name and sound. */
data class Letter(
    val jamo: String,
    val roman: String,
    val name: String,
    val hint: String
)

object Curriculum {

    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "greetings",
            title = "Greetings",
            titleKorean = "인사",
            emoji = "👋",
            blurb = "The five phrases that open every Korean conversation.",
            phrases = listOf(
                Phrase("안녕하세요", "Hello / Good day", "annyeonghaseyo"),
                Phrase("반갑습니다", "Nice to meet you", "bangapseumnida"),
                Phrase("감사합니다", "Thank you", "gamsahamnida"),
                Phrase("안녕히 가세요", "Goodbye (to someone leaving)", "annyeonghi gaseyo"),
                Phrase("안녕히 계세요", "Goodbye (to someone staying)", "annyeonghi gyeseyo"),
                Phrase("죄송합니다", "I'm sorry", "joesonghamnida"),
                Phrase("괜찮아요", "It's okay / I'm fine", "gwaenchanayo")
            )
        ),
        Lesson(
            id = "introductions",
            title = "Introducing yourself",
            titleKorean = "자기소개",
            emoji = "🙋",
            blurb = "Say who you are, where you are from, and what you do.",
            phrases = listOf(
                Phrase("저는 민수입니다", "I am Minsu", "jeoneun minsuimnida"),
                Phrase("제 이름은 사라예요", "My name is Sarah", "je ireumeun sarayeyo"),
                Phrase("저는 미국에서 왔어요", "I am from the United States", "jeoneun migugeseo wasseoyo"),
                Phrase("저는 학생이에요", "I am a student", "jeoneun haksaengieyo"),
                Phrase("만나서 반갑습니다", "Pleased to meet you", "mannaseo bangapseumnida"),
                Phrase("한국어를 배워요", "I am learning Korean", "hangugeoreul baewoyo")
            )
        ),
        Lesson(
            id = "cafe",
            title = "At the café",
            titleKorean = "카페에서",
            emoji = "☕",
            blurb = "Order, ask for the price and get the bill without pointing.",
            phrases = listOf(
                Phrase("커피 한 잔 주세요", "One coffee, please", "keopi han jan juseyo"),
                Phrase("아이스 아메리카노 주세요", "An iced americano, please", "aiseu americano juseyo"),
                Phrase("이거 얼마예요?", "How much is this?", "igeo eolmayeyo?"),
                Phrase("카드로 계산할게요", "I'll pay by card", "kadeuro gyesanhalgeyo"),
                Phrase("물 한 잔 주세요", "A glass of water, please", "mul han jan juseyo"),
                Phrase("맛있어요", "It's delicious", "masisseoyo")
            )
        ),
        Lesson(
            id = "directions",
            title = "Getting around",
            titleKorean = "길 찾기",
            emoji = "🚇",
            blurb = "Ask for directions and understand the answer.",
            phrases = listOf(
                Phrase("지하철역이 어디예요?", "Where is the subway station?", "jihacheollyeogi eodiyeyo?"),
                Phrase("여기에서 가까워요?", "Is it close from here?", "yeogieseo gakkawoyo?"),
                Phrase("왼쪽으로 가세요", "Go to the left", "oenjjogeuro gaseyo"),
                Phrase("오른쪽으로 가세요", "Go to the right", "oreunjjogeuro gaseyo"),
                Phrase("똑바로 가세요", "Go straight", "ttokbaro gaseyo"),
                Phrase("화장실이 어디예요?", "Where is the restroom?", "hwajangsiri eodiyeyo?")
            )
        ),
        Lesson(
            id = "numbers",
            title = "Numbers & money",
            titleKorean = "숫자",
            emoji = "🔢",
            blurb = "Native and Sino-Korean numbers, plus the phrases they live in.",
            phrases = listOf(
                Phrase("하나, 둘, 셋, 넷, 다섯", "One, two, three, four, five (native)", "hana, dul, set, net, daseot"),
                Phrase("일, 이, 삼, 사, 오", "One, two, three, four, five (Sino)", "il, i, sam, sa, o"),
                Phrase("만 원이에요", "It is 10,000 won", "man wonieyo"),
                Phrase("스무 살이에요", "I am twenty years old", "seumu sarieyo"),
                Phrase("두 명이요", "Two people, please", "du myeongiyo"),
                Phrase("세 시에 만나요", "Let's meet at three o'clock", "se sie mannayo")
            )
        ),
        Lesson(
            id = "feelings",
            title = "Feelings & small talk",
            titleKorean = "기분과 안부",
            emoji = "🙂",
            blurb = "How are you, I'm tired, that's fun — real conversation glue.",
            phrases = listOf(
                Phrase("잘 지내세요?", "How are you doing?", "jal jinaeseyo?"),
                Phrase("저는 괜찮아요", "I'm fine", "jeoneun gwaenchanayo"),
                Phrase("너무 피곤해요", "I'm very tired", "neomu pigonhaeyo"),
                Phrase("정말 재미있어요", "It's really fun", "jeongmal jaemiisseoyo"),
                Phrase("배고파요", "I'm hungry", "baegopayo"),
                Phrase("오늘 날씨가 좋아요", "The weather is nice today", "oneul nalssiga joayo")
            )
        )
    )

    fun lesson(id: String): Lesson? = lessons.firstOrNull { it.id == id }

    /** All phrases flattened — used by the "random challenge" button. */
    val allPhrases: List<Phrase> = lessons.flatMap { it.phrases }

    val consonants: List<Letter> = listOf(
        Letter("ㄱ", "g / k", "기역", "Soft g at the start, hard k at the end."),
        Letter("ㄴ", "n", "니은", "Same as English n. Tongue on the ridge."),
        Letter("ㄷ", "d / t", "디귿", "Soft d at the start, t at the end."),
        Letter("ㄹ", "r / l", "리을", "A light flap — between r and l."),
        Letter("ㅁ", "m", "미음", "Same as English m."),
        Letter("ㅂ", "b / p", "비읍", "Soft b at the start, p at the end."),
        Letter("ㅅ", "s", "시옷", "s, but closer to sh before ㅣ and ㅑ."),
        Letter("ㅇ", "silent / ng", "이응", "Silent before a vowel, ng at the end."),
        Letter("ㅈ", "j", "지읒", "Between English j and ch."),
        Letter("ㅊ", "ch", "치읓", "Like ch with a puff of air."),
        Letter("ㅋ", "k", "키읔", "k with a puff of air."),
        Letter("ㅌ", "t", "티읕", "t with a puff of air."),
        Letter("ㅍ", "p", "피읖", "p with a puff of air."),
        Letter("ㅎ", "h", "히읗", "Like English h; weak between vowels."),
        Letter("ㄲ", "kk", "쌍기역", "Tense, tight k — no puff of air."),
        Letter("ㄸ", "tt", "쌍디귿", "Tense, tight t."),
        Letter("ㅃ", "pp", "쌍비읍", "Tense, tight p."),
        Letter("ㅆ", "ss", "쌍시옷", "Tense, hissing s."),
        Letter("ㅉ", "jj", "쌍지읒", "Tense, tight j.")
    )

    val vowels: List<Letter> = listOf(
        Letter("ㅏ", "a", "아", "Open ah, like father."),
        Letter("ㅑ", "ya", "야", "y + ah."),
        Letter("ㅓ", "eo", "어", "A deeper uh, mouth relaxed."),
        Letter("ㅕ", "yeo", "여", "y + eo."),
        Letter("ㅗ", "o", "오", "Rounded oh."),
        Letter("ㅛ", "yo", "요", "y + oh."),
        Letter("ㅜ", "u", "우", "oo, like food."),
        Letter("ㅠ", "yu", "유", "y + oo."),
        Letter("ㅡ", "eu", "으", "Tight uh — smile and say oo without rounding."),
        Letter("ㅣ", "i", "이", "ee, like see."),
        Letter("ㅐ", "ae", "애", "Like e in bed."),
        Letter("ㅔ", "e", "에", "Like e in bed (now merged with ㅐ)."),
        Letter("ㅚ", "oe", "외", "Like weh."),
        Letter("ㅟ", "wi", "위", "Like we."),
        Letter("ㅢ", "ui", "의", "eui — the trickiest vowel.")
    )

    /** A tiny set of practice words for each letter. */
    val letterExamples: Map<String, String> = mapOf(
        "ㄱ" to "가방 (bag)", "ㄴ" to "나무 (tree)", "ㄷ" to "다리 (leg)",
        "ㄹ" to "라면 (ramen)", "ㅁ" to "모자 (hat)", "ㅂ" to "바다 (sea)",
        "ㅅ" to "사과 (apple)", "ㅇ" to "아이 (child)", "ㅈ" to "자동차 (car)",
        "ㅊ" to "책 (book)", "ㅋ" to "커피 (coffee)", "ㅌ" to "토끼 (rabbit)",
        "ㅍ" to "포도 (grape)", "ㅎ" to "하늘 (sky)", "ㄲ" to "꽃 (flower)",
        "ㄸ" to "딸 (daughter)", "ㅃ" to "빵 (bread)", "ㅆ" to "쌀 (rice)",
        "ㅉ" to "짜장면 (black-bean noodles)", "ㅏ" to "아기 (baby)",
        "ㅑ" to "야구 (baseball)", "ㅓ" to "어머니 (mother)", "ㅕ" to "여자 (woman)",
        "ㅗ" to "오이 (cucumber)", "ㅛ" to "요리 (cooking)", "ㅜ" to "우유 (milk)",
        "ㅠ" to "유리 (glass)", "ㅡ" to "음악 (music)", "ㅣ" to "이름 (name)",
        "ㅐ" to "개 (dog)", "ㅔ" to "네 (yes)", "ㅚ" to "외국 (foreign country)",
        "ㅟ" to "귀 (ear)", "ㅢ" to "의사 (doctor)"
    )
}
