package com.example.core.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Az app felületének nyelve (független a Neptun-szerver nyelvétől).
 *
 * @property languageTag ISO nyelvkód ("hu"/"en"/"de"), SYSTEM esetén null.
 */
enum class AppLocale(val languageTag: String?) {
    SYSTEM(null),
    HUNGARIAN("hu"),
    ENGLISH("en"),
    GERMAN("de");

    companion object {
        fun fromName(name: String?): AppLocale {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
        }
    }
}

/**
 * Sima (nem titkosított) tároló az app nyelvválasztásához. A Keystore-alapú
 * [com.example.core.security.EncryptedPreferencesManager] is ezt használja a
 * háttérben, de az [attachBaseContext]-idejű beolvasáshoz nem kell példányosítani
 * a titkosított tárolót (MasterKey + folyamok nélkül, gyorsan működik).
 */
object AppLocaleStore {
    private const val PREFS_NAME = "neptun_ui_prefs"
    private const val KEY_APP_LOCALE = "app_locale"

    fun read(context: Context): AppLocale {
        return try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_APP_LOCALE, AppLocale.SYSTEM.name)
            AppLocale.fromName(raw)
        } catch (e: Exception) {
            AppLocale.SYSTEM
        }
    }

    fun write(context: Context, locale: AppLocale) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_APP_LOCALE, locale.name)
                .apply()
        } catch (e: Exception) {
            // A nyelvválasztás elvesztése nem törheti meg az appot.
        }
    }
}

object AppLocales {

    /**
     * Feloldja az effektív [Locale]-t: explicit választás esetén azt, SYSTEM
     * esetén az eszköz nyelvét (API 33+ rendszer-szintű per-app nyelvet is
     * tiszteletben tartva, ha az a támogatott nyelvek közé esik).
     */
    fun resolve(context: Context, appLocale: AppLocale = AppLocaleStore.read(context)): Locale {
        appLocale.languageTag?.let { return Locale.forLanguageTag(it) }
        systemAppLocaleOverride(context)?.let { return it }
        return deviceLocale()
    }

    /**
     * Új, a feloldott nyelvre állított [Context]-et ad vissza, és a
     * [Locale.setDefault]-ot is frissíti (dátum-/számformátumok miatt).
     * Az Application és az Activity [android.content.ContextWrapper.attachBaseContext]
     * metódusából hívandó.
     */
    fun wrap(context: Context): Context {
        val locale = resolve(context)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    private fun deviceLocale(): Locale {
        return try {
            LocaleList.getDefault().get(0) ?: Locale.getDefault()
        } catch (e: Exception) {
            Locale.getDefault()
        }
    }

    /**
     * API 33+: a rendszerbeállításokban az apphez rendelt nyelv, ha támogatott
     * (hu/en/de). Csak SYSTEM módban vesszük figyelembe.
     */
    private fun systemAppLocaleOverride(context: Context): Locale? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        return try {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? android.app.LocaleManager
            val tags = localeManager?.applicationLocales?.toLanguageTags()
            val first = tags?.split(",")?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val supported = setOf("hu", "en", "de")
            val match = Locale.forLanguageTag(first).language?.lowercase()
            if (match in supported) Locale.forLanguageTag(match!!) else null
        } catch (e: Exception) {
            null
        }
    }
}
