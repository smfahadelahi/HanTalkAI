package com.hantalk.koreanai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hantalk.koreanai.ui.HanTalkApp
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.theme.HanTalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanTalkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: HanTalkViewModel = viewModel()
                    HanTalkApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun RootScaffold(content: @Composable () -> Unit) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
    ) { inner ->
        androidx.compose.foundation.layout.Box(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .consumeWindowInsets(inner)
        ) { content() }
    }
}
