package com.example.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.model.CalendarEvent
import java.time.LocalDate
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "alarm_scheduler_prefs"
        const val KEY_SCHEDULED_ALARM_IDS = "scheduled_alarm_ids"
    }

    private fun getStoredAlarmIds(): Set<Int> {
        val raw = prefs.getStringSet(KEY_SCHEDULED_ALARM_IDS, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toSet()
    }

    internal fun getScheduledAlarmIds(): Set<Int> = getStoredAlarmIds()

    private fun addStoredAlarmId(id: Int) {
        val current = prefs.getStringSet(KEY_SCHEDULED_ALARM_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(id.toString())
        prefs.edit().putStringSet(KEY_SCHEDULED_ALARM_IDS, current).apply()
    }

    private fun removeStoredAlarmId(id: Int) {
        val current = prefs.getStringSet(KEY_SCHEDULED_ALARM_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(id.toString())
        prefs.edit().putStringSet(KEY_SCHEDULED_ALARM_IDS, current).apply()
    }

    private fun clearStoredAlarmIds() {
        prefs.edit().remove(KEY_SCHEDULED_ALARM_IDS).apply()
    }

    fun scheduleClassAlarm(event: CalendarEvent, reminderMinutesBefore: Int = 15) {
        if (!event.isActualAttendedClass) return
        if (event.startHour == 0 && event.startMinute == 0) return

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        if (event.dateString.isNotBlank()) {
            try {
                val date = LocalDate.parse(event.dateString.take(10))
                calendar.set(Calendar.YEAR, date.year)
                calendar.set(Calendar.MONTH, date.monthValue - 1)
                calendar.set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
                calendar.set(Calendar.HOUR_OF_DAY, event.startHour)
                calendar.set(Calendar.MINUTE, event.startMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            } catch (e: Exception) {
                setCalendarByDayOfWeek(calendar, event)
            }
        } else {
            setCalendarByDayOfWeek(calendar, event)
        }

        // Subtract reminder minutes
        calendar.add(Calendar.MINUTE, -reminderMinutesBefore)

        val triggerTime = calendar.timeInMillis

        // Do not schedule alarms that have already passed
        if (triggerTime <= now) {
            return
        }

        val uniqueKey = generateClassKey(event)
        val alarmId = uniqueKey.hashCode()

        val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra(ClassAlarmReceiver.EXTRA_SUBJECT_NAME, event.subjectName)
            putExtra(ClassAlarmReceiver.EXTRA_ROOM, event.room)
            putExtra(ClassAlarmReceiver.EXTRA_START_TIME, "%02d:%02d".format(event.startHour, event.startMinute))
            putExtra(ClassAlarmReceiver.EXTRA_COURSE_TYPE, event.courseType.displayName)
            putExtra(ClassAlarmReceiver.EXTRA_NOTIFICATION_ID, alarmId)
            putExtra(ClassAlarmReceiver.EXTRA_MINUTES_BEFORE, reminderMinutesBefore)
            putExtra(ClassAlarmReceiver.EXTRA_DATE_STRING, event.dateString)
            putExtra(ClassAlarmReceiver.EXTRA_DAY_OF_WEEK, event.dayOfWeek)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            addStoredAlarmId(alarmId)
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            addStoredAlarmId(alarmId)
        }
    }

    fun cancelClassAlarm(event: CalendarEvent) {
        val uniqueKey = generateClassKey(event)
        val alarmId = uniqueKey.hashCode()
        val intent = Intent(context, ClassAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        removeStoredAlarmId(alarmId)
    }

    fun cancelAllClassAlarms() {
        val ids = getStoredAlarmIds()
        for (alarmId in ids) {
            val intent = Intent(context, ClassAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        clearStoredAlarmIds()
    }

    private fun generateClassKey(event: CalendarEvent): String {
        // A tantárgykód + kurzuskód + terem bevonásával csökkentjük a hashCode ütközések esélyét.
        return if (event.dateString.isNotBlank()) {
            "${event.subjectName.trim()}_${event.subjectCode.trim()}_${event.courseCode.trim()}_${event.dateString.take(10)}_${event.startHour}:${event.startMinute}"
        } else {
            "${event.subjectName.trim()}_${event.subjectCode.trim()}_${event.courseCode.trim()}_day${event.dayOfWeek}_${event.startHour}:${event.startMinute}"
        }
    }

    private fun setCalendarByDayOfWeek(calendar: Calendar, event: CalendarEvent) {
        val calDayOfWeek = when (event.dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }
        calendar.set(Calendar.DAY_OF_WEEK, calDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, event.startHour)
        calendar.set(Calendar.MINUTE, event.startMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
    }
}
