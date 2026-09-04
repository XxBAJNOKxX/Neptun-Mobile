package com.example

import com.example.data.network.NeptunApiClient
import org.junit.Assert.assertEquals
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
}
