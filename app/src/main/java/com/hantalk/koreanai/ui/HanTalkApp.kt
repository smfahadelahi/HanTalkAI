package com.hantalk.koreanai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hantalk.koreanai.domain.Phrase
import com.hantalk.koreanai.ui.screens.AlphabetScreen
import com.hantalk.koreanai.ui.screens.LessonsScreen
import com.hantalk.koreanai.ui.screens.ModelsScreen
import com.hantalk.koreanai.ui.screens.PracticeScreen
import com.hantalk.koreanai.ui.screens.TutorScreen
import com.hantalk.koreanai.ui.screens.VoiceScreen
import com.hantalk.koreanai.ui.theme.HanTalkTokens

enum class Tab(val label: String, val icon: ImageVector) {
    Voice("Voice", Icons.Filled.Mic),
    Tutor("Chat", Icons.Filled.Translate),
    Lessons("Learn", Icons.AutoMirrored.Filled.MenuBook),
    Alphabet("한글", Icons.Filled.Lightbulb),
    Practice("Practise", Icons.Filled.Edit)
}

@Composable
fun HanTalkApp(viewModel: HanTalkViewModel) {
    // Voice is the default: it is the fastest way for a beginner to start.
    var tab by rememberSaveable { mutableStateOf(Tab.Voice.name) }
    var showModels by rememberSaveable { mutableStateOf(false) }
    var practiceSeed by remember { mutableStateOf<Phrase?>(null) }

    val currentTab = Tab.entries.firstOrNull { it.name == tab } ?: Tab.Voice

    if (showModels) {
        ModelsScreen(viewModel = viewModel, onBack = { showModels = false })
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HanTalkNavBar(
                current = currentTab,
                onSelect = { tab = it.name }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                Tab.Voice -> VoiceScreen(
                    viewModel = viewModel,
                    onOpenChat = { tab = Tab.Tutor.name },
                    onOpenModels = { showModels = true },
                    onPractice = { phrase ->
                        practiceSeed = phrase
                        viewModel.startPractice(phrase)
                        tab = Tab.Practice.name
                    }
                )

                Tab.Tutor -> TutorScreen(
                    viewModel = viewModel,
                    onOpenModels = { showModels = true }
                )

                Tab.Lessons -> LessonsScreen(
                    viewModel = viewModel,
                    onPractice = { phrase ->
                        practiceSeed = phrase
                        viewModel.startPractice(phrase)
                        tab = Tab.Practice.name
                    }
                )

                Tab.Alphabet -> AlphabetScreen(viewModel = viewModel)

                Tab.Practice -> PracticeScreen(
                    viewModel = viewModel,
                    startWith = practiceSeed?.korean
                )
            }
        }
    }
}

/**
 * A custom bottom bar: hairline top edge, no tonal elevation, and the selected
 * destination gets a gradient pill. Material's NavigationBar with its 32 dp grey
 * indicator is the single most "default-looking" part of a Compose app, so it is
 * replaced here.
 */
@Composable
private fun HanTalkNavBar(current: Tab, onSelect: (Tab) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HanTalkTokens.extra.hairline)
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tab.entries.forEach { entry ->
                    NavItem(
                        entry = entry,
                        selected = entry == current,
                        onClick = { onSelect(entry) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    entry: Tab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "navScale"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navColor"
    )
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Gradient pill behind the selected icon. Wrapped in a Box so the
                // top-level (non-ColumnScope) AnimatedVisibility overload is used.
                Box {
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn() + scaleIn(initialScale = 0.7f),
                        exit = fadeOut() + scaleOut(targetScale = 0.7f)
                    ) {
                        Box(
                            Modifier
                                .size(width = 46.dp, height = 30.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(
                                    Brush.linearGradient(HanTalkTokens.gradients.brand)
                                )
                        )
                    }
                }
                Icon(
                    imageVector = entry.icon,
                    contentDescription = entry.label,
                    tint = contentColor,
                    modifier = Modifier
                        .size((21 * scale).dp)
                        .padding(0.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
