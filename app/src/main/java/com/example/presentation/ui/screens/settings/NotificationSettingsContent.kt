package com.example.presentation.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.currentStrings
import com.example.core.security.NotificationPreferences
import com.example.ui.theme.NeptunBlue40
import com.example.ui.theme.NeptunGold
import com.example.ui.theme.NeptunGreen
import com.example.ui.theme.NeptunPurple

@Composable
fun NotificationSettingsContent(
    notificationPreferences: NotificationPreferences,
    isNotificationPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onNotifyClassesChange: (Boolean) -> Unit,
    onNotifyGradesChange: (Boolean) -> Unit,
    onNotifyMessagesChange: (Boolean) -> Unit,
    onNotifyFinancesChange: (Boolean) -> Unit,
    onSimulateClassNotification: () -> Unit,
    onSimulateGradeNotification: () -> Unit,
    onSimulateMessageNotification: () -> Unit,
    onSimulateFinanceNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // OS Permission status banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNotificationPermissionGranted) {
                    NeptunGreen.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                }
            ),
            border = BorderStroke(
                1.dp,
                if (isNotificationPermissionGranted) NeptunGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isNotificationPermissionGranted) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (isNotificationPermissionGranted) NeptunGreen else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isNotificationPermissionGranted) strings.notifPermissionGranted else strings.notifPermissionRequired,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isNotificationPermissionGranted) NeptunGreen else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (isNotificationPermissionGranted) strings.notifPermissionGrantedDesc else strings.notifPermissionRequiredDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isNotificationPermissionGranted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(strings.grantPermissionBtn, fontSize = 12.sp)
                    }
                }
            }
        }

        // Értesítési Kategóriák
        PreferenceGroupCard(
            title = strings.notificationsTitle
        ) {
            NotificationCategoryTile(
                icon = Icons.Default.Alarm,
                iconColor = NeptunBlue40,
                title = strings.notifClassesTitle,
                description = strings.notifClassesDesc,
                checked = notificationPreferences.notifyClasses,
                onCheckedChange = onNotifyClassesChange,
                testLabel = strings.testClassBtn,
                onTest = onSimulateClassNotification
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            NotificationCategoryTile(
                icon = Icons.Default.School,
                iconColor = NeptunGold,
                title = strings.notifGradesTitle,
                description = strings.notifGradesDesc,
                checked = notificationPreferences.notifyGrades,
                onCheckedChange = onNotifyGradesChange,
                testLabel = strings.testGradeBtn,
                onTest = onSimulateGradeNotification
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            NotificationCategoryTile(
                icon = Icons.Default.Email,
                iconColor = NeptunPurple,
                title = strings.notifMessagesTitle,
                description = strings.notifMessagesDesc,
                checked = notificationPreferences.notifyMessages,
                onCheckedChange = onNotifyMessagesChange,
                testLabel = strings.testMsgBtn,
                onTest = onSimulateMessageNotification
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            NotificationCategoryTile(
                icon = Icons.Default.AccountBalanceWallet,
                iconColor = NeptunGreen,
                title = strings.notifFinancesTitle,
                description = strings.notifFinancesDesc,
                checked = notificationPreferences.notifyFinances,
                onCheckedChange = onNotifyFinancesChange,
                testLabel = strings.testFinanceBtn,
                onTest = onSimulateFinanceNotification
            )
        }
    }
}

@Composable
private fun NotificationCategoryTile(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testLabel: String,
    onTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else null
            )
        }

        if (checked) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onTest,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(testLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
