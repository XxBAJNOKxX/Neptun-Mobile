package com.example.domain.usecase

import com.example.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.LocalTime

/**
 * A "ma" események kiszűrése: kezeli a pontos dátumú (dateString) és a
 * heti ismétlődésű (dayOfWeek) eseményeket is, valamint az A/B hét szűrést.
 */
class GetTodayClassesUseCase {

    operator fun invoke(
        events: List<CalendarEvent>,
        today: LocalDate = LocalDate.now(),
        now: LocalTime = LocalTime.now(),
        weekTypeToday: Int = WeekParityUtil.weekTypeOf(today)
    ): List<CalendarEvent> {
        val todayIso = today.toString()
        return events
            .filter { event -> isToday(event, todayIso) }
            .filter { event -> matchesWeekType(event, weekTypeToday, today) }
            .sortedWith(
                compareBy({ it.startHour }, { it.startMinute })
            )
    }

    fun ongoingClass(classes: List<CalendarEvent>, now: LocalTime = LocalTime.now()): CalendarEvent? {
        return classes.firstOrNull { event ->
            val start = event.startHour * 60 + event.startMinute
            val end = event.endHour * 60 + event.endMinute
            val nowMin = now.hour * 60 + now.minute
            nowMin in start..end && event.isActualAttendedClass
        }
    }

    fun nextClass(classes: List<CalendarEvent>, now: LocalTime = LocalTime.now()): CalendarEvent? {
        val nowMin = now.hour * 60 + now.minute
        return classes.firstOrNull { event ->
            event.isActualAttendedClass && (event.startHour * 60 + event.startMinute) > nowMin
        }
    }

    private fun isToday(event: CalendarEvent, todayIso: String): Boolean {
        return if (event.dateString.isNotBlank()) {
            event.dateString.startsWith(todayIso)
        } else {
            event.dayOfWeek == LocalDate.parse(todayIso).dayOfWeek.value
        }
    }

    private fun matchesWeekType(event: CalendarEvent, weekTypeToday: Int, today: LocalDate): Boolean {
        if (event.weekType == 0) return true
        if (event.dateString.isNotBlank()) return true // pontos dátumú esemény mindig aktuális
        return event.weekType == weekTypeToday
    }
}

/** ISO hét sorszámából A/B (páratlan/páros) hét meghatározása. 1 = A (páratlan), 2 = B (páros). */
object WeekParityUtil {
    fun weekTypeOf(date: LocalDate): Int {
        val weekNumber = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
        return if (weekNumber % 2 == 1) 1 else 2
    }

    /**
     * Az aktuális hetünkhöz képest [offset] héttel későbbi/ korábbi hét A/B típusa.
     * A "hét" mindig hétfőn kezdődik (ISO).
     */
    fun weekTypeForOffset(offset: Int, today: LocalDate = LocalDate.now()): Int {
        val isWeekend = today.dayOfWeek == java.time.DayOfWeek.SATURDAY || today.dayOfWeek == java.time.DayOfWeek.SUNDAY
        val baseMonday = if (isWeekend) {
            today.plusDays((8 - today.dayOfWeek.value).toLong())
        } else {
            today.minusDays((today.dayOfWeek.value - 1).toLong())
        }
        return weekTypeOf(baseMonday.plusWeeks(offset.toLong()))
    }
}
