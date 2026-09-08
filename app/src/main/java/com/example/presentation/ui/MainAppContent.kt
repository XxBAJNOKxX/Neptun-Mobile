package com.example.presentation.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.NeptunApp
import com.example.core.crash.CrashReporter
import com.example.core.export.IcsExporter
import com.example.core.security.DataMode
import com.example.presentation.navigation.NavigationItem
import com.example.presentation.ui.components.BiometricLockScreen
import com.example.presentation.ui.components.InAppUpdateDialog
import com.example.presentation.ui.components.NeptunBottomBar
import com.example.presentation.ui.screens.DashboardScreen
import com.example.presentation.ui.screens.FinancesScreen
import com.example.presentation.ui.screens.GradesScreen
import com.example.presentation.ui.screens.LoginScreen
import com.example.presentation.ui.screens.MessagesScreen
import com.example.presentation.ui.screens.SettingsScreen
import com.example.presentation.ui.screens.TimetableScreen
import com.example.presentation.viewmodel.AppUpdateViewModel
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.DashboardViewModel
import com.example.presentation.viewmodel.FinancesViewModel
import com.example.presentation.viewmodel.GradesViewModel
import com.example.presentation.viewmodel.MessagesViewModel
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.presentation.viewmodel.TimetableViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val app = context.applicationContext as NeptunApp
    val appContainer = app.appContainer

    // Előző futás crash naplójának megjelenítése (ha volt)
    var lastCrashLog by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        lastCrashLog = CrashReporter.consumeLastCrash(context)
    }
    lastCrashLog?.let { crashLog ->
        AlertDialog(
            onDismissRequest = { lastCrashLog = null },
            title = { Text("Az alkalmazás váratlanul leállt") },
            text = {
                Column {
                    Text(
                        text = "Az előző futás hibanaplója (a hibajelentéshez másolható):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = crashLog.take(3000),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        maxLines = 12,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText("Neptun crash log", crashLog)
                    )
                    lastCrashLog = null
                }) {
                    Text("Másolás")
                }
            },
            dismissButton = {
                TextButton(onClick = { lastCrashLog = null }) {
                    Text("Bezárás")
                }
            }
        )
    }

    val appUpdateViewModel: AppUpdateViewModel = viewModel(factory = AppUpdateViewModel.Factory)
    val updateState by appUpdateViewModel.updateState.collectAsStateWithLifecycle()

    // Auto-check for update on app open
    LaunchedEffect(Unit) {
        appUpdateViewModel.checkForUpdatesOnLaunch()
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(
            authRepository = appContainer.authRepository,
            neptunRepository = appContainer.neptunRepository
        )
    )

    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    InAppUpdateDialog(
        updateState = updateState,
        onStartUpdate = { info -> appUpdateViewModel.startInAppUpdate(context, info) },
        onInstallApk = { appUpdateViewModel.installDownloadedApk(context) },
        onDismiss = appUpdateViewModel::dismissUpdate
    )

    if (authState.credentials == null || !authState.credentials!!.isLoggedIn) {
        LoginScreen(
            uiState = authState,
            onSearchQueryChange = authViewModel::onSearchQueryChange,
            onSelectUniversity = authViewModel::onSelectUniversity,
            onNeptunCodeChange = authViewModel::onNeptunCodeChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onLoginClick = authViewModel::login,
            onQuickDemoFill = {
                val bme = authState.universities.firstOrNull { it.id == "bme" }
                    ?: authState.universities.firstOrNull()
                if (bme != null) {
                    authViewModel.onSelectUniversity(bme)
                }
                authViewModel.quickDemoFill()
            },
            onTwoFactorCodeChange = authViewModel::onTwoFactorCodeChange,
            onTwoFactorMethodChange = authViewModel::onTwoFactorMethodChange,
            onRequestEmailCode = authViewModel::requestEmailCode,
            onSubmitTwoFactor = authViewModel::submitTwoFactor,
            onCancelTwoFactor = authViewModel::cancelTwoFactor
        )
    } else {
        // Biometrikus zár (ha be van kapcsolva)
        val personalization by appContainer.prefsManager.personalizationFlow.collectAsStateWithLifecycle()
        var lockUnlocked by rememberSaveable { mutableStateOf(false) }

        if (personalization.biometricLockEnabled && !lockUnlocked) {
            BiometricLockScreen(onUnlock = { lockUnlocked = true })
        } else {
            MainDashboard(
                app = app,
                authViewModel = authViewModel,
                appUpdateViewModel = appUpdateViewModel
            )
        }
    }
}

