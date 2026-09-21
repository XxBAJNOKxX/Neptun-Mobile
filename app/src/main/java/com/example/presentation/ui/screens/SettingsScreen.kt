package com.example.presentation.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.BuildConfig
import com.example.core.i18n.currentStrings
import com.example.core.security.AppPersonalization
import com.example.core.security.NotificationPreferences
import com.example.core.security.UpdateChannel
import com.example.domain.model.NeptunLanguage
import com.example.domain.model.StudentCredentials
import com.example.presentation.ui.components.NeptunTopBar
import com.example.presentation.ui.screens.settings.AboutSettingsContent
import com.example.presentation.ui.screens.settings.AppearanceSettingsContent
import com.example.presentation.ui.screens.settings.LanguageSettingsContent
import com.example.presentation.ui.screens.settings.NotificationSettingsContent
import com.example.presentation.ui.screens.settings.PersonalizationSettingsContent
import com.example.presentation.ui.screens.settings.PreferenceGroupCard
import com.example.presentation.ui.screens.settings.PreferenceNavigationItem
import com.example.presentation.ui.screens.settings.ProfileHeroCard
import com.example.presentation.ui.screens.settings.SecuritySettingsContent
import com.example.presentation.ui.screens.settings.SettingsCategory
import com.example.presentation.viewmodel.UpdateCheckState
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemeSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    credentials: StudentCredentials?,
    themeSettings: ThemeSettings,
    notificationPreferences: NotificationPreferences,
    personalization: AppPersonalization = AppPersonalization(),
    isSyncing: Boolean,
    syncSuccessMessage: String?,
    updateCheckState: UpdateCheckState = UpdateCheckState(),
    updateChannel: UpdateChannel = UpdateChannel.STABLE,
    isClearingCache: Boolean = false,
    cacheClearedMessage: String? = null,
    currentLanguage: NeptunLanguage = NeptunLanguage.HUNGARIAN,
    supportedLanguages: List<NeptunLanguage> = NeptunLanguage.DEFAULT_LANGUAGES,
    isChangingLanguage: Boolean = false,
    languageMessage: String? = null,
    onLanguageSelect: (NeptunLanguage) -> Unit = {},
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    onAccentColorSelect: (AppAccentColor) -> Unit,
    onNotifyClassesChange: (Boolean) -> Unit,
    onNotifyGradesChange: (Boolean) -> Unit,
    onNotifyMessagesChange: (Boolean) -> Unit,
    onNotifyFinancesChange: (Boolean) -> Unit,
    onQuietHoursEnabledChange: (Boolean) -> Unit = {},
    onQuietHoursWindowChange: (Int, Int) -> Unit = { _, _ -> },
    onStartScreenChange: (String) -> Unit = {},
    onShowWeekendChange: (Boolean) -> Unit = {},
    onHiddenPagesChange: (Set<String>) -> Unit = {},
    onTargetCreditsChange: (Int) -> Unit = {},
    onBiometricLockChange: (Boolean) -> Unit = {},
    onUpdateChannelChange: (UpdateChannel) -> Unit = {},
    onExportIcs: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onSimulateClassNotification: () -> Unit,
    onSimulateMessageNotification: () -> Unit,
    onSimulateGradeNotification: () -> Unit,
    onSimulateFinanceNotification: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    onLogoutClick: () -> Unit,
    onManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }

    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    var isNotificationPermissionGranted by remember { mutableStateOf(checkPermission()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationPermissionGranted = checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val activity = context as? Activity ?: return
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
    }

    val lastSyncFormatted = remember(credentials?.lastSyncTime) {
        if (credentials != null && credentials.lastSyncTime > 0) {
            val sdf = SimpleDateFormat("yyyy. MM. dd. HH:mm:ss", Locale("hu", "HU"))
            sdf.format(Date(credentials.lastSyncTime))
        } else {
            strings.notSyncedYet
        }
    }

    // Handle back button when viewing a category sub-screen
    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        if (selectedCategory == null) {
            NeptunTopBar(
                title = strings.settingsTitle,
                subtitle = credentials?.studentName ?: strings.profileSubtitle,
                isRefreshing = isSyncing,
                onRefresh = onManualSync
            )
        } else {
            TopAppBar(
                title = {
                    Text(
                        text = selectedCategory!!.getTitle(strings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { selectedCategory = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally { width -> width / 3 } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { width -> -width / 3 } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut())
                }
            },
            label = "settings_category_navigation",
            modifier = Modifier.fillMaxSize()
        ) { category ->
            if (category == null) {
                // ==========================================
                // MAIN SETTINGS DIRECTORY (M3 EXPRESSIVE)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Hero Card with User Initials, Neptun Code, and Quick Actions
                    ProfileHeroCard(
                        credentials = credentials,
                        isSyncing = isSyncing,
                        onManualSync = onManualSync,
                        onLogoutClick = { showLogoutDialog = true }
                    )

                    // Categories Navigation Group Card
                    PreferenceGroupCard {
                        PreferenceNavigationItem(
                            title = SettingsCategory.APPEARANCE.getTitle(strings),
                            subtitle = SettingsCategory.APPEARANCE.getSubtitle(strings),
                            icon = SettingsCategory.APPEARANCE.icon,
                            onClick = { selectedCategory = SettingsCategory.APPEARANCE }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        PreferenceNavigationItem(
                            title = SettingsCategory.NOTIFICATIONS.getTitle(strings),
                            subtitle = SettingsCategory.NOTIFICATIONS.getSubtitle(strings),
                            icon = SettingsCategory.NOTIFICATIONS.icon,
                            onClick = { selectedCategory = SettingsCategory.NOTIFICATIONS }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        PreferenceNavigationItem(
                            title = SettingsCategory.PERSONALIZATION.getTitle(strings),
                            subtitle = SettingsCategory.PERSONALIZATION.getSubtitle(strings),
                            icon = SettingsCategory.PERSONALIZATION.icon,
                            onClick = { selectedCategory = SettingsCategory.PERSONALIZATION }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        PreferenceNavigationItem(
                            title = SettingsCategory.SECURITY.getTitle(strings),
                            subtitle = SettingsCategory.SECURITY.getSubtitle(strings),
                            icon = SettingsCategory.SECURITY.icon,
                            onClick = { selectedCategory = SettingsCategory.SECURITY }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        PreferenceNavigationItem(
                            title = SettingsCategory.LANGUAGE.getTitle(strings),
                            subtitle = SettingsCategory.LANGUAGE.getSubtitle(strings),
                            icon = SettingsCategory.LANGUAGE.icon,
                            badgeText = "${currentLanguage.flagEmoji} ${currentLanguage.displayLabel}",
                            onClick = { selectedCategory = SettingsCategory.LANGUAGE }
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        PreferenceNavigationItem(
                            title = SettingsCategory.ABOUT.getTitle(strings),
                            subtitle = SettingsCategory.ABOUT.getSubtitle(strings),
                            icon = SettingsCategory.ABOUT.icon,
                            badgeText = "v${BuildConfig.VERSION_NAME}",
                            onClick = { selectedCategory = SettingsCategory.ABOUT }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                // ==========================================
                // SUB-CATEGORY PAGES
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (category) {
                        SettingsCategory.APPEARANCE -> {
                            AppearanceSettingsContent(
                                themeSettings = themeSettings,
                                onThemeModeChange = onThemeModeChange,
                                onDynamicColorToggle = onDynamicColorToggle,
                                onAccentColorSelect = onAccentColorSelect
                            )
                        }

                        SettingsCategory.NOTIFICATIONS -> {
                            NotificationSettingsContent(
                                notificationPreferences = notificationPreferences,
                                isNotificationPermissionGranted = isNotificationPermissionGranted,
                                onRequestPermission = ::requestNotificationPermission,
                                onNotifyClassesChange = onNotifyClassesChange,
                                onNotifyGradesChange = onNotifyGradesChange,
                                onNotifyMessagesChange = onNotifyMessagesChange,
                                onNotifyFinancesChange = onNotifyFinancesChange,
                                onSimulateClassNotification = onSimulateClassNotification,
                                onSimulateGradeNotification = onSimulateGradeNotification,
                                onSimulateMessageNotification = onSimulateMessageNotification,
                                onSimulateFinanceNotification = onSimulateFinanceNotification
                            )
                        }

                        SettingsCategory.PERSONALIZATION -> {
                            PersonalizationSettingsContent(
                                personalization = personalization,
                                onStartScreenChange = onStartScreenChange,
                                onShowWeekendChange = onShowWeekendChange,
                                onHiddenPagesChange = onHiddenPagesChange,
                                onTargetCreditsChange = onTargetCreditsChange
                            )
                        }

                        SettingsCategory.SECURITY -> {
                            SecuritySettingsContent(
                                personalization = personalization,
                                lastSyncFormatted = lastSyncFormatted,
                                isSyncing = isSyncing,
                                syncSuccessMessage = syncSuccessMessage,
                                isClearingCache = isClearingCache,
                                cacheClearedMessage = cacheClearedMessage,
                                onBiometricLockChange = onBiometricLockChange,
                                onManualSync = onManualSync,
                                onExportIcs = onExportIcs,
                                onClearCache = onClearCache
                            )
                        }

                        SettingsCategory.LANGUAGE -> {
                            LanguageSettingsContent(
                                currentLanguage = currentLanguage,
                                supportedLanguages = supportedLanguages,
                                isChangingLanguage = isChangingLanguage,
                                languageMessage = languageMessage,
                                onLanguageSelect = onLanguageSelect
                            )
                        }

                        SettingsCategory.ABOUT -> {
                            AboutSettingsContent(
                                updateCheckState = updateCheckState,
                                updateChannel = updateChannel,
                                onUpdateChannelChange = onUpdateChannelChange,
                                onCheckForUpdates = onCheckForUpdates
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(strings.logoutDialogTitle) },
            text = { Text(strings.logoutDialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(
                        strings.logoutBtn,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
