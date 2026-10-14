package com.example.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object BatteryOptimizationHelper {
    private const val TAG = "BatteryOptHelper"

    /**
     * Checks if the app is currently whitelisted / ignoring battery optimizations.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    /**
     * Attempts to open the system battery optimization settings screen.
     * Falls back to application details settings if the intent cannot be resolved.
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = createBatteryOptimizationIntent()
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            when (e) {
                is ActivityNotFoundException, is SecurityException -> {
                    Log.w(TAG, "Failed to open ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, falling back to app details", e)
                    openAppDetailsSettings(context)
                }
                else -> throw e
            }
        }
    }

    /**
     * Fallback to open the app's application details settings page.
     */
    fun openAppDetailsSettings(context: Context) {
        try {
            val detailsIntent = createAppDetailsSettingsIntent(context.packageName)
            context.startActivity(detailsIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open application details settings", e)
        }
    }

    /**
     * Creates an intent for opening ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS.
     */
    fun createBatteryOptimizationIntent(): Intent {
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates an intent for opening ACTION_APPLICATION_DETAILS_SETTINGS for the given package.
     */
    fun createAppDetailsSettingsIntent(packageName: String): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Returns helpful guidance text or tip for aggressive OEMs (Samsung, Xiaomi/MIUI, Huawei, OnePlus)
     * to allow autostart / disable battery saver.
     */
    @JvmOverloads
    fun getManufacturerGuidance(manufacturer: String = Build.MANUFACTURER): String {
        val m = manufacturer.lowercase()
        return when {
            m.contains("samsung") -> {
                "Samsung: Beállítások → Alkalmazások → Neptun Mobile → Akkumulátor → állítsd „Nem korlátozott” (Unrestricted) módra."
            }
            m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> {
                "Xiaomi / MIUI / HyperOS: Beállítások → Alkalmazások → Alkalmazások kezelése → Neptun Mobile → kapcsold be az „Automatikus indítás” (Autostart) opciót, az Akkumulátorkímélőt pedig állítsd „Nincs korlátozás”-ra."
            }
            m.contains("huawei") || m.contains("honor") -> {
                "Huawei / Honor: Beállítások → Akkumulátor → Alkalmazásindítás → Neptun Mobile → kapcsold ki az automatikus kezelést, és engedélyezd az Automatikus és Háttérben futtatást."
            }
            m.contains("oneplus") || m.contains("oppo") || m.contains("realme") -> {
                "OnePlus / OPPO / Realme: Beállítások → Alkalmazások → Neptun Mobile → Akkumulátorhasználat → engedélyezd a „Háttértevékenység engedélyezése” és „Automatikus indítás” opciókat."
            }
            else -> {
                "Tipp: Bizonyos készülékeken (pl. Samsung, Xiaomi, Huawei) a megbízható háttérszinkronhoz és óraértesítésekhez szükséges lehet az automatikus indítás és a háttérben futás engedélyezése a rendszerbeállításokban."
            }
        }
    }
}
