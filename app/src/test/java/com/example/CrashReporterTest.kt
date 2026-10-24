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

    @Test
    fun `maskSensitiveData replaces 2FA keys, tokens and codes`() {
        val input = "2FA state error: { \"Key\": \"3f8a91c2-98ab-43ef\", \"twoFactorCode\": \"654321\", \"codePrefix\": \"AZ-\" }"
        val output = CrashReporter.maskSensitiveData(input)
        assertFalse(output.contains("3f8a91c2-98ab-43ef"))
        assertFalse(output.contains("654321"))
        assertTrue(output.contains("[REDACTED]"))
    }

    @Test
    fun `maskSensitiveData replaces Cookie headers, UserLogin and devicecookie`() {
        val input = "Set-Cookie: .AspNetCore.Cookies=secretCookieVal123; Path=/\n" +
            "Body: {\"UserLogin\":\"ABC123\",\"Password\":\"MyPass99\",\"devicecookie\":\"dev98765\"}"
        val output = CrashReporter.maskSensitiveData(input)
        assertFalse(output.contains("secretCookieVal123"))
        assertFalse(output.contains("ABC123"))
        assertFalse(output.contains("MyPass99"))
        assertFalse(output.contains("dev98765"))
        assertTrue(output.contains("[REDACTED]"))
    }
}
