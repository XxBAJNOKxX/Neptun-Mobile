package com.example.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.NeptunApp
import kotlinx.coroutines.flow.first

/**
 * Helyi adatbázisból újraütemezi az órarendi emlékeztetőket.
 * Boot, app-frissítés vagy időzóna-váltás után fut.
 */
class AlarmRescheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? NeptunApp ?: return Result.success()
            val prefs = app.appContainer.prefsManager
            val notifPrefs = prefs.loadNotificationPreferences()
            if (notifPrefs.notifyClasses) {
                val events = app.appContainer.neptunRepository.getCalendarEvents().first()
                events.filter { it.isActualAttendedClass }
                    .distinctBy { it.id }
                    .forEach { event ->
                        try {
                            app.appContainer.alarmScheduler.scheduleClassAlarm(
                                event,
                                notifPrefs.reminderMinutesBefore
                            )
                        } catch (e: Exception) {
                            // Egyetlen óra ütemezési hibája ne állítsa le a többit
                        }
                    }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "neptun_alarm_reschedule"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<AlarmRescheduleWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
