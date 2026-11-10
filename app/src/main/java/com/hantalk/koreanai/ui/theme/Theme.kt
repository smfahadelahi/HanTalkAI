package com.hantalk.koreanai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ErrorRedDark = Color(0xFFFFB4AB)

private val LightColors = lightColorScheme(
    primary = Indigo40,
    onPrimary = PaperLight,
    primaryContainer = Indigo80,
    onPrimaryContainer = Indigo10,
    secondary = Seafoam40,
    onSecondary = PaperLight,
    secondaryContainer = Seafoam80,
    onSecondaryContainer = Seafoam10,
    tertiary = Clay40,
    onTertiary = PaperLight,
    tertiaryContainer = Clay80,
    onTertiaryContainer = Clay10,
    background = PaperLight,
    onBackground = InkDark,
    surface = PaperLight,
    onSurface = InkDark,
    surfaceVariant = PaperDim,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = Indigo80,
    onPrimary = Indigo10,
    primaryContainer = Indigo40,
    onPrimaryContainer = PaperLight,
    secondary = Seafoam80,
    onSecondary = Seafoam10,
    secondaryContainer = Seafoam40,
    onSecondaryContainer = PaperLight,
    tertiary = Clay80,
    onTertiary = Clay10,
    tertiaryContainer = Clay40,
    onTertiaryContainer = PaperLight,
    background = InkDark,
    onBackground = PaperLight,
    surface = InkSurface,
    onSurface = PaperLight,
    surfaceVariant = InkSurfaceHigh,
    error = ErrorRedDark
)

@Composable
fun HanTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = HanTalkTypography,
        content = content
    )
}
