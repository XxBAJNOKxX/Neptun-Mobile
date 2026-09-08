package com.example.core.export

import com.example.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * Az órarend ICS (iCalendar) formátumba való exportálása, hogy a felhasználó
 * a Google / beépített naptárba is importálhassa az óráit.
 *
 * A heti ismétlődésű órák RRULE=WKLY ismétlődéssel, a pontos dátumú
 * események egyszeri VEVENT-ként kerülnek a fájlba.
 */
object IcsExporter {

    private val dayIcalMap = mapOf(
        1 to "MO",
        2 to "TU",
        3 to "WE",
        4 to "TH",
        5 to "FR",
        6 to "SA",
        7 to "SU"
    )

    fun buildIcs(
        events: List<CalendarEvent>,
        weeksAhead: Int = 14,
        today: LocalDate = LocalDate.now()
    ): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//Neptun Mobile//Orarend Export//HU\r\n")
        sb.append("CALSCALE:GREGORIAN\r\n")
        sb.append("METHOD:PUBLISH\r\n")

        val interesting = events.filter { it.isActualAttendedClass }
        var counter = 1

        for (event in interesting) {
            if (event.dateString.isNotBlank()) {
                appendVEvent(sb, event, event.dateString.take(10), recurring = false, count = 1, counter++)
            } else {
                // Következő előfordulás kiszámítása a mai napról nézve
                var date = today
                var guard = 0
                while (date.dayOfWeek.value != event.dayOfWeek && guard < 8) {
                    date = date.plusDays(1)
                    guard++
                }
                // A/B hét igazítása: ha az óra B hetes és most A hét van, egy hetet csúsztatunk
                if (event.weekType != 0) {
                    while (weekTypeOf(date) != event.weekType && guard < 16) {
                        date = date.plusWeeks(1)
                        guard++
                    }
                }
                if (guard >= 16) continue
                appendVEvent(sb, event, date.toString(), recurring = true, count = weeksAhead, counter++)
            }
        }

        sb.append("END:VCALENDAR\r\n")
        return sb.toString()
    }

    private fun appendVEvent(
        sb: StringBuilder,
        event: CalendarEvent,
        startDateIso: String,
        recurring: Boolean,
        count: Int,
        index: Int
    ) {
        val start = "%02d%02d00".format(event.startHour, event.startMinute)
        val end = "%02d%02d00".format(event.endHour, event.endMinute)
        val dateCompact = startDateIso.replace("-", "")

        sb.append("BEGIN:VEVENT\r\n")
        sb.append("UID:neptun-${event.id}-${dateCompact}-${index}@neptun.mobile\r\n")
        sb.append("DTSTAMP:${nowStamp()}\r\n")
        sb.append("DTSTART;TZID=Europe/Budapest:${dateCompact}T$start\r\n")
        sb.append("DTEND;TZID=Europe/Budapest:${dateCompact}T$end\r\n")
        sb.append("SUMMARY:").append(escape(event.courseType.displayName + ": " + event.subjectName)).append("\r\n")
        val location = listOfNotNull(
            event.location.takeIf { it.isNotBlank() },
            event.room.takeIf { it.isNotBlank() && it != event.location }
        ).joinToString(", ")
        if (location.isNotBlank()) {
            sb.append("LOCATION:").append(escape(location)).append("\r\n")
        }
        if (event.teacherName.isNotBlank()) {
            sb.append("DESCRIPTION:").append(escape("Oktató: " + event.teacherName)).append("\r\n")
        }
        if (recurring) {
            val byDay = dayIcalMap[event.dayOfWeek] ?: "MO"
            sb.append("RRULE:FREQ=WEEKLY;COUNT=$count;BYDAY=$byDay\r\n")
        }
        sb.append("BEGIN:VALARM\r\n")
        sb.append("TRIGGER:-PT15M\r\n")
        sb.append("ACTION:DISPLAY\r\n")
        sb.append("DESCRIPTION:Emlékeztető\r\n")
        sb.append("END:VALARM\r\n")
        sb.append("END:VEVENT\r\n")
    }

    private fun nowStamp(): String {
        val now = java.time.LocalDateTime.now()
        return "%04d%02d%02dT%02d%02d%02dZ".format(
            now.year, now.monthValue, now.dayOfMonth, now.hour, now.minute, now.second
        )
    }

    private fun escape(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }

    private fun weekTypeOf(date: LocalDate): Int {
        val week = date.get(WeekFields.ISO.weekOfWeekBasedYear())
        return if (week % 2 == 1) 1 else 2
    }
}
