package com.example.core.security

import com.example.core.notification.NotifiedStore

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

/**
 * Adataink forrása (UI visszajelzéshez): valódi szerveradat, demo bejelentkezés vagy mock adat.
 */
enum class DataMode {
    REAL, DEMO, MOCK;

    companion object {
        fun fromName(name: String?): DataMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: REAL
        }
    }
}

/** Felhasználói személyreszabási beállítások (téma mellett). */
data class AppPersonalization(
    val startScreen: String = "HOME",
    val showWeekend: Boolean = false,
    val targetCredits: Int = 240,
    val biometricLockEnabled: Boolean = false,
    /** A kikapcsolt (elrejtett) oldalak nevei (NavigationItem.name). */
    val hiddenPages: Set<String> = emptySet()
)

class EncryptedPreferencesManager(context: Context) : NotifiedStore {

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

    private val _personalizationFlow = MutableStateFlow(loadPersonalization())
    val personalizationFlow: StateFlow<AppPersonalization> = _personalizationFlow.asStateFlow()

    private val _dataModeFlow = MutableStateFlow(loadDataMode())
    val dataModeFlow: StateFlow<DataMode> = _dataModeFlow.asStateFlow()

    private val _sessionExpiredFlow = MutableStateFlow(prefs.getBoolean(KEY_SESSION_EXPIRED, false))
    val sessionExpiredFlow: StateFlow<Boolean> = _sessionExpiredFlow.asStateFlow()

    fun updateLastSyncTime(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
        _credentialsFlow.value = loadCredentials()
    }

    // ------------------------------------------------------------------ //
    // Értesítési beállítások
    // ------------------------------------------------------------------ //

    fun loadNotificationPreferences(): NotificationPreferences {
        return NotificationPreferences(
            notifyClasses = prefs.getBoolean(KEY_NOTIFY_CLASSES, true),
            notifyGrades = prefs.getBoolean(KEY_NOTIFY_GRADES, true),
            notifyMessages = prefs.getBoolean(KEY_NOTIFY_MESSAGES, true),
            notifyFinances = prefs.getBoolean(KEY_NOTIFY_FINANCES, true),
            reminderMinutesBefore = prefs.getInt(KEY_CLASS_REMINDER_MINUTES, 15),
            quietHoursEnabled = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false),
            quietStartMinute = prefs.getInt(KEY_QUIET_START_MINUTE, 22 * 60),
            quietEndMinute = prefs.getInt(KEY_QUIET_END_MINUTE, 7 * 60)
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
        prefs.edit().putInt(KEY_CLASS_REMINDER_MINUTES, minutes.coerceIn(5, 120)).apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    fun setQuietHours(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    fun setQuietHoursWindow(startMinute: Int, endMinute: Int) {
        prefs.edit()
            .putInt(KEY_QUIET_START_MINUTE, startMinute.coerceIn(0, 24 * 60 - 1))
            .putInt(KEY_QUIET_END_MINUTE, endMinute.coerceIn(0, 24 * 60 - 1))
            .apply()
        _notificationPreferencesFlow.value = loadNotificationPreferences()
    }

    // ------------------------------------------------------------------ //
    // Személyreszabás
    // ------------------------------------------------------------------ //

    fun loadPersonalization(): AppPersonalization {
        return AppPersonalization(
            startScreen = prefs.getString(KEY_START_SCREEN, "HOME") ?: "HOME",
            showWeekend = prefs.getBoolean(KEY_SHOW_WEEKEND, false),
            targetCredits = prefs.getInt(KEY_TARGET_CREDITS, 240).coerceIn(30, 400),
            biometricLockEnabled = prefs.getBoolean(KEY_BIOMETRIC_LOCK, false),
            hiddenPages = prefs.getStringSet(KEY_HIDDEN_PAGES, emptySet())?.toSet() ?: emptySet()
        )
    }

    fun setStartScreen(screen: String) {
        prefs.edit().putString(KEY_START_SCREEN, screen).apply()
        _personalizationFlow.value = loadPersonalization()
    }

    fun setShowWeekend(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_WEEKEND, enabled).apply()
        _personalizationFlow.value = loadPersonalization()
    }

    fun setHiddenPages(hiddenPages: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_PAGES, hiddenPages).apply()
        _personalizationFlow.value = loadPersonalization()
    }

