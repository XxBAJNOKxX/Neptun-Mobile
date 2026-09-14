package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.R
import com.example.core.locale.AppLocale
import com.example.core.locale.StringProvider
import com.example.core.notification.NotificationHelper
import com.example.core.security.AppPersonalization
import com.example.core.security.EncryptedPreferencesManager
import com.example.core.security.NotificationPreferences
import com.example.core.security.UpdateChannel
import com.example.core.update.AppUpdateManager
import com.example.domain.model.CourseType
import com.example.domain.model.StudentCredentials
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LanguageRepository
import com.example.domain.repository.NeptunRepository
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

data class UpdateCheckState(
    val isChecking: Boolean = false,
    val updateAvailable: Boolean = false,
    val latestVersionName: String? = null,
    val downloadUrl: String? = null,
    val message: String? = null
)

data class SettingsUiState(
    val appLocale: AppLocale = AppLocale.SYSTEM,
    val credentials: StudentCredentials? = null,
    val themeSettings: ThemeSettings = ThemeSettings(),
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val personalization: AppPersonalization = AppPersonalization(),
    val updateChannel: UpdateChannel = UpdateChannel.STABLE,
    val isSyncing: Boolean = false,
    val syncSuccessMessage: String? = null,
    val updateCheckState: UpdateCheckState = UpdateCheckState(),
    val isClearingCache: Boolean = false,
    val cacheClearedMessage: String? = null,
    // Neptun szervernyelv-választó
    val serverLanguage: ServerLanguageUiState = ServerLanguageUiState(),
    /** A bejelentkezéskor használt LCID (0 = ismeretlen). */
    val loginLcid: Int = 0
)

