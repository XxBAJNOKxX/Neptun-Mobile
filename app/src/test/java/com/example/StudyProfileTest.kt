package com.example

import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.domain.model.SubjectGrade
import com.example.domain.usecase.BuildStudyProfileUseCase
import com.example.presentation.ui.components.formatHungarian
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A Kezdőlap "tanulmányi profil" számításai – pár determinisztikus esettel,
 * hogy a Filc-kártya ne tálaljon hülyeséget.
 */
class StudyProfileTest {

    private val useCase = BuildStudyProfileUseCase()

    private fun grade(
        id: String,
        subject: String,
        credit: Int,
        grade: Int?,
        termId: String = "2025/26/1",
        gradeText: String = ""
    ) = SubjectGrade(
        id = id,
        termId = termId,
        termName = termId,
        subjectName = subject,
        subjectCode = "TEST$id",
        credit = credit,
        grade = grade,
        gradeText = gradeText
    )

    private fun event(id: String, day: Int, startHour: Int, endHour: Int) = CalendarEvent(
        id = id,
        subjectName = "Teszt tárgy",
        subjectCode = "TEST",
        courseCode = "EA",
        location = "A épület",
        room = "A101",
        teacherName = "Nagy Géza",
        startHour = startHour,
        startMinute = 15,
        endHour = endHour,
        endMinute = 0,
        dayOfWeek = day,
        courseType = CourseType.LECTURE
    )

    @Test
    fun emptyGradesProduceEmptyProfile() {
        val profile = useCase(
            emptyList(),
            listOf(event("e1", 1, 8, 10), event("e2", 1, 10, 12))
        )

        assertEquals(0, profile.totalGrades)
        assertEquals(0.0, profile.weightedAverage, 0.0001)
        // Az üres profil is tudja, mennyi óra van a héten.
        assertEquals(2, profile.weeklyLessonCount)
    }

    @Test
    fun weightedAverageAndCreditsAreCalculated() {
        val grades = listOf(
            grade("g1", "Algoritmusok", 5, 5),
            grade("g2", "Hálózatok", 4, 4),
            grade("g3", "Adatbázisok", 4, 3),
            grade("g4", "Mobilalkalmazások", 6, null) // még nincs jegy
        )

        val profile = useCase(grades, emptyList())

        // (5*5 + 4*4 + 3*4) / (5 + 4 + 4) = 53 / 13 = 4.076... -> 4.07
        assertEquals(4.07, profile.weightedAverage, 0.0001)
        assertEquals(3, profile.totalGrades)
        assertEquals(13, profile.creditsEarned)
        assertEquals(19, profile.creditsPlanned)
        assertEquals("Algoritmusok", profile.bestSubject)
        assertEquals("Adatbázisok", profile.hardestSubject)
        assertEquals(2, profile.goodGradeStreak)
        assertTrue(profile.title.isNotBlank())
        assertTrue(profile.emoji.isNotBlank())
    }

    @Test
    fun pendingTermWithoutGradesIsStillFriendly() {
        val profile = useCase(
            listOf(grade("s1", "Testnevelés", 2, null, gradeText = "Még nincs jegy")),
            emptyList()
        )

        assertEquals(0.0, profile.weightedAverage, 0.0001)
        assertEquals(0, profile.creditsEarned)
        assertEquals(2, profile.creditsPlanned)
        assertEquals("Kalandor", profile.title)
    }

    @Test
    fun hungarianDecimalSeparatorIsUsed() {
        assertEquals("4,07", formatHungarian(4.07))
        assertEquals("3,00", formatHungarian(3.0))
    }
}
