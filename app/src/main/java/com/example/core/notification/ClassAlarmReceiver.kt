package com.example.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ClassAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Óra"
        val room = intent.getStringExtra(EXTRA_ROOM) ?: "-"
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""
        val courseType = intent.getStringExtra(EXTRA_COURSE_TYPE) ?: "Óra"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        NotificationHelper.showClassReminder(
            context = context,
            notificationId = notificationId,
            subjectName = subjectName,
            room = room,
            startTime = startTime,
            courseType = courseType
        )
    }

    companion object {
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_ROOM = "extra_room"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_COURSE_TYPE = "extra_course_type"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
