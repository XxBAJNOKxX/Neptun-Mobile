package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
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
    val dayOfWeek: Int,
    val courseType: String,
    val dateString: String
) {
    fun toDomain(): CalendarEvent = CalendarEvent(
        id = id,
        subjectName = subjectName,
        subjectCode = subjectCode,
        courseCode = courseCode,
        location = location,
        room = room,
        teacherName = teacherName,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        dayOfWeek = dayOfWeek,
        courseType = try { CourseType.valueOf(courseType) } catch (e: Exception) { CourseType.LECTURE },
        dateString = dateString
    )

    companion object {
        fun fromDomain(event: CalendarEvent): CalendarEventEntity = CalendarEventEntity(
            id = event.id,
            subjectName = event.subjectName,
            subjectCode = event.subjectCode,
            courseCode = event.courseCode,
            location = event.location,
            room = event.room,
            teacherName = event.teacherName,
            startHour = event.startHour,
            startMinute = event.startMinute,
            endHour = event.endHour,
            endMinute = event.endMinute,
            dayOfWeek = event.dayOfWeek,
            courseType = event.courseType.name,
            dateString = event.dateString
        )
    }
}

@Entity(tableName = "subject_grades")
data class SubjectGradeEntity(
    @PrimaryKey val id: String,
    val termId: String,
    val termName: String,
    val subjectName: String,
    val subjectCode: String,
    val credit: Int,
    val grade: Int?,
    val gradeText: String,
    val isSigned: Boolean,
    val ghostGrade: Int? = null
) {
    fun toDomain(): SubjectGrade = SubjectGrade(
        id = id,
        termId = termId,
        termName = termName,
        subjectName = subjectName,
        subjectCode = subjectCode,
        credit = credit,
        grade = grade,
        gradeText = gradeText,
        isSigned = isSigned,
        ghostGrade = ghostGrade
    )

    companion object {
        fun fromDomain(grade: SubjectGrade): SubjectGradeEntity = SubjectGradeEntity(
            id = grade.id,
            termId = grade.termId,
            termName = grade.termName,
            subjectName = grade.subjectName,
            subjectCode = grade.subjectCode,
            credit = grade.credit,
            grade = grade.grade,
            gradeText = gradeTextFor(grade.grade, grade.gradeText),
            isSigned = grade.isSigned,
            ghostGrade = grade.ghostGrade
        )

        private fun gradeTextFor(grade: Int?, fallback: String): String {
            return when (grade) {
                5 -> "Jeles (5)"
                4 -> "Jó (4)"
                3 -> "Közepes (3)"
                2 -> "Elégséges (2)"
                1 -> "Elégtelen (1)"
                else -> fallback.ifEmpty { "Még nincs jegy" }
            }
        }
    }
}

@Entity(tableName = "neptun_messages")
data class NeptunMessageEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val sender: String,
    val sendDate: String,
    val previewText: String,
    val bodyHtml: String,
    val isRead: Boolean,
    val isOfficial: Boolean
) {
    fun toDomain(): NeptunMessage = NeptunMessage(
        id = id,
        subject = subject,
        sender = sender,
        sendDate = sendDate,
        previewText = previewText,
        bodyHtml = bodyHtml,
        isRead = isRead,
        isOfficial = isOfficial
    )

    companion object {
        fun fromDomain(msg: NeptunMessage): NeptunMessageEntity = NeptunMessageEntity(
            id = msg.id,
            subject = msg.subject,
            sender = msg.sender,
            sendDate = msg.sendDate,
            previewText = msg.previewText,
            bodyHtml = msg.bodyHtml,
            isRead = msg.isRead,
            isOfficial = msg.isOfficial
        )
    }
}

@Entity(tableName = "finance_items")
data class FinanceItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val termName: String,
    val amountHuf: Int,
    val status: String,
    val dueDate: String,
    val paymentDate: String?,
    val transactionId: String
) {
    fun toDomain(): FinanceItem = FinanceItem(
        id = id,
        title = title,
        termName = termName,
        amountHuf = amountHuf,
        status = try { FinanceStatus.valueOf(status) } catch (e: Exception) { FinanceStatus.PENDING },
        dueDate = dueDate,
        paymentDate = paymentDate,
        transactionId = transactionId
    )

    companion object {
        fun fromDomain(item: FinanceItem): FinanceItemEntity = FinanceItemEntity(
            id = item.id,
            title = item.title,
            termName = item.termName,
            amountHuf = item.amountHuf,
            status = item.status.name,
            dueDate = item.dueDate,
            paymentDate = item.paymentDate,
            transactionId = item.transactionId
        )
    }
}
