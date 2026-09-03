package com.example.domain.usecase

import com.example.domain.model.GradeCalculation
import com.example.domain.model.SubjectGrade

class CalculateAveragesUseCase {

    operator fun invoke(termId: String, subjects: List<SubjectGrade>): GradeCalculation {
        val enrolledCredits = subjects.sumOf { it.credit }

        // Actual grades calculation (only real completed subjects >= 2)
        var actualSumWeighted = 0
        var actualCompletedCredits = 0

        // Ghost grades calculation (effectiveGrade: ghostGrade ?: grade)
        var ghostSumWeighted = 0
        var ghostCompletedCredits = 0
        var ghostCount = 0

        for (subject in subjects) {
            val grade = subject.grade
            if (grade != null && grade >= 2) {
                actualSumWeighted += grade * subject.credit
                actualCompletedCredits += subject.credit
            }

            val effective = subject.effectiveGrade
            if (effective != null && effective >= 2) {
                ghostSumWeighted += effective * subject.credit
                ghostCompletedCredits += subject.credit
            }

            if (subject.ghostGrade != null) {
                ghostCount++
            }
        }

        // Formula: (Szum(Jegy * Kredit)) / Szum(Teljesített Kredit)
        val weightedAverage = if (actualCompletedCredits > 0) {
            actualSumWeighted.toDouble() / actualCompletedCredits.toDouble()
        } else {
            0.0
        }

        // Standard credit index: Szum(Jegy * Kredit) / 30
        val creditIndex = actualSumWeighted.toDouble() / 30.0

        val ghostWeightedAverage = if (ghostCompletedCredits > 0) {
            ghostSumWeighted.toDouble() / ghostCompletedCredits.toDouble()
        } else {
            0.0
        }

        val ghostCreditIndex = ghostSumWeighted.toDouble() / 30.0

        return GradeCalculation(
            termId = termId,
            totalCreditsEnrolled = enrolledCredits,
            completedCredits = actualCompletedCredits,
            weightedAverage = roundTwoDecimals(weightedAverage),
            creditIndex = roundTwoDecimals(creditIndex),
            ghostWeightedAverage = roundTwoDecimals(ghostWeightedAverage),
            ghostCreditIndex = roundTwoDecimals(ghostCreditIndex),
            ghostCount = ghostCount
        )
    }

    private fun roundTwoDecimals(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }
}
