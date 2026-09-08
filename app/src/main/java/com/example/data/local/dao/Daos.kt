package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ExamItemEntity
import com.example.data.local.entity.FinanceItemEntity
import com.example.data.local.entity.NeptunMessageEntity
import com.example.data.local.entity.SubjectGradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY dayOfWeek ASC, startHour ASC, startMinute ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()
}

@Dao
interface GradesDao {
    @Query("SELECT * FROM subject_grades ORDER BY termId DESC, subjectName ASC")
    fun getAllGrades(): Flow<List<SubjectGradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<SubjectGradeEntity>)

    @Query("UPDATE subject_grades SET ghostGrade = :ghostGrade WHERE id = :id")
    suspend fun updateGhostGrade(id: String, ghostGrade: Int?)

    @Query("UPDATE subject_grades SET ghostGrade = NULL")
    suspend fun resetAllGhostGrades()

    @Query("DELETE FROM subject_grades")
    suspend fun clearAll()
}

@Dao
interface MessagesDao {
    @Query("SELECT * FROM neptun_messages ORDER BY sendDate DESC")
    fun getAllMessages(): Flow<List<NeptunMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<NeptunMessageEntity>)

    @Query("UPDATE neptun_messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE neptun_messages SET bodyHtml = :bodyHtml, previewText = :previewText WHERE id = :id")
    suspend fun updateMessageBody(id: String, bodyHtml: String, previewText: String)

    @Query("DELETE FROM neptun_messages")
    suspend fun clearAll()
}

@Dao
interface FinancesDao {
    @Query("SELECT * FROM finance_items ORDER BY dueDate ASC")
    fun getAllFinances(): Flow<List<FinanceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinances(items: List<FinanceItemEntity>)

    @Query("DELETE FROM finance_items")
    suspend fun clearAll()
}

@Dao
interface ExamsDao {
    @Query("SELECT * FROM exam_items ORDER BY examDate ASC, startTime ASC")
    fun getAllExams(): Flow<List<ExamItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(items: List<ExamItemEntity>)

    @Query("DELETE FROM exam_items")
    suspend fun clearAll()
}
