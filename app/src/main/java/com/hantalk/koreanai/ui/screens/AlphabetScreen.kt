package com.hantalk.koreanai.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hantalk.koreanai.domain.Curriculum
import com.hantalk.koreanai.domain.Letter
import com.hantalk.koreanai.domain.hangul.Hangul
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.components.Pill
import com.hantalk.koreanai.ui.components.ScreenHeader

@Composable
fun AlphabetScreen(viewModel: HanTalkViewModel) {
    val selected by viewModel.selectedLetter.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "한글 Alphabet",
                subtitle = "19 consonants · 15 vowels · tap a letter to learn it"
            )
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Pill(text = "${stats.learnedLetters} / ${stats.totalLetters} learned")
                Pill(
                    text = "${(stats.learnedLetters * 100 / stats.totalLetters.coerceAtLeast(1))}%",
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        item { SectionLabel("자음 · Consonants") }

        item {
            LetterGrid(
                letters = Curriculum.consonants,
                selected = selected,
                isLearned = viewModel::isLetterLearned,
                onSelect = viewModel::selectLetter
            )
        }

        item { Spacer(Modifier.height(20.dp)) }
        item { SectionLabel("모음 · Vowels") }

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
            item { Spacer(Modifier.height(20.dp)) }
            item {
                LetterDetail(
                    letter = detail,
                    learned = viewModel.isLetterLearned(detail.jamo),
                    onToggleLearned = { viewModel.toggleLearned(detail.jamo) }
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item { BlockHint() }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private val allLetters: List<Letter> = Curriculum.consonants + Curriculum.vowels

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun LetterGrid(
    letters: List<Letter>,
    selected: String?,
    isLearned: (String) -> Boolean,
    onSelect: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 460.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(letters, key = { it.jamo }) { letter ->
            val isSelected = selected == letter.jamo
            val learned = isLearned(letter.jamo)
            Surface(
                onClick = { onSelect(letter.jamo) },
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    learned -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier
                    .height(64.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = letter.jamo,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = letter.roman,
                            fontSize = 10.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterDetail(
    letter: Letter,
    learned: Boolean,
    onToggleLearned: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = letter.jamo,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(Modifier.padding(start = 16.dp)) {
                    Text(
                        text = letter.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "sound: ${letter.roman}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(text = letter.hint, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))

            val example = Curriculum.letterExamples[letter.jamo]
            if (example != null) {
                Text(
                    text = "Example — $example",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // A syllable built with this letter, when possible.
            val built = buildDemoSyllable(letter)
            if (built != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "In a block — $built = ${Hangul.romanizeSyllable(built)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onToggleLearned,
                    shape = RoundedCornerShape(50),
                    color = if (learned) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (learned) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(Modifier.size(6.dp))
                        }
                        Text(
                            text = if (learned) "Learned" else "Mark as learned",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (learned) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "How blocks work",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Every syllable is 초성 (initial) + 중성 (vowel) + optional 종성 (final).\n\n" +
                    "ㅎ + ㅏ + ㄴ → 한\n" +
                    "ㄱ + ㅡ + ㄹ → 글\n\n" +
                    "Tap any letter above to see it inside a real block.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
