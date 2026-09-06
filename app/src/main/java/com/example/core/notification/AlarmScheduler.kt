package com.example.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.model.CalendarEvent
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleClassAlarm(event: CalendarEvent) {
        if (!event.isActualAttendedClass) return
        if (event.startHour == 0 && event.startMinute == 0) return

        val calendar = Calendar.getInstance().apply {
            // Set to event day of week (Monday is 1 in domain, Calendar.MONDAY is 2)
            val calDayOfWeek = when (event.dayOfWeek) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                else -> Calendar.SUNDAY
            }
            set(Calendar.DAY_OF_WEEK, calDayOfWeek)
            set(Calendar.HOUR_OF_DAY, event.startHour)
            set(Calendar.MINUTE, event.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // 15 minutes before
            add(Calendar.MINUTE, -15)

            // If time is in the past for this week, schedule for next week
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra(ClassAlarmReceiver.EXTRA_SUBJECT_NAME, event.subjectName)
            putExtra(ClassAlarmReceiver.EXTRA_ROOM, event.room)
            putExtra(ClassAlarmReceiver.EXTRA_START_TIME, "%02d:%02d".format(event.startHour, event.startMinute))
            putExtra(ClassAlarmReceiver.EXTRA_COURSE_TYPE, event.courseType.displayName)
            putExtra(ClassAlarmReceiver.EXTRA_NOTIFICATION_ID, event.id.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback if SCHEDULE_EXACT_ALARM is not granted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelClassAlarm(event: CalendarEvent) {
        val intent = Intent(context, ClassAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
