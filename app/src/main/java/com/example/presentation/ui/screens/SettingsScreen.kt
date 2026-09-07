package com.example.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.NotificationPreferences
import com.example.domain.model.StudentCredentials
import com.example.presentation.ui.components.FilcAvatar
import com.example.presentation.ui.components.FilcBackBar
import com.example.presentation.ui.components.FilcBottomSheet
import com.example.presentation.ui.components.FilcCard
import com.example.presentation.ui.components.FilcChip
import com.example.presentation.ui.components.FilcDivider
import com.example.presentation.ui.components.FilcPanel
import com.example.presentation.ui.components.FilcPanelButton
import com.example.presentation.ui.components.FilcSwitch
import com.example.presentation.viewmodel.UpdateCheckState
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemeSettings
import com.example.ui.theme.filcColors

/**
 * Profil és beállítások – a reFilc "profile" oldala: kártyás, paneljes,
 * accent-választós megjelenés, a kilépés pedig megerősítős alsó lap.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    credentials: StudentCredentials?,
    themeSettings: ThemeSettings,
    notificationPreferences: NotificationPreferences,
    isSyncing: Boolean,
    syncSuccessMessage: String?,
    updateCheckState: UpdateCheckState = UpdateCheckState(),
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    onAccentColorSelect: (AppAccentColor) -> Unit,
    onNotifyClassesChange: (Boolean) -> Unit,
    onNotifyGradesChange: (Boolean) -> Unit,
    onNotifyMessagesChange: (Boolean) -> Unit,
    onNotifyFinancesChange: (Boolean) -> Unit,
    onSimulateClassNotification: () -> Unit,
    onSimulateMessageNotification: () -> Unit,
    onSimulateGradeNotification: () -> Unit,
    onSimulateFinanceNotification: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    onLogoutClick: () -> Unit,
    onManualSync: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    var showLogoutSheet by remember { mutableStateOf(false) }
    val fullName = credentials?.studentName?.takeIf { it.isNotBlank() } ?: "Neptun hallgató"

    Box(modifier = modifier.fillMaxSize().background(filc.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilcBackBar(
                title = "Profil",
                subtitle = credentials?.universityName?.takeIf { it.isNotBlank() }
                    ?: "Neptun Mobile",
                onBack = onBack,
                modifier = Modifier.testTag("profile_back_bar")
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    FilcCard(contentPadding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilcAvatar(name = fullName, size = 54.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = filc.text,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append(credentials?.neptunCode?.takeIf { it.isNotBlank() } ?: "–")
                                        credentials?.trainingProgram?.let {
                                            append(" · ")
                                            append(it)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = filc.textMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (credentials?.isLoggedIn == true) {
                                FilcChip(
                                    text = "szinkron",
                                    color = filc.green,
                                    background = filc.green.copy(alpha = 0.16f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        FilcDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.Refresh,
                                contentDescription = null,
                                tint = filc.icon,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isSyncing) "Szinkronizálás…" else "Adatok frissítése",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = filc.text
                                )
                                Text(
                                    text = lastSyncLabel(credentials?.lastSyncTime ?: 0L),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = filc.textMuted,
                                    maxLines = 1
                                )
                            }
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = filc.accent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(45.dp))
                                        .background(filc.accent)
                                        .clickable(onClick = onManualSync)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("profile_manual_sync")
                                ) {
                                    Text(
                                        text = "Frissítés",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (filc.isLight) Color.Black.copy(alpha = 0.85f) else filc.onAccent
                                    )
                                }
                            }
                        }

                        if (syncSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = syncSuccessMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = filc.green
                            )
                        }
                    }
                }

                item {
                    FilcPanel(title = "Megjelenés") {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val selected = themeSettings.themeMode == mode && !themeSettings.useDynamicColor
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (selected) filc.accent.copy(alpha = 0.18f) else filc.text.copy(alpha = 0.04f)
                                        )
                                        .border(
                                            width = if (selected) 1.5.dp else 0.dp,
                                            color = if (selected) filc.accent else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable(onClick = { onThemeModeChange(mode) })
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                            ThemeMode.LIGHT -> Icons.Default.LightMode
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                        },
                                        contentDescription = null,
                                        tint = if (selected) filc.accent else filc.textMuted,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) filc.accent else filc.textSecondary
                                    )
                                }
                            }
                        }

                        FilcPanelButton(
                            title = "Anyacsík-színek (Material You)",
                            subtitle = "A rendszerpaletta elsődleges színe az accent",
                            leading = {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = filc.icon,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            trailing = {
                                FilcSwitch(
                                    checked = themeSettings.useDynamicColor,
                                    onCheckedChange = onDynamicColorToggle
                                )
                            },
                            onClick = { onDynamicColorToggle(!themeSettings.useDynamicColor) },
                            modifier = Modifier.testTag("profile_dynamic_color")
                        )

                        FilcDivider(modifier = Modifier.padding(vertical = 4.dp))

                        FilcPanelButton(
                            title = "Filc accent",
                            subtitle = themeSettings.accentColor.title,
                            leading = {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = themeSettings.accentColor.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        )
                        FilcAccentPicker(
                            selected = themeSettings.accentColor,
                            onSelect = onAccentColorSelect
                        )
                    }
                }

                item {
                    FilcPanel(
                        title = "Értesítések",
                        titleTrailing = {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = filc.textMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    ) {
                        NotificationSwitchRow(
                            title = "Órák előtt",
                            subtitle = "${notificationPreferences.reminderMinutesBefore} perccel a kezdés előtt",
                            checked = notificationPreferences.notifyClasses,
                            onCheckedChange = onNotifyClassesChange,
                            modifier = Modifier.testTag("notify_classes")
                        )
                        NotificationSwitchRow(
                            title = "Új jegy",
                            subtitle = "Értesítés, ha bekerül egy jegy",
                            checked = notificationPreferences.notifyGrades,
                            onCheckedChange = onNotifyGradesChange,
                            modifier = Modifier.testTag("notify_grades")
                        )
                        NotificationSwitchRow(
                            title = "Új üzenet",
                            subtitle = "Neptun postafiók átnézése",
                            checked = notificationPreferences.notifyMessages,
                            onCheckedChange = onNotifyMessagesChange,
                            modifier = Modifier.testTag("notify_messages")
                        )
                        NotificationSwitchRow(
                            title = "Befizetés",
                            subtitle = "Csoportköltség határidők",
                            checked = notificationPreferences.notifyFinances,
                            onCheckedChange = onNotifyFinancesChange,
                            modifier = Modifier.testTag("notify_finances")
                        )

                        FilcDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            text = "Küldés próbája",
                            style = MaterialTheme.typography.labelSmall,
                            color = filc.textMuted,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 10.dp, bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TestNotificationChip("Óra", onSimulateClassNotification, Modifier.weight(1f))
                            TestNotificationChip("Jegy", onSimulateGradeNotification, Modifier.weight(1f))
                            TestNotificationChip("Üzenet", onSimulateMessageNotification, Modifier.weight(1f))
                            TestNotificationChip("Pénz", onSimulateFinanceNotification, Modifier.weight(1f))
                        }
                    }
                }

                item {
                    FilcPanel(title = "Alkalmazás") {
                        FilcPanelButton(
                            title = "Frissítés ellenőrzése",
                            subtitle = updateCheckState.message
                                ?: if (updateCheckState.isChecking) {
                                    "Ellenőrzés folyamatban…"
                                } else {
                                    "példány: ${com.example.BuildConfig.VERSION_NAME}"
                                },
                            leading = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = filc.icon,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            trailing = {
                                if (updateCheckState.isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = filc.accent,
                                        strokeWidth = 2.dp
                                    )
                                } else if (updateCheckState.updateAvailable) {
                                    FilcChip(
                                        text = "új verzió",
                                        color = filc.green,
                                        background = filc.green.copy(alpha = 0.16f)
                                    )
                                }
                            },
                            onClick = onCheckForUpdates,
                            modifier = Modifier.testTag("profile_check_updates")
                        )
                        FilcDivider()
                        FilcPanelButton(
                            title = "Kilépés",
                            subtitle = "Helyi adatok törlése és kijelentkezés",
                            leading = {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = filc.red,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            onClick = { showLogoutSheet = true },
                            modifier = Modifier.testTag("profile_logout_button")
                        )
                    }
                }
            }
        }

        FilcBottomSheet(visible = showLogoutSheet, onDismiss = { showLogoutSheet = false }) {
            Text(
                text = "Kilépsz a Neptun Mobile-ból?",
                style = MaterialTheme.typography.titleLarge,
                color = filc.text
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "A jegyek, órák és üzenetek törlődnek erről a telefonról. A Neptunban nem változik semmi.",
                style = MaterialTheme.typography.bodyMedium,
                color = filc.textMuted,
                lineHeight = 21.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(filc.text.copy(alpha = 0.06f))
                        .clickable(onClick = { showLogoutSheet = false })
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mégse",
                        style = MaterialTheme.typography.titleSmall,
                        color = filc.text
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(filc.red.copy(alpha = 0.16f))
                        .clickable {
                            showLogoutSheet = false
                            onLogoutClick()
                        }
                        .padding(vertical = 13.dp)
                        .testTag("profile_logout_confirm"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kilépés",
                        style = MaterialTheme.typography.titleSmall,
                        color = filc.red
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilcPanelButton(
        title = title,
        subtitle = subtitle,
        trailing = {
            FilcSwitch(checked = checked, onCheckedChange = onCheckedChange)
        },
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
    )
}

@Composable
private fun TestNotificationChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(45.dp))
            .background(filc.text.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = filc.textSecondary
        )
    }
}

/** Accent választó kör-grid – a Filc beállításokban ez színezi az egész appot. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FilcAccentPicker(
    selected: AppAccentColor,
    onSelect: (AppAccentColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppAccentColor.entries.forEach { accent ->
            val isSelected = accent == selected
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.primary)
                    .border(
                        width = if (isSelected) 2.5.dp else 0.dp,
                        color = if (isSelected) filc.text else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable(onClick = { onSelect(accent) })
                    .testTag("accent_${'$'}{accent.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Kivalasztott accent",
                        tint = if (accent == AppAccentColor.FILC) {
                            Color.Black.copy(alpha = 0.85f)
                        } else {
                            Color.White
                        },
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

private fun lastSyncLabel(timestamp: Long): String {
    if (timestamp <= 0L) return "Még nem történt szinkronizálás"
    val formatter = java.text.SimpleDateFormat("MMM. d. HH:mm", java.util.Locale("hu", "HU"))
    return "Utolsó szinkron: " + formatter.format(java.util.Date(timestamp))
}