    fun setTargetCredits(credits: Int) {
        prefs.edit().putInt(KEY_TARGET_CREDITS, credits.coerceIn(30, 400)).apply()
        _personalizationFlow.value = loadPersonalization()
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
        _personalizationFlow.value = loadPersonalization()
    }

    // ------------------------------------------------------------------ //
    // Adatforrás mód (valódi / demo / mock)
    // ------------------------------------------------------------------ //

    fun loadDataMode(): DataMode = DataMode.fromName(prefs.getString(KEY_DATA_MODE, DataMode.REAL.name))

    fun getDataMode(): DataMode = loadDataMode()

    fun setDataMode(mode: DataMode) {
        prefs.edit().putString(KEY_DATA_MODE, mode.name).apply()
        _dataModeFlow.value = mode
    }

    // ------------------------------------------------------------------ //
    // Lejárt munkamenet jelzés
    // ------------------------------------------------------------------ //

    fun isSessionExpired(): Boolean = prefs.getBoolean(KEY_SESSION_EXPIRED, false)

    fun markSessionExpired() {
        prefs.edit().putBoolean(KEY_SESSION_EXPIRED, true).apply()
        _sessionExpiredFlow.value = true
    }

    fun clearSessionExpired() {
        prefs.edit().putBoolean(KEY_SESSION_EXPIRED, false).apply()
        _sessionExpiredFlow.value = false
    }

    // ------------------------------------------------------------------ //
    // Már értesített elemek követése (diff-alapú értesítésekhez)
    // ------------------------------------------------------------------ //

    override fun getNotifiedIds(key: String): Set<String> {
        return prefs.getStringSet(KEY_NOTIFIED_PREFIX + key, emptySet()) ?: emptySet()
    }

    override fun setNotifiedIds(key: String, ids: Set<String>) {
        prefs.edit().putStringSet(KEY_NOTIFIED_PREFIX + key, ids).apply()
    }

    override fun isBaselineDone(key: String): Boolean {
        return prefs.getBoolean(KEY_NOTIFIED_BASELINE_PREFIX + key, false)
    }

    override fun markBaselineDone(key: String) {
        prefs.edit().putBoolean(KEY_NOTIFIED_BASELINE_PREFIX + key, true).apply()
    }

    // ------------------------------------------------------------------ //
    // Egyetem / belépés
    // ------------------------------------------------------------------ //

    fun saveSelectedUniversity(universityId: String, universityName: String, neptunUrl: String) {
        prefs.edit()
            .putString(KEY_SELECTED_UNIVERSITY_ID, universityId)
            .putString(KEY_SELECTED_UNIVERSITY_NAME, universityName)
            .putString(KEY_SELECTED_UNIVERSITY_URL, neptunUrl)
            .putString(KEY_BASE_URL, neptunUrl)
            .apply()
    }

    fun getSelectedUniversityId(): String {
        val selectedId = prefs.getString(KEY_SELECTED_UNIVERSITY_ID, "") ?: ""
        if (selectedId.isNotEmpty()) return selectedId
        return prefs.getString(KEY_UNIVERSITY_ID, "") ?: ""
    }

    fun getSelectedUniversityName(): String {
        val selectedName = prefs.getString(KEY_SELECTED_UNIVERSITY_NAME, "") ?: ""
        if (selectedName.isNotEmpty()) return selectedName
        return prefs.getString(KEY_UNIVERSITY_NAME, "") ?: ""
    }

    fun getSelectedUniversityUrl(): String {
        val selectedUrl = prefs.getString(KEY_SELECTED_UNIVERSITY_URL, "") ?: ""
        if (selectedUrl.isNotEmpty()) return selectedUrl
        val uniUrl = prefs.getString(KEY_NEPTUN_URL, "") ?: ""
        if (uniUrl.isNotEmpty()) return uniUrl
        return prefs.getString(KEY_BASE_URL, "") ?: ""
    }

    fun saveLastNeptunCode(code: String) {
        if (code.isNotBlank()) {
            prefs.edit().putString(KEY_NEPTUN_CODE, code.trim().uppercase()).apply()
        }
    }

    fun getLastNeptunCode(): String {
        return prefs.getString(KEY_NEPTUN_CODE, "") ?: ""
    }

