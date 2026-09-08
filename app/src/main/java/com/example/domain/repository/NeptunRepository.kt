package com.example.domain.repository

import com.example.domain.model.CalendarEvent
import com.example.domain.model.ExamItem
import com.example.domain.model.FinanceItem
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade
import kotlinx.coroutines.flow.Flow

interface NeptunRepository {
    fun getCalendarEvents(): Flow<List<CalendarEvent>>
    fun getSubjectGrades(): Flow<List<SubjectGrade>>
    fun getMessages(): Flow<List<NeptunMessage>>
    fun getFinances(): Flow<List<FinanceItem>>
    fun getExams(): Flow<List<ExamItem>>

    suspend fun syncAllData(neptunCode: String, sessionToken: String): Result<Unit>
    suspend fun refreshCalendar(): Result<Unit>
    suspend fun refreshGrades(): Result<Unit>
    suspend fun refreshMessages(): Result<Unit>
    suspend fun refreshFinances(): Result<Unit>
    suspend fun refreshExams(): Result<Unit>

    suspend fun setGhostGrade(subjectId: String, ghostGrade: Int?)
    suspend fun resetAllGhostGrades()
    suspend fun markMessageAsRead(messageId: String)
    suspend fun getMessageContent(messageId: String): String
    suspend fun clearLocalData()
}
