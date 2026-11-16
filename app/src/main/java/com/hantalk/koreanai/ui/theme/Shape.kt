package com.hantalk.koreanai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Large, soft radii. The old build leaned on the Material defaults (11–16 dp),
 * which read as "designed in 2014"; these give the app its modern, friendly
 * surface language.
 */
val HanTalkShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val CardShape = RoundedCornerShape(24.dp)
// Kept as a named token for future full-bleed hero surfaces.
val HeroShape = RoundedCornerShape(30.dp)
val PillShape = RoundedCornerShape(percent = 50)
