package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.ui.MainAppContent
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NeptunApp
        val prefsManager = app.appContainer.prefsManager

        setContent {
            val themeSettings by prefsManager.themeSettingsFlow.collectAsStateWithLifecycle()
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


