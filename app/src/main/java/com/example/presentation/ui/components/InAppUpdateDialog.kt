package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.update.InAppUpdateState
import com.example.core.update.UpdateInfo
import com.example.ui.theme.filcColors

/**
 * Frissítés-értesítő – a Filc "bottom card" formában jelenik meg, nem a
 * Material alapértelmezett ablakában.
 */
@Composable
fun InAppUpdateDialog(
    updateState: InAppUpdateState,
    onStartUpdate: (UpdateInfo) -> Unit,
    onInstallApk: () -> Unit,
    onDismiss: () -> Unit
) {
    val visible = updateState !is InAppUpdateState.Idle && updateState !is InAppUpdateState.Checking
    FilcBottomSheet(visible = visible, onDismiss = onDismiss, scrollable = false) {
        when (updateState) {
            is InAppUpdateState.UpdateAvailable -> UpdateAvailableCard(
                info = updateState.info,
                onStartUpdate = { onStartUpdate(updateState.info) },
                onDismiss = onDismiss
            )

            is InAppUpdateState.Downloading -> DownloadingCard(
                progress = updateState.progress,
                downloadedBytes = updateState.downloadedBytes,
                totalBytes = updateState.totalBytes
            )

            is InAppUpdateState.ReadyToInstall -> ReadyToInstallCard(onInstallApk = onInstallApk)

            is InAppUpdateState.Error -> UpdateErrorCard(message = updateState.message, onDismiss = onDismiss)

            else -> Unit
        }
    }
}

@Composable
private fun UpdateAvailableCard(
    info: UpdateInfo,
    onStartUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    val filc = filcColors()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(filc.accent.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = filc.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Új verzió érhető el",
                style = MaterialTheme.typography.titleLarge,
                color = filc.text
            )
            Text(
                text = "v${info.currentVersion} → v${info.latestVersion}",
                style = MaterialTheme.typography.labelSmall,
                color = filc.textMuted
            )
        }
        FilcChip(
            text = info.tagName.ifBlank { "frissítés" },
            color = filc.accent,
            background = filc.accent.copy(alpha = 0.16f)
        )
    }

    if (info.releaseNotes.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(filc.text.copy(alpha = 0.04f))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 150.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = info.releaseNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = filc.textSecondary,
                    lineHeight = 19.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(filc.text.copy(alpha = 0.06f))
                .clickable(onClick = onDismiss)
                .testTag("in_app_update_later_button")
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Később",
                style = MaterialTheme.typography.titleSmall,
                color = filc.text
            )
        }
        Box(
            modifier = Modifier
                .weight(1.4f)
                .clip(RoundedCornerShape(14.dp))
                .background(filc.accent)
                .clickable(onClick = onStartUpdate)
                .testTag("in_app_update_start_button")
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = if (filc.isLight) Color(0xFF1C2605) else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "Frissítés most",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (filc.isLight) Color(0xFF1C2605) else Color.White
                )
            }
        }
    }
}

@Composable
private fun DownloadingCard(
    progress: Float,
    downloadedBytes: Long,
    totalBytes: Long
) {
    val filc = filcColors()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                progress = { if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()) else 0f },
                modifier = Modifier.size(26.dp),
                color = filc.accent,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Letöltés folyamatban",
                    style = MaterialTheme.typography.titleMedium,
                    color = filc.text
                )
                Text(
                    text = if (totalBytes > 0) {
                        "${downloadedBytes / 1024 / 1024} MB / ${totalBytes / 1024 / 1024} MB"
                    } else {
                        "Az APK csomag letöltése…"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted
                )
            }
        }
        FilcProgressBar(
            progress = if (totalBytes > 0) progress else 0f,
            barHeight = 8.dp
        )
    }
}

@Composable
private fun ReadyToInstallCard(onInstallApk: () -> Unit) {
    val filc = filcColors()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.OpenInBrowser,
                contentDescription = null,
                tint = filc.green,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "A telepítő csomag kész – nyisd meg a telepítéshez.",
                style = MaterialTheme.typography.bodyLarge,
                color = filc.text,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(filc.accent)
                .clickable(onClick = onInstallApk)
                .testTag("in_app_update_install_button")
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Telepítés",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (filc.isLight) Color(0xFF1C2605) else Color.White
            )
        }
    }
}

@Composable
private fun UpdateErrorCard(message: String, onDismiss: () -> Unit) {
    val filc = filcColors()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = filc.red,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = filc.text,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(45.dp))
                .background(filc.text.copy(alpha = 0.06f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = "OK",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = filc.textSecondary
            )
        }
    }
}
