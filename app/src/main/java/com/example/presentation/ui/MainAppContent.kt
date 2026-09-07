package com.example.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.NeptunApp
import com.example.presentation.navigation.NavigationItem
import com.example.presentation.ui.components.InAppUpdateDialog
import com.example.presentation.ui.components.NeptunBottomBar
import com.example.presentation.ui.screens.GradesScreen
import com.example.presentation.ui.screens.LoginScreen
import com.example.presentation.ui.screens.MessagesScreen
import com.example.presentation.ui.screens.SettingsScreen
import com.example.presentation.ui.screens.TimetableScreen
import com.example.presentation.viewmodel.AppUpdateViewModel
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.GradesViewModel
import com.example.presentation.viewmodel.MessagesViewModel
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.presentation.viewmodel.TimetableViewModel

@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val app = context.applicationContext as NeptunApp
    val appContainer = app.appContainer

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
            onQuickElteTestFill = authViewModel::quickElteTestFill,
            onTwoFactorCodeChange = authViewModel::onTwoFactorCodeChange,
            onTwoFactorMethodChange = authViewModel::onTwoFactorMethodChange,
            onRequestEmailCode = authViewModel::requestEmailCode,
            onSubmitTwoFactor = authViewModel::submitTwoFactor,
            onCancelTwoFactor = authViewModel::cancelTwoFactor
        )
    } else {
        MainDashboard(
            app = app,
            authViewModel = authViewModel,
            appUpdateViewModel = appUpdateViewModel
        )
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
            calculateAveragesUseCase = appContainer.calculateAveragesUseCase
        )
    )

    val messagesViewModel: MessagesViewModel = viewModel(
        factory = MessagesViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository
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
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    var currentDestination by rememberSaveable { mutableStateOf(NavigationItem.TIMETABLE) }

    Scaffold(
        bottomBar = {
            NeptunBottomBar(
                currentDestination = currentDestination,
                unreadMessageCount = messagesState.unreadCount,
                onNavigate = { currentDestination = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentDestination,
                label = "navigation_crossfade"
            ) { destination ->
                when (destination) {
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
                        onRefresh = gradesViewModel::refreshGrades
                    )

                    NavigationItem.MESSAGES -> MessagesScreen(
                        uiState = messagesState,
                        onToggleUnreadFilter = messagesViewModel::toggleUnreadFilter,
                        onOpenMessage = messagesViewModel::openMessage,
                        onCloseMessage = messagesViewModel::closeMessage,
                        onReloadMessage = messagesViewModel::reloadSelectedMessageContent,
                        onRefresh = messagesViewModel::refreshMessages
                    )

                    NavigationItem.SETTINGS -> SettingsScreen(
                        credentials = authState.credentials,
                        themeSettings = settingsState.themeSettings,
                        notificationPreferences = settingsState.notificationPreferences,
                        isSyncing = settingsState.isSyncing,
                        syncSuccessMessage = settingsState.syncSuccessMessage,
                        updateCheckState = settingsState.updateCheckState,
                        onThemeModeChange = settingsViewModel::setThemeMode,
                        onDynamicColorToggle = settingsViewModel::setDynamicColor,
                        onAccentColorSelect = settingsViewModel::setAccentColor,
                        onNotifyClassesChange = settingsViewModel::setNotifyClasses,
                        onNotifyGradesChange = settingsViewModel::setNotifyGrades,
                        onNotifyMessagesChange = settingsViewModel::setNotifyMessages,
                        onNotifyFinancesChange = settingsViewModel::setNotifyFinances,
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
