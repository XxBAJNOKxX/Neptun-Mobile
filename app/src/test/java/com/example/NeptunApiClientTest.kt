package com.example

import com.example.data.network.NeptunApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeptunApiClientTest {

    private val client = NeptunApiClient(FakeStringProvider())

    @Test
    fun testNormalizeBaseUrl() {
        assertEquals("https://neptun2.ppke.hu/hallgato_uj", client.normalizeBaseUrl("https://neptun2.ppke.hu/hallgato_uj/"))
        assertEquals("https://neptun2.ppke.hu/hallgato_uj", client.normalizeBaseUrl("https://neptun2.ppke.hu/hallgato_uj/login.aspx"))
        assertEquals("https://neptun2.ppke.hu/hallgato_uj", client.normalizeBaseUrl("https://neptun2.ppke.hu/hallgato_uj/MobileService.svc"))
        assertEquals("https://neptun.elte.hu", client.normalizeBaseUrl("https://neptun.elte.hu/Account/Login"))
        assertEquals("https://neptun.elte.hu", client.normalizeBaseUrl("https://neptun.elte.hu/Account/Login2FA?NeptunCode=ABC"))
    }

    @Test
    fun testGetLegacyServiceUrl() {
        assertEquals(
            "https://neptun2.ppke.hu/hallgato_uj/MobileService.svc/GetCalendarData",
            client.getLegacyServiceUrl("https://neptun2.ppke.hu/hallgato_uj", "GetCalendarData")
        )
        assertEquals(
            "https://neptun2.ppke.hu/hallgato_uj/MobileService.svc/GetMarkbookData",
            client.getLegacyServiceUrl("https://neptun2.ppke.hu/hallgato_uj/MobileService.svc", "GetMarkbookData")
        )
        assertEquals(
            "https://neptun.bme.hu/hallgatoi/MobileService.svc/GetMessages",
            client.getLegacyServiceUrl("https://neptun.bme.hu/hallgatoi/", "GetMessages")
        )
    }

    @Test
    fun testParseCookiesAndFormInputs() {
        val rawHeaders = listOf(
            ".AspNetCore.Identity.TwoFactorRememberMe=val123; path=/; secure; samesite=lax; httponly",
            ".AspNetCore.Antiforgery.W31415=anti_xyz; path=/; samesite=strict; httponly"
        )
        val cookies = client.parseCookiesFromHeaders(rawHeaders)
        assertEquals("val123", cookies[".AspNetCore.Identity.TwoFactorRememberMe"])
        assertEquals("anti_xyz", cookies[".AspNetCore.Antiforgery.W31415"])

        val cookieStr = client.cookieHeaderString(cookies)
        assertTrue(cookieStr.contains(".AspNetCore.Identity.TwoFactorRememberMe=val123"))
        assertTrue(cookieStr.contains(".AspNetCore.Antiforgery.W31415=anti_xyz"))

        val mockHtml = """
            <form action="/Account/Login2FA" method="post">
                <input type="hidden" name="__RequestVerificationToken" value="token_abc_123" />
                <input type="hidden" name="Key" value="USER_KEY_XYZ" />
                <input type="hidden" name="Phase" value="Step2" />
                <input type="text" name="TwoFactorCode" value="" />
            </form>
        """.trimIndent()
        val inputs = client.parseFormInputs(mockHtml)
        assertEquals("token_abc_123", inputs["__RequestVerificationToken"])
        assertEquals("USER_KEY_XYZ", inputs["Key"])
        assertEquals("Step2", inputs["Phase"])
    }

    @Test
    fun testFormToNeptunParsing() {
        val formHtml = """
            <form id="FormToNeptun" action="/ToNeptunWeb/ToNeptunHWeb" method="post">
                <input type="hidden" name="NeptunWebType" value="HWeb" />
                <input type="hidden" name="NeptunWebIndex" value="2" />
                <input type="hidden" name="__RequestVerificationToken" value="cf_anti_token_999" />
            </form>
        """.trimIndent()
        val inputs = client.parseFormInputs(formHtml)
        assertEquals("HWeb", inputs["NeptunWebType"])
        assertEquals("2", inputs["NeptunWebIndex"])
        assertEquals("cf_anti_token_999", inputs["__RequestVerificationToken"])
    }

    @Test
    fun testIsJwtExpired() {
        // Empty / invalid tokens should be marked as expired
        assertTrue(client.isJwtExpired(""))
        assertTrue(client.isJwtExpired("invalid.token"))

        val encoder = java.util.Base64.getUrlEncoder().withoutPadding()

        // Future token (exp in 2049) -> not expired
        val futureJson = """{"exp":2500000000,"unique_name":"TEST01"}"""
        val futureToken = "eyJhbGciOiJIUzI1NiJ9." + encoder.encodeToString(futureJson.toByteArray()) + ".sig"
        org.junit.Assert.assertFalse(client.isJwtExpired(futureToken))

        // Past token (exp in 2017) -> expired
        val pastJson = """{"exp":1500000000,"unique_name":"TEST01"}"""
        val pastToken = "eyJhbGciOiJIUzI1NiJ9." + encoder.encodeToString(pastJson.toByteArray()) + ".sig"
        assertTrue(client.isJwtExpired(pastToken))

        // Token expiring in 10 seconds with default 30s buffer -> expired due to buffer
        val nowSec = System.currentTimeMillis() / 1000L
        val expiringSoonJson = """{"exp":${nowSec + 10},"unique_name":"TEST01"}"""
        val expiringSoonToken = "eyJhbGciOiJIUzI1NiJ9." + encoder.encodeToString(expiringSoonJson.toByteArray()) + ".sig"
        assertTrue(client.isJwtExpired(expiringSoonToken, bufferSeconds = 30L))

        // But with 0s buffer -> not expired yet
        org.junit.Assert.assertFalse(client.isJwtExpired(expiringSoonToken, bufferSeconds = 0L))
    }

    @Test
    fun testMergeCookies() {
        val oldCookies = mapOf("sess" to "old_val", "pref" to "dark")
        val newCookies = mapOf("sess" to "new_val", "extra" to "123")
        val merged = client.mergeCookies(oldCookies, newCookies)
        assertEquals("new_val", merged["sess"])
        assertEquals("dark", merged["pref"])
        assertEquals("123", merged["extra"])
    }
}

