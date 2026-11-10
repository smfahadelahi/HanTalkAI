package com.hantalk.koreanai.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hantalk.koreanai.llm.ModelRegistry
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.ModelUiState
import com.hantalk.koreanai.ui.components.CenteredTextWrapper
import com.hantalk.koreanai.ui.components.Pill
import com.hantalk.koreanai.ui.components.ScreenHeader

@Composable
fun ModelsScreen(viewModel: HanTalkViewModel, onBack: () -> Unit) {
    val models by viewModel.models.collectAsStateWithLifecycle()
    val status by viewModel.engineStatus.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "AI models",
                subtitle = "Optional. Everything below runs offline on your phone."
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "HanTalk already works with a built-in rule-based tutor — " +
                            "no download needed. Installing a model unlocks free-form " +
                            "conversation and translations, still 100% on-device.\n\n" +
                            "Model files are large, so download over Wi-Fi. " +
                            "Downloads resume automatically if interrupted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "Active: ${statusText(status)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(models, key = { it.spec.id }) { model ->
            ModelCard(
                model = model,
                onDownload = { viewModel.downloadModel(model.spec) },
                onActivate = { viewModel.activateModel(model.spec) },
                onDelete = { viewModel.deleteModel(model.spec) }
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            CenteredTextWrapper(
                "Tip: models are stored in app storage and are never uploaded. " +
                    "Uninstalling the app removes them."
            )
        }

        item {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Back to tutor") }
        }
    }
}

@Composable
private fun ModelCard(
    model: ModelUiState,
    onDownload: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.spec.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Pill(text = ModelRegistry.formatSize(model.spec.approxSizeMb))
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = model.spec.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(
                    text = "needs ${model.spec.minRamGb.toInt()} GB RAM",
                    container = if (model.canRun) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.errorContainer,
                    content = if (model.canRun) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer
                )
                if (model.installed) {
                    Pill(
                        text = "installed",
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (model.downloading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { model.percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${model.percent}% · " +
                        "${formatBytes(model.bytes)} of ${formatBytes(model.total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (model.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = model.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    model.installed -> {
                        Button(onClick = onActivate) { Text("Use this model") }
                        OutlinedButton(onClick = onDelete) { Text("Delete") }
                    }

                    model.downloading -> {
                        OutlinedButton(onClick = onDownload) { Text("Downloading…") }
                    }

                    else -> {
                        Button(onClick = onDownload) { Text("Download") }
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L ->
        String.format(java.util.Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))

    else -> String.format(java.util.Locale.US, "%.0f MB", bytes / (1024.0 * 1024.0))
}

private fun statusText(status: com.hantalk.koreanai.llm.EngineStatus): String = when (status) {
    is com.hantalk.koreanai.llm.EngineStatus.Ready -> status.modelName
    is com.hantalk.koreanai.llm.EngineStatus.Loading -> "loading ${status.modelName}…"
    is com.hantalk.koreanai.llm.EngineStatus.Failed -> "failed (${status.message})"
    com.hantalk.koreanai.llm.EngineStatus.NoModel -> "built-in offline tutor"
}
