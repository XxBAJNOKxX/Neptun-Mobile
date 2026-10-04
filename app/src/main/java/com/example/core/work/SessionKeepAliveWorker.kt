package com.example.core.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.NeptunApp
import java.util.concurrent.TimeUnit

class SessionKeepAliveWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? NeptunApp ?: return Result.success()
            val repo = app.appContainer.neptunRepository
            val prefs = app.appContainer.prefsManager
            val creds = prefs.loadCredentials()

            if (creds != null && creds.isLoggedIn) {
                Log.d(TAG, "Executing periodic keep-alive ping...")
                val isAlive = repo.keepAliveSession()
                Log.d(TAG, "Keep-alive ping result: isAlive=$isAlive")
            }
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Keep-alive worker failed: ${e.message}")
            Result.success()
        }
    }

    companion object {
        private const val TAG = "SessionKeepAliveWorker"
        private const val WORK_NAME_PERIODIC = "SessionKeepAliveWorkerPeriodic"

        fun schedulePeriodicKeepAlive(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // 15 minutes is the minimum interval supported by Android WorkManager
            val request = PeriodicWorkRequestBuilder<SessionKeepAliveWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelPeriodicKeepAlive(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cancel keep-alive worker: ${e.message}")
            }
        }
    }
}