@Composable
private fun MainDashboard(
    app: NeptunApp,
    authViewModel: AuthViewModel,
    appUpdateViewModel: AppUpdateViewModel
) {
    val context = LocalContext.current
    val appContainer = app.appContainer
    val coroutineScope = rememberCoroutineScope()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val timetableViewModel: TimetableViewModel = viewModel(
        factory = TimetableViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository,
            alarmScheduler = appContainer.alarmScheduler,
            prefsManager = appContainer.prefsManager
        )
    )

    val gradesViewModel: GradesViewModel = viewModel(
        factory = GradesViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository,
            calculateAveragesUseCase = appContainer.calculateAveragesUseCase,
            prefsManager = appContainer.prefsManager
        )
    )

    val messagesViewModel: MessagesViewModel = viewModel(
        factory = MessagesViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository
        )
    )

    val financesViewModel: FinancesViewModel = viewModel(
        factory = FinancesViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository
        )
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository,
            calculateAveragesUseCase = appContainer.calculateAveragesUseCase
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            prefsManager = appContainer.prefsManager,
            authRepository = appContainer.authRepository,
            neptunRepository = appContainer.neptunRepository
        )
    )

    val timetableState by timetableViewModel.uiState.collectAsStateWithLifecycle()
    val gradesState by gradesViewModel.uiState.collectAsStateWithLifecycle()
    val messagesState by messagesViewModel.uiState.collectAsStateWithLifecycle()
    val financesState by financesViewModel.uiState.collectAsStateWithLifecycle()
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val dataMode by appContainer.prefsManager.dataModeFlow.collectAsStateWithLifecycle()
    val sessionExpired by appContainer.prefsManager.sessionExpiredFlow.collectAsStateWithLifecycle()
    val personalization by appContainer.prefsManager.personalizationFlow.collectAsStateWithLifecycle()

    // Elrejtett oldalak kiszűrése (a Profil mindig látható)
    val visibleItems = remember(personalization.hiddenPages) {
        NavigationItem.entries.filter { it == NavigationItem.SETTINGS || it.name !in personalization.hiddenPages }
    }

    val startDestination = remember(personalization.startScreen, personalization.hiddenPages) {
        val preferred = NavigationItem.fromName(personalization.startScreen)
        if (preferred in visibleItems) preferred else visibleItems.firstOrNull() ?: NavigationItem.SETTINGS
    }

    var currentDestination by rememberSaveable {
        mutableStateOf(startDestination)
    }

    // Ha a jelenlegi oldalt épp elrejtették, váltsunk a kezdőképernyőre
    LaunchedEffect(visibleItems) {
        if (currentDestination !in visibleItems) {
            currentDestination = startDestination
        }
    }

    // Vissza gomb: bármelyik fülről a kezdőképernyőre ugrik
    BackHandler(enabled = currentDestination != startDestination) {
        currentDestination = startDestination
    }

    // Lejárt munkamenet jelzése
    if (sessionExpired) {
        AlertDialog(
            onDismissRequest = { appContainer.prefsManager.clearSessionExpired() },
            title = { Text("Lejárt a munkamenet") },
            text = {
                Text(
                    "A Neptun szerver visszautasította a munkamenetet, és nem sikerült automatikusan megújítani. " +
                        "Kérlek, jelentkezz be újra a friss adatokért."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        appContainer.prefsManager.clearSessionExpired()
                        authViewModel.logout()
                    }
                ) {
                    Text("Bejelentkezés")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { appContainer.prefsManager.clearSessionExpired() }
                ) {
                    Text("Később")
                }
            }
        )
    }

    androidx.compose.material3.Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                if (dataMode != DataMode.REAL) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dataMode == DataMode.DEMO) {
                                    "Demo mód – a megjelenített adatok nem valódiak"
                                } else {
                                    "Mintaadatok láthatók (szinkronizálás nem sikerült)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                NeptunBottomBar(
                    currentDestination = currentDestination,
                    items = visibleItems,
                    unreadMessageCount = messagesState.unreadCount,
                    onNavigate = { item ->
                        if (item in visibleItems) {
                            currentDestination = item
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = currentDestination,
                    label = "navigation_crossfade"
                ) { destination ->
                    when (destination) {
                        NavigationItem.HOME -> DashboardScreen(
                            uiState = dashboardState,
                            studentName = authState.credentials?.studentName ?: "Hallgató",
                            isDemoData = dataMode != DataMode.REAL,
                            onNavigate = { item ->
                                if (item in visibleItems) {
                                    currentDestination = item
                                }
                            },
                            onRefresh = dashboardViewModel::refresh
                        )

                        NavigationItem.TIMETABLE -> TimetableScreen(
                            uiState = timetableState,
                            onDaySelect = timetableViewModel::selectDay,
                            onPreviousWeek = timetableViewModel::previousWeek,
                            onNextWeek = timetableViewModel::nextWeek,
                            onCurrentWeek = timetableViewModel::currentWeek,
                            onToggleWeekView = timetableViewModel::toggleWeekView,
                            onRefresh = timetableViewModel::refreshCalendar,
                            onScheduleReminder = timetableViewModel::scheduleClassReminder
                        )

                        NavigationItem.GRADES -> GradesScreen(
                            uiState = gradesState,
                            onSelectTerm = gradesViewModel::selectTerm,
                            onOpenGhostDialog = gradesViewModel::openGhostMarkDialog,
                            onCloseGhostDialog = gradesViewModel::closeGhostMarkDialog,
                            onSetGhostGrade = gradesViewModel::setGhostGrade,
                            onResetAllGhostGrades = gradesViewModel::resetAllGhostGrades,
                            onTabSelect = gradesViewModel::selectTab,
                            onRefresh = gradesViewModel::refreshGrades,
                            onRefreshExams = gradesViewModel::refreshExams
                        )

                        NavigationItem.MESSAGES -> MessagesScreen(
                            uiState = messagesState,
                            onToggleUnreadFilter = messagesViewModel::toggleUnreadFilter,
                            onSearchQueryChange = messagesViewModel::onSearchQueryChange,
                            onOpenMessage = messagesViewModel::openMessage,
                            onCloseMessage = messagesViewModel::closeMessage,
                            onReloadMessage = messagesViewModel::reloadSelectedMessageContent,
                            onRefresh = messagesViewModel::refreshMessages
                        )

                        NavigationItem.FINANCES -> FinancesScreen(
                            uiState = financesState,
                            onFilterSelect = financesViewModel::setFilter,
                            onRefresh = financesViewModel::refreshFinances
                        )

                        NavigationItem.SETTINGS -> SettingsScreen(
                            credentials = authState.credentials,
                            themeSettings = settingsState.themeSettings,
                            notificationPreferences = settingsState.notificationPreferences,
                            personalization = settingsState.personalization,
                            isSyncing = settingsState.isSyncing,
                            syncSuccessMessage = settingsState.syncSuccessMessage,
                            updateCheckState = settingsState.updateCheckState,
                            isClearingCache = settingsState.isClearingCache,
                            cacheClearedMessage = settingsState.cacheClearedMessage,
                            onThemeModeChange = settingsViewModel::setThemeMode,
                            onDynamicColorToggle = settingsViewModel::setDynamicColor,
                            onAccentColorSelect = settingsViewModel::setAccentColor,
                            onNotifyClassesChange = settingsViewModel::setNotifyClasses,
                            onNotifyGradesChange = settingsViewModel::setNotifyGrades,
                            onNotifyMessagesChange = settingsViewModel::setNotifyMessages,
                            onNotifyFinancesChange = settingsViewModel::setNotifyFinances,
                            onQuietHoursEnabledChange = settingsViewModel::setQuietHoursEnabled,
                            onQuietHoursWindowChange = settingsViewModel::setQuietHoursWindow,
                            onStartScreenChange = settingsViewModel::setStartScreen,
                            onShowWeekendChange = settingsViewModel::setShowWeekend,
                            onHiddenPagesChange = settingsViewModel::setHiddenPages,
                            onTargetCreditsChange = settingsViewModel::setTargetCredits,
                            onBiometricLockChange = settingsViewModel::setBiometricLockEnabled,
                            onExportIcs = {
                                coroutineScope.launch {
                                    exportTimetableAsIcs(app)
                                }
                            },
                            onClearCache = settingsViewModel::clearCachedData,
                            onSimulateClassNotification = { settingsViewModel.simulateClassNotification(context) },
                            onSimulateMessageNotification = { settingsViewModel.simulateMessageNotification(context) },
                            onSimulateGradeNotification = { settingsViewModel.simulateGradeNotification(context) },
                            onSimulateFinanceNotification = { settingsViewModel.simulateFinanceNotification(context) },
                            onCheckForUpdates = {
                                settingsViewModel.checkForUpdates()
                                appUpdateViewModel.checkForUpdatesOnLaunch()
                            },
                            onLogoutClick = authViewModel::logout,
                            onManualSync = {
                                settingsViewModel.triggerManualSync()
                            }
                        )
                    }
                }
            }
        }
    }

/** Az órarend exportálása .ics fájlba és megosztási szándék indítása. */
private suspend fun exportTimetableAsIcs(app: NeptunApp) {
    try {
        val events = app.appContainer.neptunRepository.getCalendarEvents().first()
        if (events.isEmpty()) return

        val icsContent = IcsExporter.buildIcs(events)
        val dir = File(app.cacheDir, "export").apply { mkdirs() }
        val file = File(dir, "neptun-orarend.ics")
        file.writeText(icsContent, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            app,
            "${app.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        app.startActivity(Intent.createChooser(shareIntent, "Órarend megosztása").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
