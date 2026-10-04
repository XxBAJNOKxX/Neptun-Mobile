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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.presentation.ui.components.TwoFactorDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.NeptunApp
import com.example.core.crash.CrashReporter
import com.example.core.di.AppContainer
import com.example.core.export.IcsExporter
import com.example.core.security.DataMode
import com.example.presentation.navigation.NavigationItem
import com.example.presentation.ui.components.BiometricLockScreen
import com.example.presentation.ui.components.InAppUpdateDialog
import com.example.presentation.ui.components.NeptunBottomBar
import com.example.presentation.ui.components.NeptunNavigationRail
import com.example.presentation.ui.screens.DashboardScreen
import com.example.presentation.ui.screens.FinancesScreen
import com.example.presentation.ui.screens.GradesScreen
import com.example.presentation.ui.screens.LoginScreen
import com.example.presentation.ui.screens.MessagesScreen
import com.example.presentation.ui.screens.SettingsScreen
import com.example.presentation.ui.screens.TimetableScreen
import com.example.presentation.viewmodel.AppUpdateViewModel
import com.example.presentation.viewmodel.AuthUiState
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.DashboardViewModel
import com.example.presentation.viewmodel.FinancesViewModel
import com.example.presentation.viewmodel.GradesViewModel
import com.example.presentation.viewmodel.MessagesViewModel
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.presentation.viewmodel.TimetableViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val strings = com.example.core.i18n.currentStrings()
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
            title = { Text(strings.crashDialogTitle) },
            text = {
                Column {
                    Text(
                        text = strings.crashDialogDesc,
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
                    Text(strings.copy)
                }
            },
            dismissButton = {
                TextButton(onClick = { lastCrashLog = null }) {
                    Text(strings.close)
                }
            }
        )
    }

    val appUpdateViewModel: AppUpdateViewModel = viewModel(
        factory = AppUpdateViewModel.provideFactory(prefsManager = appContainer.prefsManager)
    )
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
            onCancelTwoFactor = authViewModel::cancelTwoFactor,
            onSelectLanguage = authViewModel::selectLanguage,
            onContinueOffline = authViewModel::continueOffline
        )
    } else {
        // Biometrikus zár (ha be van kapcsolva)
        val personalization by appContainer.prefsManager.personalizationFlow.collectAsStateWithLifecycle()
        var lockUnlocked by rememberSaveable { mutableStateOf(false) }

        if (personalization.biometricLockEnabled && !lockUnlocked) {
            BiometricLockScreen(
                onUnlock = { lockUnlocked = true },
                onLogout = { authViewModel.logout() }
            )
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
    val strings = com.example.core.i18n.currentStrings()
    val appContainer = app.appContainer
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val unreadMessageCount by remember(appContainer.neptunRepository) {
        appContainer.neptunRepository.getMessages().map { msgs -> msgs.count { !it.isRead } }
    }.collectAsStateWithLifecycle(initialValue = 0)

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

    var gradesInitialTab by rememberSaveable { mutableIntStateOf(0) }
    var timetableInitialTab by rememberSaveable { mutableIntStateOf(0) }

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

    // Lejárt munkamenet jelzése és gyors megújítás
    if (sessionExpired && !authState.isQuickReAuthOpen && !authState.isTwoFactorRequired && !authState.isPasswordPromptRequired) {
        AlertDialog(
            onDismissRequest = { appContainer.prefsManager.clearSessionExpired() },
            title = { Text(strings.sessionExpiredTitle) },
            text = {
                Text(strings.sessionExpiredDesc)
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.initiateQuickReAuth()
                    }
                ) {
                    Text(strings.quickReAuth)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            appContainer.prefsManager.clearSessionExpired()
                            // Nem töröljük a helyi adatokat szerveroldali munkamenet-lejáratkor!
                            authViewModel.logout(clearLocalData = false)
                        }
                    ) {
                        Text(strings.logout)
                    }
                    TextButton(
                        onClick = { appContainer.prefsManager.clearSessionExpired() }
                    ) {
                        Text(strings.later)
                    }
                }
            }
        )
    }

    // Gyors megújítás folyamatban (pl. jelszavas login kérés a Neptunhoz)
    if (authState.isQuickReAuthOpen && authState.isLoading && !authState.isTwoFactorRequired && !authState.isPasswordPromptRequired) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(strings.quickReAuth) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Text(strings.loggingIn)
                }
            },
            confirmButton = {}
        )
    }

    // Jelszó megadása szükséges gyors megújításhoz (ha a mentett jelszó nincs meg vagy hibás volt)
    if (authState.isPasswordPromptRequired) {
        var pwdInput by rememberSaveable { mutableStateOf("") }
        var pwdVisible by rememberSaveable { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                authViewModel.cancelTwoFactor()
            },
            title = { Text(strings.quickReAuth) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.enterPasswordForReAuth)
                    OutlinedTextField(
                        value = pwdInput,
                        onValueChange = { pwdInput = it },
                        label = { Text(strings.password) },
                        singleLine = true,
                        visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { pwdVisible = !pwdVisible }) {
                                Icon(
                                    imageVector = if (pwdVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (authState.errorMessage != null) {
                        Text(
                            text = authState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { authViewModel.submitReAuthPassword(pwdInput) },
                    enabled = pwdInput.isNotBlank() && !authState.isLoading
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(strings.loginButton)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { authViewModel.cancelTwoFactor() }
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // 2FA dialógus felugrása a Dashboard felett
    if (authState.isTwoFactorRequired) {
        TwoFactorDialog(
            uiState = authState,
            strings = strings,
            onTwoFactorCodeChange = { authViewModel.onTwoFactorCodeChange(it) },
            onTwoFactorMethodChange = { authViewModel.onTwoFactorMethodChange(it) },
            onRequestEmailCode = { authViewModel.requestEmailCode() },
            onSubmitTwoFactor = { authViewModel.submitTwoFactor() },
            onCancelTwoFactor = { authViewModel.cancelTwoFactor() }
        )
    }

    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    if (isExpanded) {
        Row(modifier = Modifier.fillMaxSize()) {
            NeptunNavigationRail(
                currentDestination = currentDestination,
                items = visibleItems,
                unreadMessageCount = unreadMessageCount,
                onNavigate = { item ->
                    if (item in visibleItems) {
                        currentDestination = item
                    }
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .navigationBarsPadding()
            ) {
                DemoModeBanner(dataMode = dataMode, strings = strings)
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DashboardNavContent(
                        currentDestination = currentDestination,
                        visibleItems = visibleItems,
                        app = app,
                        appContainer = appContainer,
                        authState = authState,
                        dataMode = dataMode,
                        strings = strings,
                        authViewModel = authViewModel,
                        appUpdateViewModel = appUpdateViewModel,
                        gradesInitialTab = gradesInitialTab,
                        timetableInitialTab = timetableInitialTab,
                        onNavigate = { item ->
                            if (item in visibleItems) {
                                currentDestination = item
                            }
                        },
                        onOpenExams = {
                            gradesInitialTab = 1
                            if (NavigationItem.GRADES in visibleItems) {
                                currentDestination = NavigationItem.GRADES
                            }
                        },
                        onOpenProgress = {
                            gradesInitialTab = 2
                            if (NavigationItem.GRADES in visibleItems) {
                                currentDestination = NavigationItem.GRADES
                            }
                        },
                        onOpenPeriods = {
                            timetableInitialTab = 1
                            if (NavigationItem.TIMETABLE in visibleItems) {
                                currentDestination = NavigationItem.TIMETABLE
                            }
                        },
                        onResetTimetableInitialTab = { timetableInitialTab = 0 },
                        onResetGradesInitialTab = { gradesInitialTab = 0 }
                    )
                }
            }
        }
    } else {
        androidx.compose.material3.Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column {
                    DemoModeBanner(dataMode = dataMode, strings = strings)
                    NeptunBottomBar(
                        currentDestination = currentDestination,
                        items = visibleItems,
                        unreadMessageCount = unreadMessageCount,
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
                DashboardNavContent(
                    currentDestination = currentDestination,
                    visibleItems = visibleItems,
                    app = app,
                    appContainer = appContainer,
                    authState = authState,
                    dataMode = dataMode,
                    strings = strings,
                    authViewModel = authViewModel,
                    appUpdateViewModel = appUpdateViewModel,
                    gradesInitialTab = gradesInitialTab,
                    timetableInitialTab = timetableInitialTab,
                    onNavigate = { item ->
                        if (item in visibleItems) {
                            currentDestination = item
                        }
                    },
                    onOpenExams = {
                        gradesInitialTab = 1
                        if (NavigationItem.GRADES in visibleItems) {
                            currentDestination = NavigationItem.GRADES
                        }
                    },
                    onOpenProgress = {
                        gradesInitialTab = 2
                        if (NavigationItem.GRADES in visibleItems) {
                            currentDestination = NavigationItem.GRADES
                        }
                    },
                    onOpenPeriods = {
                        timetableInitialTab = 1
                        if (NavigationItem.TIMETABLE in visibleItems) {
                            currentDestination = NavigationItem.TIMETABLE
                        }
                    },
                    onResetTimetableInitialTab = { timetableInitialTab = 0 },
                    onResetGradesInitialTab = { gradesInitialTab = 0 }
                )
            }
        }
    }
}

@Composable
private fun DemoModeBanner(
    dataMode: DataMode,
    strings: com.example.core.i18n.AppStrings,
    modifier: Modifier = Modifier
) {
    if (dataMode == DataMode.REAL) return
    if (!com.example.core.debug.DebugFeatures.isDemoAllowed && dataMode == DataMode.DEMO) return
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        modifier = modifier
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
                    strings.demoModeBanner
                } else {
                    strings.fallbackSampleDataBanner
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun DashboardNavContent(
    currentDestination: NavigationItem,
    visibleItems: List<NavigationItem>,
    app: NeptunApp,
    appContainer: AppContainer,
    authState: AuthUiState,
    dataMode: DataMode,
    strings: com.example.core.i18n.AppStrings,
    authViewModel: AuthViewModel,
    appUpdateViewModel: AppUpdateViewModel,
    gradesInitialTab: Int,
    timetableInitialTab: Int,
    onNavigate: (NavigationItem) -> Unit,
    onOpenExams: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenPeriods: () -> Unit,
    onResetTimetableInitialTab: () -> Unit,
    onResetGradesInitialTab: () -> Unit
) {
    Crossfade(
        targetState = currentDestination,
        label = "navigation_crossfade"
    ) { destination ->
        when (destination) {
            NavigationItem.HOME -> HomeTabRoute(
                appContainer = appContainer,
                authState = authState,
                dataMode = dataMode,
                strings = strings,
                onNavigate = onNavigate,
                onOpenExams = onOpenExams,
                onOpenProgress = onOpenProgress,
                onOpenPeriods = onOpenPeriods
            )

            NavigationItem.TIMETABLE -> TimetableTabRoute(
                appContainer = appContainer,
                initialTab = timetableInitialTab,
                onResetInitialTab = onResetTimetableInitialTab
            )

            NavigationItem.GRADES -> GradesTabRoute(
                appContainer = appContainer,
                initialTab = gradesInitialTab,
                onResetInitialTab = onResetGradesInitialTab
            )

            NavigationItem.MESSAGES -> MessagesTabRoute(
                appContainer = appContainer
            )

            NavigationItem.FINANCES -> FinancesTabRoute(
                appContainer = appContainer
            )

            NavigationItem.SETTINGS -> SettingsTabRoute(
                app = app,
                authViewModel = authViewModel,
                appUpdateViewModel = appUpdateViewModel,
                authState = authState
            )
        }
    }
}

@Composable
private fun HomeTabRoute(
    appContainer: AppContainer,
    authState: AuthUiState,
    dataMode: DataMode,
    strings: com.example.core.i18n.AppStrings,
    onNavigate: (NavigationItem) -> Unit,
    onOpenExams: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenPeriods: () -> Unit
) {
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository,
            calculateAveragesUseCase = appContainer.calculateAveragesUseCase
        )
    )
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = dashboardState,
        studentName = authState.credentials?.studentName ?: strings.studentDefaultName,
        universityName = authState.credentials?.universityName ?: "",
        trainingProgram = authState.credentials?.trainingProgram ?: "",
        isDemoData = authState.credentials?.neptunCode.equals("DEMO01", ignoreCase = true) || (authState.credentials == null && dataMode != DataMode.REAL),
        onNavigate = onNavigate,
        onOpenExams = onOpenExams,
        onOpenProgress = onOpenProgress,
        onOpenPeriods = onOpenPeriods,
        onRefresh = dashboardViewModel::refresh
    )
}

@Composable
private fun TimetableTabRoute(
    appContainer: AppContainer,
    initialTab: Int,
    onResetInitialTab: () -> Unit
) {
    val timetableViewModel: TimetableViewModel = viewModel(
        factory = TimetableViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository,
            alarmScheduler = appContainer.alarmScheduler,
            prefsManager = appContainer.prefsManager
        )
    )
    LaunchedEffect(initialTab) {
        if (initialTab != 0) {
            timetableViewModel.selectTab(initialTab)
            onResetInitialTab()
        }
    }
    val timetableState by timetableViewModel.uiState.collectAsStateWithLifecycle()

    TimetableScreen(
        uiState = timetableState,
        onDaySelect = timetableViewModel::selectDay,
        onPreviousWeek = timetableViewModel::previousWeek,
        onNextWeek = timetableViewModel::nextWeek,
        onCurrentWeek = timetableViewModel::currentWeek,
        onToggleWeekView = timetableViewModel::toggleWeekView,
        onRefresh = timetableViewModel::refreshCalendar,
        onScheduleReminder = timetableViewModel::scheduleClassReminder,
        onTabSelect = timetableViewModel::selectTab,
        onPeriodFilterChange = timetableViewModel::setPeriodFilter,
        onRefreshPeriods = timetableViewModel::refreshAcademicPeriods
    )
}

@Composable
private fun GradesTabRoute(
    appContainer: AppContainer,
    initialTab: Int,
    onResetInitialTab: () -> Unit
) {
    val gradesViewModel: GradesViewModel = viewModel(
        factory = GradesViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository,
            calculateAveragesUseCase = appContainer.calculateAveragesUseCase,
            prefsManager = appContainer.prefsManager
        )
    )
    LaunchedEffect(initialTab) {
        if (initialTab != 0) {
            gradesViewModel.selectTab(initialTab)
            onResetInitialTab()
        }
    }
    val gradesState by gradesViewModel.uiState.collectAsStateWithLifecycle()

    GradesScreen(
        uiState = gradesState,
        onSelectTerm = gradesViewModel::selectTerm,
        onOpenGhostDialog = gradesViewModel::openGhostMarkDialog,
        onCloseGhostDialog = gradesViewModel::closeGhostMarkDialog,
        onSetGhostGrade = gradesViewModel::setGhostGrade,
        onResetAllGhostGrades = gradesViewModel::resetAllGhostGrades,
        onTabSelect = gradesViewModel::selectTab,
        onExamFilterChange = gradesViewModel::setExamFilter,
        onRefresh = gradesViewModel::refreshGrades,
        onRefreshExams = gradesViewModel::refreshExams,
        onRefreshProgress = gradesViewModel::refreshDegreeProgress
    )
}

@Composable
private fun MessagesTabRoute(
    appContainer: AppContainer
) {
    val messagesViewModel: MessagesViewModel = viewModel(
        factory = MessagesViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository
        )
    )
    val messagesState by messagesViewModel.uiState.collectAsStateWithLifecycle()

    MessagesScreen(
        uiState = messagesState,
        onToggleUnreadFilter = messagesViewModel::toggleUnreadFilter,
        onSearchQueryChange = messagesViewModel::onSearchQueryChange,
        onOpenMessage = messagesViewModel::openMessage,
        onCloseMessage = messagesViewModel::closeMessage,
        onReloadMessage = messagesViewModel::reloadSelectedMessageContent,
        onRefresh = messagesViewModel::refreshMessages
    )
}