class SettingsViewModel(
    private val prefsManager: EncryptedPreferencesManager,
    private val authRepository: AuthRepository,
    private val neptunRepository: NeptunRepository,
    private val languageRepository: LanguageRepository,
    private val strings: StringProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appLocale = prefsManager.getAppLocale(),
            credentials = prefsManager.loadCredentials(),
            themeSettings = prefsManager.loadThemeSettings(),
            notificationPreferences = prefsManager.loadNotificationPreferences(),
            personalization = prefsManager.loadPersonalization(),
            updateChannel = prefsManager.loadUpdateChannel(),
            loginLcid = prefsManager.getLoginLcid()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsManager.credentialsFlow.collect { creds ->
                _uiState.update { it.copy(credentials = creds) }
            }
        }
        viewModelScope.launch {
            prefsManager.themeSettingsFlow.collect { theme ->
                _uiState.update { it.copy(themeSettings = theme) }
            }
        }
        viewModelScope.launch {
            prefsManager.notificationPreferencesFlow.collect { notifPrefs ->
                _uiState.update { it.copy(notificationPreferences = notifPrefs) }
            }
        }
        viewModelScope.launch {
            prefsManager.personalizationFlow.collect { personalization ->
                _uiState.update { it.copy(personalization = personalization) }
            }
        }
        viewModelScope.launch {
            prefsManager.updateChannelFlow.collect { channel ->
                _uiState.update { it.copy(updateChannel = channel) }
            }
        }
        viewModelScope.launch {
            prefsManager.appLocaleFlow.collect { appLocale ->
                _uiState.update { it.copy(appLocale = appLocale) }
            }
        }
        viewModelScope.launch {
            prefsManager.loginLcidFlow.collect { loginLcid ->
                _uiState.update { it.copy(loginLcid = loginLcid) }
            }
        }
        viewModelScope.launch {
            languageRepository.selectedLcidFlow.collect { lcid ->
                _uiState.update {
                    it.copy(serverLanguage = it.serverLanguage.copy(selectedLcid = lcid))
                }
            }
        }
        loadServerLanguages()
    }

    private fun currentBaseUrl(): String {
        return prefsManager.getBaseUrl().ifEmpty { prefsManager.getSelectedUniversityUrl() }
    }

    /**
     * Az intézmény támogatott nyelveinek betöltése: először a gyorsítótárból
     * (azonnal), majd háttérben a szerverről frissítve.
     */
    fun loadServerLanguages() {
        val baseUrl = currentBaseUrl()
        val cached = languageRepository.getCachedLanguages(baseUrl)
        _uiState.update {
            it.copy(
                serverLanguage = ServerLanguageUiState(
                    languages = cached.languages,
                    selectedLcid = languageRepository.getSelectedLcid(),
                    isLoading = true,
                    isFallback = cached.isFallback
                )
            )
        }
        viewModelScope.launch {
            val fresh = languageRepository.refreshLanguages(baseUrl)
            _uiState.update {
                it.copy(
                    serverLanguage = ServerLanguageUiState(
                        languages = fresh.languages,
                        selectedLcid = languageRepository.getSelectedLcid(),
                        isLoading = false,
                        isFallback = fresh.isFallback
                    )
                )
            }
        }
    }

    fun refreshServerLanguages() {
        loadServerLanguages()
    }

    fun selectServerLanguage(lcid: Int) {
        viewModelScope.launch {
            languageRepository.setSelectedLcid(lcid)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefsManager.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) {
        prefsManager.setDynamicColor(enabled)
    }

    fun setAccentColor(accent: AppAccentColor) {
        prefsManager.setAccentColor(accent)
        if (_uiState.value.themeSettings.useDynamicColor) {
            prefsManager.setDynamicColor(false)
        }
    }

    fun setNotifyClasses(enabled: Boolean) {
        prefsManager.setNotifyClasses(enabled)
    }

    fun setNotifyGrades(enabled: Boolean) {
        prefsManager.setNotifyGrades(enabled)
    }

    fun setNotifyMessages(enabled: Boolean) {
        prefsManager.setNotifyMessages(enabled)
    }

    fun setNotifyFinances(enabled: Boolean) {
        prefsManager.setNotifyFinances(enabled)
    }

    fun setReminderMinutesBefore(minutes: Int) {
        prefsManager.setReminderMinutesBefore(minutes)
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        prefsManager.setQuietHours(enabled)
    }

    fun setQuietHoursWindow(startMinute: Int, endMinute: Int) {
        prefsManager.setQuietHoursWindow(startMinute, endMinute)
    }

    fun setStartScreen(screen: String) {
        prefsManager.setStartScreen(screen)
    }

    fun setShowWeekend(enabled: Boolean) {
        prefsManager.setShowWeekend(enabled)
    }

    /**
     * Oldalak elrejtése/bejelentése. Ha a jelenlegi kezdőképernyőt rejtik el,
     * automatikusan az első látható oldalra váltunk vissza.
     */
    fun setHiddenPages(hidden: Set<String>) {
        val current = _uiState.value.personalization
        var startScreen = current.startScreen
        if (startScreen in hidden) {
            startScreen = com.example.presentation.navigation.NavigationItem.entries
                .firstOrNull { it.name !in hidden }?.name ?: "HOME"
            prefsManager.setStartScreen(startScreen)
        }
        prefsManager.setHiddenPages(hidden)
    }

    fun setTargetCredits(credits: Int) {
        prefsManager.setTargetCredits(credits)
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefsManager.setBiometricLockEnabled(enabled)
    }

    fun setUpdateChannel(channel: UpdateChannel) {
        prefsManager.setUpdateChannel(channel)
    }

    /**
     * Az app felületi nyelvének váltása. A perzisztálás szinkron; az Activity
     * újralétrehozását (ami az új nyelvet érvényesíti) a hívó képernyő végzi.
     */
    fun setAppLocale(locale: AppLocale) {
        if (_uiState.value.appLocale == locale) return
        prefsManager.setAppLocale(locale)
    }

    fun clearCachedData() {
        if (_uiState.value.isClearingCache) return
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingCache = true, cacheClearedMessage = null) }
            try {
                neptunRepository.clearLocalData()
                _uiState.update {
                    it.copy(
                        isClearingCache = false,
                        cacheClearedMessage = strings.getString(R.string.set_cache_ok)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isClearingCache = false, cacheClearedMessage = strings.getString(R.string.set_cache_fail))
                }
            }
            kotlinx.coroutines.delay(4000)
            _uiState.update { it.copy(cacheClearedMessage = null) }
        }
    }

    fun triggerManualSync(onDataReload: (() -> Unit)? = null) {
        if (_uiState.value.isSyncing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncSuccessMessage = null) }
            val creds = prefsManager.loadCredentials()
            val token = prefsManager.getSessionToken()

            try {
                if (creds != null && creds.isLoggedIn) {
                    neptunRepository.syncAllData(creds.neptunCode, token)
                } else {
                    neptunRepository.refreshCalendar()
                    neptunRepository.refreshGrades()
                    neptunRepository.refreshMessages()
                    neptunRepository.refreshFinances()
                }
                onDataReload?.invoke()
                val now = System.currentTimeMillis()
                prefsManager.updateLastSyncTime(now)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncSuccessMessage = strings.getString(R.string.set_sync_ok)
                    )
                }
            } catch (e: Exception) {
                val now = System.currentTimeMillis()
                prefsManager.updateLastSyncTime(now)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncSuccessMessage = strings.getString(R.string.set_sync_done)
                    )
                }
            }

            // Clear the feedback banner after 4 seconds
            delay(4000)
            _uiState.update { it.copy(syncSuccessMessage = null) }
        }
    }

    fun simulateClassNotification(context: Context) {
        NotificationHelper.showClassReminder(
            context = context,
            notificationId = (1000..9999).random(),
            subjectName = strings.getString(R.string.demo_class_subject),
            room = "IB025",
            startTime = "08:15",
            courseType = strings.getString(CourseType.LECTURE.labelRes),
            minutesBefore = _uiState.value.notificationPreferences.reminderMinutesBefore
        )
    }

    fun simulateMessageNotification(context: Context) {
        NotificationHelper.showMessageNotification(
            context = context,
            notificationId = (1000..9999).random(),
            sender = strings.getString(R.string.demo_msg_sender),
            subject = strings.getString(R.string.demo_msg_subject),
            preview = strings.getString(R.string.demo_msg_preview)
        )
    }

    fun simulateGradeNotification(context: Context) {
        NotificationHelper.showGradeNotification(
            context = context,
            notificationId = (1000..9999).random(),
            subjectName = strings.getString(R.string.demo_grade_subject),
            grade = 5,
            gradeText = strings.getString(R.string.grade_5),
            credit = 5
        )
    }

    fun simulateFinanceNotification(context: Context) {
        NotificationHelper.showFinanceNotification(
            context = context,
            notificationId = (1000..9999).random(),
            title = strings.getString(R.string.demo_fin_title),
            amount = demoFinanceAmount(),
            dueDate = demoFinanceDueDate()
        )
    }

    fun checkForUpdates() {
        if (_uiState.value.updateCheckState.isChecking) return
        val currentChannel = _uiState.value.updateChannel
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(updateCheckState = UpdateCheckState(isChecking = true))
            }
            val repo = BuildConfig.GITHUB_REPO
            try {
                val info = AppUpdateManager(strings).checkForUpdates(currentChannel)
                if (info != null) {
                    if (info.isUpdateAvailable) {
                        _uiState.update {
                            it.copy(
                                updateCheckState = UpdateCheckState(
                                    isChecking = false,
                                    updateAvailable = true,
                                    latestVersionName = info.latestVersion,
                                    downloadUrl = info.downloadUrl,
                                    message = strings.getString(R.string.set_update_available, info.tagName, strings.getString(currentChannel.labelRes))
                                )
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                updateCheckState = UpdateCheckState(
                                    isChecking = false,
                                    updateAvailable = false,
                                    latestVersionName = info.latestVersion,
                                    downloadUrl = info.downloadUrl,
                                    message = strings.getString(R.string.set_update_current, BuildConfig.VERSION_NAME)
                                )
                            )
                        }
                    }
                } else {
                    val fallbackUrl = "https://github.com/$repo/releases"
                    _uiState.update {
                        it.copy(
                            updateCheckState = UpdateCheckState(
                                isChecking = false,
                                downloadUrl = fallbackUrl,
                                message = strings.getString(R.string.set_update_open_github)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                val fallbackUrl = "https://github.com/$repo/releases"
                _uiState.update {
                    it.copy(
                        updateCheckState = UpdateCheckState(
                            isChecking = false,
                            downloadUrl = fallbackUrl,
                            message = strings.getString(R.string.set_update_open_github)
                        )
                    )
                }
            }
        }
    }

    /** Bemutató pénzügyi tétel összege az app nyelvének megfelelő számformátumban. */
    private fun demoFinanceAmount(): String =
        java.text.NumberFormat.getInstance(Locale.getDefault()).format(14500)

    /** Bemutató befizetési határidő az app nyelvének megfelelő dátumformátumban. */
    private fun demoFinanceDueDate(): String =
        LocalDate.of(2026, 9, 15).format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        )

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val cleanLatest = latest.removePrefix("v").trim()
        val cleanCurrent = current.removePrefix("v").trim()
        val lParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }
        val cParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(lParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val l = lParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    companion object {
        fun provideFactory(
            prefsManager: EncryptedPreferencesManager,
            authRepository: AuthRepository,
            neptunRepository: NeptunRepository,
            languageRepository: LanguageRepository,
            strings: StringProvider
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(prefsManager, authRepository, neptunRepository, languageRepository, strings) as T
            }
        }
    }
}
