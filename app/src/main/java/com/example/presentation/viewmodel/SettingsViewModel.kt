package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.core.notification.NotificationHelper
import com.example.core.security.AppPersonalization
import com.example.core.security.EncryptedPreferencesManager
import com.example.core.security.NotificationPreferences
import com.example.domain.model.StudentCredentials
import com.example.domain.repository.AuthRepository
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
import java.util.concurrent.TimeUnit

data class UpdateCheckState(
    val isChecking: Boolean = false,
    val updateAvailable: Boolean = false,
    val latestVersionName: String? = null,
    val downloadUrl: String? = null,
    val message: String? = null
)

data class SettingsUiState(
    val credentials: StudentCredentials? = null,
    val themeSettings: ThemeSettings = ThemeSettings(),
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val personalization: AppPersonalization = AppPersonalization(),
    val isSyncing: Boolean = false,
    val syncSuccessMessage: String? = null,
    val updateCheckState: UpdateCheckState = UpdateCheckState(),
    val isClearingCache: Boolean = false,
    val cacheClearedMessage: String? = null
)

class SettingsViewModel(
    private val prefsManager: EncryptedPreferencesManager,
    private val authRepository: AuthRepository,
    private val neptunRepository: NeptunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            credentials = prefsManager.loadCredentials(),
            themeSettings = prefsManager.loadThemeSettings(),
            notificationPreferences = prefsManager.loadNotificationPreferences(),
            personalization = prefsManager.loadPersonalization()
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

    fun setWeekFilterMode(mode: String) {
        prefsManager.setWeekFilterMode(mode)
    }

    fun setHourRange(first: Int, last: Int) {
        prefsManager.setHourRange(first, last)
    }

    fun setTargetCredits(credits: Int) {
        prefsManager.setTargetCredits(credits)
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefsManager.setBiometricLockEnabled(enabled)
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
                        cacheClearedMessage = "A helyi gyorsítótár törölve. A következő szinkronizáláskor friss adatok töltődnek le."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isClearingCache = false, cacheClearedMessage = "A törlés nem sikerült.")
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
                        syncSuccessMessage = "Sikeres szinkronizálás! Minden adat naprakész."
                    )
                }
            } catch (e: Exception) {
                val now = System.currentTimeMillis()
                prefsManager.updateLastSyncTime(now)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncSuccessMessage = "Szinkronizálás befejeződött."
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
            subjectName = "Mesterséges intelligencia",
            room = "IB025",
            startTime = "08:15",
            courseType = "Előadás",
            minutesBefore = _uiState.value.notificationPreferences.reminderMinutesBefore
        )
    }

    fun simulateMessageNotification(context: Context) {
        NotificationHelper.showMessageNotification(
            context = context,
            notificationId = (1000..9999).random(),
            sender = "Dr. Kovács István (Oktató)",
            subject = "Vizsgakurzus tájékoztató és konzultáció",
            preview = "Kedves Hallgatók! A jövő heti konzultáció időpontja módosult..."
        )
    }

    fun simulateGradeNotification(context: Context) {
        NotificationHelper.showGradeNotification(
            context = context,
            notificationId = (1000..9999).random(),
            subjectName = "Algoritmuselmélet",
            grade = 5,
            gradeText = "Jeles (5)",
            credit = 5
        )
    }

    fun simulateFinanceNotification(context: Context) {
        NotificationHelper.showFinanceNotification(
            context = context,
            notificationId = (1000..9999).random(),
            title = "Kollégiumi térítési díj (2026/27/1)",
            amount = "14 500",
            dueDate = "2026. 09. 15"
        )
    }

    fun checkForUpdates() {
        if (_uiState.value.updateCheckState.isChecking) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(updateCheckState = UpdateCheckState(isChecking = true))
            }
            val repo = BuildConfig.GITHUB_REPO
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(6, TimeUnit.SECONDS)
                    .readTimeout(6, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("https://api.github.com/repos/$repo/releases/latest")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "NeptunMobileApp")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: ""
                        val json = JSONObject(bodyString)
                        val tagName = json.optString("tag_name", "")
                        val htmlUrl = json.optString("html_url", "https://github.com/$repo/releases")
                        var apkUrl: String? = null
                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk")) {
                                    apkUrl = asset.optString("browser_download_url")
                                    break
                                }
                            }
                        }
                        val finalDownloadUrl = apkUrl ?: htmlUrl
                        val currentVersion = BuildConfig.VERSION_NAME
                        val isNewer = isNewerVersion(tagName, currentVersion)

                        if (isNewer) {
                            _uiState.update {
                                it.copy(
                                    updateCheckState = UpdateCheckState(
                                        isChecking = false,
                                        updateAvailable = true,
                                        latestVersionName = tagName,
                                        downloadUrl = finalDownloadUrl,
                                        message = "Új verzió elérhető: $tagName!"
                                    )
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    updateCheckState = UpdateCheckState(
                                        isChecking = false,
                                        updateAvailable = false,
                                        latestVersionName = tagName,
                                        downloadUrl = htmlUrl,
                                        message = "A legfrissebb verziót használod (v$currentVersion)."
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
                                    message = "Nyisd meg a GitHub Releases oldalt a letöltéshez."
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val fallbackUrl = "https://github.com/$repo/releases"
                _uiState.update {
                    it.copy(
                        updateCheckState = UpdateCheckState(
                            isChecking = false,
                            downloadUrl = fallbackUrl,
                            message = "Nyisd meg a GitHub Releases oldalt a letöltéshez."
                        )
                    )
                }
            }
        }
    }

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
            neptunRepository: NeptunRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(prefsManager, authRepository, neptunRepository) as T
            }
        }
    }
}
