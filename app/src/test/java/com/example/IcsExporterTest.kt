package com.example

import com.example.core.export.IcsExporter
import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IcsExporterTest {

    private fun weeklyEvent(dayOfWeek: Int = 1, weekType: Int = 0) = CalendarEvent(
        id = "cal_1",
        subjectName = "Analízis 1",
        subjectCode = "BMEMATM01",
        courseCode = "EA01",
        location = "I épület",
        room = "IB028",
        teacherName = "Dr. Teszt Elek",
        startHour = 8,
        startMinute = 15,
        endHour = 10,
        endMinute = 0,
        dayOfWeek = dayOfWeek,
        courseType = CourseType.LECTURE,
        dateString = "",
        weekType = weekType
    )

    private fun datedEvent(date: String) = weeklyEvent().copy(
        id = "cal_2",
        dateString = date,
        dayOfWeek = LocalDate.parse(date).dayOfWeek.value
    )

    @Test
    fun `ics contains calendar envelope and events`() {
        val ics = IcsExporter.buildIcs(listOf(weeklyEvent()), weeksAhead = 12)

        assertTrue(ics.startsWith("BEGIN:VCALENDAR"))
        assertTrue(ics.trimEnd().endsWith("END:VCALENDAR"))
        assertTrue(ics.contains("BEGIN:VEVENT"))
        assertTrue(ics.contains("END:VEVENT"))
        assertTrue(ics.contains("SUMMARY:Előadás: Analízis 1"))
    }

    @Test
    fun `weekly event has RRULE`() {
        val ics = IcsExporter.buildIcs(listOf(weeklyEvent(dayOfWeek = 1)), weeksAhead = 12)
        assertTrue(ics.contains("RRULE:FREQ=WEEKLY;COUNT=12;BYDAY=MO"))
    }

    @Test
    fun `dated event has no RRULE`() {
        val ics = IcsExporter.buildIcs(listOf(datedEvent("2030-03-04")), weeksAhead = 12)
        assertFalse(ics.contains("RRULE"))
        assertTrue(ics.contains("DTSTART;TZID=Europe/Budapest:20300304T081500"))
    }

    @Test
    fun `special characters are escaped`() {
        val event = weeklyEvent().copy(subjectName = "Algoritmusok; adatszerkezetek, I.")
        val ics = IcsExporter.buildIcs(listOf(event))
        assertTrue(ics.contains("Algoritmusok\\; adatszerkezetek\\, I."))
    }

    @Test
    fun `time fields formatted correctly`() {
        val ics = IcsExporter.buildIcs(listOf(weeklyEvent()))
        assertTrue(ics.contains("T081500"))
        assertTrue(ics.contains("T100000"))
    }

    @Test
    fun `alarm is attached`() {
        val ics = IcsExporter.buildIcs(listOf(weeklyEvent()))
        assertTrue(ics.contains("BEGIN:VALARM"))
        assertTrue(ics.contains("TRIGGER:-PT15M"))
    }
}
