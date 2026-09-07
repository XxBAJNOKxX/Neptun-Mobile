package com.example

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.EncryptedPreferencesManager
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.FilcLime
import com.example.ui.theme.FilcPalette
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
    fun defaultThemeIsFilc() {
        val themeSettings = prefsManager.loadThemeSettings()
        assertEquals(ThemeMode.SYSTEM, themeSettings.themeMode)
        // A Filc arculat kezeli a dynamic color-t: a külső paletta helyett a
        // saját, accentből levezetett színtársulatot használjuk.
        assertFalse(themeSettings.useDynamicColor)
        assertEquals(AppAccentColor.FILC, themeSettings.accentColor)
    }

    @Test
    fun testUpdateThemeMode() {
        prefsManager.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, prefsManager.loadThemeSettings().themeMode)

        prefsManager.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, prefsManager.loadThemeSettings().themeMode)
    }

    @Test
    fun testUpdateAccentColor() {
        prefsManager.setDynamicColor(false)
        assertFalse(prefsManager.loadThemeSettings().useDynamicColor)

        prefsManager.setAccentColor(AppAccentColor.PURPLE)
        assertEquals(AppAccentColor.PURPLE, prefsManager.loadThemeSettings().accentColor)
    }

    @Test
    fun legacyAccentIdsStillResolve() {
        // A régi (Material-kiegészítő) accentnevek ne töröljék a mentett beállítást.
        assertEquals(AppAccentColor.GREEN, AppAccentColor.fromId("emerald"))
        assertEquals(AppAccentColor.YELLOW, AppAccentColor.fromId("gold"))
        assertEquals(AppAccentColor.RED, AppAccentColor.fromId("crimson"))
        assertEquals(AppAccentColor.PINK, AppAccentColor.fromId("rose"))
        assertEquals(AppAccentColor.FILC, AppAccentColor.fromId("nincs-ilyen"))
        assertEquals(AppAccentColor.FILC, AppAccentColor.fromId("filc"))
    }

    @Test
    fun filcPaletteMatchesReferenceTones() {
        val light = FilcPalette.of(FilcLime, dark = false)
        val dark = FilcPalette.of(FilcLime, dark = true)

        // A reFilc értékei: #FAFFF0 háttér, #F3FBDE felület, #0D1202 / #141905 sötétben.
        assertEquals(0xFFFAFFF0.toInt(), light.background.toArgb())
        assertEquals(0xFFF3FBDE.toInt(), light.surface.toArgb())
        assertEquals(0xFF0D1202.toInt(), dark.background.toArgb())
        assertEquals(0xFF141905.toInt(), dark.surface.toArgb())

        assertTrue(light.isLight)
        assertFalse(dark.isLight)
        // A jegyszínek mindkét módban azonosak (a reFilc is így csinálja).
        assertEquals(dark.gradeFive, light.gradeFive)
        assertNotEquals(light.text, dark.text)
    }
}
