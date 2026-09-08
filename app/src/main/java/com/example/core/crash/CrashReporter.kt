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
                        appendLine(Log.getStackTraceString(throwable))
                    }
                )
            } catch (e: Exception) {
                // Semmi esetre se nyeljük el az eredeti kivételt a rögzítés miatt
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
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
