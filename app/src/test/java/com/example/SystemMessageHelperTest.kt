package com.example

import com.example.core.util.SystemMessageHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemMessageHelperTest {

    @Test
    fun testNormalizeSenderName_systemSenders() {
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("SYSTEM USER"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("system user"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("SYSTEM"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("system_user"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("Rendszerüzenet"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("System message"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("Systemnachricht"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("Neptun"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("Neptun Rendszer"))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName(""))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName("   "))
        assertEquals("Rendszerüzenet", SystemMessageHelper.normalizeSenderName(null))
    }

    @Test
    fun testNormalizeSenderName_customFallback() {
        assertEquals("System Message", SystemMessageHelper.normalizeSenderName("SYSTEM USER", fallback = "System Message"))
        assertEquals("Systemnachricht", SystemMessageHelper.normalizeSenderName("SYSTEM", fallback = "Systemnachricht"))
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
