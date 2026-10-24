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

    private val AUTH_HEADER_REGEX = Regex("(Authorization\\s*:\\s*)[^\\r\\n]+", RegexOption.IGNORE_CASE)
    private val COOKIE_HEADER_REGEX = Regex("((?:Set-)?Cookie\\s*:\\s*)[^\\r\\n]+", RegexOption.IGNORE_CASE)
    private val BEARER_TOKEN_REGEX = Regex("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", RegexOption.IGNORE_CASE)
    private val PASSWORD_FIELD_REGEX = Regex("([\"']?(?:password|pass|pwd|jelszo)[\"']?\\s*[:=]\\s*[\"']?)[^\"'\\s&,\n}]+([\"']?)", RegexOption.IGNORE_CASE)
    private val TOKEN_FIELD_REGEX = Regex("([\"']?(?:accessToken|refreshToken|token|api_key|apiKey)[\"']?\\s*[:=]\\s*[\"']?)[^\"'\\s&,\n}]+([\"']?)", RegexOption.IGNORE_CASE)
    private val TWO_FACTOR_FIELD_REGEX = Regex("([\"']?(?:Key|twoFactorToken|twoFactorLoginToken|twoFactorCode|totpCode|emailCode|codePrefix|verificationToken|GUID)[\"']?\\s*[:=]\\s*[\"']?)[^\"'\\s&,\n}]+([\"']?)", RegexOption.IGNORE_CASE)
    private val SESSION_COOKIE_REGEX = Regex("([\"']?(?:cookie|sessionId|NeptunSession|ASP\\.NET_SessionId|\\.AspNetCore\\.[A-Za-z0-9_.]+|devicecookie)[\"']?\\s*[:=]\\s*[\"']?)[^\"'\\s&;,\n}]+([\"']?)", RegexOption.IGNORE_CASE)
    private val USER_CODE_REGEX = Regex("([\"']?(?:user|username|userLogin|neptunCode|login)[\"']?\\s*[:=]\\s*[\"']?)[A-Za-z0-9]{6}([\"']?)", RegexOption.IGNORE_CASE)

    /**
     * Maszkolja a potenciálisan érzékeny adatokat (Bearer tokenek, jelszavak, sütik, Neptun kódok).
     */
    fun maskSensitiveData(raw: String): String {
        var masked = raw
        masked = AUTH_HEADER_REGEX.replace(masked, "$1[REDACTED]")
        masked = COOKIE_HEADER_REGEX.replace(masked, "$1[REDACTED]")
        masked = BEARER_TOKEN_REGEX.replace(masked, "Bearer [REDACTED]")
        masked = PASSWORD_FIELD_REGEX.replace(masked, "$1[REDACTED]$2")
        masked = TOKEN_FIELD_REGEX.replace(masked, "$1[REDACTED]$2")
        masked = TWO_FACTOR_FIELD_REGEX.replace(masked, "$1[REDACTED]$2")
        masked = SESSION_COOKIE_REGEX.replace(masked, "$1[REDACTED]$2")
        masked = USER_CODE_REGEX.replace(masked, "$1[REDACTED]$2")
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
