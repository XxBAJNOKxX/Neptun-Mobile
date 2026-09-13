package com.example.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.R
import com.example.domain.model.CourseType

class ClassAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME)
            ?: context.getString(R.string.notif_class_default)
        val room = intent.getStringExtra(EXTRA_ROOM) ?: "-"
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""
        // Az óratípus nevét az értesítés pillanatában fordítjuk: nyelvválasztás után is helyes.
        val courseType = runCatching {
            intent.getStringExtra(EXTRA_COURSE_TYPE)?.let { CourseType.valueOf(it) }
        }.getOrNull()?.let { context.getString(it.labelRes) }
            ?: context.getString(R.string.notif_class_default)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 15)

        NotificationHelper.showClassReminder(
            context = context,
            notificationId = notificationId,
            subjectName = subjectName,
            room = room,
            startTime = startTime,
            courseType = courseType,
            minutesBefore = minutesBefore
        )
    }

    companion object {
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_ROOM = "extra_room"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_COURSE_TYPE = "extra_course_type"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
    }
}
