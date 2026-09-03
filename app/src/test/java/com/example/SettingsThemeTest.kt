package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.EncryptedPreferencesManager
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsThemeTest {

    private lateinit var prefsManager: EncryptedPreferencesManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefsManager = EncryptedPreferencesManager(context)
    }

    @Test
    fun testDefaultThemeSettings() {
        val themeSettings = prefsManager.loadThemeSettings()
        assertEquals(ThemeMode.SYSTEM, themeSettings.themeMode)
        assertTrue(themeSettings.useDynamicColor)
        assertEquals(AppAccentColor.BLUE, themeSettings.accentColor)
    }

    @Test
    fun testUpdateThemeMode() {
        prefsManager.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, prefsManager.loadThemeSettings().themeMode)

        prefsManager.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, prefsManager.loadThemeSettings().themeMode)
    }

    @Test
    fun testUpdateDynamicColorAndAccent() {
        prefsManager.setDynamicColor(false)
        assertFalse(prefsManager.loadThemeSettings().useDynamicColor)

        prefsManager.setAccentColor(AppAccentColor.EMERALD)
        assertEquals(AppAccentColor.EMERALD, prefsManager.loadThemeSettings().accentColor)
    }
}
