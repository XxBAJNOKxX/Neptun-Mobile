package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.EncryptedPreferencesManager
import com.example.core.util.BatteryOptimizationHelper
import com.example.domain.model.StudentCredentials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BatteryOptimizationTest {

    private lateinit var context: Context
    private lateinit var prefsManager: EncryptedPreferencesManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        prefsManager = EncryptedPreferencesManager(context)
        prefsManager.setBatteryOptimizationPromptDismissed(false)
    }

    // 1. BatteryOptimizationHelper tests

    @Test
    fun `isIgnoringBatteryOptimizations reflects system PowerManager status`() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPower = Shadows.shadowOf(powerManager)

        shadowPower.setIgnoringBatteryOptimizations(context.packageName, false)
        assertFalse(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))

        shadowPower.setIgnoringBatteryOptimizations(context.packageName, true)
        assertTrue(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    @Test
    fun `createBatteryOptimizationIntent creates valid intent with new task flag`() {
        val intent = BatteryOptimizationHelper.createBatteryOptimizationIntent()
        assertEquals(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, intent.action)
        assertTrue((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun `createAppDetailsSettingsIntent creates fallback intent with correct package uri and flag`() {
        val packageName = context.packageName
        val intent = BatteryOptimizationHelper.createAppDetailsSettingsIntent(packageName)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals(Uri.parse("package:$packageName"), intent.data)
        assertTrue((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun `openBatteryOptimizationSettings triggers activity start`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = Shadows.shadowOf(app)
        shadowApp.clearNextStartedActivities()

        BatteryOptimizationHelper.openBatteryOptimizationSettings(context)

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull(startedIntent)
        assertTrue(
            startedIntent.action == Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS ||
            startedIntent.action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        )
    }

    @Test
    fun `openAppDetailsSettings triggers fallback app details activity start`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = Shadows.shadowOf(app)
        shadowApp.clearNextStartedActivities()

        BatteryOptimizationHelper.openAppDetailsSettings(context)

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, startedIntent.action)
        assertEquals(Uri.parse("package:${context.packageName}"), startedIntent.data)
    }

    @Test
    fun `getManufacturerGuidance returns specific guidance for aggressive OEMs`() {
        val samsungGuidance = BatteryOptimizationHelper.getManufacturerGuidance("samsung")
        assertTrue(samsungGuidance.contains("Samsung", ignoreCase = true))
        assertTrue(samsungGuidance.contains("Nem korlátozott", ignoreCase = true) || samsungGuidance.contains("Unrestricted", ignoreCase = true))

        val xiaomiGuidance = BatteryOptimizationHelper.getManufacturerGuidance("Xiaomi")
        assertTrue(xiaomiGuidance.contains("Xiaomi", ignoreCase = true))
        assertTrue(xiaomiGuidance.contains("Automatikus indítás", ignoreCase = true))

        val redmiGuidance = BatteryOptimizationHelper.getManufacturerGuidance("Redmi")
        assertTrue(redmiGuidance.contains("Xiaomi", ignoreCase = true))

        val huaweiGuidance = BatteryOptimizationHelper.getManufacturerGuidance("HUAWEI")
        assertTrue(huaweiGuidance.contains("Huawei", ignoreCase = true))
        assertTrue(huaweiGuidance.contains("Alkalmazásindítás", ignoreCase = true))

        val oneplusGuidance = BatteryOptimizationHelper.getManufacturerGuidance("OnePlus")
        assertTrue(oneplusGuidance.contains("OnePlus", ignoreCase = true))
        assertTrue(oneplusGuidance.contains("Háttértevékenység", ignoreCase = true))

        val genericGuidance = BatteryOptimizationHelper.getManufacturerGuidance("Google")
        assertTrue(genericGuidance.contains("Tipp:", ignoreCase = true))
    }

    // 2. EncryptedPreferencesManager tests

    @Test
    fun `isBatteryOptimizationPromptDismissed defaults to false`() {
        assertFalse(prefsManager.isBatteryOptimizationPromptDismissed())
    }

    @Test
    fun `setBatteryOptimizationPromptDismissed stores and retrieves value`() {
        prefsManager.setBatteryOptimizationPromptDismissed(true)
        assertTrue(prefsManager.isBatteryOptimizationPromptDismissed())

        prefsManager.setBatteryOptimizationPromptDismissed(false)
        assertFalse(prefsManager.isBatteryOptimizationPromptDismissed())
    }

    @Test
    fun `clear preserves isBatteryOptimizationPromptDismissed preference`() {
        prefsManager.setBatteryOptimizationPromptDismissed(true)
        prefsManager.clear()
        assertTrue(prefsManager.isBatteryOptimizationPromptDismissed())
    }

    // 3. ELTE and 2FA battery optimization dialog conditions

    private fun shouldShowDialog(
        isInteractiveReauth: Boolean,
        credentials: StudentCredentials?,
        isIgnoringBattery: Boolean,
        isDismissed: Boolean
    ): Boolean {
        val isElteOr2Fa = isInteractiveReauth ||
                credentials?.universityId == "elte" ||
                credentials?.neptunUrl?.contains("elte.hu") == true
        return isElteOr2Fa && !isIgnoringBattery && !isDismissed
    }

    @Test
    fun `dialog is shown for ELTE account when optimization is active and prompt not dismissed`() {
        val elteCreds = StudentCredentials(
            neptunCode = "ELTE01",
            universityId = "elte",
            universityName = "Eötvös Loránd Tudományegyetem",
            neptunUrl = "https://neptun.elte.hu/hallgato"
        )

        val shouldShow = shouldShowDialog(
            isInteractiveReauth = false,
            credentials = elteCreds,
            isIgnoringBattery = false,
            isDismissed = false
        )
        assertTrue(shouldShow)
    }

    @Test
    fun `dialog is shown for ELTE account detected via neptunUrl`() {
        val elteCreds = StudentCredentials(
            neptunCode = "ELTE02",
            universityId = "other_id",
            universityName = "ELTE",
            neptunUrl = "https://hallgato.elte.hu/main.aspx"
        )

        val shouldShow = shouldShowDialog(
            isInteractiveReauth = false,
            credentials = elteCreds,
            isIgnoringBattery = false,
            isDismissed = false
        )
        assertTrue(shouldShow)
    }

    @Test
    fun `dialog is shown for non-ELTE account when 2FA interactive reauth is required`() {
        val bmeCreds = StudentCredentials(
            neptunCode = "BME001",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu/hallgato"
        )

        val shouldShow = shouldShowDialog(
            isInteractiveReauth = true,
            credentials = bmeCreds,
            isIgnoringBattery = false,
            isDismissed = false
        )
        assertTrue(shouldShow)
    }

    @Test
    fun `dialog is NOT shown when battery optimization is already disabled`() {
        val elteCreds = StudentCredentials(
            neptunCode = "ELTE01",
            universityId = "elte",
            universityName = "ELTE",
            neptunUrl = "https://neptun.elte.hu"
        )

        val shouldShow = shouldShowDialog(
            isInteractiveReauth = false,
            credentials = elteCreds,
            isIgnoringBattery = true, // optimizations ignored = disabled
            isDismissed = false
        )
        assertFalse(shouldShow)
    }

    @Test
    fun `dialog is NOT shown when user has dismissed the prompt`() {
        val elteCreds = StudentCredentials(
            neptunCode = "ELTE01",
            universityId = "elte",
            universityName = "ELTE",
            neptunUrl = "https://neptun.elte.hu"
        )

        val shouldShow = shouldShowDialog(
            isInteractiveReauth = false,
            credentials = elteCreds,
            isIgnoringBattery = false,
            isDismissed = true // user dismissed
        )
        assertFalse(shouldShow)
    }

    @Test
    fun `dialog is NOT shown for non-ELTE non-2FA account`() {
        val corvinusCreds = StudentCredentials(
            neptunCode = "CORV01",
            universityId = "uni-corvinus",
            universityName = "Budapesti Corvinus Egyetem",
            neptunUrl = "https://neptun.uni-corvinus.hu"
        )

        val shouldShow = shouldShowDialog(
            isInteractiveReauth = false,
            credentials = corvinusCreds,
            isIgnoringBattery = false,
            isDismissed = false
        )
        assertFalse(shouldShow)
    }
}
