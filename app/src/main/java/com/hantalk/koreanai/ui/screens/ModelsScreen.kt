// The clickable Surface overload is experimental on some Material 3 versions and
// stable on others; opting in unconditionally keeps the build warning-free either way.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hantalk.koreanai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hantalk.koreanai.llm.EngineStatus
import com.hantalk.koreanai.llm.ModelRegistry
import com.hantalk.koreanai.ui.HanTalkViewModel
import com.hantalk.koreanai.ui.ModelUiState
import com.hantalk.koreanai.ui.components.CenteredTextWrapper
import com.hantalk.koreanai.ui.components.Pill
import com.hantalk.koreanai.ui.components.ProgressBlock
import com.hantalk.koreanai.ui.components.SoftCard
import com.hantalk.koreanai.ui.theme.HanTalkTokens
import com.hantalk.koreanai.ui.theme.PillShape

@Composable
fun ModelsScreen(viewModel: HanTalkViewModel, onBack: () -> Unit) {
    val models by viewModel.models.collectAsStateWithLifecycle()
    val status by viewModel.engineStatus.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "AI models",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        item {
            Column {
                Text(
                    text = "Optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.4.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Bigger brain, same privacy",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "HanTalk already works with a built-in tutor — no download " +
                        "needed. A model unlocks free-form conversation, still 100% on " +
                        "your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---- Active engine ----
        item {
            SoftCard(
                container = if (status is EngineStatus.Ready) HanTalkTokens.extra.jadeSoft
                else MaterialTheme.colorScheme.surfaceContainerLowest,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (status is EngineStatus.Ready) HanTalkTokens.extra.jade
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (status is EngineStatus.Ready) Icons.Filled.CheckCircle
                            else Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp),
                            tint = if (status is EngineStatus.Ready) Color.White
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Active tutor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = statusHeadline(status),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = statusDetail(status),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "AVAILABLE MODELS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(top = 8.dp)
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
            Spacer(Modifier.height(4.dp))
            CenteredTextWrapper(
                "Models are stored in app storage and never uploaded. " +
                    "Download over Wi-Fi; interrupted downloads resume."
            )
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
    SoftCard(contentPadding = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Pill(
                text = "${model.spec.minRamGb.toInt()} GB RAM",
                container = if (model.canRun) HanTalkTokens.extra.jadeSoft
                else MaterialTheme.colorScheme.errorContainer,
                content = if (model.canRun) HanTalkTokens.extra.jade
                else MaterialTheme.colorScheme.onErrorContainer
            )
            if (model.installed) {
                Pill(
                    text = "Installed",
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (model.downloading) {
            Spacer(Modifier.height(14.dp))
            ProgressBlock(
                label = "${formatBytes(model.bytes)} of ${formatBytes(model.total)}",
                percent = model.percent
            )
        }

        if (model.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = model.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                model.installed -> {
                    Button(
                        onClick = onActivate,
                        shape = PillShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Use this model", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        shape = PillShape,
                        modifier = Modifier.size(44.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                model.downloading -> {
                    OutlinedButton(
                        onClick = onDownload,
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Downloading · ${model.percent}%") }
                }

                else -> {
                    Button(
                        onClick = onDownload,
                        shape = PillShape,
                        enabled = model.canRun,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = if (model.canRun) "Download" else "Too large for this device",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun statusHeadline(status: EngineStatus): String = when (status) {
    is EngineStatus.Ready -> status.modelName
    is EngineStatus.Loading -> "Loading ${status.modelName}…"
    is EngineStatus.Failed -> "Offline tutor (model failed)"
    EngineStatus.NoModel -> "Offline tutor"
}

private fun statusDetail(status: EngineStatus): String = when (status) {
    is EngineStatus.Ready -> "Running on this phone · no network"
    is EngineStatus.Loading -> "Warming up the model"
    is EngineStatus.Failed -> status.message
    EngineStatus.NoModel -> "Rule-based · works instantly · no download"
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L ->
        String.format(java.util.Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))

    else -> String.format(java.util.Locale.US, "%.0f MB", bytes / (1024.0 * 1024.0))
}
