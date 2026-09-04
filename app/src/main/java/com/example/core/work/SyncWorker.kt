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
                    // Reschedule exact alarms for upcoming week events
                    events.forEach { event ->
                        try {
                            app.appContainer.alarmScheduler.scheduleClassAlarm(event)
                        } catch (e: Exception) {
                            // Handled if exact alarm permission is restricted
                        }
                    }

                    // Also check if any class is within the reminder window today
                    val cal = Calendar.getInstance()
                    val todayDayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> 1
                        Calendar.TUESDAY -> 2
                        Calendar.WEDNESDAY -> 3
                        Calendar.THURSDAY -> 4
                        Calendar.FRIDAY -> 5
                        Calendar.SATURDAY -> 6
                        else -> 7
                    }
                    val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                    val currentMin = cal.get(Calendar.MINUTE)
                    val currentTimeInMinutes = currentHour * 60 + currentMin

                    val todayEvents = events.filter { it.dayOfWeek == todayDayOfWeek }
                    val upcomingSoon = todayEvents.firstOrNull { event ->
                        val eventStartInMinutes = event.startHour * 60 + event.startMinute
                        val diff = eventStartInMinutes - currentTimeInMinutes
                        diff in 0..notifPrefs.reminderMinutesBefore + 10
                    }

                    if (upcomingSoon != null) {
                        NotificationHelper.showClassReminder(
                            context = applicationContext,
                            notificationId = upcomingSoon.id.hashCode(),
                            subjectName = upcomingSoon.subjectName,
                            room = upcomingSoon.room,
                            startTime = "%02d:%02d".format(upcomingSoon.startHour, upcomingSoon.startMinute),
                            courseType = upcomingSoon.courseType.displayName,
                            minutesBefore = notifPrefs.reminderMinutesBefore
                        )
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
