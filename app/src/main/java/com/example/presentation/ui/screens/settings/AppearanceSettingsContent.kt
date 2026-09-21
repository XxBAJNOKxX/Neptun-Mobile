package com.example.presentation.ui.screens.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.currentStrings
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemeSettings
import com.example.ui.theme.getLocalizedTitle

@Composable
fun AppearanceSettingsContent(
    themeSettings: ThemeSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    onAccentColorSelect: (AppAccentColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Téma Mód (Rendszer / Világos / Sötét)
        PreferenceGroupCard(
            title = strings.themeModeLabel
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                ThemeModeSelector(
                    selectedMode = themeSettings.themeMode,
                    onSelectMode = onThemeModeChange,
                    strings = strings
                )
            }
        }

        // 2. Dinamikus Színek (Material You)
        PreferenceGroupCard(
            title = strings.dynamicColorLabel
        ) {
            PreferenceSwitchItem(
                title = strings.dynamicColorLabel,
                subtitle = if (isDynamicColorSupported) strings.dynamicColorDesc else strings.dynamicColorOnlyAndroid12,
                icon = Icons.Default.AutoAwesome,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                checked = themeSettings.useDynamicColor && isDynamicColorSupported,
                onCheckedChange = onDynamicColorToggle,
                enabled = isDynamicColorSupported
            )

            AnimatedVisibility(visible = themeSettings.useDynamicColor && isDynamicColorSupported) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.dynamicColorActiveBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Kiemelőszín Paletta
        PreferenceGroupCard(
            title = strings.accentColorTitle
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.accentColorSelected} ${themeSettings.accentColor.getLocalizedTitle(strings)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (themeSettings.useDynamicColor && isDynamicColorSupported) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = strings.dynamicColors,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                AccentColorPicker(
                    selectedAccent = themeSettings.accentColor,
                    onAccentSelect = { accent ->
                        if (themeSettings.useDynamicColor) {
                            onDynamicColorToggle(false)
                        }
                        onAccentColorSelect(accent)
                    }
                )
            }
        }

        // 4. Élő Téma Előnézet
        PreferenceGroupCard(
            title = strings.livePreviewTitle
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.activeThemeLabel} ${if (themeSettings.useDynamicColor && isDynamicColorSupported) strings.dynamicColors else themeSettings.accentColor.getLocalizedTitle(strings)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = themeSettings.themeMode.getLocalizedTitle(strings),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(strings.btnPrimary, fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = {},
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(strings.btnTonal, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(strings.btnOutlined, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
