package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class University(
    val id: String,
    val name: String,
    val shortName: String,
    val city: String,
    val neptunUrl: String
)

@Serializable
data class NeptunLanguage(
    val code: String,
    val name: String,
    val lcid: Int,
    val isSelected: Boolean = false
) {
    val flagEmoji: String
        get() = when (code.lowercase()) {
            "hu" -> "🇭🇺"
            "en" -> "🇬🇧"
            "de" -> "🇩🇪"
            "sk" -> "🇸🇰"
            "fr" -> "🇫🇷"
            "ro" -> "🇷🇴"
            "it" -> "🇮🇹"
            "es" -> "🇪🇸"
            "sr" -> "🇷🇸"
            "hr" -> "🇭🇷"
            "pl" -> "🇵🇱"
            "uk" -> "🇺🇦"
            else -> "🌐"
        }

    val displayShort: String
        get() = code.uppercase()

    val displayLabel: String
        get() = when (code.lowercase()) {
            "hu" -> "Magyar"
            "en" -> "English"
            "de" -> "Deutsch"
            "sk" -> "Slovenčina"
            "fr" -> "Français"
            "ro" -> "Română"
            "it" -> "Italiano"
            "es" -> "Español"
            else -> name.split("(").firstOrNull()?.trim() ?: name
        }

    companion object {
        val HUNGARIAN = NeptunLanguage("hu", "magyar (Magyarország)", 1038, true)
        val ENGLISH = NeptunLanguage("en", "English (United States)", 1033, false)
        val GERMAN = NeptunLanguage("de", "Deutsch (Deutschland)", 1031, false)

        val DEFAULT_LANGUAGES = listOf(HUNGARIAN, ENGLISH, GERMAN)
    }
}

data class StudentCredentials(
    val neptunCode: String,
    val universityId: String,
    val universityName: String,
    val neptunUrl: String,
    val studentName: String = "",
    val trainingProgram: String = "Mérnökinformatikus BSc",
    val isLoggedIn: Boolean = false,
    val lastSyncTime: Long = 0L
)

enum class CourseType(val displayName: String) {
    LECTURE("Előadás"),
    PRACTICE("Gyakorlat"),
    LAB("Labor"),
    SEMINAR("Szeminárium"),
    EXAM("Vizsga");

    fun getLocalizedName(strings: com.example.core.i18n.AppStrings): String {
        return when (this) {
            LECTURE -> strings.courseTypeLecture
            PRACTICE -> strings.courseTypePractice
            LAB -> strings.courseTypeLab
            SEMINAR -> strings.courseTypeSeminar
            EXAM -> strings.courseTypeExam
        }
    }
}

data class CalendarEvent(
    val id: String,
    val subjectName: String,
    val subjectCode: String,
    val courseCode: String,
    val location: String,
    val room: String,
    val teacherName: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val dayOfWeek: Int, // 1 = Monday, ..., 7 = Sunday
    val courseType: CourseType,
    val dateString: String = "",
    val weekType: Int = 0 // 0 = minden héten, 1 = páratlan (A) hét, 2 = páros (B) hét
) {
    val timeFormatted: String
        get() = "%02d:%02d - %02d:%02d".format(startHour, startMinute, endHour, endMinute)

    val isHolidayOrBreak: Boolean
        get() {
            val nameLower = subjectName.lowercase()
            return nameLower.contains("szünnap") ||
                   nameLower.contains("szünet") ||
                   nameLower.contains("munkaszünet") ||
                   nameLower.contains("ünnep") ||
                   nameLower.contains("tanítás nélküli") ||
                   nameLower.contains("oktatási szünet") ||
                   nameLower.contains("rektori") ||
                   nameLower.contains("dékáni") ||
                   (startHour == 0 && startMinute == 0 && (endHour == 0 || endHour == 23 || endHour == 24))
        }

    val isActualAttendedClass: Boolean
        get() = !isHolidayOrBreak && !(startHour == 0 && startMinute == 0 && endHour == 0 && endMinute == 0)
}

data class SubjectGrade(
    val id: String,
    val termId: String, // e.g. "2025/26/1"
    val termName: String,
    val subjectName: String,
    val subjectCode: String,
    val credit: Int,
    val grade: Int?, // 1..5, or null if pending
    val gradeText: String = "", // e.g. "Jeles (5)", "Jó (4)", "Aláírva", "Nem teljesített", "Még nincs jegy"
    val isSigned: Boolean = true,
    val ghostGrade: Int? = null // Virtual grade for simulation
) {
    val effectiveGrade: Int?
        get() = ghostGrade ?: grade

    val isCompleted: Boolean
        get() = (effectiveGrade ?: 0) >= 2
}

