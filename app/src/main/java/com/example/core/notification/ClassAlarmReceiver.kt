package com.example.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.local.NeptunDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ClassAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefsManager = EncryptedPreferencesManager(context)
        if (!prefsManager.isLoggedIn() || prefsManager.getNeptunCode().isBlank()) {
            return
        }

        val db = NeptunDatabase.getInstance(context)
        val allEvents = runBlocking {
            try {
                db.calendarDao().getAllEvents().first()
            } catch (e: Exception) {
                emptyList()
            }
        }
        if (allEvents.isEmpty()) {
            return
        }

        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Óra"
        val matches = allEvents.any { it.subjectName.equals(subjectName, ignoreCase = true) }
        if (!matches) {
            return
        }

        val room = intent.getStringExtra(EXTRA_ROOM) ?: "-"
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""
        val courseType = intent.getStringExtra(EXTRA_COURSE_TYPE) ?: "Óra"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 15)
        val dateString = intent.getStringExtra(EXTRA_DATE_STRING) ?: ""
        val dayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, 0)

        NotificationHelper.showClassReminder(
            context = context,
            notificationId = notificationId,
            subjectName = subjectName,
            room = room,
            startTime = startTime,
            courseType = courseType,
            minutesBefore = minutesBefore,
            dateString = dateString,
            dayOfWeek = dayOfWeek
        )
    }

    companion object {
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_ROOM = "extra_room"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_COURSE_TYPE = "extra_course_type"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        const val EXTRA_DATE_STRING = "extra_date_string"
        const val EXTRA_DAY_OF_WEEK = "extra_day_of_week"
    }
}
