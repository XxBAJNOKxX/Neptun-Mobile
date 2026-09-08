package com.example

import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.domain.usecase.GetTodayClassesUseCase
import com.example.domain.usecase.WeekParityUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class GetTodayClassesUseCaseTest {

    private val useCase = GetTodayClassesUseCase()

    private fun event(
        dayOfWeek: Int,
        startHour: Int,
        weekType: Int = 0,
        dateString: String = ""
    ) = CalendarEvent(
        id = "evt_${dayOfWeek}_${startHour}_${weekType}_$dateString",
        subjectName = "Tárgy $dayOfWeek:$startHour",
        subjectCode = "XYZ",
        courseCode = "EA01",
        location = "I épület",
        room = "IB028",
        teacherName = "Dr. Teszt Elek",
        startHour = startHour,
        startMinute = 15,
        endHour = startHour + 2,
        endMinute = 0,
        dayOfWeek = dayOfWeek,
        courseType = CourseType.LECTURE,
        dateString = dateString,
        weekType = weekType
    )

    private val monday = LocalDate.of(2030, 9, 2) // ez hétfő

    @Test
    fun `today filters by day of week`() {
        assertTrue(monday.dayOfWeek == DayOfWeek.MONDAY)
        val classes = useCase(
            events = listOf(event(1, 8), event(2, 10), event(3, 12)),
            today = monday,
            now = LocalTime.of(7, 0)
        )
        assertEquals(1, classes.size)
        assertEquals(8, classes.first().startHour)
    }

    @Test
    fun `dated events match by date string`() {
        val classes = useCase(
            events = listOf(event(1, 8, dateString = "2030-09-02")),
            today = monday,
            now = LocalTime.of(7, 0)
        )
        assertEquals(1, classes.size)
    }

    @Test
    fun `ongoing class detection`() {
        val classes = useCase(
            events = listOf(event(1, 8), event(1, 12)),
            today = monday,
            now = LocalTime.of(9, 0)
        )
        val ongoing = useCase.ongoingClass(classes, LocalTime.of(9, 0))
        assertNotNull(ongoing)
        assertEquals(8, ongoing!!.startHour)
    }

    @Test
    fun `next class detection`() {
        val classes = useCase(
            events = listOf(event(1, 8), event(1, 12)),
            today = monday,
            now = LocalTime.of(7, 0)
        )
        val next = useCase.nextClass(classes, LocalTime.of(7, 0))
        assertEquals(8, next!!.startHour)
    }

    @Test
    fun `no next class after all started`() {
        val classes = useCase(
            events = listOf(event(1, 8)),
            today = monday,
            now = LocalTime.of(7, 0)
        )
        assertNull(useCase.nextClass(classes, LocalTime.of(10, 0)))
    }

    @Test
    fun `all week type events are visible regardless of parity`() {
        // Az A/B hét szűrést eltávolítottuk: minden felvett óra látszik
        val events = listOf(event(1, 8, weekType = 1), event(1, 10, weekType = 2))
        val classes = useCase(events, today = monday, now = LocalTime.of(7, 0))

        assertEquals(2, classes.size)
    }

    @Test
    fun `results are sorted by start time`() {
        val classes = useCase(
            events = listOf(event(1, 14), event(1, 8), event(1, 10)),
            today = monday,
            now = LocalTime.of(7, 0)
        )
        assertEquals(listOf(8, 10, 14), classes.map { it.startHour })
    }
}

class WeekParityUtilTest {

    @Test
    fun `odd ISO week is type 1 even is type 2`() {
        // 2030-01-01: ISO 1. hét -> páratlan -> A (1)
        assertEquals(1, WeekParityUtil.weekTypeOf(LocalDate.of(2030, 1, 1)))
        // 2030-01-08: ISO 2. hét -> páros -> B (2)
        assertEquals(2, WeekParityUtil.weekTypeOf(LocalDate.of(2030, 1, 8)))
    }

    @Test
    fun `weekTypeForOffset alternates`() {
        val base = WeekParityUtil.weekTypeForOffset(0)
        val next = WeekParityUtil.weekTypeForOffset(1)
        assertTrue(base != next)
        assertEquals(base, WeekParityUtil.weekTypeForOffset(2))
    }
}
