package com.example

import com.example.core.crash.CrashReporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashReporterTest {

    @Test
    fun `maskSensitiveData replaces Bearer tokens and Authorization header`() {
        val input = "Error calling API: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        val output = CrashReporter.maskSensitiveData(input)
        assertFalse(output.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
        assertTrue(output.contains("Authorization: [REDACTED]"))

        val standalone = "Received token Bearer abc123def456 from response"
        val standaloneOutput = CrashReporter.maskSensitiveData(standalone)
        assertFalse(standaloneOutput.contains("abc123def456"))
        assertTrue(standaloneOutput.contains("Bearer [REDACTED]"))
    }

    @Test
    fun `maskSensitiveData replaces passwords and credentials`() {
        val input = "Request payload: { \"username\": \"DEMO01\", \"password\": \"Secret123!\", \"sessionId\": \"abc123xyz\" }"
        val output = CrashReporter.maskSensitiveData(input)
        assertFalse(output.contains("Secret123!"))
        assertFalse(output.contains("DEMO01"))
        assertFalse(output.contains("abc123xyz"))
        assertTrue(output.contains("[REDACTED]"))
    }
}
