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
            val prefs = appContainer.prefsManager
            val creds = prefs.loadCredentials()
            if (creds != null && creds.isLoggedIn && (prefs.isSessionKeepAliveEnabled() || creds.sessionKeepAlive)) {
                com.example.core.work.SessionKeepAliveWorker.schedulePeriodicKeepAlive(this)
            } else {
                com.example.core.work.SessionKeepAliveWorker.cancelPeriodicKeepAlive(this)
            }
        } catch (e: Exception) {
            // Handled gracefully in unit test / Robolectric environments
        }
        startForegroundKeepAlive()
        setupLifecycleAndNetworkKeepAliveTriggers()
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
                    val prefs = appContainer.prefsManager
                    val creds = prefs.loadCredentials()
                    if (creds != null && creds.isLoggedIn && (prefs.isSessionKeepAliveEnabled() || creds.sessionKeepAlive)) {
                        appContainer.neptunRepository.keepAliveSession()
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun setupLifecycleAndNetworkKeepAliveTriggers() {
        var lastKeepAliveTrigger = 0L
        val minTriggerIntervalMs = 5 * 60 * 1000L // 5 perc védelmi időszak

        val triggerKeepAliveIfNeeded = {
            val now = System.currentTimeMillis()
            if (now - lastKeepAliveTrigger >= minTriggerIntervalMs) {
                lastKeepAliveTrigger = now
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val prefs = appContainer.prefsManager
                        val creds = prefs.loadCredentials()
                        if (creds != null && creds.isLoggedIn && (prefs.isSessionKeepAliveEnabled() || creds.sessionKeepAlive)) {
                            appContainer.neptunRepository.keepAliveSession()
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        // 1. App előtérbe kerülési trigger (Activity lifecycle)
        var startedActivities = 0
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: android.app.Activity) {
                startedActivities++
                if (startedActivities == 1) {
                    triggerKeepAliveIfNeeded()
                }
            }
            override fun onActivityStopped(activity: android.app.Activity) {
                startedActivities--
            }
            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityResumed(activity: android.app.Activity) {}
            override fun onActivityPaused(activity: android.app.Activity) {}
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: android.app.Activity) {}
        })

        // 2. Hálózat visszatérési trigger (NetworkCallback)
        try {
            val cm = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            cm?.registerDefaultNetworkCallback(object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    triggerKeepAliveIfNeeded()
                }
            })
        } catch (_: Exception) {
            // Nem minden környezetben (pl. mock/teszt) elérhető
        }
    }
}
