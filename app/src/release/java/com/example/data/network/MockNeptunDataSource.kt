package com.example.data.network

import com.example.domain.model.AcademicPeriod
import com.example.domain.model.CalendarEvent
import com.example.domain.model.DegreeProgress
import com.example.domain.model.ExamItem
import com.example.domain.model.FinanceItem
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade

/**
 * Release No-Op csonk.
 * A release buildben a demó adatok és konstansok nem kerülhetnek be a DEX-be.
 */
object MockNeptunDataSource {
    fun getMockCalendarEvents(): List<CalendarEvent> = emptyList()
    fun getMockGrades(): List<SubjectGrade> = emptyList()
    fun getMockMessages(): List<NeptunMessage> = emptyList()
    fun getMockFinances(): List<FinanceItem> = emptyList()
    fun getMockExams(): List<ExamItem> = emptyList()
    fun getMockDegreeProgress(): DegreeProgress = DegreeProgress(
        totalCreditsNeeded = 0,
        completedCredits = 0,
        inProgressCredits = 0,
        weightedAverage = 0.0,
        creditIndex = 0.0
    )
    fun getMockAcademicPeriods(): List<AcademicPeriod> = emptyList()
}