    fun saveCredentials(
        neptunCode: String,
        password: String,
        universityId: String,
        universityName: String,
        neptunUrl: String,
        studentName: String = "Teszt Hallgató",
        sessionToken: String = "",
        trainingProgram: String = "Egyetemi Képzés"
    ) {
        prefs.edit()
            .putString(KEY_NEPTUN_CODE, neptunCode)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_UNIVERSITY_ID, universityId)
            .putString(KEY_UNIVERSITY_NAME, universityName)
            .putString(KEY_NEPTUN_URL, neptunUrl)
            .putString(KEY_BASE_URL, neptunUrl)
            .putString(KEY_SELECTED_UNIVERSITY_ID, universityId)
            .putString(KEY_SELECTED_UNIVERSITY_NAME, universityName)
            .putString(KEY_SELECTED_UNIVERSITY_URL, neptunUrl)
            .putString(KEY_STUDENT_NAME, studentName)
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .putString(KEY_TRAINING_PROGRAM, trainingProgram)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()

        _credentialsFlow.value = loadCredentials()
    }

    fun updateStudentInfo(
        studentName: String? = null,
        trainingProgram: String? = null,
        studentTrainingId: String? = null
    ) {
        val editor = prefs.edit()
        if (!studentName.isNullOrBlank()) {
            editor.putString(KEY_STUDENT_NAME, studentName)
        }
        if (!trainingProgram.isNullOrBlank()) {
            editor.putString(KEY_TRAINING_PROGRAM, trainingProgram)
        }
        if (!studentTrainingId.isNullOrBlank()) {
            editor.putString(KEY_TRAINING_ID, studentTrainingId)
        }
        editor.apply()
        _credentialsFlow.value = loadCredentials()
    }

    fun loadCredentials(): StudentCredentials? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val neptunCode = prefs.getString(KEY_NEPTUN_CODE, null) ?: return null
        val universityId = prefs.getString(KEY_UNIVERSITY_ID, "") ?: ""
        val universityName = prefs.getString(KEY_UNIVERSITY_NAME, "Egyetem") ?: "Egyetem"
        val neptunUrl = prefs.getString(KEY_NEPTUN_URL, "") ?: ""
        val studentName = prefs.getString(KEY_STUDENT_NAME, "Hallgató") ?: "Hallgató"
        val trainingProgram = prefs.getString(KEY_TRAINING_PROGRAM, "Mérnökinformatikus BSc") ?: "Mérnökinformatikus BSc"
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)

        return StudentCredentials(
            neptunCode = neptunCode,
            universityId = universityId,
            universityName = universityName,
            neptunUrl = neptunUrl,
            studentName = studentName,
            trainingProgram = trainingProgram,
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
        // Keep selected university and personalization across logouts so user does not need to re-select university
        val savedUniId = getSelectedUniversityId()
        val savedUniName = getSelectedUniversityName()
        val savedUniUrl = getSelectedUniversityUrl()
        val savedNeptunCode = prefs.getString(KEY_NEPTUN_CODE, "") ?: ""
        val savedThemeMode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val savedDynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val savedAccent = prefs.getString(KEY_ACCENT_COLOR, AppAccentColor.BLUE.id)
        val notifyClasses = prefs.getBoolean(KEY_NOTIFY_CLASSES, true)
        val notifyGrades = prefs.getBoolean(KEY_NOTIFY_GRADES, true)
        val notifyMessages = prefs.getBoolean(KEY_NOTIFY_MESSAGES, true)
        val notifyFinances = prefs.getBoolean(KEY_NOTIFY_FINANCES, true)
        val reminderMins = prefs.getInt(KEY_CLASS_REMINDER_MINUTES, 15)
        val savedPersonalization = loadPersonalization()
        val savedNotifQuiet = loadNotificationPreferences()

        prefs.edit()
            .clear()
            .putString(KEY_SELECTED_UNIVERSITY_ID, savedUniId)
            .putString(KEY_SELECTED_UNIVERSITY_NAME, savedUniName)
            .putString(KEY_SELECTED_UNIVERSITY_URL, savedUniUrl)
            .putString(KEY_BASE_URL, savedUniUrl)
            .putString(KEY_NEPTUN_CODE, savedNeptunCode)
            .putString(KEY_THEME_MODE, savedThemeMode)
            .putBoolean(KEY_DYNAMIC_COLOR, savedDynamicColor)
            .putString(KEY_ACCENT_COLOR, savedAccent)
            .putBoolean(KEY_NOTIFY_CLASSES, notifyClasses)
            .putBoolean(KEY_NOTIFY_GRADES, notifyGrades)
            .putBoolean(KEY_NOTIFY_MESSAGES, notifyMessages)
            .putBoolean(KEY_NOTIFY_FINANCES, notifyFinances)
            .putInt(KEY_CLASS_REMINDER_MINUTES, reminderMins)
            .putString(KEY_START_SCREEN, savedPersonalization.startScreen)
            .putBoolean(KEY_SHOW_WEEKEND, savedPersonalization.showWeekend)
            .putInt(KEY_TARGET_CREDITS, savedPersonalization.targetCredits)
            .putBoolean(KEY_BIOMETRIC_LOCK, savedPersonalization.biometricLockEnabled)
            .putStringSet(KEY_HIDDEN_PAGES, savedPersonalization.hiddenPages)
            .putBoolean(KEY_QUIET_HOURS_ENABLED, savedNotifQuiet.quietHoursEnabled)
            .putInt(KEY_QUIET_START_MINUTE, savedNotifQuiet.quietStartMinute)
            .putInt(KEY_QUIET_END_MINUTE, savedNotifQuiet.quietEndMinute)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()

        _credentialsFlow.value = null
        _sessionExpiredFlow.value = false
    }

    companion object {
        private const val KEY_NEPTUN_CODE = "key_neptun_code"
        private const val KEY_PASSWORD = "key_password"
        private const val KEY_UNIVERSITY_ID = "key_uni_id"
        private const val KEY_UNIVERSITY_NAME = "key_uni_name"
        private const val KEY_NEPTUN_URL = "key_neptun_url"
        private const val KEY_SELECTED_UNIVERSITY_ID = "key_selected_uni_id"
        private const val KEY_SELECTED_UNIVERSITY_NAME = "key_selected_uni_name"
        private const val KEY_SELECTED_UNIVERSITY_URL = "key_selected_uni_url"
        private const val KEY_STUDENT_NAME = "key_student_name"
        private const val KEY_SESSION_TOKEN = "key_session_token"
        private const val KEY_ACCESS_TOKEN = "key_access_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_DEVICE_COOKIE = "key_device_cookie"
        private const val KEY_IS_MODERN_API = "key_is_modern_api"
        private const val KEY_BASE_URL = "key_base_url"
        private const val KEY_TRAINING_ID = "key_training_id"
        private const val KEY_TRAINING_PROGRAM = "key_training_program"
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
        private const val KEY_QUIET_HOURS_ENABLED = "key_quiet_hours_enabled"
        private const val KEY_QUIET_START_MINUTE = "key_quiet_start_minute"
        private const val KEY_QUIET_END_MINUTE = "key_quiet_end_minute"
        private const val KEY_START_SCREEN = "key_start_screen"
        private const val KEY_SHOW_WEEKEND = "key_show_weekend"
        private const val KEY_TARGET_CREDITS = "key_target_credits"
        private const val KEY_BIOMETRIC_LOCK = "key_biometric_lock"
        private const val KEY_HIDDEN_PAGES = "key_hidden_pages"
        private const val KEY_DATA_MODE = "key_data_mode"
        private const val KEY_SESSION_EXPIRED = "key_session_expired"
        private const val KEY_NOTIFIED_PREFIX = "key_notified_"
        private const val KEY_NOTIFIED_BASELINE_PREFIX = "key_notified_baseline_"
    }
}

data class NotificationPreferences(
    val notifyClasses: Boolean = true,
    val notifyGrades: Boolean = true,
    val notifyMessages: Boolean = true,
    val notifyFinances: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val quietHoursEnabled: Boolean = false,
    val quietStartMinute: Int = 22 * 60,
    val quietEndMinute: Int = 7 * 60
) {
    /**
     * A [nowMinuteOfDay] (0..1439) a halk időszakban van-e.
     * Támogatja az éjfélen átnyúló (pl. 22:00–07:00) ablakot is.
     */
    fun isQuietNow(nowMinuteOfDay: Int): Boolean {
        if (!quietHoursEnabled) return false
        val start = quietStartMinute
        val end = quietEndMinute
        return if (start == end) {
            false
        } else if (start < end) {
            nowMinuteOfDay in start until end
        } else {
            nowMinuteOfDay >= start || nowMinuteOfDay < end
        }
    }

    companion object {
        fun formatMinute(minuteOfDay: Int): String {
            val h = minuteOfDay / 60
            val m = minuteOfDay % 60
            return "%02d:%02d".format(h, m)
        }
    }
}