@Composable
private fun FinancesTabRoute(
    appContainer: AppContainer
) {
    val financesViewModel: FinancesViewModel = viewModel(
        factory = FinancesViewModel.provideFactory(
            neptunRepository = appContainer.neptunRepository
        )
    )
    val financesState by financesViewModel.uiState.collectAsStateWithLifecycle()

    FinancesScreen(
        uiState = financesState,
        onFilterSelect = financesViewModel::setFilter,
        onRefresh = financesViewModel::refreshFinances
    )
}

@Composable
private fun SettingsTabRoute(
    app: NeptunApp,
    authViewModel: AuthViewModel,
    appUpdateViewModel: AppUpdateViewModel,
    authState: AuthUiState
) {
    val context = LocalContext.current
    val appContainer = app.appContainer
    val coroutineScope = rememberCoroutineScope()

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            prefsManager = appContainer.prefsManager,
            authRepository = appContainer.authRepository,
            neptunRepository = appContainer.neptunRepository
        )
    )
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        credentials = authState.credentials,
        themeSettings = settingsState.themeSettings,
        notificationPreferences = settingsState.notificationPreferences,
        personalization = settingsState.personalization,
        isSyncing = settingsState.isSyncing,
        syncSuccessMessage = settingsState.syncSuccessMessage,
        updateCheckState = settingsState.updateCheckState,
        updateChannel = settingsState.updateChannel,
        isClearingCache = settingsState.isClearingCache,
        cacheClearedMessage = settingsState.cacheClearedMessage,
        currentLanguage = settingsState.currentLanguage,
        supportedLanguages = settingsState.supportedLanguages,
        isChangingLanguage = settingsState.isChangingLanguage,
        languageMessage = settingsState.languageMessage,
        onLanguageSelect = settingsViewModel::selectLanguage,
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
        onUpdateChannelChange = settingsViewModel::setUpdateChannel,
        onExportIcs = {
            coroutineScope.launch {
                exportTimetableAsIcs(app)
            }
        },
        onClearCache = {
            settingsViewModel.clearCachedData()
            com.example.core.update.AppUpdateManager.clearUpdateCache(context)
        },
        onSimulateClassNotification = { settingsViewModel.simulateClassNotification(context) },
        onSimulateMessageNotification = { settingsViewModel.simulateMessageNotification(context) },
        onSimulateGradeNotification = { settingsViewModel.simulateGradeNotification(context) },
        onSimulateFinanceNotification = { settingsViewModel.simulateFinanceNotification(context) },
        onCheckForUpdates = {
            settingsViewModel.checkForUpdates()
            appUpdateViewModel.checkForUpdatesOnLaunch()
        },
        onLogoutClick = { authViewModel.logout(clearLocalData = true) },
        onManualSync = settingsViewModel::triggerManualSync
    )
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
        val strings = com.example.core.i18n.AppStringsProvider.getForContext(app)
        app.startActivity(Intent.createChooser(shareIntent, strings.shareTimetableChooser).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
