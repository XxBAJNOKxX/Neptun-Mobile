package com.example

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NeptunDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    private fun createInMemoryDb(): SupportSQLiteDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {}
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    @Test
    fun `migration metadata matches expected start and end versions`() {
        assertEquals(1, NeptunDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, NeptunDatabase.MIGRATION_1_2.endVersion)
        assertEquals(2, NeptunDatabase.MIGRATION_2_3.startVersion)
        assertEquals(3, NeptunDatabase.MIGRATION_2_3.endVersion)
        assertEquals(3, NeptunDatabase.MIGRATION_3_4.startVersion)
        assertEquals(4, NeptunDatabase.MIGRATION_3_4.endVersion)
    }

    @Test
    fun `migration 1 to 2 creates exam_items table with expected columns and primary key`() {
        val db = createInMemoryDb()

        // Apply MIGRATION_1_2 on version 1 database
        NeptunDatabase.MIGRATION_1_2.migrate(db)

        // Verify table exists by inserting a record
        db.execSQL(
            """
            INSERT INTO exam_items (id, subjectName, subjectCode, courseCode, examDate, startTime, room, location, examType, isSignedUp)
            VALUES ('exam_1', 'Fizika I', 'FIZ101', '01', '2026-06-20', '08:00', 'IB028', 'I épület', 'Kollokvium', 1)
            """.trimIndent()
        )

        val cursor = db.query("SELECT id, subjectName, subjectCode, courseCode, examDate, startTime, room, location, examType, isSignedUp FROM exam_items WHERE id = 'exam_1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("exam_1", cursor.getString(0))
        assertEquals("Fizika I", cursor.getString(1))
        assertEquals("FIZ101", cursor.getString(2))
        assertEquals("01", cursor.getString(3))
        assertEquals("2026-06-20", cursor.getString(4))
        assertEquals("08:00", cursor.getString(5))
        assertEquals("IB028", cursor.getString(6))
        assertEquals("I épület", cursor.getString(7))
        assertEquals("Kollokvium", cursor.getString(8))
        assertEquals(1, cursor.getInt(9))
        cursor.close()

        // Verify column details from pragma table_info
        val infoCursor = db.query("PRAGMA table_info(exam_items)")
        val columnNames = mutableListOf<String>()
        var pkColumn = ""
        while (infoCursor.moveToNext()) {
            val name = infoCursor.getString(1)
            val isPk = infoCursor.getInt(5) == 1
            columnNames.add(name)
            if (isPk) pkColumn = name
        }
        infoCursor.close()

        val expectedColumns = listOf(
            "id", "subjectName", "subjectCode", "courseCode",
            "examDate", "startTime", "room", "location", "examType", "isSignedUp"
        )
        for (col in expectedColumns) {
            assertTrue("Expected column $col to exist in exam_items", columnNames.contains(col))
        }
        assertEquals("id", pkColumn)

        db.close()
    }

    @Test
    fun `migration 2 to 3 adds teacherName and applicationDeadline columns with default values`() {
        val db = createInMemoryDb()

        // Create version 2 schema using MIGRATION_1_2
        NeptunDatabase.MIGRATION_1_2.migrate(db)

        // Insert legacy row before migration 2->3
        db.execSQL(
            """
            INSERT INTO exam_items (id, subjectName, subjectCode, courseCode, examDate, startTime, room, location, examType, isSignedUp)
            VALUES ('exam_2', 'Analízis I', 'MAT101', '01', '2026-06-15', '09:00', 'IB025', 'I épület', 'Kollokvium', 1)
            """.trimIndent()
        )

        // Apply MIGRATION_2_3
        NeptunDatabase.MIGRATION_2_3.migrate(db)

        // Verify that existing row has default empty strings
        val cursor = db.query("SELECT teacherName, applicationDeadline FROM exam_items WHERE id = 'exam_2'")
        assertTrue(cursor.moveToFirst())
        assertEquals("", cursor.getString(0))
        assertEquals("", cursor.getString(1))
        cursor.close()

        // Verify we can insert new row with both fields populated
        db.execSQL(
            """
            INSERT INTO exam_items (id, subjectName, subjectCode, courseCode, examDate, startTime, room, location, examType, isSignedUp, teacherName, applicationDeadline)
            VALUES ('exam_3', 'Adatbázisok', 'DAT101', '02', '2026-06-22', '14:00', 'IB027', 'I épület', 'Gyakorlati', 0, 'Dr. Kovács', '2026-06-21 12:00')
            """.trimIndent()
        )
        val cursor2 = db.query("SELECT teacherName, applicationDeadline FROM exam_items WHERE id = 'exam_3'")
        assertTrue(cursor2.moveToFirst())
        assertEquals("Dr. Kovács", cursor2.getString(0))
        assertEquals("2026-06-21 12:00", cursor2.getString(1))
        cursor2.close()

        db.close()
    }

    @Test
    fun `migration 3 to 4 creates composite and single-column indices on entity tables`() {
        val db = createInMemoryDb()

        // Create version 3 tables
        db.execSQL(
            """
            CREATE TABLE calendar_events (
                id TEXT PRIMARY KEY NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                courseCode TEXT NOT NULL,
                location TEXT NOT NULL,
                room TEXT NOT NULL,
                teacherName TEXT NOT NULL,
                startHour INTEGER NOT NULL,
                startMinute INTEGER NOT NULL,
                endHour INTEGER NOT NULL,
                endMinute INTEGER NOT NULL,
                dayOfWeek INTEGER NOT NULL,
                courseType TEXT NOT NULL,
                dateString TEXT NOT NULL,
                weekType INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE subject_grades (
                id TEXT PRIMARY KEY NOT NULL,
                termId TEXT NOT NULL,
                termName TEXT NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                credit INTEGER NOT NULL,
                grade INTEGER,
                gradeText TEXT NOT NULL,
                isSigned INTEGER NOT NULL,
                ghostGrade INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE neptun_messages (
                id TEXT PRIMARY KEY NOT NULL,
                subject TEXT NOT NULL,
                sender TEXT NOT NULL,
                sendDate TEXT NOT NULL,
                previewText TEXT NOT NULL,
                bodyHtml TEXT NOT NULL,
                isRead INTEGER NOT NULL,
                isOfficial INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE exam_items (
                id TEXT PRIMARY KEY NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                courseCode TEXT NOT NULL,
                examDate TEXT NOT NULL,
                startTime TEXT NOT NULL,
                room TEXT NOT NULL,
                location TEXT NOT NULL,
                examType TEXT NOT NULL,
                isSignedUp INTEGER NOT NULL,
                teacherName TEXT NOT NULL,
                applicationDeadline TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE finance_items (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                termName TEXT NOT NULL,
                amountHuf INTEGER NOT NULL,
                status TEXT NOT NULL,
                dueDate TEXT NOT NULL,
                paymentDate TEXT,
                transactionId TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Apply MIGRATION_3_4
        NeptunDatabase.MIGRATION_3_4.migrate(db)

        val cursor = db.query("SELECT name FROM sqlite_master WHERE type = 'index'")
        val indexNames = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            indexNames.add(cursor.getString(0))
        }
        cursor.close()

        val expectedIndices = listOf(
            "index_calendar_events_dayOfWeek_startHour_startMinute",
            "index_calendar_events_dateString",
            "index_subject_grades_termId",
            "index_subject_grades_subjectCode",
            "index_neptun_messages_sendDate",
            "index_neptun_messages_isRead",
            "index_exam_items_examDate_startTime",
            "index_finance_items_dueDate"
        )

        for (expected in expectedIndices) {
            assertTrue("Expected index '$expected' to be created", indexNames.contains(expected))
        }

        db.close()
    }

    @Test
    fun `sequential migration from 1 to 4 completes successfully with data intact`() {
        val db = createInMemoryDb()

        // 1. Initial version 1 setup: create base tables (without exam_items)
        db.execSQL(
            """
            CREATE TABLE calendar_events (
                id TEXT PRIMARY KEY NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                courseCode TEXT NOT NULL,
                location TEXT NOT NULL,
                room TEXT NOT NULL,
                teacherName TEXT NOT NULL,
                startHour INTEGER NOT NULL,
                startMinute INTEGER NOT NULL,
                endHour INTEGER NOT NULL,
                endMinute INTEGER NOT NULL,
                dayOfWeek INTEGER NOT NULL,
                courseType TEXT NOT NULL,
                dateString TEXT NOT NULL,
                weekType INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE subject_grades (
                id TEXT PRIMARY KEY NOT NULL,
                termId TEXT NOT NULL,
                termName TEXT NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                credit INTEGER NOT NULL,
                grade INTEGER,
                gradeText TEXT NOT NULL,
                isSigned INTEGER NOT NULL,
                ghostGrade INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE neptun_messages (
                id TEXT PRIMARY KEY NOT NULL,
                subject TEXT NOT NULL,
                sender TEXT NOT NULL,
                sendDate TEXT NOT NULL,
                previewText TEXT NOT NULL,
                bodyHtml TEXT NOT NULL,
                isRead INTEGER NOT NULL,
                isOfficial INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE finance_items (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                termName TEXT NOT NULL,
                amountHuf INTEGER NOT NULL,
                status TEXT NOT NULL,
                dueDate TEXT NOT NULL,
                paymentDate TEXT,
                transactionId TEXT NOT NULL
            )
            """.trimIndent()
        )

        // 2. Run MIGRATION_1_2
        NeptunDatabase.MIGRATION_1_2.migrate(db)
        db.execSQL(
            """
            INSERT INTO exam_items (id, subjectName, subjectCode, courseCode, examDate, startTime, room, location, examType, isSignedUp)
            VALUES ('exam_chain', 'Programozás Alapjai', 'PRG101', '01', '2026-06-18', '11:00', 'IB026', 'I épület', 'Kollokvium', 1)
            """.trimIndent()
        )

        // 3. Run MIGRATION_2_3
        NeptunDatabase.MIGRATION_2_3.migrate(db)
        val cursorV3 = db.query("SELECT teacherName, applicationDeadline FROM exam_items WHERE id = 'exam_chain'")
        assertTrue(cursorV3.moveToFirst())
        assertEquals("", cursorV3.getString(0))
        assertEquals("", cursorV3.getString(1))
        cursorV3.close()

        // 4. Run MIGRATION_3_4
        NeptunDatabase.MIGRATION_3_4.migrate(db)

        // Verify all 8 indices exist
        val indexCursor = db.query("SELECT name FROM sqlite_master WHERE type = 'index'")
        val indexNames = mutableSetOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()

        val expectedIndices = listOf(
            "index_calendar_events_dayOfWeek_startHour_startMinute",
            "index_calendar_events_dateString",
            "index_subject_grades_termId",
            "index_subject_grades_subjectCode",
            "index_neptun_messages_sendDate",
            "index_neptun_messages_isRead",
            "index_exam_items_examDate_startTime",
            "index_finance_items_dueDate"
        )
        for (expected in expectedIndices) {
            assertTrue("Expected index '$expected' to be present after full migration", indexNames.contains(expected))
        }

        // Verify existing data is preserved
        val examCursor = db.query("SELECT subjectName, isSignedUp, teacherName FROM exam_items WHERE id = 'exam_chain'")
        assertTrue(examCursor.moveToFirst())
        assertEquals("Programozás Alapjai", examCursor.getString(0))
        assertEquals(1, examCursor.getInt(1))
        assertEquals("", examCursor.getString(2))
        examCursor.close()

        db.close()
    }
}
