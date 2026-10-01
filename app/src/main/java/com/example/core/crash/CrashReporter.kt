package com.example.core.crash

import android.content.Context
import android.util.Log
import java.io.File
import java.util.Date

/**
 * Kezeletlen kivételek rögzítése: a stack trace fájlba íródik, és a következő
 * indításkor az app párbeszédablakban megjeleníti (innen be lehet másolni a
 * hibajelentéshez).
 */
object CrashReporter {

    private const val FILE_NAME = "last_crash.log"
    private const val TAG = "CrashReporter"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(appContext.filesDir, FILE_NAME)
                file.writeText(
                    buildString {
                        appendLine("Időpont: ${Date()}")
                        appendLine("Verzió: ${appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName}")
                        appendLine("Szál: ${thread.name}")
                        appendLine()
                        appendLine(maskSensitiveData(Log.getStackTraceString(throwable)))
                    }
                )
            } catch (e: Exception) {
                // Semmi esetre se nyeljük el az eredeti kivételt a rögzítés miatt
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Maszkolja a potenciálisan érzékeny adatokat (Bearer tokenek, jelszavak, sütik, Neptun kódok).
     */
    fun maskSensitiveData(raw: String): String {
        var masked = raw
        masked = Regex("(Authorization\\s*:\\s*)[^\\r\\n]+", RegexOption.IGNORE_CASE).replace(masked, "$1[REDACTED]")
        masked = Regex("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", RegexOption.IGNORE_CASE).replace(masked, "Bearer [REDACTED]")
        masked = Regex("([\"']?(?:password|pass|pwd|jelszo)[\"']?\\s*[:=]\\s*[\"']?)[^\"'\\s&,\n]+([\"']?)", RegexOption.IGNORE_CASE).replace(masked, "$1[REDACTED]$2")
        masked = Regex("([\"']?(?:accessToken|refreshToken|token|api_key|apiKey)[\"']?\\s*[:=]\\s*[\"']?)[^\"'\\s&,\n]+([\"']?)", RegexOption.IGNORE_CASE).replace(masked, "$1[REDACTED]$2")
        masked = Regex("([\"']?(?:cookie|sessionId|NeptunSession|ASP\\.NET_SessionId)[\"']?\\s*[:=]\\s*[\"']?)[^\"'\\s&;,\n]+([\"']?)", RegexOption.IGNORE_CASE).replace(masked, "$1[REDACTED]$2")
        masked = Regex("([\"']?(?:user|username|neptunCode|login)[\"']?\\s*[:=]\\s*[\"']?)[A-Za-z0-9]{6}([\"']?)", RegexOption.IGNORE_CASE).replace(masked, "$1[REDACTED]$2")
        return masked
    }

    /** Visszaadja és törli az utolsó rögzített crash naplóját (ha volt). */
    fun consumeLastCrash(context: Context): String? {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return null
            val content = file.readText()
            file.delete()
            content.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }
}
