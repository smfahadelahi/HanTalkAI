// The clickable Surface overload is experimental on some Material 3 versions and
// stable on others; opting in unconditionally keeps the build warning-free either way.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hantalk.koreanai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hantalk.koreanai.speech.KoreanSpeaker
import com.hantalk.koreanai.speech.VoiceRecognizer
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.components.Bar
import com.hantalk.koreanai.ui.components.ScoreBadge
import com.hantalk.koreanai.ui.components.ScreenHeader
import com.hantalk.koreanai.ui.components.SoftCard
import com.hantalk.koreanai.ui.components.StatBlock
import com.hantalk.koreanai.ui.components.StatStrip
import com.hantalk.koreanai.ui.theme.HanTalkTokens
import com.hantalk.koreanai.ui.theme.PillShape

@Composable
fun PracticeScreen(
    viewModel: HanTalkViewModel,
    startWith: String? = null
) {
    val state by viewModel.practice.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val speaker = remember { KoreanSpeaker(context) }
    val recognizer = remember { VoiceRecognizer(context) }
    val voice by recognizer.state.collectAsStateWithLifecycle()

    LaunchedEffect(startWith) {
        if (state.phrase == null) viewModel.startPractice()
    }

    DisposableEffect(speaker, recognizer) {
        onDispose {
            recognizer.release()
            speaker.shutdown()
        }
    }

    // A recognised answer is dropped straight into the text field.
    LaunchedEffect(voice.phase, voice.text) {
        if (voice.phase == VoiceRecognizer.Phase.Done && voice.text.isNotBlank()) {
            viewModel.updateAttempt(voice.text)
            recognizer.cancel()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) recognizer.start() else recognizer.cancel() }

    val phrase = state.phrase
    val score = state.score

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        ScreenHeader(
            eyebrow = "Practise",
            title = "Say it back",
            subtitle = "Type or speak the Korean for the sentence below."
        )

        StatStrip(
            left = { StatBlock(value = "${stats.practiceCount}", label = "SESSIONS") },
            middle = { StatBlock(value = "${stats.bestScore}%", label = "BEST") },
            right = { StatBlock(value = "${stats.streak}", label = "DAY STREAK") }
        )

        Spacer(Modifier.height(16.dp))

        if (phrase == null) {
            Text("Preparing a phrase…")
            return@Column
        }

        // ---- Prompt ----
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
                        text = "TRANSLATE INTO KOREAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        onClick = { speaker.speak(phrase.korean) },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription = "Listen",
                                modifier = Modifier.size(19.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = phrase.english,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                if (score == null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "No peeking — write it from memory.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- Answer input ----
        OutlinedTextField(
            value = state.attempt,
            onValueChange = viewModel::updateAttempt,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your Korean") },
            placeholder = { Text("안녕하세요 …") },
            shape = RoundedCornerShape(20.dp),
            minLines = 2,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = HanTalkTokens.extra.hairline
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.submitAttempt() })
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = {
                    if (recognizer.canListen()) recognizer.start()
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                shape = PillShape,
                color = if (voice.isListening) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = if (voice.isListening) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = if (voice.isListening) "Listening…" else "Speak",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (voice.isListening) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = viewModel::submitAttempt,
                enabled = state.attempt.isNotBlank(),
                shape = PillShape,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Text("Check", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
            }

            OutlinedButton(
                onClick = { viewModel.startPractice() },
                shape = CircleShape,
                modifier = Modifier.size(46.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "New phrase",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        voice.message?.let { message ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---- Result ----
        AnimatedVisibility(
            visible = score != null,
            enter = fadeIn() + slideInVertically { it / 3 }
        ) {
            Column(Modifier.padding(top = 18.dp)) {
                val value = score ?: 0.0
                val percent = (value * 100).toInt()
                val tint = when {
                    value >= 0.8 -> HanTalkTokens.extra.jade
                    value >= 0.5 -> HanTalkTokens.extra.amber
                    else -> HanTalkTokens.extra.coral
                }
                SoftCard(contentPadding = PaddingValues(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.feedback,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreBadge(percent = percent, good = value >= 0.7)
                    }
                    Spacer(Modifier.height(12.dp))
                    Bar(
                        fraction = value.toFloat(),
                        height = 9.dp,
                        brush = Brush.linearGradient(listOf(tint, tint))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "ANSWER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
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
                        Surface(
                            onClick = { speaker.speak(phrase.korean) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.VolumeUp,
                                    contentDescription = "Hear the answer",
                                    modifier = Modifier.size(19.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { speaker.speakSlowly(phrase.korean) },
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Row(
                                Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.SlowMotionVideo,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "Play slowly",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.startPractice() },
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Next phrase", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        SoftCard(
            container = MaterialTheme.colorScheme.surfaceContainer,
            bordered = false,
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = "Tip",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "No 한글 keyboard? Type the romanisation (annyeonghaseyo) — " +
                    "spelling is compared loosely. Or tap Speak and say it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
