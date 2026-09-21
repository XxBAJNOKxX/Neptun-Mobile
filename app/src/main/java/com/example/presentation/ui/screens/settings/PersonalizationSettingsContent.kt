package com.example.presentation.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.currentStrings
import com.example.core.security.AppPersonalization
import com.example.presentation.navigation.NavigationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationSettingsContent(
    personalization: AppPersonalization,
    onStartScreenChange: (String) -> Unit,
    onShowWeekendChange: (Boolean) -> Unit,
    onHiddenPagesChange: (Set<String>) -> Unit,
    onTargetCreditsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()
    var startScreenDropdownOpen by remember { mutableStateOf(false) }
    val selectedStartItem = NavigationItem.entries.firstOrNull {
        it.name == personalization.startScreen
    } ?: NavigationItem.HOME

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Kezdőképernyő választó
        PreferenceGroupCard(
            title = strings.startScreenLabel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.startScreenDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = startScreenDropdownOpen,
                    onExpandedChange = { startScreenDropdownOpen = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedStartItem.getLocalizedTitle(strings),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text(strings.selectStartScreenPlaceholder) },
                        leadingIcon = {
                            Icon(
                                imageVector = selectedStartItem.selectedIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = startScreenDropdownOpen)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = startScreenDropdownOpen,
                        onDismissRequest = { startScreenDropdownOpen = false }
                    ) {
                        for (item in NavigationItem.entries) {
                            val isSelected = item == selectedStartItem
                            val isHidden = item.name in personalization.hiddenPages

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = item.getLocalizedTitle(strings),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isHidden) {
                                            Text(
                                                text = strings.currentlyHiddenPage,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = strings.select,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null,
                                onClick = {
                                    startScreenDropdownOpen = false
                                    onStartScreenChange(item.name)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        }

        // 2. Órarend hétvége
        PreferenceGroupCard(
            title = strings.timetableSectionLabel
        ) {
            PreferenceSwitchItem(
                title = strings.showWeekendLabel,
                subtitle = strings.showWeekendDesc,
                icon = Icons.Default.CalendarMonth,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                checked = personalization.showWeekend,
                onCheckedChange = onShowWeekendChange
            )
        }

        // 3. Látható oldalak / Alsó sáv fülei
        PreferenceGroupCard(
            title = strings.visiblePagesLabel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.visiblePagesDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val availablePages = NavigationItem.entries.filter { it != NavigationItem.SETTINGS }
                for ((index, item) in availablePages.withIndex()) {
                    val isHidden = item.name in personalization.hiddenPages

                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (!isHidden) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null,
                                tint = if (!isHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.getLocalizedTitle(strings),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (!isHidden) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (!isHidden) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = !isHidden,
                            onCheckedChange = { visible ->
                                val newHidden = if (visible) {
                                    personalization.hiddenPages - item.name
                                } else {
                                    personalization.hiddenPages + item.name
                                }
                                onHiddenPagesChange(newHidden)
                            }
                        )
                    }
                }
            }
        }

        // 4. Tanulmányok: Cél kreditek
        PreferenceGroupCard(
            title = strings.targetCreditsLabel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.targetCreditsLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.targetCreditsDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onTargetCreditsChange(personalization.targetCredits - 10) },
                        enabled = personalization.targetCredits > 30,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("−10", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "${personalization.targetCredits} ${strings.creditsUnit}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = { onTargetCreditsChange(personalization.targetCredits + 10) },
                        enabled = personalization.targetCredits < 400,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+10", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
