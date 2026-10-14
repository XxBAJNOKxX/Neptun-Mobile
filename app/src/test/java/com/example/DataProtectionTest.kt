package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.export.IcsParser
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.local.NeptunDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ExamItemEntity
import com.example.data.local.entity.FinanceItemEntity
import com.example.data.local.entity.NeptunMessageEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.example.data.network.NeptunApiClient
import com.example.data.repository.NeptunRepositoryImpl
import com.example.domain.model.CourseType
import com.example.domain.model.DegreeProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataProtectionTest {

    private lateinit var context: Context
    private lateinit var database: NeptunDatabase
    private lateinit var prefsManager: EncryptedPreferencesManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, NeptunDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        prefsManager = EncryptedPreferencesManager(context)
    }

    @After
    fun teardown() {
        database.close()
    }

    // -------------------------------------------------------------------------
    // 1. IcsParser tests
    // -------------------------------------------------------------------------

    @Test
    fun testIcsParserWithTwoEvents() {
        val sampleIcs = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Neptun Mobile//Orarend Export//HU
            BEGIN:VEVENT
            UID:neptun-evt-101@neptun.mobile
            DTSTART;TZID=Europe/Budapest:20260908T081500
            DTEND;TZID=Europe/Budapest:20260908T100000
            SUMMARY:Előadás: Szoftvertechnológia [BMEVIII01]
            LOCATION:IB028
            DESCRIPTION:Oktató: Dr. Kovács János
            RRULE:FREQ=WEEKLY;BYDAY=TU
            END:VEVENT
            BEGIN:VEVENT
            UID:neptun-evt-102@neptun.mobile
            DTSTART:20260910T140000Z
            DTEND:20260910T160000Z
            SUMMARY:Adatbázisok (Labor)
            LOCATION:IL401
            DESCRIPTION:Oktató: Nagy Béla
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val parsed = IcsParser.parse(sampleIcs)
        assertEquals(2, parsed.size)

        // Event 1 assertions
        val event1 = parsed[0]
        assertEquals("Szoftvertechnológia", event1.subjectName)
        assertEquals("IB028", event1.location)
        assertEquals(8, event1.startHour)
        assertEquals(15, event1.startMinute)
        assertEquals(10, event1.endHour)
        assertEquals(0, event1.endMinute)
        assertEquals(2, event1.dayOfWeek) // Tuesday
        assertEquals(CourseType.LECTURE, event1.courseType)
        assertEquals("2026-09-08", event1.dateString)
        assertEquals("Dr. Kovács János", event1.teacherName)

        // Event 2 assertions
        val event2 = parsed[1]
        assertEquals("Adatbázisok", event2.subjectName)
        assertEquals("IL401", event2.location)
        assertEquals(14, event2.startHour)
        assertEquals(0, event2.startMinute)
        assertEquals(16, event2.endHour)
        assertEquals(0, event2.endMinute)
        assertEquals(4, event2.dayOfWeek) // 2026-09-10 is Thursday (4)
        assertEquals(CourseType.LAB, event2.courseType)
        assertEquals("2026-09-10", event2.dateString)
        assertEquals("Nagy Béla", event2.teacherName)
    }

    @Test
    fun testIcsParserDateOnlyAndExamType() {
        val sampleIcs = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:exam-201
            DTSTART:20261015
            DTEND:20261015
            SUMMARY:Vizsga: Algoritmusok elmélet
            LOCATION:CH.MAX
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val parsed = IcsParser.parse(sampleIcs)
        assertEquals(1, parsed.size)
        val event = parsed[0]
        assertEquals("Algoritmusok elmélet", event.subjectName)
        assertEquals("CH.MAX", event.location)
        assertEquals(0, event.startHour)
        assertEquals(0, event.startMinute)
        assertEquals(CourseType.EXAM, event.courseType)
        assertEquals(4, event.dayOfWeek) // 2026-10-15 is Thursday (4)
        assertEquals("2026-10-15", event.dateString)
    }

    // -------------------------------------------------------------------------
    // 2. DAOs empty protection tests
    // -------------------------------------------------------------------------

    @Test
    fun testCalendarDaoEmptyProtection() = runBlocking {
        val dao = database.calendarDao()
        val initial = CalendarEventEntity(
            id = "evt_1",
            subjectName = "Fizika 1",
            subjectCode = "BMEFIZ01",
            courseCode = "01",
            location = "F épület",
            room = "F212",
            teacherName = "Prof. Teszt",
            startHour = 8,
            startMinute = 15,
            endHour = 10,
            endMinute = 0,
            dayOfWeek = 1,
            courseType = "LECTURE",
            dateString = "2026-09-07"
        )
        dao.replaceEvents(listOf(initial))
        assertEquals(1, dao.getAllEvents().first().size)

        // Empty list must not clear the table
        dao.replaceEvents(emptyList())
        val retained = dao.getAllEvents().first()
        assertEquals(1, retained.size)
        assertEquals("Fizika 1", retained[0].subjectName)
    }

    @Test
    fun testGradesDaoEmptyProtection() = runBlocking {
        val dao = database.gradesDao()
        val initial = SubjectGradeEntity(
            id = "grade_1",
            termId = "2025/26/1",
            termName = "2025/26/1",
            subjectName = "Matematika A1",
            subjectCode = "BMEMAT01",
            credit = 6,
            grade = 5,
            gradeText = "Jeles (5)",
            isSigned = true
        )
        dao.replaceGrades(listOf(initial))
        assertEquals(1, dao.getAllGrades().first().size)

        // Empty list must not clear the table
        dao.replaceGrades(emptyList())
        val retained = dao.getAllGrades().first()
        assertEquals(1, retained.size)
        assertEquals("Matematika A1", retained[0].subjectName)
    }

    @Test
    fun testMessagesDaoEmptyProtection() = runBlocking {
        val dao = database.messagesDao()
        val initial = NeptunMessageEntity(
            id = "msg_1",
            subject = "Tájékoztatás",
            sender = "Dékáni Hivatal",
            sendDate = "2026-09-01 10:00",
            previewText = "Fontos információ",
            bodyHtml = "<p>Fontos információ</p>",
            isRead = false,
            isOfficial = true
        )
        dao.replaceMessages(listOf(initial))
        assertEquals(1, dao.getAllMessages().first().size)

        // Empty list must not clear the table
        dao.replaceMessages(emptyList())
        val retained = dao.getAllMessages().first()
        assertEquals(1, retained.size)
        assertEquals("Tájékoztatás", retained[0].subject)
    }

    @Test
    fun testFinancesDaoEmptyProtection() = runBlocking {
        val dao = database.financesDao()
        val initial = FinanceItemEntity(
            id = "fin_1",
            title = "Költségtérítés",
            termName = "2025/26/1",
            amountHuf = 150000,
            status = "PENDING",
            dueDate = "2026-10-15",
            paymentDate = null,
            transactionId = "tx_123"
        )
        dao.replaceFinances(listOf(initial))
        assertEquals(1, dao.getAllFinances().first().size)

        // Empty list must not clear the table
        dao.replaceFinances(emptyList())
        val retained = dao.getAllFinances().first()
        assertEquals(1, retained.size)
        assertEquals(150000, retained[0].amountHuf)
    }

    @Test
    fun testExamsDaoEmptyProtection() = runBlocking {
        val dao = database.examsDao()
        val initial = ExamItemEntity(
            id = "exam_1",
            subjectName = "Adatbázisok",
            subjectCode = "BMEAUT02",
            courseCode = "01",
            examDate = "2026-12-18",
            startTime = "09:00",
            room = "IB028",
            location = "I épület",
            examType = "Kollokvium",
            isSignedUp = true
        )
        dao.replaceExams(listOf(initial))
        assertEquals(1, dao.getAllExams().first().size)

        // Empty list must not clear the table
        dao.replaceExams(emptyList())
        val retained = dao.getAllExams().first()
        assertEquals(1, retained.size)
        assertEquals("Adatbázisok", retained[0].subjectName)
    }

    // -------------------------------------------------------------------------
    // 3. Degree progress protection tests
    // -------------------------------------------------------------------------

    @Test
    fun testDegreeProgressPreservedWhenGradesEmpty() = runBlocking {
        // Set target credits to 180 and pre-populate saved progress with 60 completed credits
        prefsManager.setTargetCredits(180)
        val savedProgress = DegreeProgress(
            completedCredits = 60,
            totalRequiredCredits = 180,
            cumulativeWeightedAverage = 4.25,
            cumulativeCreditIndex = 4.0
        )
        prefsManager.saveDegreeProgress(savedProgress)

        // Save valid non-demo credentials so refreshDegreeProgress attempts logic
        prefsManager.saveCredentials(
            neptunCode = "TEST01",
            password = "testpassword",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu",
            trainingProgram = "Mérnökinformatikus BSc"
        )

        // Create repository with empty DB (grades table is empty)
        val repository = NeptunRepositoryImpl(database, prefsManager, NeptunApiClient())

        // Refresh degree progress - local grades are empty, API progress returns null
        repository.refreshDegreeProgress()

        // Cached degree progress must NOT be overwritten with 0 credits
        val current = prefsManager.getDegreeProgress()
        assertNotNull(current)
        assertEquals(60, current?.completedCredits)
        assertEquals(180, current?.totalRequiredCredits)
    }
}
