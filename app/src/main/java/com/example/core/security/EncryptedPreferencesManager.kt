package com.example.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.domain.model.StudentCredentials
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EncryptedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "neptun_secure_storage",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to standard private preferences if keystore issue occurs on emulator
        context.getSharedPreferences("neptun_secure_storage_fallback", Context.MODE_PRIVATE)
    }

    private val _credentialsFlow = MutableStateFlow(loadCredentials())
    val credentialsFlow: StateFlow<StudentCredentials?> = _credentialsFlow.asStateFlow()

    private val _themeSettingsFlow = MutableStateFlow(loadThemeSettings())
    val themeSettingsFlow: StateFlow<ThemeSettings> = _themeSettingsFlow.asStateFlow()

    private val _notificationPreferencesFlow = MutableStateFlow(loadNotificationPreferences())
    val notificationPreferencesFlow: StateFlow<NotificationPreferences> = _notificationPreferencesFlow.asStateFlow()

    fun updateLastSyncTime(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
        _credentialsFlow.value = loadCredentials()
    }

    fun loadNotificationPreferences(): NotificationPreferences {
        return NotificationPreferences(
            notifyClasses = prefs.getBoolean(KEY_NOTIFY_CLASSES, true),
            notifyGrades = prefs.getBoolean(KEY_NOTIFY_GRADES, true),
            notifyMessages = prefs.getBoolean(KEY_NOTIFY_MESSAGES, true),
            notifyFinances = prefs.getBoolean(KEY_NOTIFY_FINANCES, true),
            reminderMinutesBefore = prefs.getInt(KEY_CLASS_REMINDER_MINUTES, 15)
        )
    }

    fun setNotifyClasses(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_CLASSES, enabled).apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    fun setNotifyGrades(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_GRADES, enabled).apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    fun setNotifyMessages(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_MESSAGES, enabled).apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    fun setNotifyFinances(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_FINANCES, enabled).apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    fun setReminderMinutesBefore(minutes: Int) {
        prefs.edit().putInt(KEY_CLASS_REMINDER_MINUTES, minutes).apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    fun saveCredentials(
        neptunCode: String,
        password: String,
        universityId: String,
        universityName: String,
        neptunUrl: String,
        studentName: String = "Teszt Hallgató",
        sessionToken: String = ""
    ) {
        prefs.edit()
            .putString(KEY_NEPTUN_CODE, neptunCode)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_UNIVERSITY_ID, universityId)
            .putString(KEY_UNIVERSITY_NAME, universityName)
            .putString(KEY_NEPTUN_URL, neptunUrl)
            .putString(KEY_STUDENT_NAME, studentName)
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()

        _credentialsFlow.value = loadCredentials()
    }

    fun loadCredentials(): StudentCredentials? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val neptunCode = prefs.getString(KEY_NEPTUN_CODE, null) ?: return null
        val universityId = prefs.getString(KEY_UNIVERSITY_ID, "") ?: ""
        val universityName = prefs.getString(KEY_UNIVERSITY_NAME, "Egyetem") ?: "Egyetem"
        val neptunUrl = prefs.getString(KEY_NEPTUN_URL, "") ?: ""
        val studentName = prefs.getString(KEY_STUDENT_NAME, "Hallgató") ?: "Hallgató"
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)

        return StudentCredentials(
            neptunCode = neptunCode,
            universityId = universityId,
            universityName = universityName,
            neptunUrl = neptunUrl,
            studentName = studentName,
            isLoggedIn = isLoggedIn,
            lastSyncTime = lastSync
        )
    }

    fun getSessionToken(): String {
        return prefs.getString(KEY_SESSION_TOKEN, "") ?: ""
    }

    fun getAccessToken(): String {
        return prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
    }

    fun setAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getRefreshToken(): String {
        return prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
    }

    fun setRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getDeviceCookie(username: String): String {
        return prefs.getString("${KEY_DEVICE_COOKIE}_${username.uppercase()}", "") ?: ""
    }

    fun setDeviceCookie(username: String, cookie: String) {
        prefs.edit().putString("${KEY_DEVICE_COOKIE}_${username.uppercase()}", cookie).apply()
    }

    fun isModernApi(): Boolean {
        return prefs.getBoolean(KEY_IS_MODERN_API, true)
    }

    fun setIsModernApi(isModern: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MODERN_API, isModern).apply()
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, "") ?: ""
    }

    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
    }

    fun getStudentTrainingId(): String {
        return prefs.getString(KEY_TRAINING_ID, "") ?: ""
    }

    fun setStudentTrainingId(id: String) {
        prefs.edit().putString(KEY_TRAINING_ID, id).apply()
    }

    fun getPassword(): String {
        return prefs.getString(KEY_PASSWORD, "") ?: ""
    }

    fun setPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    fun loadThemeSettings(): ThemeSettings {
        val modeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val accentId = prefs.getString(KEY_ACCENT_COLOR, AppAccentColor.BLUE.id) ?: AppAccentColor.BLUE.id
        val accentColor = AppAccentColor.fromId(accentId)

        return ThemeSettings(
            themeMode = themeMode,
            useDynamicColor = dynamicColor,
            accentColor = accentColor
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeSettingsFlow.value = loadThemeSettings()
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _themeSettingsFlow.value = loadThemeSettings()
    }

    fun setAccentColor(accent: AppAccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, accent.id).apply()
        _themeSettingsFlow.value = loadThemeSettings()
    }

    fun clear() {
        prefs.edit().clear().apply()
        _credentialsFlow.value = null
    }

    companion object {
        private const val KEY_NEPTUN_CODE = "key_neptun_code"
        private const val KEY_PASSWORD = "key_password"
        private const val KEY_UNIVERSITY_ID = "key_uni_id"
        private const val KEY_UNIVERSITY_NAME = "key_uni_name"
        private const val KEY_NEPTUN_URL = "key_neptun_url"
        private const val KEY_STUDENT_NAME = "key_student_name"
        private const val KEY_SESSION_TOKEN = "key_session_token"
        private const val KEY_ACCESS_TOKEN = "key_access_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_DEVICE_COOKIE = "key_device_cookie"
        private const val KEY_IS_MODERN_API = "key_is_modern_api"
        private const val KEY_BASE_URL = "key_base_url"
        private const val KEY_TRAINING_ID = "key_training_id"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_LAST_SYNC = "key_last_sync"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"
        private const val KEY_ACCENT_COLOR = "key_accent_color"
        private const val KEY_NOTIFY_CLASSES = "key_notify_classes"
        private const val KEY_NOTIFY_GRADES = "key_notify_grades"
        private const val KEY_NOTIFY_MESSAGES = "key_notify_messages"
        private const val KEY_NOTIFY_FINANCES = "key_notify_finances"
        private const val KEY_CLASS_REMINDER_MINUTES = "key_class_reminder_minutes"
    }
}

data class NotificationPreferences(
    val notifyClasses: Boolean = true,
    val notifyGrades: Boolean = true,
    val notifyMessages: Boolean = true,
    val notifyFinances: Boolean = true,
    val reminderMinutesBefore: Int = 15
)