data class GradeCalculation(
    val termId: String,
    val totalCreditsEnrolled: Int,
    val completedCredits: Int,
    val weightedAverage: Double, // Súlyozott Tanulmányi Átlag
    val creditIndex: Double,     // Hagyományos Kreditindex: (Sum(Grade * Credit)) / 30
    val ghostWeightedAverage: Double,
    val ghostCreditIndex: Double,
    val ghostCount: Int
)

data class NeptunMessage(
    val id: String,
    val subject: String,
    val sender: String,
    val sendDate: String,
    val previewText: String,
    val bodyHtml: String,
    val isRead: Boolean,
    val isOfficial: Boolean = false
)

enum class FinanceStatus(val displayName: String) {
    COMPLETED("Teljesítve"),
    PENDING("Kiírva"),
    OVERDUE("Késedelmes")
}

data class Neptun2FASession(
    val neptunCode: String,
    val key: String,
    val phase: String = "RequestTOTP",
    val rendered: String = "",
    val verificationToken: String = "",
    val hasTotp: Boolean = false,
    val hasEmail: Boolean = true,
    val codePrefix: String = "",
    val cookies: Map<String, String> = emptyMap(),
    val baseUrl: String = "https://neptun.elte.hu"
)

enum class TwoFactorMethod(val displayName: String) {
    EMAIL("E-mail kód"),
    TOTP("Hitelesítő App (TOTP)")
}

/** Vizsgaelem a Neptun vizsgalista oldaláról. */
data class ExamItem(
    val id: String,
    val subjectName: String,
    val subjectCode: String = "",
    val courseCode: String = "",
    val examDate: String = "",
    val startTime: String = "",
    val room: String = "",
    val location: String = "",
    val examType: String = "",
    val isSignedUp: Boolean = true,
    val teacherName: String = "",
    val applicationDeadline: String = ""
) {
    val fullDateTimeString: String
        get() = if (startTime.isNotBlank()) "$examDate $startTime" else examDate

    val daysUntilExam: Long?
        get() {
            if (examDate.isBlank()) return null
            return try {
                val cleanDate = examDate.replace(".", "-").trim().take(10)
                val parsed = java.time.LocalDate.parse(cleanDate)
                java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), parsed)
            } catch (e: Exception) {
                null
            }
        }
}

@kotlinx.serialization.Serializable
data class CurriculumTemplateItem(
    val id: String,
    val name: String,
    val code: String = "",
    val status: String = "", // e.g. "Teljesített"
    val completedSubjects: Int = 0,
    val totalSubjects: Int = 0,
    val completedCredits: Int = 0,
    val totalCredits: Int = 0,
    val isCompleted: Boolean = false
)

@kotlinx.serialization.Serializable
data class DegreeProgress(
    val completedCredits: Int = 0,
    val totalRequiredCredits: Int = 210,
    val completedCurriculums: Int = 0,
    val totalCurriculums: Int = 0,
    val compulsoryCompleted: Int = 0,
    val compulsoryTotal: Int = 0,
    val compulsoryElectiveCompleted: Int = 0,
    val compulsoryElectiveTotal: Int = 0,
    val freeElectiveCompleted: Int = 0,
    val freeElectiveTotal: Int = 0,
    val thesisCompleted: Int = 0,
    val thesisTotal: Int = 0,
    val criteriaPassedCount: Int = 0,
    val criteriaTotalCount: Int = 0,
    val cumulativeWeightedAverage: Double = 0.0,
    val cumulativeCreditIndex: Double = 0.0,
    val templates: List<CurriculumTemplateItem> = emptyList()
) {
    val progressFraction: Float
        get() = if (totalRequiredCredits > 0) {
            (completedCredits.toFloat() / totalRequiredCredits.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val progressPercentage: Int
        get() = (progressFraction * 100).toInt()
}

@kotlinx.serialization.Serializable
data class AcademicPeriod(
    val id: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val type: String = "", // e.g. "REGISTRATION", "COURSE_REG", "EXAM", "EDUCATION", "BREAK"
    val isActive: Boolean = true
) {
    val daysRemaining: Long?
        get() {
            if (endDate.isBlank()) return null
            return try {
                val cleanDate = endDate.replace(".", "-").trim().take(10)
                val parsed = java.time.LocalDate.parse(cleanDate)
                val diff = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), parsed)
                diff.coerceAtLeast(0)
            } catch (e: Exception) {
                null
            }
        }
}

data class FinanceItem(
    val id: String,
    val title: String,
    val termName: String,
    val amountHuf: Int,
    val status: FinanceStatus,
    val dueDate: String,
    val paymentDate: String? = null,
    val transactionId: String = ""
)

