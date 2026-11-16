// The clickable Surface overload is experimental on some Material 3 versions and
// stable on others; opting in unconditionally keeps the build warning-free either way.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hantalk.koreanai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hantalk.koreanai.domain.Curriculum
import com.hantalk.koreanai.domain.Lesson
import com.hantalk.koreanai.domain.Phrase
import com.hantalk.koreanai.speech.KoreanSpeaker
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.components.GhostPill
import com.hantalk.koreanai.ui.components.PhraseRow
import com.hantalk.koreanai.ui.components.ScreenHeader
import com.hantalk.koreanai.ui.components.SoftCard
import com.hantalk.koreanai.ui.components.SpeakButton
import com.hantalk.koreanai.ui.theme.HanTalkTokens
import com.hantalk.koreanai.ui.theme.PillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    viewModel: HanTalkViewModel,
    onPractice: (Phrase) -> Unit
) {
    var openLesson by remember { mutableStateOf<Lesson?>(null) }
    var detail by remember { mutableStateOf<Phrase?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val context = LocalContext.current
    val speaker = remember { KoreanSpeaker(context) }
    DisposableEffect(speaker) { onDispose { speaker.shutdown() } }

    val daily = remember { Curriculum.allPhrases.random() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Learn",
                title = "Lessons",
                subtitle = "${Curriculum.lessons.size} themed sets · " +
                    "${Curriculum.allPhrases.size} phrases with audio"
            )
        }

        // ---- Phrase of the day ----
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Brush.linearGradient(HanTalkTokens.gradients.brand))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "오늘의 문장",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "PHRASE OF THE DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = daily.korean,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = daily.romanisation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = daily.english,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { speaker.speak(daily.korean) },
                            shape = PillShape,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text("Hear it", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onPractice(daily) },
                            shape = PillShape,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.4f)
                            ),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) { Text("Practise") }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(4.dp)) }

        items(Curriculum.lessons, key = { it.id }) { lesson ->
            LessonCard(lesson = lesson, onClick = { openLesson = lesson })
        }
    }

    // ---- Lesson sheet ----
    val lesson = openLesson
    if (lesson != null) {
        ModalBottomSheet(
            onDismissRequest = { openLesson = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { openLesson = null }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${lesson.titleKorean} · ${lesson.phrases.size} phrases",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(lesson.phrases) { phrase ->
                        PhraseRow(
                            korean = phrase.korean,
                            roman = phrase.romanisation,
                            english = phrase.english,
                            onClick = { detail = phrase },
                            actions = {
                                SpeakButton(
                                    onClick = { speaker.speak(phrase.korean) },
                                    size = 36
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // ---- Phrase detail sheet ----
    val phrase = detail
    if (phrase != null) {
        ModalBottomSheet(
            onDismissRequest = { detail = null },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = phrase.korean,
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = phrase.romanisation,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = phrase.english,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SpeakButton(
                        onClick = { speaker.speak(phrase.korean) },
                        size = 52
                    )
                }

                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AudioChip("Play", Icons.Filled.VolumeUp, primary = true) {
                        speaker.speak(phrase.korean)
                    }
                    AudioChip("Slowly", Icons.Filled.SlowMotionVideo) {
                        speaker.speakSlowly(phrase.korean)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "JAMO BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(8.dp))
                SoftCard(contentPadding = PaddingValues(16.dp)) {
                    Text(
                        text = viewModel.breakdown(phrase.korean),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            onPractice(phrase)
                            detail = null
                        },
                        shape = PillShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) { Text("Practise", fontWeight = FontWeight.Bold) }

                    OutlinedButton(
                        onClick = {
                            viewModel.explainPhrase(phrase)
                            detail = null
                        },
                        shape = PillShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Explain")
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    SoftCard(onClick = onClick, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = lesson.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = lesson.titleKorean,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = lesson.blurb,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(8.dp))
            GhostPill(text = "${lesson.phrases.size}")
        }
    }
}

@Composable
private fun AudioChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = PillShape,
        color = if (primary) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (primary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (primary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
