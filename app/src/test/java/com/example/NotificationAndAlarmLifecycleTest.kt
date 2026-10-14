package com.example

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.notification.AlarmScheduler
import com.example.core.notification.ClassAlarmReceiver
import com.example.core.notification.NotificationHelper
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.local.NeptunDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.NeptunRepositoryImpl
import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.domain.model.DegreeProgress
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationAndAlarmLifecycleTest {

    private lateinit var context: Context
    private lateinit var database: NeptunDatabase
    private lateinit var prefsManager: EncryptedPreferencesManager
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val shadowApp = Shadows.shadowOf(context as Application)
        shadowApp.grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

        database = Room.inMemoryDatabaseBuilder(context, NeptunDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        NeptunDatabase.setInstance(database)

        prefsManager = EncryptedPreferencesManager(context)
        prefsManager.clear()

        alarmScheduler = AlarmScheduler(context)
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationHelper.createNotificationChannels(context)
    }

    @After
    fun teardown() {
        alarmScheduler.cancelAllClassAlarms()
        NotificationHelper.cancelAll(context)
        database.close()
        NeptunDatabase.setInstance(null)
    }

    private fun createTestCalendarEvent(id: String, subjectName: String, dayOffset: Long = 2): CalendarEvent {
        val targetDate = LocalDate.now().plusDays(dayOffset)
        return CalendarEvent(
            id = id,
            subjectName = subjectName,
            subjectCode = "SUBJ_$id",
            courseCode = "01",
            location = "Budapest",
            room = "IB028",
            teacherName = "Oktató",
            startHour = 14,
            startMinute = 0,
            endHour = 16,
            endMinute = 0,
            dayOfWeek = targetDate.dayOfWeek.value,
            courseType = CourseType.LECTURE,
            dateString = targetDate.toString()
        )
    }

    @Test
    fun testCancelAllClassAlarmsCancelsScheduledAlarms() {
        val shadowAlarm = Shadows.shadowOf(alarmManager)
        val event1 = createTestCalendarEvent("1", "Szoftvertechnológia", dayOffset = 2)
        val event2 = createTestCalendarEvent("2", "Adatbázisok", dayOffset = 3)

        alarmScheduler.scheduleClassAlarm(event1, 15)
        alarmScheduler.scheduleClassAlarm(event2, 15)

        assertEquals(2, alarmScheduler.getScheduledAlarmIds().size)
        assertTrue(shadowAlarm.scheduledAlarms.isNotEmpty())

        alarmScheduler.cancelAllClassAlarms()

        assertTrue(alarmScheduler.getScheduledAlarmIds().isEmpty())
        assertTrue(shadowAlarm.scheduledAlarms.isEmpty())
    }

    @Test
    fun testClassAlarmReceiverDropsNotificationWhenUserLoggedOut() {
        prefsManager.setLoggedIn(false)

        runBlocking {
            database.calendarDao().insertEvents(
                listOf(
                    CalendarEventEntity(
                        id = "evt_1",
                        subjectName = "Szoftvertechnológia",
                        subjectCode = "SUBJ_1",
                        courseCode = "01",
                        location = "Budapest",
                        room = "IB028",
                        teacherName = "Oktató",
                        startHour = 14,
                        startMinute = 0,
                        endHour = 16,
                        endMinute = 0,
                        dayOfWeek = 4,
                        courseType = "Előadás",
                        dateString = "2026-10-15"
                    )
                )
            )
        }

        val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra(ClassAlarmReceiver.EXTRA_SUBJECT_NAME, "Szoftvertechnológia")
            putExtra(ClassAlarmReceiver.EXTRA_ROOM, "IB028")
            putExtra(ClassAlarmReceiver.EXTRA_START_TIME, "14:00")
            putExtra(ClassAlarmReceiver.EXTRA_COURSE_TYPE, "Előadás")
            putExtra(ClassAlarmReceiver.EXTRA_NOTIFICATION_ID, 1001)
            putExtra(ClassAlarmReceiver.EXTRA_MINUTES_BEFORE, 15)
            putExtra(ClassAlarmReceiver.EXTRA_DATE_STRING, "2026-10-15")
            putExtra(ClassAlarmReceiver.EXTRA_DAY_OF_WEEK, 4)
        }

        ClassAlarmReceiver().onReceive(context, intent)

        val shadowNotif = Shadows.shadowOf(notificationManager)
        assertEquals(0, shadowNotif.allNotifications.size)
    }

    @Test
    fun testClassAlarmReceiverDropsNotificationWhenDatabaseEventsEmpty() {
        prefsManager.saveCredentials(
            neptunCode = "TEST01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu"
        )
        assertTrue(prefsManager.isLoggedIn())

        runBlocking {
            database.calendarDao().clearAll()
        }

        val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra(ClassAlarmReceiver.EXTRA_SUBJECT_NAME, "Szoftvertechnológia")
            putExtra(ClassAlarmReceiver.EXTRA_ROOM, "IB028")
            putExtra(ClassAlarmReceiver.EXTRA_START_TIME, "14:00")
            putExtra(ClassAlarmReceiver.EXTRA_COURSE_TYPE, "Előadás")
            putExtra(ClassAlarmReceiver.EXTRA_NOTIFICATION_ID, 1001)
            putExtra(ClassAlarmReceiver.EXTRA_MINUTES_BEFORE, 15)
            putExtra(ClassAlarmReceiver.EXTRA_DATE_STRING, "2026-10-15")
            putExtra(ClassAlarmReceiver.EXTRA_DAY_OF_WEEK, 4)
        }

        ClassAlarmReceiver().onReceive(context, intent)

        val shadowNotif = Shadows.shadowOf(notificationManager)
        assertEquals(0, shadowNotif.allNotifications.size)
    }

    @Test
    fun testClassAlarmReceiverDropsNotificationWhenSubjectDoesNotMatch() {
        prefsManager.saveCredentials(
            neptunCode = "TEST01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu"
        )

        runBlocking {
            database.calendarDao().insertEvents(
                listOf(
                    CalendarEventEntity(
                        id = "evt_diff",
                        subjectName = "Villamosságtan",
                        subjectCode = "SUBJ_DIFF",
                        courseCode = "01",
                        location = "Budapest",
                        room = "QBF11",
                        teacherName = "Tanár",
                        startHour = 8,
                        startMinute = 15,
                        endHour = 10,
                        endMinute = 0,
                        dayOfWeek = 1,
                        courseType = "Előadás",
                        dateString = "2026-10-15"
                    )
                )
            )
        }

        val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra(ClassAlarmReceiver.EXTRA_SUBJECT_NAME, "Szoftvertechnológia")
            putExtra(ClassAlarmReceiver.EXTRA_ROOM, "IB028")
            putExtra(ClassAlarmReceiver.EXTRA_START_TIME, "14:00")
            putExtra(ClassAlarmReceiver.EXTRA_COURSE_TYPE, "Előadás")
            putExtra(ClassAlarmReceiver.EXTRA_NOTIFICATION_ID, 1001)
            putExtra(ClassAlarmReceiver.EXTRA_MINUTES_BEFORE, 15)
            putExtra(ClassAlarmReceiver.EXTRA_DATE_STRING, "2026-10-15")
            putExtra(ClassAlarmReceiver.EXTRA_DAY_OF_WEEK, 4)
        }

        ClassAlarmReceiver().onReceive(context, intent)

        val shadowNotif = Shadows.shadowOf(notificationManager)
        assertEquals(0, shadowNotif.allNotifications.size)
    }

    @Test
    fun testClassAlarmReceiverShowsNotificationWhenLoggedInAndSubjectMatches() {
        prefsManager.saveCredentials(
            neptunCode = "TEST01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu"
        )

        runBlocking {
            database.calendarDao().insertEvents(
                listOf(
                    CalendarEventEntity(
                        id = "evt_match",
                        subjectName = "Szoftvertechnológia",
                        subjectCode = "SUBJ_1",
                        courseCode = "01",
                        location = "Budapest",
                        room = "IB028",
                        teacherName = "Oktató",
                        startHour = 14,
                        startMinute = 0,
                        endHour = 16,
                        endMinute = 0,
                        dayOfWeek = 4,
                        courseType = "Előadás",
                        dateString = "2026-10-15"
                    )
                )
            )
        }

        val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra(ClassAlarmReceiver.EXTRA_SUBJECT_NAME, "Szoftvertechnológia")
            putExtra(ClassAlarmReceiver.EXTRA_ROOM, "IB028")
            putExtra(ClassAlarmReceiver.EXTRA_START_TIME, "14:00")
            putExtra(ClassAlarmReceiver.EXTRA_COURSE_TYPE, "Előadás")
            putExtra(ClassAlarmReceiver.EXTRA_NOTIFICATION_ID, 1001)
            putExtra(ClassAlarmReceiver.EXTRA_MINUTES_BEFORE, 15)
            putExtra(ClassAlarmReceiver.EXTRA_DATE_STRING, "2026-10-15")
            putExtra(ClassAlarmReceiver.EXTRA_DAY_OF_WEEK, 4)
        }

        ClassAlarmReceiver().onReceive(context, intent)

        val shadowNotif = Shadows.shadowOf(notificationManager)
        assertEquals(1, shadowNotif.allNotifications.size)
        assertNotNull(shadowNotif.getNotification(1001))
    }

    @Test
    fun testClearLocalDataTriggersAlarmAndNotificationCancellation() {
        val event = createTestCalendarEvent("1", "Szoftvertechnológia", dayOffset = 2)
        alarmScheduler.scheduleClassAlarm(event, 15)
        assertTrue(alarmScheduler.getScheduledAlarmIds().isNotEmpty())

        NotificationHelper.showClassReminder(context, 777, "Szoftvertechnológia", "IB028", "14:00", "Előadás")
        val shadowNotif = Shadows.shadowOf(notificationManager)
        assertEquals(1, shadowNotif.allNotifications.size)

        val repository = NeptunRepositoryImpl(database, prefsManager, context = context)
        runBlocking {
            repository.clearLocalData()
        }

        assertTrue(alarmScheduler.getScheduledAlarmIds().isEmpty())
        assertEquals(0, shadowNotif.allNotifications.size)
    }

    @Test
    fun testAuthRepositoryLogoutCancelsAlarmsAndNotificationsAndHandlesClearLocalData() {
        prefsManager.saveCredentials(
            neptunCode = "TEST01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu"
        )
        prefsManager.saveDegreeProgress(
            DegreeProgress(
                completedCredits = 150,
                totalRequiredCredits = 210
            )
        )

        val event = createTestCalendarEvent("1", "Szoftvertechnológia", dayOffset = 2)
        alarmScheduler.scheduleClassAlarm(event, 15)
        NotificationHelper.showClassReminder(context, 888, "Szoftvertechnológia", "IB028", "14:00", "Előadás")

        val shadowNotif = Shadows.shadowOf(notificationManager)
        assertEquals(1, shadowNotif.allNotifications.size)
        assertTrue(alarmScheduler.getScheduledAlarmIds().isNotEmpty())

        val authRepo = AuthRepositoryImpl(context, prefsManager)

        // Logout with clearLocalData = false: alarms & notifications cancelled, credentials set to loggedIn = false, user data retained
        runBlocking {
            authRepo.logout(clearLocalData = false)
        }

        assertTrue(alarmScheduler.getScheduledAlarmIds().isEmpty())
        assertEquals(0, shadowNotif.allNotifications.size)
        assertFalse(prefsManager.isLoggedIn())
        assertEquals("TEST01", prefsManager.getNeptunCode())
        assertNotNull(prefsManager.getDegreeProgress())

        // Re-schedule alarm and notification
        alarmScheduler.scheduleClassAlarm(event, 15)
        NotificationHelper.showClassReminder(context, 888, "Szoftvertechnológia", "IB028", "14:00", "Előadás")
        assertEquals(1, shadowNotif.allNotifications.size)
        assertTrue(alarmScheduler.getScheduledAlarmIds().isNotEmpty())

        // Logout with clearLocalData = true: alarms & notifications cancelled, prefsManager.clear() called
        runBlocking {
            authRepo.logout(clearLocalData = true)
        }

        assertTrue(alarmScheduler.getScheduledAlarmIds().isEmpty())
        assertEquals(0, shadowNotif.allNotifications.size)
        assertFalse(prefsManager.isLoggedIn())
        assertNull(prefsManager.getDegreeProgress())
    }

    @Test
    fun testShowClassReminderIncludesTimetableNavigationExtras() {
        NotificationHelper.showClassReminder(
            context = context,
            notificationId = 555,
            subjectName = "Szoftvertechnológia",
            room = "IB028",
            startTime = "14:00",
            courseType = "Előadás",
            minutesBefore = 15,
            dateString = "2026-10-15",
            dayOfWeek = 4
        )

        val shadowNotif = Shadows.shadowOf(notificationManager)
        assertEquals(1, shadowNotif.allNotifications.size)
        val notif = shadowNotif.getNotification(555)
        assertNotNull(notif)
        val shadowPending = Shadows.shadowOf(notif!!.contentIntent)
        val savedIntent = shadowPending.savedIntent

        assertEquals(NotificationHelper.NAVIGATE_TIMETABLE, savedIntent.getStringExtra(NotificationHelper.EXTRA_NAVIGATE_TO))
        assertEquals("2026-10-15", savedIntent.getStringExtra(NotificationHelper.EXTRA_TARGET_DATE))
        assertEquals(4, savedIntent.getIntExtra(NotificationHelper.EXTRA_TARGET_DAY, 0))
    }
}
