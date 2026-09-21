package com.example.presentation.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.currentStrings
import com.example.core.security.AppPersonalization
import com.example.ui.theme.NeptunGreen

@Composable
fun SecuritySettingsContent(
    personalization: AppPersonalization,
    lastSyncFormatted: String,
    isSyncing: Boolean,
    syncSuccessMessage: String?,
    isClearingCache: Boolean,
    cacheClearedMessage: String?,
    onBiometricLockChange: (Boolean) -> Unit,
    onManualSync: () -> Unit,
    onExportIcs: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Biometrikus Védelem
        PreferenceGroupCard(
            title = strings.securityTitle
        ) {
            PreferenceSwitchItem(
                title = strings.biometricLockTitle,
                subtitle = strings.biometricLockDesc,
                icon = Icons.Default.Fingerprint,
                iconContainerColor = NeptunGreen.copy(alpha = 0.15f),
                iconContentColor = NeptunGreen,
                checked = personalization.biometricLockEnabled,
                onCheckedChange = onBiometricLockChange
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = strings.securityDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                lineHeight = 16.sp
            )
        }

        // 2. Szinkronizáció és adatok
        PreferenceGroupCard(
            title = strings.sectionDataCache
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${strings.lastSyncLabel} $lastSyncFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Sync Feedback Banner
                AnimatedVisibility(
                    visible = syncSuccessMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val displaySyncMessage = when (syncSuccessMessage) {
                        "SUCCESS", "Sikeres szinkronizálás! Minden adat naprakész." -> strings.syncSuccess
                        "COMPLETED", "Szinkronizálás befejeződött." -> strings.syncCompleted
                        else -> syncSuccessMessage ?: ""
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeptunGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeptunGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeptunGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = displaySyncMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeptunGreen
                            )
                        }
                    }
                }

                FilledTonalButton(
                    onClick = onManualSync,
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("manual_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.syncInProgress, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.manualSyncBtn, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onExportIcs,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(strings.exportIcsBtn, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onClearCache,
                    enabled = !isClearingCache,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    if (isClearingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.clearingCacheInProgress, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.clearCacheBtn, fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(
                    visible = cacheClearedMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val displayCacheMessage = when (cacheClearedMessage) {
                        "SUCCESS", "A helyi gyorsítótár törölve. A következő szinkronizáláskor friss adatok töltődnek le." -> strings.cacheClearedSuccess
                        "FAILED", "A törlés nem sikerült." -> strings.cacheClearFailed
                        else -> cacheClearedMessage ?: ""
                    }
                    Text(
                        text = displayCacheMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
