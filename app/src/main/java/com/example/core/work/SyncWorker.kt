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
import androidx.glance.appwidget.updateAll
import com.example.NeptunApp
import com.example.core.notification.NotificationHelper
import com.example.core.notification.NotifiedItemsTracker
import com.example.core.security.EncryptedPreferencesManager
import com.example.core.widget.TodayWidget
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

                val tracker = NotifiedItemsTracker(prefs)
                val quiet = notifPrefs.isQuietNow(currentMinuteOfDay())

                // 2. Új, olvasatlan üzenetekről értesítés (csak egyszer minden üzenetről)
                if (notifPrefs.notifyMessages && !quiet) {
                    notifyNewMessages(app, tracker)
                }

                // 3. Órarendi emlékeztetők ütemezése
                if (notifPrefs.notifyClasses) {
                    scheduleClassAlarms(app, notifPrefs)
                }

                // 4. Új jegyekről értesítés
                if (notifPrefs.notifyGrades && !quiet) {
                    notifyNewGrades(app, tracker)
                }

                // 5. Befizetendő pénzügyi tételekről értesítés
                if (notifPrefs.notifyFinances && !quiet) {
                    notifyPendingFinances(app, tracker)
                }

                // 6. Kezdőképernyő-widget frissítése
                try {
                    TodayWidget().updateAll(applicationContext)
                } catch (e: Exception) {
                    // A widget frissítés nem kritikus
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun currentMinuteOfDay(): Int {
        val now = Calendar.getInstance()
        return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    }

    private suspend fun notifyNewMessages(app: NeptunApp, tracker: NotifiedItemsTracker) {
        val messages = app.appContainer.neptunRepository.getMessages().first()
        val unread = messages.filter { !it.isRead }
        if (unread.isEmpty()) return

        val newOnes = tracker.filterNewItems(KEY_MESSAGES, unread) { it.id }
        if (newOnes.isEmpty()) return

        if (newOnes.size == 1) {
            val msg = newOnes.first()
            NotificationHelper.showMessageNotification(
                context = applicationContext,
                notificationId = msg.id.hashCode(),
                sender = msg.sender,
                subject = msg.subject,
                preview = msg.previewText
            )
        } else {
            NotificationHelper.showSummaryNotification(
                context = applicationContext,
                channelId = NotificationHelper.CHANNEL_ID_MESSAGES,
                notificationId = NOTIFICATION_ID_MESSAGES_SUMMARY,
                title = "${newOnes.size} új üzenet",
                text = newOnes.take(4).joinToString("\n") { "• ${it.sender}: ${it.subject}" }
            )
        }
        tracker.markNotified(KEY_MESSAGES, newOnes.map { it.id })
    }

    private suspend fun notifyNewGrades(app: NeptunApp, tracker: NotifiedItemsTracker) {
        val grades = app.appContainer.neptunRepository.getSubjectGrades().first()
        val graded = grades.filter { it.grade != null }
        if (graded.isEmpty()) return

        val newOnes = tracker.filterNewItems(KEY_GRADES, graded) { it.id }
        if (newOnes.isEmpty()) return

        if (newOnes.size == 1) {
            val grade = newOnes.first()
            NotificationHelper.showGradeNotification(
                context = applicationContext,
                notificationId = grade.id.hashCode(),
                subjectName = grade.subjectName,
                grade = grade.grade,
                gradeText = grade.gradeText,
                credit = grade.credit
            )
        } else {
            NotificationHelper.showSummaryNotification(
                context = applicationContext,
                channelId = NotificationHelper.CHANNEL_ID_GRADES,
                notificationId = NOTIFICATION_ID_GRADES_SUMMARY,
                title = "${newOnes.size} új érdemjegy",
                text = newOnes.take(4).joinToString("\n") { "• ${it.subjectName}: ${it.gradeText}" },
                priorityHigh = true
            )
        }
        tracker.markNotified(KEY_GRADES, newOnes.map { it.id })
    }

    private suspend fun notifyPendingFinances(app: NeptunApp, tracker: NotifiedItemsTracker) {
        val finances = app.appContainer.neptunRepository.getFinances().first()
        val pending = finances.filter {
            it.status == com.example.domain.model.FinanceStatus.PENDING ||
                it.status == com.example.domain.model.FinanceStatus.OVERDUE
        }
        if (pending.isEmpty()) return

        val newOnes = tracker.filterNewItems(KEY_FINANCES, pending) { it.id }
        if (newOnes.isEmpty()) return

        if (newOnes.size == 1) {
            val item = newOnes.first()
            NotificationHelper.showFinanceNotification(
                context = applicationContext,
                notificationId = item.id.hashCode(),
                title = item.title,
                amount = item.amountHuf.toString(),
                dueDate = item.dueDate
            )
        } else {
            NotificationHelper.showSummaryNotification(
                context = applicationContext,
                channelId = NotificationHelper.CHANNEL_ID_FINANCES,
                notificationId = NOTIFICATION_ID_FINANCES_SUMMARY,
                title = "${newOnes.size} befizetendő tétel",
                text = newOnes.take(4).joinToString("\n") { "• ${it.title} – ${it.amountHuf} Ft (határidő: ${it.dueDate})" }
            )
        }
        tracker.markNotified(KEY_FINANCES, newOnes.map { it.id })
    }

    private suspend fun scheduleClassAlarms(app: NeptunApp, notifPrefs: com.example.core.security.NotificationPreferences) {
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

    companion object {
        private const val WORK_NAME_PERIODIC = "neptun_periodic_sync"
        private const val WORK_NAME_ONE_TIME = "neptun_immediate_sync"
        private const val KEY_MESSAGES = "messages"
        private const val KEY_GRADES = "grades"
        private const val KEY_FINANCES = "finances"
        private const val NOTIFICATION_ID_MESSAGES_SUMMARY = 900001
        private const val NOTIFICATION_ID_GRADES_SUMMARY = 900002
        private const val NOTIFICATION_ID_FINANCES_SUMMARY = 900003

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
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }
}
