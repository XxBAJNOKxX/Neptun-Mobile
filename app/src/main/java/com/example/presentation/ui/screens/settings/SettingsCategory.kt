package com.example.presentation.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.i18n.AppStrings

enum class SettingsCategory(
    val icon: ImageVector
) {
    APPEARANCE(Icons.Default.Palette),
    NOTIFICATIONS(Icons.Default.Notifications),
    PERSONALIZATION(Icons.Default.Tune),
    SECURITY(Icons.Default.Security),
    LANGUAGE(Icons.Default.Language),
    ABOUT(Icons.Default.Info);

    fun getTitle(strings: AppStrings): String = when (this) {
        APPEARANCE -> strings.appearanceTitle
        NOTIFICATIONS -> strings.notificationsTitle
        PERSONALIZATION -> strings.personalizationTitle
        SECURITY -> strings.securityTitle
        LANGUAGE -> strings.sectionLanguage
        ABOUT -> strings.appUpdatesTitle
    }

    fun getSubtitle(strings: AppStrings): String = when (this) {
        APPEARANCE -> strings.appearanceSubtitle
        NOTIFICATIONS -> strings.notificationsSubtitle
        PERSONALIZATION -> strings.personalizationSubtitle
        SECURITY -> strings.securitySubtitle
        LANGUAGE -> strings.languageSubtitle
        ABOUT -> strings.appUpdatesSubtitle
    }
}
