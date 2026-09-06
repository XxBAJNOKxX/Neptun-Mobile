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
    EXAM("Vizsga")
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
    val dayOfWeek: Int, // 1 = Monday, ..., 5 = Friday
    val courseType: CourseType,
    val dateString: String = ""
) {
    val timeFormatted: String
        get() = "%02d:%02d - %02d:%02d".format(startHour, startMinute, endHour, endMinute)
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
