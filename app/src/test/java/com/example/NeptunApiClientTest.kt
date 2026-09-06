package com.example

import com.example.data.network.NeptunApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeptunApiClientTest {

    private val client = NeptunApiClient()

    @Test
    fun testNormalizeBaseUrl() {
        assertEquals("https://neptun2.ppke.hu/hallgato_uj", client.normalizeBaseUrl("https://neptun2.ppke.hu/hallgato_uj/"))
        assertEquals("https://neptun2.ppke.hu/hallgato_uj", client.normalizeBaseUrl("https://neptun2.ppke.hu/hallgato_uj/login.aspx"))
        assertEquals("https://neptun2.ppke.hu/hallgato_uj", client.normalizeBaseUrl("https://neptun2.ppke.hu/hallgato_uj/MobileService.svc"))
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
}

