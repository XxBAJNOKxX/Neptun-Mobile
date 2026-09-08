package com.example

import com.example.domain.model.SubjectGrade
import com.example.domain.usecase.CalculateAveragesUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateAveragesUseCaseTest {

    private val calculateAverages = CalculateAveragesUseCase()

    @Test
    fun testWeightedAverageFormula() {
        // Test subjects:
        // Subject 1: 5 credits, Grade 5 -> 25
        // Subject 2: 4 credits, Grade 4 -> 16
        // Subject 3: 3 credits, Grade 3 -> 9
        // Completed credits: 5 + 4 + 3 = 12
        // Weighted sum = 25 + 16 + 9 = 50
        // Expected weighted average = 50 / 12 = 4.17
        // Credit index = 50 / 30 = 1.67
        val subjects = listOf(
            SubjectGrade(id = "1", termId = "t1", termName = "t1", subjectName = "A", subjectCode = "A", credit = 5, grade = 5),
            SubjectGrade(id = "2", termId = "t1", termName = "t1", subjectName = "B", subjectCode = "B", credit = 4, grade = 4),
            SubjectGrade(id = "3", termId = "t1", termName = "t1", subjectName = "C", subjectCode = "C", credit = 3, grade = 3),
            SubjectGrade(id = "4", termId = "t1", termName = "t1", subjectName = "D", subjectCode = "D", credit = 5, grade = null) // pending
        )

        val result = calculateAverages("t1", subjects)

        assertEquals(17, result.totalCreditsEnrolled)
        assertEquals(12, result.completedCredits)
        assertEquals(4.17, result.weightedAverage, 0.01)
        assertEquals(1.67, result.creditIndex, 0.01)
        assertEquals(0, result.ghostCount)
    }

    @Test
    fun testGhostMarkRecalculation() {
        // Pending subject with 5 credits given ghost mark 5:
        // Previous sum = 50, credits = 12
        // New sum = 50 + 25 = 75, credits = 12 + 5 = 17
        // New ghost weighted average = 75 / 17 = 4.41
        // Ghost credit index = 75 / 30 = 2.50
        val subjects = listOf(
            SubjectGrade(id = "1", termId = "t1", termName = "t1", subjectName = "A", subjectCode = "A", credit = 5, grade = 5),
            SubjectGrade(id = "2", termId = "t1", termName = "t1", subjectName = "B", subjectCode = "B", credit = 4, grade = 4),
            SubjectGrade(id = "3", termId = "t1", termName = "t1", subjectName = "C", subjectCode = "C", credit = 3, grade = 3),
            SubjectGrade(id = "4", termId = "t1", termName = "t1", subjectName = "D", subjectCode = "D", credit = 5, grade = null, ghostGrade = 5)
        )

        val result = calculateAverages("t1", subjects)

        assertEquals(4.17, result.weightedAverage, 0.01)
        assertEquals(4.41, result.ghostWeightedAverage, 0.01)
        assertEquals(2.50, result.ghostCreditIndex, 0.01)
        assertEquals(1, result.ghostCount)
    }
}

