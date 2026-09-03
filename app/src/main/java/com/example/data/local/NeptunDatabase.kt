package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CalendarDao
import com.example.data.local.dao.FinancesDao
import com.example.data.local.dao.GradesDao
import com.example.data.local.dao.MessagesDao
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.FinanceItemEntity
import com.example.data.local.entity.NeptunMessageEntity
import com.example.data.local.entity.SubjectGradeEntity

@Database(
    entities = [
        CalendarEventEntity::class,
        SubjectGradeEntity::class,
        NeptunMessageEntity::class,
        FinanceItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NeptunDatabase : RoomDatabase() {

    abstract fun calendarDao(): CalendarDao
    abstract fun gradesDao(): GradesDao
    abstract fun messagesDao(): MessagesDao
    abstract fun financesDao(): FinancesDao

    companion object {
        @Volatile
        private var INSTANCE: NeptunDatabase? = null

        fun getInstance(context: Context): NeptunDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NeptunDatabase::class.java,
                    "neptun_mobile.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
