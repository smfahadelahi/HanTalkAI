package com.hantalk.koreanai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hantalk.koreanai.speech.KoreanSpeaker
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.components.Pill
import com.hantalk.koreanai.ui.components.ScreenHeader
import com.hantalk.koreanai.ui.components.StatBlock

@Composable
fun PracticeScreen(
    viewModel: HanTalkViewModel,
    startWith: String? = null
) {
    val state by viewModel.practice.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val speaker = remember { KoreanSpeaker(context) }

    DisposableEffect(Unit) {
        if (state.phrase == null) viewModel.startPractice()
        onDispose { }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "Practice",
            subtitle = "Type or speak the Korean you see. Scored by edit distance."
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBlock(value = "${stats.practiceCount}", label = "sessions")
            StatBlock(value = "${stats.bestScore}%", label = "best")
            StatBlock(value = "${stats.streak}", label = "day streak")
        }

        val phrase = state.phrase
        if (phrase == null) {
            Text("Preparing a phrase…")
            return@Column
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Translate into Korean",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { speaker.speak(phrase.korean) }) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "Listen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = phrase.english,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = state.attempt,
                    onValueChange = viewModel::updateAttempt,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Your Korean") },
                    placeholder = { Text("안녕하세요 …") },
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.submitAttempt() }
                    )
                )

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = viewModel::submitAttempt,
                        enabled = state.attempt.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("Check") }

                    OutlinedButton(
                        onClick = {
                            viewModel.startPractice()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.width(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("New")
                    }
                }

                AnimatedVisibility(visible = state.score != null) {
                    val score = state.score ?: 0.0
                    Column(Modifier.padding(top = 16.dp)) {
                        LinearProgressIndicator(
                            progress = { score.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = when {
                                score >= 0.8 -> MaterialTheme.colorScheme.secondary
                                score >= 0.5 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.feedback,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Pill(text = "${(score * 100).toInt()}%")
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Answer",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = phrase.korean,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = phrase.romanisation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Tip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "한글 keyboard not installed? Type the romanisation " +
                        "(e.g. annyeonghaseyo) — spelling is compared loosely, " +
                        "so a close Korean answer scores best.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
