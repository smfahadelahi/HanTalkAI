package com.hantalk.koreanai.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * HanTalk brand palette.
 *
 * Deep "haneul" indigo for the voice-first surfaces, a warm paper background so
 * long reading sessions stay comfortable, and dancheong-inspired accents
 * (mint + coral) used for feedback and progress.
 */

// ---- Brand ----
val Haneul = Color(0xFF2A47E0)          // primary
val HaneulDeep = Color(0xFF11205E)
val HaneulSoft = Color(0xFFE4E8FF)
val HaneulMist = Color(0xFFF2F4FF)
val HaneulLight = Color(0xFFB6C3FF)

val Jade = Color(0xFF0E9F76)            // success / correct
val JadeSoft = Color(0xFFD6F4E9)

val Coral = Color(0xFFE24B36)           // accent / mistake
val CoralSoft = Color(0xFFFFE2DB)

val Amber = Color(0xFFF0A32A)
val AmberSoft = Color(0xFFFFF0D6)

// ---- Neutrals (warm paper) ----
val Paper = Color(0xFFFDFCF9)
val PaperRaised = Color(0xFFFFFFFF)
val PaperSoft = Color(0xFFF3F1EB)
val PaperSunken = Color(0xFFEAE7DF)

val Ink = Color(0xFF14161B)
val InkMuted = Color(0xFF5C616B)
val InkLine = Color(0xFFE4E0D8)

// ---- Dark ----
val Night = Color(0xFF0C0D11)
val NightSurface = Color(0xFF14161C)
val NightRaised = Color(0xFF1C1F27)
val NightLine = Color(0xFF2A2E38)
val NightMuted = Color(0xFFA6ACB8)

// ---- Semantic ----
val ErrorRed = Color(0xFFB3261E)
val ErrorRedDark = Color(0xFFFFB4AB)

// ---- Legacy aliases kept so older call sites keep compiling ----
val Indigo40 = Haneul
val Indigo80 = HaneulLight
val Indigo10 = HaneulDeep
val Seafoam40 = Jade
val Seafoam80 = JadeSoft
val Seafoam10 = Color(0xFF00301F)
val Clay40 = Coral
val Clay80 = CoralSoft
val Clay10 = Color(0xFF3B0900)
val PaperLight = Paper
val PaperDim = PaperSoft
val InkDark = Night
val InkSurface = NightSurface
val InkSurfaceHigh = NightRaised
val SuccessGreen = Jade
val WarnAmber = Amber
