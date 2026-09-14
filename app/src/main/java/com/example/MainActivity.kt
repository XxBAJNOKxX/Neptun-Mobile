package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.AppStringsProvider
import com.example.core.i18n.LocalAppStrings
import com.example.presentation.ui.MainAppContent
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

// FragmentActivity, mert a biometrikus zár (BiometricPrompt) igényli.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NeptunApp
        val prefsManager = app.appContainer.prefsManager

        // Initial locale setup
        val initialLang = prefsManager.loadLanguage()
        updateAppLocale(initialLang.code)

        setContent {
            val themeSettings by prefsManager.themeSettingsFlow.collectAsStateWithLifecycle()
            val currentLanguage by prefsManager.languageFlow.collectAsStateWithLifecycle()
            val appStrings = AppStringsProvider.getForCode(currentLanguage.code)

            LaunchedEffect(currentLanguage.code) {
                updateAppLocale(currentLanguage.code)
            }

            CompositionLocalProvider(LocalAppStrings provides appStrings) {
                MyApplicationTheme(
                    themeMode = themeSettings.themeMode,
                    dynamicColor = themeSettings.useDynamicColor,
                    accentColor = themeSettings.accentColor
                ) {
                    MainAppContent()
                }
            }
        }
    }

    private fun updateAppLocale(languageCode: String) {
        val locale = when (languageCode.lowercase()) {
            "en" -> Locale.ENGLISH
            "de" -> Locale.GERMAN
            else -> Locale("hu", "HU")
        }
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
