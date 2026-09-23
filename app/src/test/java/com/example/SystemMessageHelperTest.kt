package com.example

import com.example.core.util.SystemMessageHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemMessageHelperTest {

    @Test
    fun testNormalizeSenderName_systemSenders() {
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("SYSTEM USER"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("system user"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("SYSTEM"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("system_user"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("Rendszerüzenet"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("System message"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("Systemnachricht"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("Neptun"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("Neptun Rendszer"))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName(""))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName("   "))
        assertEquals("Neptun", SystemMessageHelper.normalizeSenderName(null))
    }

    @Test
    fun testNormalizeSenderName_regularSenders() {
        assertEquals("Dr. Szabó Péter", SystemMessageHelper.normalizeSenderName("Dr. Szabó Péter"))
        assertEquals("Központi Tanulmányi Hivatal", SystemMessageHelper.normalizeSenderName("Központi Tanulmányi Hivatal"))
        assertEquals("Kovács Tamás (Gyakorlatvezető)", SystemMessageHelper.normalizeSenderName("Kovács Tamás (Gyakorlatvezető)"))
    }

    @Test
    fun testIsSystemSender() {
        assertTrue(SystemMessageHelper.isSystemSender("SYSTEM USER"))
        assertTrue(SystemMessageHelper.isSystemSender("SYSTEM"))
        assertTrue(SystemMessageHelper.isSystemSender("system_user"))
        assertTrue(SystemMessageHelper.isSystemSender("Rendszerüzenet"))
        assertTrue(SystemMessageHelper.isSystemSender(""))
        assertTrue(SystemMessageHelper.isSystemSender(null))

        assertFalse(SystemMessageHelper.isSystemSender("Dr. Kovács István"))
        assertFalse(SystemMessageHelper.isSystemSender("Dékáni Hivatal"))
    }
}
