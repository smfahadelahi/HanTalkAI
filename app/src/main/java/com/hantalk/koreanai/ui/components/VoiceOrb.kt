package com.hantalk.koreanai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hantalk.koreanai.ui.theme.HanTalkTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** What the orb is currently expressing. */
enum class OrbState { Idle, Listening, Thinking, Speaking }

/**
 * A large, tappable voice button.
 *
 * The visuals are entirely procedural — concentric drifting rings plus a ring of
 * audio bars — so there are no assets to ship and it animates smoothly on any
 * device. [level] (0f..1f) can be fed from the microphone or from TTS.
 */
@Composable
fun VoiceOrb(
    state: OrbState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    level: Float = 0f
) {
    val brand = HanTalkTokens.gradients.brand
    val success = HanTalkTokens.gradients.success
    val warm = HanTalkTokens.gradients.warm
    val extra = HanTalkTokens.extra

    val target by animateColorAsState(
        targetValue = when (state) {
            OrbState.Idle -> MaterialTheme.colorScheme.primary
            OrbState.Listening -> MaterialTheme.colorScheme.primary
            OrbState.Thinking -> extra.amber
            OrbState.Speaking -> extra.jade
        },
        animationSpec = tween(400),
        label = "orbColor"
    )

    val rings = rememberInfiniteTransition(label = "orbRings")
    val drift by rings.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )
    val pulse by rings.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val wave by rings.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == OrbState.Thinking) 900 else 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val active = state != OrbState.Idle
    val glowStrength = when (state) {
        OrbState.Idle -> 0.10f + pulse * 0.05f
        OrbState.Listening -> 0.22f + level * 0.30f
        OrbState.Thinking -> 0.18f + pulse * 0.14f
        OrbState.Speaking -> 0.20f + level * 0.26f
    }
    val barsVisible = state == OrbState.Listening || state == OrbState.Speaking

    Box(
        modifier = modifier.size(size + 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size + 56.dp)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            // Soft halo.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(target.copy(alpha = glowStrength), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Two slowly counter-rotating arcs = a "breathing" ring.
            val ringRadius = radius * (0.78f + pulse * 0.05f)
            rotate(degrees = drift, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color.Transparent, target.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    startAngle = 0f,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = 3f * density, cap = StrokeCap.Round)
                )
            }
            rotate(degrees = -drift * 0.6f, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color.Transparent, target.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    startAngle = 90f,
                    sweepAngle = 200f,
                    useCenter = false,
                    topLeft = Offset(center.x - ringRadius * 0.92f, center.y - ringRadius * 0.92f),
                    size = Size(ringRadius * 1.84f, ringRadius * 1.84f),
                    style = Stroke(width = 2f * density, cap = StrokeCap.Round)
                )
            }

            // Audio bars around the rim while listening or speaking.
            if (barsVisible) {
                val count = 44
                val barRadius = radius * 0.62f
                for (i in 0 until count) {
                    val t = i / count.toFloat()
                    val angle = t * 2f * PI.toFloat() - PI.toFloat() / 2f
                    val phase = sin((t * 4f + wave) * 2f * PI.toFloat())
                    val energy = (0.35f + 0.65f * kotlin.math.abs(phase)) *
                        (0.35f + 0.65f * level.coerceIn(0f, 1f))
                    val length = radius * (0.06f + 0.16f * energy)
                    val startX = center.x + cos(angle) * barRadius
                    val startY = center.y + sin(angle) * barRadius
                    drawLine(
                        color = target.copy(alpha = 0.25f + 0.6f * energy),
                        start = Offset(startX, startY),
                        end = Offset(
                            center.x + cos(angle) * (barRadius + length),
                            center.y + sin(angle) * (barRadius + length)
                        ),
                        strokeWidth = 3f * density,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // The core button.
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        when (state) {
                            OrbState.Speaking -> success
                            OrbState.Thinking -> warm
                            else -> brand
                        }
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(size * 0.78f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            )
            Icon(
                imageVector = if (active) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (active) "Stop" else "Speak",
                tint = Color.White,
                modifier = Modifier.size(size * 0.32f)
            )
        }
    }
}
