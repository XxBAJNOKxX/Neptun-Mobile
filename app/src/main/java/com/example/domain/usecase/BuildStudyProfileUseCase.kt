package com.example.domain.usecase

import com.example.domain.model.CalendarEvent
import com.example.domain.model.SubjectGrade
import kotlin.math.max

/**
 * A Kezdőlapon megjelenő "Tanulmányi profilom" kártya adatai.
 *
 * A reFilc `PersonalityCard` mintájára nem nyers statisztikát mutatunk, hanem
 * a hallgató teljesítményéből összerakott, játékos karakterlapot – hosszú
 * nyomásra derül ki (lásd `PersonalityCard` a UI-ban).
 */
data class StudyProfile(
    val title: String,
    val emoji: String,
    val description: String,
    val weightedAverage: Double,
    val totalGrades: Int,
    val signedSubjects: Int,
    val bestSubject: String,
    val bestSubjectAverage: Double,
    val hardestSubject: String,
    val hardestSubjectAverage: Double,
    val creditsEarned: Int,
    val creditsPlanned: Int,
    val weeklyLessonCount: Int,
    val goodGradeStreak: Int,
    val mostCommonGrade: Int
) {
    val creditProgress: Float
        get() = if (creditsPlanned <= 0) 0f else creditsEarned.toFloat() / creditsPlanned.toFloat()

    companion object {
        val Empty = StudyProfile(
            title = "Kezdő félév",
            emoji = "🌱",
            description = "Még nincs jegyed – a félév elején járunk.",
            weightedAverage = 0.0,
            totalGrades = 0,
            signedSubjects = 0,
            bestSubject = "",
            bestSubjectAverage = 0.0,
            hardestSubject = "",
            hardestSubjectAverage = 0.0,
            creditsEarned = 0,
            creditsPlanned = 0,
            weeklyLessonCount = 0,
            goodGradeStreak = 0,
            mostCommonGrade = 0
        )
    }
}

class BuildStudyProfileUseCase {

    operator fun invoke(
        termGrades: List<SubjectGrade>,
        weekEvents: List<CalendarEvent>
    ): StudyProfile {
        if (termGrades.isEmpty()) {
            val lessons = weekEvents.count { it.isActualAttendedClass }
            return StudyProfile.Empty.copy(weeklyLessonCount = lessons)
        }

        val graded = termGrades.filter { it.effectiveGrade != null }
        val perSubject = graded.groupBy { it.subjectName }
            .mapValues { entries -> entries.value.mapNotNull { it.effectiveGrade }.average() }
            .filter { !it.value.isNaN() }

        val best = perSubject.maxByOrNull { it.value }
        val worst = perSubject.minByOrNull { it.value }

        val weightedSum = graded.sumOf { (it.effectiveGrade ?: 0) * it.credit }
        val creditSum = graded.filter { (it.effectiveGrade ?: 0) >= 2 }.sumOf { it.credit }
        val weightedAverage = if (creditSum > 0) {
            (weightedSum.toDouble() / creditSum.toDouble() * 100.0).toLong() / 100.0
        } else {
            0.0
        }

        val gradeCounts = graded.mapNotNull { it.grade }.groupingBy { it }.eachCount()
        val mostCommon = gradeCounts.maxByOrNull { it.value }?.key ?: 0

        // A legjobb jegyekből álló, egymást követő sorozat (a jegyek listájának
        // sorrendjében számolva, a reFilc "grade streak" ötletére).
        var streak = 0
        var current = 0
        for (grade in graded) {
            if ((grade.grade ?: 0) >= 4) {
                current++
                streak = max(streak, current)
            } else {
                current = 0
            }
        }

        val plannedCredits = termGrades.sumOf { it.credit }
        val weeklyLessons = weekEvents.count { it.isActualAttendedClass }
        val signedCount = termGrades.count { it.isCompleted }

        val (title, emoji, description) = describe(
            average = weightedAverage,
            gradedCount = graded.size,
            weeklyLessons = weeklyLessons
        )

        return StudyProfile(
            title = title,
            emoji = emoji,
            description = description,
            weightedAverage = weightedAverage,
            totalGrades = graded.size,
            signedSubjects = signedCount,
            bestSubject = best?.key.orEmpty(),
            bestSubjectAverage = best?.value?.let { (it * 100.0).toLong() / 100.0 } ?: 0.0,
            hardestSubject = worst?.key.orEmpty(),
            hardestSubjectAverage = worst?.value?.let { (it * 100.0).toLong() / 100.0 } ?: 0.0,
            creditsEarned = creditSum,
            creditsPlanned = plannedCredits,
            weeklyLessonCount = weeklyLessons,
            goodGradeStreak = streak,
            mostCommonGrade = mostCommon
        )
    }

    /** Játékos karakterlap a statisztikák alapján. */
    private fun describe(
        average: Double,
        gradedCount: Int,
        weeklyLessons: Int
    ): Triple<String, String, String> = when {
        gradedCount == 0 -> Triple(
            "Kalandor",
            "🧭",
            "Ebben a félévben még nem született jegy – de az órarend tele van lehetőségekkel."
        )

        average >= 4.7 -> Triple(
            "Rektori dicséret",
            "🏆",
            "Kiemelkedő átlag, tisztes kredit és stabil hétköznapok. Írd ki a szobádba!"
        )

        average >= 4.2 -> Triple(
            "Szikraagy",
            "⚡",
            "Gyorsan kapcsolol: a jegyeid zöme jó vagy jeles, és a kreditjeid is gyarapodnak."
        )

        average >= 3.5 -> Triple(
            "Stabil motor",
            "🚀",
            "Egyenletes tempó, nincs nagy lyuk a tanulásban. Ezzel a ritmussal el fogsz érni mindent."
        )

        average >= 2.5 -> Triple(
            "Ügyes túlélő",
            "🛟",
            "A félév nem egyszerű, de tartod magad – páran irigyelnék a kitartásodat."
        )

        else -> Triple(
            "Állampolgári kötelesség",
            "☕",
            "A lényeg most nem a jegyek, hanem a túlélés. A következő félév aztán jön!"
        )
    }.let { (title, emoji, desc) ->
        val extra = if (weeklyLessons >= 25) {
            " Ráadásul hetente $weeklyLessons órád van – lesz mit bepótolni."
        } else {
            ""
        }
        Triple(title, emoji, desc + extra)
    }
}
