package com.example

import android.app.Application
import com.example.core.di.AppContainer
import com.example.core.di.DefaultAppContainer
import com.example.core.notification.NotificationHelper
import com.example.core.work.SyncWorker

class NeptunApp : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
        NotificationHelper.createNotificationChannels(this)
        try {
            SyncWorker.schedulePeriodicSync(this)
        } catch (e: Exception) {
            // Handled gracefully in unit test / Robolectric environments
        }
    }
}
