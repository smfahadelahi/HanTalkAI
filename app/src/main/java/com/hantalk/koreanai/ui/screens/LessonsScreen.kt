package com.hantalk.koreanai.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hantalk.koreanai.domain.Curriculum
import com.hantalk.koreanai.domain.Lesson
import com.hantalk.koreanai.domain.Phrase
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.components.PhraseRow
import com.hantalk.koreanai.ui.components.Pill
import com.hantalk.koreanai.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    viewModel: HanTalkViewModel,
    onPractice: (Phrase) -> Unit
) {
    var openLesson by remember { mutableStateOf<Lesson?>(null) }
    var detail by remember { mutableStateOf<Phrase?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "Lessons",
                subtitle = "Six themed sets · ${Curriculum.allPhrases.size} phrases · tap any phrase"
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "오늘의 문장",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    val daily = remember { Curriculum.allPhrases.random() }
                    Text(
                        text = daily.korean,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = daily.romanisation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = daily.english,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { onPractice(daily) }) {
                        Text("Practice this")
                    }
                }
            }
        }

        items(Curriculum.lessons, key = { it.id }) { lesson ->
            Card(
                onClick = { openLesson = lesson },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = lesson.emoji, fontSize = 30.sp)
                    Spacer(Modifier.height(0.dp))
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = lesson.titleKorean,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = lesson.blurb,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Pill(text = "${lesson.phrases.size}")
                }
            }
        }
    }

    val lesson = openLesson
    if (lesson != null) {
        ModalBottomSheet(
            onDismissRequest = { openLesson = null },
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { openLesson = null }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column {
                        Text(
                            text = "${lesson.emoji}  ${lesson.title}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = lesson.titleKorean,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(lesson.phrases) { phrase ->
                        PhraseRow(
                            korean = phrase.korean,
                            roman = phrase.romanisation,
                            english = phrase.english,
                            onClick = { detail = phrase }
                        )
                    }
                }
            }
        }
    }

    val phrase = detail
    if (phrase != null) {
        ModalBottomSheet(onDismissRequest = { detail = null }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = phrase.korean,
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = phrase.romanisation,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = phrase.english,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Jamo breakdown",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = viewModel.breakdown(phrase.korean),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            onPractice(phrase)
                            detail = null
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Practice") }

                    OutlinedButton(
                        onClick = {
                            viewModel.explainPhrase(phrase)
                            detail = null
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Explain") }
                }
            }
        }
    }
}

@Composable
private fun BoxSpacer() = Box(Modifier.height(0.dp))
