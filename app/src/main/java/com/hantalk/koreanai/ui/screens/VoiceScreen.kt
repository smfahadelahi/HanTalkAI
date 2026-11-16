// The clickable Surface overload is experimental on some Material 3 versions and
// stable on others; opting in unconditionally keeps the build warning-free either way.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hantalk.koreanai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hantalk.koreanai.domain.Phrase
import com.hantalk.koreanai.llm.EngineStatus
import com.hantalk.koreanai.speech.KoreanSpeaker
import com.hantalk.koreanai.speech.VoiceRecognizer
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.components.OrbState
import com.hantalk.koreanai.ui.components.Pill
import com.hantalk.koreanai.ui.components.SoftCard
import com.hantalk.koreanai.ui.components.VoiceOrb
import com.hantalk.koreanai.ui.theme.HanTalkTokens
import com.hantalk.koreanai.ui.theme.PillShape

/**
 * The default screen of the app.
 *
 * Voice mode is the shortest path from "I don't speak Korean" to "I just said
 * something in Korean": hold a conversation by talking, hear the answer with
 * correct pronunciation, and get one sentence to repeat. Typing and reading are
 * still one tap away, so nothing is lost for learners who prefer them.
 */
@Composable
fun VoiceScreen(
    viewModel: HanTalkViewModel,
    onOpenChat: () -> Unit,
    onOpenModels: () -> Unit,
    onPractice: (Phrase) -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val busy by viewModel.chatBusy.collectAsStateWithLifecycle()
    val engineStatus by viewModel.engineStatus.collectAsStateWithLifecycle()
    val autoSpeak by viewModel.autoSpeak.collectAsStateWithLifecycle()
    val sayThis by viewModel.sayThis.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val speaker = remember { KoreanSpeaker(context) }
    val recognizer = remember { VoiceRecognizer(context) }
    val voice by recognizer.state.collectAsStateWithLifecycle()
    val speaking by speaker.isSpeaking.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            recognizer.release()
            speaker.shutdown()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) recognizer.start() else recognizer.cancel()
    }

    // The most recent finished tutor reply that arrived through voice.
    val lastSpokenReply = messages.lastOrNull { !it.fromUser && !it.streaming && it.spoken }
    var spokenId by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(lastSpokenReply?.id, autoSpeak) {
        val reply = lastSpokenReply ?: return@LaunchedEffect
        if (!autoSpeak || reply.id == spokenId) return@LaunchedEffect
        spokenId = reply.id
        val korean = viewModel.speakableReply(reply.text)
        if (korean.isNotBlank()) {
            speaker.speak(korean)
        }
    }

    // When recognition finishes, send it straight to the tutor.
    LaunchedEffect(voice.phase, voice.text) {
        if (voice.phase == VoiceRecognizer.Phase.Done && voice.text.isNotBlank()) {
            viewModel.sendSpokenMessage(voice.text)
            recognizer.cancel()
        }
    }

    val orbState = when {
        speaking -> OrbState.Speaking
        voice.isListening -> OrbState.Listening
        busy -> OrbState.Thinking
        else -> OrbState.Idle
    }
    val level by animateFloatAsState(
        targetValue = when (orbState) {
            OrbState.Listening -> voice.level.coerceAtLeast(0.12f)
            OrbState.Speaking -> 0.55f
            else -> 0f
        },
        label = "level"
    )

    // Only show the "what the tutor said" card once a real conversation started,
    // so the first launch stays clean and focused on the mic.
    val conversationStarted = messages.any { it.fromUser }
    val lastReplyText =
        if (conversationStarted) messages.lastOrNull { !it.fromUser }?.text.orEmpty() else ""

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        VoiceTopBar(
            status = engineStatus,
            autoSpeak = autoSpeak,
            onOpenModels = onOpenModels,
            onToggleAutoSpeak = viewModel::toggleAutoSpeak
        )

        Spacer(Modifier.height(6.dp))

        // ---- Greeting ----
        Text(
            text = greeting(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "무엇이든 물어보세요",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Ask out loud — I'll answer in Korean and English.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // ---- The orb ----
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            VoiceOrb(
                state = orbState,
                level = level,
                onClick = {
                    when {
                        speaking -> speaker.stop()
                        voice.isListening -> recognizer.stopListening()
                        busy -> viewModel.stopGenerating()
                        !recognizer.hasPermission() ->
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

                        else -> recognizer.start()
                    }
                }
            )
        }

        VoiceCaption(
            orbState = orbState,
            partial = voice.partial,
            message = voice.message,
            autoSpeak = autoSpeak
        )

        Spacer(Modifier.height(10.dp))

        // ---- What the tutor just said ----
        AnimatedVisibility(
            visible = lastReplyText.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TutorSaidCard(
                text = lastReplyText,
                busy = busy,
                onReplay = {
                    val korean = viewModel.speakableReply(lastReplyText)
                    if (korean.isNotBlank()) speaker.speak(korean)
                },
                onSlower = {
                    val korean = viewModel.speakableReply(lastReplyText)
                    if (korean.isNotBlank()) speaker.speakSlowly(korean)
                },
                onOpenChat = onOpenChat
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---- One sentence to repeat ----
        sayThis?.let { phrase ->
            SayThisCard(
                phrase = phrase,
                onHear = { speaker.speak(phrase.korean) },
                onHearSlowly = { speaker.speakSlowly(phrase.korean) },
                onNext = viewModel::nextSayThis,
                onPractice = { onPractice(phrase) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // ---- Quick starts ----
        Text(
            text = "TRY SAYING",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.2.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickChip("Teach me a greeting", Modifier.weight(1f)) {
                viewModel.sendSpokenMessage("Teach me a greeting in Korean")
            }
            QuickChip("How do I say my name?", Modifier.weight(1f)) {
                viewModel.sendSpokenMessage("How do I say my name in Korean?")
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickChip("Count to five", Modifier.weight(1f)) {
                viewModel.sendSpokenMessage("Count to five in Korean")
            }
            QuickChip("받침 explained", Modifier.weight(1f)) {
                viewModel.sendSpokenMessage("What is batchim?")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- Progress strip ----
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(HanTalkTokens.gradients.brandSoft.first())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (stats.streak > 0) {
                    "${stats.streak}-day streak · ${stats.learnedLetters}/${stats.totalLetters} letters learned"
                } else {
                    "Tap the mic for your first conversation"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun VoiceTopBar(
    status: EngineStatus,
    autoSpeak: Boolean,
    onOpenModels: () -> Unit,
    onToggleAutoSpeak: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Wordmark
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(HanTalkTokens.gradients.brand)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "한",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(9.dp))
            Column {
                Text(
                    text = "HanTalk",
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = when (status) {
                        is EngineStatus.Ready -> status.modelName
                        EngineStatus.NoModel -> "Offline tutor · no download"
                        is EngineStatus.Loading -> "Loading model…"
                        is EngineStatus.Failed -> "Offline tutor (model failed)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onToggleAutoSpeak) {
            Icon(
                imageVector = if (autoSpeak) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                contentDescription = if (autoSpeak) "Mute replies" else "Speak replies",
                tint = if (autoSpeak) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onOpenModels) {
            Icon(
                Icons.Filled.Tune,
                contentDescription = "AI models",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoiceCaption(
    orbState: OrbState,
    partial: String,
    message: String?,
    autoSpeak: Boolean
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (title, hint) = when (orbState) {
            OrbState.Idle -> "Tap to speak" to
                if (autoSpeak) "Your answer will be read aloud" else "Replies are muted"

            OrbState.Listening -> "Listening…" to "Speak naturally, then pause"
            OrbState.Thinking -> "Thinking…" to "Working it out on your phone"
            OrbState.Speaking -> "Speaking" to "Tap the orb to stop"
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = partial.ifBlank { message ?: hint },
            style = MaterialTheme.typography.bodySmall,
            color = if (message != null && partial.isBlank()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun TutorSaidCard(
    text: String,
    busy: Boolean,
    onReplay: () -> Unit,
    onSlower: () -> Unit,
    onOpenChat: () -> Unit
) {
    SoftCard(
        onClick = onOpenChat,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(
                text = if (busy) "HanTalk is typing…" else "HanTalk said",
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSlower, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Filled.SlowMotionVideo,
                    contentDescription = "Play slowly",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onReplay, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Filled.GraphicEq,
                    contentDescription = "Replay",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = text.trim().take(700).ifBlank { "…" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Read the full answer",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SayThisCard(
    phrase: Phrase,
    onHear: () -> Unit,
    onHearSlowly: () -> Unit,
    onNext: () -> Unit,
    onPractice: () -> Unit
) {
    SoftCard(
        container = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SAY THIS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Another phrase",
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = phrase.korean,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = phrase.romanisation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = phrase.english,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniAction("Hear it", Icons.Filled.VolumeUp, primary = true) { onHear() }
            MiniAction("Slowly", Icons.Filled.SlowMotionVideo) { onHearSlowly() }
            MiniAction("Practise", Icons.Filled.AutoAwesome) { onPractice() }
        }
    }
}

@Composable
private fun MiniAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = PillShape,
        color = if (primary) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (primary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (primary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickChip(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = PillShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            HanTalkTokens.extra.hairline
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        )
    }
}

private fun greeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "좋은 아침이에요 · Good morning"
        in 12..17 -> "안녕하세요 · Good afternoon"
        in 18..22 -> "좋은 저녁이에요 · Good evening"
        else -> "늦었네요 · Burning the midnight oil"
    }
}
