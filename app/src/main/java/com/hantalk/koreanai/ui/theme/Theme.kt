package com.hantalk.koreanai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Brand gradients used by the voice orb and hero cards. */
data class HanTalkGradients(
    /** Primary brand sweep — voice orb, hero cards, selected nav pill. */
    val brand: List<Color>,
    /** Barely-there tinted wash used behind quiet info strips. */
    val brandSoft: List<Color>,
    /** Correct / speaking state. */
    val success: List<Color>,
    /** Thinking / warm accent. */
    val warm: List<Color>
)

val LightGradients = HanTalkGradients(
    brand = listOf(Color(0xFF5B7BFF), Color(0xFF2A47E0), Color(0xFF6D3BEB)),
    brandSoft = listOf(Color(0xFFEEF1FF), Color(0xFFF6F1FF)),
    success = listOf(Color(0xFF17B686), Color(0xFF0E9F76)),
    warm = listOf(Color(0xFFFFC46B), Color(0xFFF0A32A))
)

val DarkGradients = HanTalkGradients(
    brand = listOf(Color(0xFF7E97FF), Color(0xFF4A62F0), Color(0xFF8A5BF5)),
    brandSoft = listOf(Color(0xFF1A1E2B), Color(0xFF201B2B)),
    success = listOf(Color(0xFF2BCB98), Color(0xFF12A87C)),
    warm = listOf(Color(0xFFFFD08A), Color(0xFFF3AE3F))
)

val LocalHanTalkGradients = staticCompositionLocalOf { LightGradients }

/** Extra semantic colours that Material's ColorScheme has no slot for. */
data class HanTalkExtra(
    val jade: Color,
    val jadeSoft: Color,
    val coral: Color,
    val coralSoft: Color,
    val amber: Color,
    val amberSoft: Color,
    val hairline: Color
)

val LightExtra = HanTalkExtra(
    jade = Jade,
    jadeSoft = JadeSoft,
    coral = Coral,
    coralSoft = CoralSoft,
    amber = Amber,
    amberSoft = AmberSoft,
    hairline = Color(0xFFE9E5DC)
)

val DarkExtra = HanTalkExtra(
    jade = Color(0xFF4BD3A5),
    jadeSoft = Color(0xFF10312A),
    coral = Color(0xFFFF8E77),
    coralSoft = Color(0xFF3A1D18),
    amber = Color(0xFFFFC76B),
    amberSoft = Color(0xFF3A2C13),
    hairline = Color(0xFF2A2E38)
)

val LocalHanTalkExtra = staticCompositionLocalOf { LightExtra }

private val LightColors = lightColorScheme(
    primary = Haneul,
    onPrimary = Color.White,
    primaryContainer = HaneulSoft,
    onPrimaryContainer = HaneulDeep,
    inversePrimary = HaneulLight,
    secondary = Jade,
    onSecondary = Color.White,
    secondaryContainer = JadeSoft,
    onSecondaryContainer = Color(0xFF00301F),
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = CoralSoft,
    onTertiaryContainer = Color(0xFF3B0900),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperSoft,
    onSurfaceVariant = InkMuted,
    surfaceTint = Haneul,
    surfaceBright = Color.White,
    surfaceDim = PaperSunken,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF9F6),
    surfaceContainer = PaperSoft,
    surfaceContainerHigh = Color(0xFFEFECE5),
    surfaceContainerHighest = Color(0xFFE9E5DC),
    outline = Color(0xFFB9B4AA),
    outlineVariant = InkLine,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = CoralSoft,
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF2B2F36),
    inverseOnSurface = Color(0xFFF2F1EC),
    scrim = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93A8FF),
    onPrimary = Color(0xFF0A1650),
    primaryContainer = Color(0xFF233A9E),
    onPrimaryContainer = Color(0xFFE3E8FF),
    secondary = Color(0xFF4BD3A5),
    onSecondary = Color(0xFF00301F),
    secondaryContainer = Color(0xFF0C4B3A),
    onSecondaryContainer = Color(0xFFC9F6E6),
    tertiary = Color(0xFFFFA48F),
    onTertiary = Color(0xFF3B0900),
    tertiaryContainer = Color(0xFF5A241A),
    onTertiaryContainer = Color(0xFFFFDAD2),
    background = Night,
    onBackground = Color(0xFFF1F2F6),
    surface = Night,
    onSurface = Color(0xFFF1F2F6),
    surfaceVariant = NightRaised,
    onSurfaceVariant = NightMuted,
    surfaceTint = Color(0xFF93A8FF),
    surfaceBright = Color(0xFF22252D),
    surfaceDim = Color(0xFF0C0D11),
    surfaceContainerLowest = Color(0xFF08090C),
    surfaceContainerLow = NightSurface,
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightRaised,
    surfaceContainerHighest = Color(0xFF23262F),
    outline = Color(0xFF5A606C),
    outlineVariant = NightLine,
    error = ErrorRedDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF5A241A),
    onErrorContainer = Color(0xFFFFDAD2),
    inverseSurface = Color(0xFFF1F2F6),
    inverseOnSurface = Color(0xFF1B1D23),
    scrim = Color(0xFF000000)
)

@Composable
fun HanTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalHanTalkGradients provides if (darkTheme) DarkGradients else LightGradients,
        LocalHanTalkExtra provides if (darkTheme) DarkExtra else LightExtra
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = HanTalkTypography,
            shapes = HanTalkShapes,
            content = content
        )
    }
}

/**
 * Shortcut so screens can reach the extra palette without importing two
 * composition locals. Named distinctly from the [HanTalkTheme] function above.
 */
object HanTalkTokens {
    val gradients: HanTalkGradients
        @Composable get() = LocalHanTalkGradients.current

    val extra: HanTalkExtra
        @Composable get() = LocalHanTalkExtra.current
}
