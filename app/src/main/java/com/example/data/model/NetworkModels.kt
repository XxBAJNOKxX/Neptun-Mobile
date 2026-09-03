package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NeptunRequest(
    @SerialName("UserCode") val userCode: String,
    @SerialName("Password") val password: String = "",
    @SerialName("SessionToken") val sessionToken: String = "",
    @SerialName("TermId") val termId: String = "",
    @SerialName("FromDate") val fromDate: String = "",
    @SerialName("ToDate") val toDate: String = ""
)

@Serializable
data class LoginRequest(
    @SerialName("UserCode") val userCode: String,
    @SerialName("Password") val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("Success") val success: Boolean,
    @SerialName("ErrorMessage") val errorMessage: String? = null,
    @SerialName("SessionToken") val sessionToken: String? = null,
    @SerialName("StudentName") val studentName: String? = null,
    @SerialName("TrainingProgram") val trainingProgram: String? = null
)

@Serializable
data class CalendarItemDto(
    @SerialName("Id") val id: String,
    @SerialName("SubjectName") val subjectName: String,
    @SerialName("SubjectCode") val subjectCode: String,
    @SerialName("CourseCode") val courseCode: String = "",
    @SerialName("Location") val location: String = "",
    @SerialName("Room") val room: String = "",
    @SerialName("TeacherName") val teacherName: String = "",
    @SerialName("StartHour") val startHour: Int,
    @SerialName("StartMinute") val startMinute: Int,
    @SerialName("EndHour") val endHour: Int,
    @SerialName("EndMinute") val endMinute: Int,
    @SerialName("DayOfWeek") val dayOfWeek: Int,
    @SerialName("CourseType") val courseType: String = "Előadás",
    @SerialName("Date") val date: String = ""
)

@Serializable
data class CalendarDataResponse(
    @SerialName("Success") val success: Boolean = true,
    @SerialName("ErrorMessage") val errorMessage: String? = null,
    @SerialName("Events") val events: List<CalendarItemDto> = emptyList()
)

@Serializable
data class SubjectGradeDto(
    @SerialName("Id") val id: String,
    @SerialName("TermId") val termId: String,
    @SerialName("TermName") val termName: String,
    @SerialName("SubjectName") val subjectName: String,
    @SerialName("SubjectCode") val subjectCode: String,
    @SerialName("Credit") val credit: Int,
    @SerialName("Grade") val grade: Int? = null,
    @SerialName("GradeText") val gradeText: String = "",
    @SerialName("IsSigned") val isSigned: Boolean = false
)

@Serializable
data class SubjectGradesResponse(
    @SerialName("Success") val success: Boolean = true,
    @SerialName("ErrorMessage") val errorMessage: String? = null,
    @SerialName("Grades") val grades: List<SubjectGradeDto> = emptyList()
)

@Serializable
data class NeptunMessageDto(
    @SerialName("Id") val id: String,
    @SerialName("Subject") val subject: String,
    @SerialName("Sender") val sender: String,
    @SerialName("SendDate") val sendDate: String,
    @SerialName("PreviewText") val previewText: String,
    @SerialName("BodyHtml") val bodyHtml: String,
    @SerialName("IsRead") val isRead: Boolean = false,
    @SerialName("IsOfficial") val isOfficial: Boolean = false
)

@Serializable
data class MessagesResponse(
    @SerialName("Success") val success: Boolean = true,
    @SerialName("ErrorMessage") val errorMessage: String? = null,
    @SerialName("Messages") val messages: List<NeptunMessageDto> = emptyList()
)

@Serializable
data class FinanceItemDto(
    @SerialName("Id") val id: String,
    @SerialName("Title") val title: String,
    @SerialName("TermName") val termName: String,
    @SerialName("AmountHuf") val amountHuf: Int,
    @SerialName("Status") val status: String,
    @SerialName("DueDate") val dueDate: String,
    @SerialName("PaymentDate") val paymentDate: String? = null,
    @SerialName("TransactionId") val transactionId: String = ""
)

@Serializable
data class FinancesResponse(
    @SerialName("Success") val success: Boolean = true,
    @SerialName("ErrorMessage") val errorMessage: String? = null,
    @SerialName("Finances") val finances: List<FinanceItemDto> = emptyList()
)
