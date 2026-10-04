package com.example

import android.app.Application
import com.example.core.crash.CrashReporter
import com.example.core.di.AppContainer
import com.example.core.di.DefaultAppContainer
import com.example.core.notification.NotificationHelper
import com.example.core.work.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NeptunApp : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        com.example.core.debug.DebugFeatures.init(this)
        appContainer = DefaultAppContainer(this)
        NotificationHelper.createNotificationChannels(this)
        try {
            SyncWorker.schedulePeriodicSync(this)
            com.example.core.work.SessionKeepAliveWorker.schedulePeriodicKeepAlive(this)
        } catch (e: Exception) {
            // Handled gracefully in unit test / Robolectric environments
        }
        startForegroundKeepAlive()
        try {
            com.example.core.update.AppUpdateManager.checkAndCleanUpdateCacheOnNewVersion(this)
        } catch (_: Exception) {
        }
    }

    private fun startForegroundKeepAlive() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            while (isActive) {
                delay(10 * 60 * 1000L) // 10 minutes
                try {
                    val creds = appContainer.prefsManager.loadCredentials()
                    if (creds != null && creds.isLoggedIn) {
                        appContainer.neptunRepository.keepAliveSession()
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}
