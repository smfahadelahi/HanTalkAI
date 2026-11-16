// The clickable Surface overload is experimental on some Material 3 versions and
// stable on others; opting in unconditionally keeps the build warning-free either way.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hantalk.koreanai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hantalk.koreanai.domain.Curriculum
import com.hantalk.koreanai.domain.Letter
import com.hantalk.koreanai.domain.hangul.Hangul
import com.hantalk.koreanai.speech.KoreanSpeaker
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.components.Bar
import com.hantalk.koreanai.ui.components.ScreenHeader
import com.hantalk.koreanai.ui.components.SectionLabel
import com.hantalk.koreanai.ui.components.SoftCard
import com.hantalk.koreanai.ui.theme.HanTalkTokens
import com.hantalk.koreanai.ui.theme.PillShape

@Composable
fun AlphabetScreen(viewModel: HanTalkViewModel) {
    val selected by viewModel.selectedLetter.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val speaker = remember { KoreanSpeaker(context) }
    DisposableEffect(speaker) { onDispose { speaker.shutdown() } }

    val learnedFraction = stats.learnedLetters.toFloat() / stats.totalLetters.coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Reading",
                title = "한글 Alphabet",
                subtitle = "19 consonants · 15 vowels · tap a letter to hear and learn it"
            )
        }

        // ---- Progress ----
        item {
            SoftCard(contentPadding = PaddingValues(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Your progress",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${stats.learnedLetters} of ${stats.totalLetters} letters learned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${(learnedFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Bar(fraction = learnedFraction, height = 10.dp)
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
        item { SectionLabel("자음 · CONSONANTS") }

        item {
            LetterGrid(
                letters = Curriculum.consonants,
                selected = selected,
                isLearned = viewModel::isLetterLearned,
                onSelect = viewModel::selectLetter
            )
        }

        item { Spacer(Modifier.height(22.dp)) }
        item { SectionLabel("모음 · VOWELS") }

        item {
            LetterGrid(
                letters = Curriculum.vowels,
                selected = selected,
                isLearned = viewModel::isLetterLearned,
                onSelect = viewModel::selectLetter
            )
        }

        val detail = allLetters.firstOrNull { it.jamo == selected }
        if (detail != null) {
            item { Spacer(Modifier.height(22.dp)) }
            item {
                LetterDetail(
                    letter = detail,
                    learned = viewModel.isLetterLearned(detail.jamo),
                    onToggleLearned = { viewModel.toggleLearned(detail.jamo) },
                    onSpeak = { speaker.speak(detail.jamo) },
                    onSpeakSlowly = { speaker.speakSlowly(detail.jamo) }
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item { BlockHint() }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

private val allLetters: List<Letter> = Curriculum.consonants + Curriculum.vowels

@Composable
private fun LetterGrid(
    letters: List<Letter>,
    selected: String?,
    isLearned: (String) -> Boolean,
    onSelect: (String) -> Unit
) {
    val rows = (letters.size + 4) / 5
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxWidth()
            .height((rows * 78).dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(letters, key = { it.jamo }) { letter ->
            LetterTile(
                letter = letter,
                selected = selected == letter.jamo,
                learned = isLearned(letter.jamo),
                onClick = { onSelect(letter.jamo) }
            )
        }
    }
}

@Composable
private fun LetterTile(
    letter: Letter,
    selected: Boolean,
    learned: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "tileScale"
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary
            learned -> HanTalkTokens.extra.jadeSoft
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        },
        label = "tileColor"
    )
    val contentColor = when {
        selected -> Color.White
        learned -> HanTalkTokens.extra.jade
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else HanTalkTokens.extra.hairline,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Surface(
            onClick = onClick,
            color = Color.Transparent,
            contentColor = contentColor,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = letter.jamo,
                        fontSize = (23 * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Text(
                        text = letter.roman,
                        fontSize = 9.sp,
                        color = contentColor.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }
            }
        }
        if (learned) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color.White.copy(alpha = 0.9f) else HanTalkTokens.extra.jade),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(9.dp),
                    tint = if (selected) HanTalkTokens.extra.jade else Color.White
                )
            }
        }
    }
}

@Composable
private fun LetterDetail(
    letter: Letter,
    learned: Boolean,
    onToggleLearned: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakSlowly: () -> Unit
) {
    SoftCard(contentPadding = PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(HanTalkTokens.gradients.brand)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.jamo,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = letter.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "sound · ${letter.roman}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = letter.hint,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Curriculum.letterExamples[letter.jamo]?.let { example ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Example · $example",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        buildDemoSyllable(letter)?.let { built ->
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$built",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "= ${Hangul.romanizeSyllable(built)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                onClick = onSpeak,
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Hear it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Surface(
                onClick = onSpeakSlowly,
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.SlowMotionVideo,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Slowly",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Surface(
                onClick = onToggleLearned,
                shape = PillShape,
                color = if (learned) HanTalkTokens.extra.jade
                else MaterialTheme.colorScheme.primary
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (learned) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        text = if (learned) "Learned" else "Mark learned",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/** Builds a plausible demo syllable for a letter: ㄱ → 가, ㅏ → 아. */
private fun buildDemoSyllable(letter: Letter): Char? = when {
    Hangul.isConsonantJamo(letter.jamo.first()) ->
        Hangul.compose(letter.jamo.first(), 'ㅏ')

    Hangul.isVowelJamo(letter.jamo.first()) ->
        Hangul.compose('ㅇ', letter.jamo.first())

    else -> null
}

@Composable
private fun BlockHint() {
    SoftCard(
        container = MaterialTheme.colorScheme.surfaceContainer,
        bordered = false,
        contentPadding = PaddingValues(18.dp)
    ) {
        Text(
            text = "How blocks work",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Every syllable is 초성 (initial) + 중성 (vowel) + optional 종성 (final).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BlockChip("ㅎ + ㅏ + ㄴ", "한")
            BlockChip("ㄱ + ㅡ + ㄹ", "글")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tap any letter above to hear it and see it inside a real block.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BlockChip(jamo: String, syllable: String) {
    Row(
        Modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = jamo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = syllable,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
