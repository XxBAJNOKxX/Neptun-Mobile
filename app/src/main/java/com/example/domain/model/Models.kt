package com.example.domain.model

import androidx.annotation.StringRes
import com.example.R
import kotlinx.serialization.Serializable

@Serializable
data class University(
    val id: String,
    val name: String,
    val shortName: String,
    val city: String,
    val neptunUrl: String
)

data class StudentCredentials(
    val neptunCode: String,
    val universityId: String,
    val universityName: String,
    val neptunUrl: String,
    val studentName: String = "",
    val trainingProgram: String = "",
    val isLoggedIn: Boolean = false,
    val lastSyncTime: Long = 0L
)

enum class CourseType(@StringRes val labelRes: Int) {
    LECTURE(R.string.course_lecture),
    PRACTICE(R.string.course_practice),
    LAB(R.string.course_lab),
    SEMINAR(R.string.course_seminar),
    EXAM(R.string.course_exam)
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
            return isBreakOrHolidayName(subjectName) ||
                   (startHour == 0 && startMinute == 0 && (endHour == 0 || endHour == 23 || endHour == 24))
        }

    val isActualAttendedClass: Boolean
        get() = !isHolidayOrBreak && !(startHour == 0 && startMinute == 0 && endHour == 0 && endMinute == 0)
}

/**
 * Szünetre / tanítási szünetre utaló óracímek felismerése. A szerver a
 * bejelentkezési LCID nyelvén küldi a neveket, ezért a minta többnyelvű
 * (magyar / angol / német) – független az app felületi nyelvétől.
 */
fun isBreakOrHolidayName(subjectName: String): Boolean {
    val nameLower = subjectName.lowercase()
    return nameLower.contains("szünnap") ||
        nameLower.contains("szünet") ||
        nameLower.contains("munkaszünet") ||
        nameLower.contains("ünnep") ||
        nameLower.contains("tanítás nélküli") ||
        nameLower.contains("oktatási szünet") ||
        nameLower.contains("rektori") ||
        nameLower.contains("dékáni") ||
        nameLower.contains("holiday") ||
        nameLower.contains("vacation") ||
        nameLower.contains("no lecture") ||
        nameLower.contains("christmas") ||
        nameLower.contains("easter") ||
        nameLower.contains("feiertag") ||
        nameLower.contains("weihnacht") ||
        nameLower.contains("ostern") ||
        nameLower.contains("unterrichtsfrei") ||
        nameLower.contains("vorlesungsfrei")
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

enum class FinanceStatus(@StringRes val labelRes: Int) {
    COMPLETED(R.string.finance_completed),
    PENDING(R.string.finance_pending),
    OVERDUE(R.string.finance_overdue)
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

enum class TwoFactorMethod(@StringRes val labelRes: Int) {
    EMAIL(R.string.tfa_email),
    TOTP(R.string.tfa_totp)
}

/** Vizsgaelem a Neptun vizsgalista oldaláról (kísérleti támogatás). */
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
    val isSignedUp: Boolean = true
)

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
