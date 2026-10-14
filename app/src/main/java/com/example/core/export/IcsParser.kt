package com.example.core.export

import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Standard RFC 5545 iCalendar (ICS) parser for Neptun calendar feeds and exports.
 */
object IcsParser {

    private val dayOfWeekMap = mapOf(
        "MO" to 1,
        "TU" to 2,
        "WE" to 3,
        "TH" to 4,
        "FR" to 5,
        "SA" to 6,
        "SU" to 7
    )

    fun parse(icsContent: String): List<CalendarEvent> {
        if (icsContent.isBlank()) return emptyList()

        // RFC 5545 line unfolding: CRLF or LF followed by a space or tab continues the previous line
        val unfolded = icsContent
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("""\n[ \t]"""), "")

        val lines = unfolded.lines()
        val events = mutableListOf<CalendarEvent>()

        var inVEvent = false
        val currentProps = mutableMapOf<String, String>()
        var eventCounter = 1

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.equals("BEGIN:VEVENT", ignoreCase = true)) {
                inVEvent = true
                currentProps.clear()
            } else if (trimmed.equals("END:VEVENT", ignoreCase = true)) {
                if (inVEvent) {
                    val event = parseVEvent(currentProps, eventCounter++)
                    if (event != null) {
                        events.add(event)
                    }
                }
                inVEvent = false
            } else if (inVEvent && trimmed.contains(":")) {
                val colonIdx = trimmed.indexOf(':')
                val keyPart = trimmed.substring(0, colonIdx).trim()
                val valuePart = trimmed.substring(colonIdx + 1).trim()
                val propName = keyPart.substringBefore(';').trim().uppercase()

                // Preserve existing or append if needed; store full key part for params like TZID or VALUE=DATE
                currentProps[propName] = valuePart
                currentProps["RAW_$propName"] = keyPart
            }
        }

        return events
    }

    private fun parseVEvent(props: Map<String, String>, index: Int): CalendarEvent? {
        val summaryRaw = props["SUMMARY"] ?: ""
        val summary = unescape(summaryRaw)

        val locationRaw = props["LOCATION"] ?: ""
        val location = unescape(locationRaw)

        val descriptionRaw = props["DESCRIPTION"] ?: ""
        val description = unescape(descriptionRaw)

        val dtStartRaw = props["DTSTART"] ?: return null
        val dtEndRaw = props["DTEND"]

        val startParsed = parseDateTime(dtStartRaw) ?: return null
        val endParsed = dtEndRaw?.let { parseDateTime(it) }

        val startDate = startParsed.date
        val startTime = startParsed.time
        val endTime = when {
            endParsed != null -> endParsed.time
            startParsed.isDateOnly -> LocalTime.MIDNIGHT
            else -> startTime.plusHours(2)
        }

        val rrule = props["RRULE"] ?: ""
        var dayOfWeek = startDate.dayOfWeek.value

        if (rrule.isNotBlank()) {
            val byDayMatch = Regex("""BYDAY=([A-Z,]+)""", RegexOption.IGNORE_CASE).find(rrule)
            if (byDayMatch != null) {
                val firstDay = byDayMatch.groupValues[1].split(",").firstOrNull()?.trim()?.uppercase()
                val mapped = dayOfWeekMap[firstDay]
                if (mapped != null) {
                    dayOfWeek = mapped
                }
            }
        }

        val courseType = detectCourseType(summary, description)
        val subjectName = extractSubjectTitle(summary)
        val teacherName = extractTeacherName(description)

        val uid = props["UID"] ?: ""
        val eventId = if (uid.isNotBlank()) uid else "ics_evt_${System.currentTimeMillis()}_$index"

        return CalendarEvent(
            id = eventId,
            subjectName = subjectName,
            subjectCode = extractSubjectCode(summary),
            courseCode = "",
            location = location,
            room = location,
            teacherName = teacherName,
            startHour = startTime.hour,
            startMinute = startTime.minute,
            endHour = endTime.hour,
            endMinute = endTime.minute,
            dayOfWeek = dayOfWeek,
            courseType = courseType,
            dateString = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            weekType = 0
        )
    }

    private fun detectCourseType(summary: String, description: String): CourseType {
        val fullText = "$summary $description".lowercase()
        return when {
            fullText.contains("labor") || fullText.contains("lab") -> CourseType.LAB
            fullText.contains("gyakorlat") || fullText.contains("gyak") || fullText.contains("practice") || fullText.contains("übung") -> CourseType.PRACTICE
            fullText.contains("szeminárium") || fullText.contains("szeminarium") || fullText.contains("szem") || fullText.contains("seminar") -> CourseType.SEMINAR
            fullText.contains("vizsga") || fullText.contains("exam") || fullText.contains("prüfung") -> CourseType.EXAM
            fullText.contains("előadás") || fullText.contains("eloadas") || fullText.contains("lecture") || fullText.contains("vorlesung") -> CourseType.LECTURE
            else -> CourseType.LECTURE
        }
    }

    private fun extractSubjectTitle(summary: String): String {
        var title = summary.trim()
        val prefixRegex = Regex("""^(?:Előadás|Eloadas|Gyakorlat|Labor|Szeminárium|Szeminarium|Vizsga|Lecture|Practice|Lab|Seminar|Exam)\s*[:\-]\s*""", RegexOption.IGNORE_CASE)
        title = prefixRegex.replace(title, "")
        val suffixRegex = Regex("""\s*[\(\[]?(?:Előadás|Eloadas|Gyakorlat|Labor|Szeminárium|Szeminarium|Vizsga|Lecture|Practice|Lab|Seminar|Exam)[\)\]]?\s*$""", RegexOption.IGNORE_CASE)
        title = suffixRegex.replace(title, "").trim()
        val codeSuffixRegex = Regex("""\s*\[[A-Za-z0-9_\-]+\]\s*$""")
        title = codeSuffixRegex.replace(title, "").trim()
        return if (title.isNotBlank()) title else summary.trim()
    }

    private fun extractTeacherName(description: String): String {
        if (description.isBlank()) return ""
        val oktatoMatch = Regex("""(?:Oktató|Teacher|Lecturer|Instructor)\s*:\s*([^\n;]+)""", RegexOption.IGNORE_CASE).find(description)
        if (oktatoMatch != null) {
            return oktatoMatch.groupValues[1].trim()
        }
        return description.lineSequence().firstOrNull()?.trim() ?: ""
    }

    private fun extractSubjectCode(summary: String): String {
        val codeMatch = Regex("""\[([A-Z0-9_\-]+)\]|\(([A-Z0-9_\-]+)\)""").find(summary)
        return codeMatch?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
            ?: codeMatch?.groupValues?.getOrNull(2)?.takeIf { it.isNotBlank() }
            ?: "-"
    }

    private data class ParsedDateTime(
        val date: LocalDate,
        val time: LocalTime,
        val isDateOnly: Boolean
    )

    private fun parseDateTime(raw: String): ParsedDateTime? {
        val clean = raw.trim().removeSuffix("Z")
        if (clean.isBlank()) return null
        return try {
            if (clean.contains("T")) {
                val datePart = clean.substringBefore("T")
                val timePart = clean.substringAfter("T")
                val date = if (datePart.length == 8 && datePart.all { it.isDigit() }) {
                    LocalDate.of(
                        datePart.substring(0, 4).toInt(),
                        datePart.substring(4, 6).toInt(),
                        datePart.substring(6, 8).toInt()
                    )
                } else {
                    LocalDate.parse(datePart)
                }

                val time = if (timePart.length >= 6 && timePart.take(6).all { it.isDigit() }) {
                    LocalTime.of(
                        timePart.substring(0, 2).toInt(),
                        timePart.substring(2, 4).toInt(),
                        timePart.substring(4, 6).toInt()
                    )
                } else if (timePart.length >= 4 && timePart.take(4).all { it.isDigit() }) {
                    LocalTime.of(
                        timePart.substring(0, 2).toInt(),
                        timePart.substring(2, 4).toInt()
                    )
                } else {
                    LocalTime.parse(timePart)
                }
                ParsedDateTime(date, time, isDateOnly = false)
            } else {
                val date = if (clean.length == 8 && clean.all { it.isDigit() }) {
                    LocalDate.of(
                        clean.substring(0, 4).toInt(),
                        clean.substring(4, 6).toInt(),
                        clean.substring(6, 8).toInt()
                    )
                } else {
                    LocalDate.parse(clean)
                }
                ParsedDateTime(date, LocalTime.MIDNIGHT, isDateOnly = true)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun unescape(text: String): String {
        return text
            .replace("\\n", "\n")
            .replace("\\N", "\n")
            .replace("\\,", ",")
            .replace("\\;", ";")
            .replace("\\\\", "\\")
            .trim()
    }
}
