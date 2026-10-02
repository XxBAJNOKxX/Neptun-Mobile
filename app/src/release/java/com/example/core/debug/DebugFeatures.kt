package com.example.core.debug

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class DebugLogEntry(
    val timestamp: Long = 0L,
    val tag: String = "",
    val maskedMessage: String = "",
    val isError: Boolean = false
) {
    val formattedTime: String get() = ""
}

/**
 * Release No-Op csonk.
 * A release buildben minden fejlesztői menü, StrictMode és naplózó kód no-op csonk,
 * így az R8 optimalizálás során a kód és a szövegek sem kerülnek bele az éles APK-ba.
 */
object DebugFeatures {
    val isDebug: Boolean = false
    val isDemoAllowed: Boolean = false

    fun init(context: Context) {
        // No-Op in release
    }

    fun maskSensitive(text: String): String = ""

    fun logNetworkSession(tag: String, rawMessage: String, isError: Boolean = false) {
        // No-Op in release
    }

    fun getLogs(): List<DebugLogEntry> = emptyList()

    fun clearLogs() {
        // No-Op in release
    }

    @Composable
    fun DevMenuSection(
        modifier: Modifier = Modifier
    ) {
        // No-Op in release: semmilyen UI elem nem jelenik meg
    }
}
