package com.hantalk.koreanai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hantalk.koreanai.domain.Phrase
import com.hantalk.koreanai.ui.screens.AlphabetScreen
import com.hantalk.koreanai.ui.screens.LessonsScreen
import com.hantalk.koreanai.ui.screens.ModelsScreen
import com.hantalk.koreanai.ui.screens.PracticeScreen
import com.hantalk.koreanai.ui.screens.TutorScreen

enum class Tab(val label: String, val icon: ImageVector) {
    Tutor("Tutor", Icons.Filled.Translate),
    Lessons("Lessons", Icons.AutoMirrored.Filled.MenuBook),
    Alphabet("한글", Icons.Filled.Lightbulb),
    Practice("Practice", Icons.Filled.Edit)
}

@Composable
fun HanTalkApp(viewModel: HanTalkViewModel) {
    var tab by rememberSaveable { mutableStateOf(Tab.Tutor.name) }
    var showModels by rememberSaveable { mutableStateOf(false) }
    var practiceSeed by remember { mutableStateOf<Phrase?>(null) }

    val currentTab = Tab.entries.firstOrNull { it.name == tab } ?: Tab.Tutor

    if (showModels) {
        ModelsScreen(viewModel = viewModel, onBack = { showModels = false })
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Tab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = entry == currentTab,
                        onClick = { tab = entry.name },
                        icon = { Icon(entry.icon, contentDescription = entry.label) },
                        label = { Text(entry.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
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
