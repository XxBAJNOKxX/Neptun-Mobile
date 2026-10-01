package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CalendarDao
import com.example.data.local.dao.ExamsDao
import com.example.data.local.dao.FinancesDao
import com.example.data.local.dao.GradesDao
import com.example.data.local.dao.MessagesDao
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ExamItemEntity
import com.example.data.local.entity.FinanceItemEntity
import com.example.data.local.entity.NeptunMessageEntity
import com.example.data.local.entity.SubjectGradeEntity

@Database(
    entities = [
        CalendarEventEntity::class,
        SubjectGradeEntity::class,
        NeptunMessageEntity::class,
        FinanceItemEntity::class,
        ExamItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class NeptunDatabase : RoomDatabase() {

    abstract fun calendarDao(): CalendarDao
    abstract fun gradesDao(): GradesDao
    abstract fun messagesDao(): MessagesDao
    abstract fun financesDao(): FinancesDao
    abstract fun examsDao(): ExamsDao

    companion object {
        @Volatile
        private var INSTANCE: NeptunDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exam_items ADD COLUMN teacherName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exam_items ADD COLUMN applicationDeadline TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calendar_events_dayOfWeek_startHour_startMinute ON calendar_events (dayOfWeek, startHour, startMinute)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calendar_events_dateString ON calendar_events (dateString)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subject_grades_termId ON subject_grades (termId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subject_grades_subjectCode ON subject_grades (subjectCode)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_neptun_messages_sendDate ON neptun_messages (sendDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_neptun_messages_isRead ON neptun_messages (isRead)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exam_items_examDate_startTime ON exam_items (examDate, startTime)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_finance_items_dueDate ON finance_items (dueDate)")
            }
        }

        fun getInstance(context: Context): NeptunDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NeptunDatabase::class.java,
                    "neptun_mobile.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
