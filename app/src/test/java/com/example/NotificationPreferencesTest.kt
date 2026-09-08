package com.example

import com.example.core.security.NotificationPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPreferencesTest {

    @Test
    fun `quiet hours disabled by default`() {
        val prefs = NotificationPreferences()
        assertFalse(prefs.isQuietNow(23 * 60))
    }

    @Test
    fun `overnight window matches late evening and early morning`() {
        val prefs = NotificationPreferences(
            quietHoursEnabled = true,
            quietStartMinute = 22 * 60,
            quietEndMinute = 7 * 60
        )

        assertTrue(prefs.isQuietNow(23 * 60))          // 23:00
        assertTrue(prefs.isQuietNow(5 * 60))           // 05:00
        assertFalse(prefs.isQuietNow(12 * 60))         // 12:00
        assertFalse(prefs.isQuietNow(21 * 60 + 59))    // 21:59
        assertFalse(prefs.isQuietNow(7 * 60))          // 07:00 (vége)
    }

    @Test
    fun `same day window`() {
        val prefs = NotificationPreferences(
            quietHoursEnabled = true,
            quietStartMinute = 12 * 60,
            quietEndMinute = 14 * 60
        )

        assertTrue(prefs.isQuietNow(13 * 60))
        assertFalse(prefs.isQuietNow(11 * 60))
        assertFalse(prefs.isQuietNow(14 * 60))
    }

    @Test
    fun `zero length window is never quiet`() {
        val prefs = NotificationPreferences(
            quietHoursEnabled = true,
            quietStartMinute = 600,
            quietEndMinute = 600
        )
        assertFalse(prefs.isQuietNow(600))
    }

    @Test
    fun `minute formatting`() {
        assertEquals("22:00", NotificationPreferences.formatMinute(22 * 60))
        assertEquals("07:05", NotificationPreferences.formatMinute(7 * 60 + 5))
        assertEquals("00:00", NotificationPreferences.formatMinute(0))
    }
}
