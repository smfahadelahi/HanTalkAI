// The clickable Surface overload is experimental on some Material 3 versions and
// stable on others; opting in unconditionally keeps the build warning-free either way.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hantalk.koreanai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hantalk.koreanai.ui.theme.CardShape
import com.hantalk.koreanai.ui.theme.HanTalkTokens
import com.hantalk.koreanai.ui.theme.PillShape

// ---------------------------------------------------------------------------
// Text
// ---------------------------------------------------------------------------

/**
 * Section heading with an optional eyebrow label and supporting line.
 * An eyebrow (small, uppercase, letter-spaced) is the single cheapest way to
 * make a screen feel designed rather than assembled.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (eyebrow != null) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (trailing != null) trailing()
        }
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(18.dp))
    }
}

/** Small uppercase section label, e.g. "CONSONANTS". */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.2.sp,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
    }
}

// ---------------------------------------------------------------------------
// Surfaces
// ---------------------------------------------------------------------------

/**
 * The standard card: light fill, hairline border, generous radius. Shadow-free
 * by default — dark mode and low-end screens both look cleaner this way.
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    bordered: Boolean = true,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val border = if (bordered) BorderStroke(1.dp, HanTalkTokens.extra.hairline) else null
    val body: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(contentPadding), content = content)
    }
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = container),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = body
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = container),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = body
        )
    }
}

/** Rounded tag, e.g. "초급" or "1.1 GB". */
@Composable
fun Pill(
    text: String,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(container)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}

/** A pill with no fill, just a hairline — quieter than [Pill]. */
@Composable
fun GhostPill(text: String, modifier: Modifier = Modifier, content: Color? = null) {
    Box(
        modifier = modifier
            .clip(PillShape)
            .border(1.dp, HanTalkTokens.extra.hairline, PillShape)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Phrase presentation
// ---------------------------------------------------------------------------

/**
 * A Korean phrase with romanisation and translation. [leading] is usually a
 * letter tile or lesson emoji; [actions] gets a row of small icon buttons.
 */
@Composable
fun PhraseRow(
    korean: String,
    roman: String,
    english: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    val body: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = korean,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = roman,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = english,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actions != null) {
                Spacer(Modifier.width(8.dp))
                actions()
            }
        }
    }
    SoftCard(modifier = modifier, onClick = onClick, content = body)
}

/** Round speaker button used next to Korean text. */
@Composable
fun SpeakButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 40,
    contentDescription: String = "Listen"
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(size.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.VolumeUp,
                contentDescription = contentDescription,
                modifier = Modifier.size((size * 0.5f).dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Stats
// ---------------------------------------------------------------------------

/** Statistic block: big number over a small label. */
@Composable
fun StatBlock(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = tint
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            letterSpacing = 0.8.sp
        )
    }
}

/** Three-up stat strip on a soft card, with hairline dividers. */
@Composable
fun StatStrip(
    modifier: Modifier = Modifier,
    left: @Composable () -> Unit,
    middle: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    SoftCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { left() }
            Divider()
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { middle() }
            Divider()
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { right() }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .height(28.dp)
            .width(1.dp)
            .background(HanTalkTokens.extra.hairline)
    )
}

// ---------------------------------------------------------------------------
// Feedback & progress
// ---------------------------------------------------------------------------

/** Empty-state block with an emoji, a title and a hint. */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/**
 * Slim rounded progress bar.
 *
 * Hand-rolled rather than using [LinearProgressIndicator] so the track gap and
 * stop-indicator behaviour stay identical on every Material 3 version, and so
 * the fill can use a brand gradient.
 */
@Composable
fun Bar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 9.dp,
    brush: Brush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    ),
    track: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    val clamped = fraction.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 450),
        label = "barFill"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(PillShape)
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated.coerceAtLeast(0.0001f))
                .clip(PillShape)
                .background(brush)
        )
    }
}

/** Inline progress with a label, used by the model downloader. */
@Composable
fun ProgressBlock(
    label: String,
    percent: Int,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "$percent%",
                style = MaterialTheme.typography.labelMedium,
                color = tint
            )
        }
        Spacer(Modifier.height(8.dp))
        Bar(
            fraction = percent / 100f,
            brush = Brush.linearGradient(listOf(tint, tint))
        )
    }
}

/** Centred spinner with a caption. */
@Composable
fun LoadingBlock(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            Modifier.size(26.dp),
            strokeWidth = 2.5.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A soft inline notice, optionally with a leading icon slot. */
@Composable
fun NoticeBanner(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    leading: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = container
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = content,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Small centred caption, used for tips and footnotes. */
@Composable
fun CenteredTextWrapper(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

/** Big percentage readout used after a practice attempt. */
@Composable
fun ScoreBadge(
    percent: Int,
    modifier: Modifier = Modifier,
    good: Boolean = percent >= 70
) {
    val extra = HanTalkTokens.extra
    val container = if (good) extra.jadeSoft else extra.amberSoft
    val content = if (good) extra.jade else extra.amber
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(container)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.titleMedium,
            color = content
        )
    }
}
