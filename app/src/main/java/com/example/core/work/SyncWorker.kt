package com.example.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.NeptunApp
import com.example.core.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? NeptunApp ?: return Result.success()
            val prefs = app.appContainer.prefsManager
            val creds = prefs.loadCredentials()
            val notifPrefs = prefs.loadNotificationPreferences()

            if (creds != null && creds.isLoggedIn) {
                // 1. Sync all repository data from network
                app.appContainer.neptunRepository.syncAllData(
                    neptunCode = creds.neptunCode,
                    sessionToken = prefs.getSessionToken()
                )
                // Update last sync timestamp
                prefs.updateLastSyncTime(System.currentTimeMillis())

                // 2. Check for new unread messages and notify if enabled
                if (notifPrefs.notifyMessages) {
                    val messages = app.appContainer.neptunRepository.getMessages().first()
                    val unreadMessages = messages.filter { !it.isRead }
                    if (unreadMessages.isNotEmpty()) {
                        val latest = unreadMessages.first()
                        NotificationHelper.showMessageNotification(
                            context = applicationContext,
                            notificationId = latest.id.hashCode(),
                            sender = latest.sender,
                            subject = latest.subject,
                            preview = latest.previewText
                        )
                    }
                }

                // 3. Check upcoming classes and remind student if enabled
                if (notifPrefs.notifyClasses) {
                    val events = app.appContainer.neptunRepository.getCalendarEvents().first()
                    val reminderMins = notifPrefs.reminderMinutesBefore

                    events.filter { it.isActualAttendedClass }
                        .distinctBy { event ->
                            if (event.dateString.isNotBlank()) {
                                "${event.subjectName.trim()}_${event.dateString.take(10)}_${event.startHour}:${event.startMinute}"
                            } else {
                                "${event.subjectName.trim()}_day${event.dayOfWeek}_${event.startHour}:${event.startMinute}"
                            }
                        }
                        .forEach { event ->
                            try {
                                app.appContainer.alarmScheduler.scheduleClassAlarm(event, reminderMins)
                            } catch (e: Exception) {
                                // Handled if exact alarm permission is restricted
                            }
                        }
                }

                // 4. Check grades if enabled
                if (notifPrefs.notifyGrades) {
                    val grades = app.appContainer.neptunRepository.getSubjectGrades().first()
                    val gradedItems = grades.filter { it.grade != null }
                    if (gradedItems.isNotEmpty()) {
                        val latestGrade = gradedItems.first()
                        // Optional reminder of latest recorded evaluation
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME_PERIODIC = "neptun_periodic_sync"
        private const val WORK_NAME_ONE_TIME = "neptun_immediate_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }
}
