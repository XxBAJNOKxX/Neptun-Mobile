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
class RoomMigrationTest {

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
    fun `migration from 2 to 3 adds teacherName and applicationDeadline columns with defaults`() {
        val db = createInMemoryDb()

        // Create version 2 schema for exam_items
        db.execSQL(
            """
            CREATE TABLE exam_items (
                id TEXT PRIMARY KEY NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                courseCode TEXT NOT NULL,
                examDate TEXT NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT NOT NULL,
                room TEXT NOT NULL,
                location TEXT NOT NULL,
                examType TEXT NOT NULL,
                isRegistered INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Insert legacy row without the new columns
        db.execSQL(
            """
            INSERT INTO exam_items (id, subjectName, subjectCode, courseCode, examDate, startTime, endTime, room, location, examType, isRegistered)
            VALUES ('exam_1', 'Matematika I', 'MAT101', '01', '2026-06-15', '09:00', '11:00', 'IB028', 'I épület', 'Kollokvium', 1)
            """.trimIndent()
        )

        // Apply MIGRATION_2_3
        NeptunDatabase.MIGRATION_2_3.migrate(db)

        // Verify that the columns were added and defaulted
        val cursor = db.query("SELECT teacherName, applicationDeadline FROM exam_items WHERE id = 'exam_1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("", cursor.getString(0))
        assertEquals("", cursor.getString(1))
        cursor.close()
        db.close()
    }

    @Test
    fun `migration from 3 to 4 creates composite indices on all entity tables`() {
        val db = createInMemoryDb()

        // Create version 3 tables
        db.execSQL(
            """
            CREATE TABLE calendar_events (
                id TEXT PRIMARY KEY NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                courseCode TEXT NOT NULL,
                dayOfWeek INTEGER NOT NULL,
                startHour INTEGER NOT NULL,
                startMinute INTEGER NOT NULL,
                endHour INTEGER NOT NULL,
                endMinute INTEGER NOT NULL,
                room TEXT NOT NULL,
                location TEXT NOT NULL,
                teacherName TEXT NOT NULL,
                courseType TEXT NOT NULL,
                dateString TEXT NOT NULL,
                isExam INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE subject_grades (
                id TEXT PRIMARY KEY NOT NULL,
                subjectCode TEXT NOT NULL,
                subjectName TEXT NOT NULL,
                grade INTEGER,
                gradeText TEXT NOT NULL,
                credits INTEGER NOT NULL,
                termId TEXT NOT NULL,
                termName TEXT NOT NULL,
                isGhost INTEGER NOT NULL
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
                isRead INTEGER NOT NULL,
                body TEXT NOT NULL
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
                endTime TEXT NOT NULL,
                room TEXT NOT NULL,
                location TEXT NOT NULL,
                examType TEXT NOT NULL,
                isRegistered INTEGER NOT NULL,
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
                amountHuf INTEGER NOT NULL,
                status TEXT NOT NULL,
                dueDate TEXT NOT NULL,
                termName TEXT NOT NULL,
                paymentDate TEXT,
                transactionId TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Apply MIGRATION_3_4
        NeptunDatabase.MIGRATION_3_4.migrate(db)

        // Query created indices
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
}
