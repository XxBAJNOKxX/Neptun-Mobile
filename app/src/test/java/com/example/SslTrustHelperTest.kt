package com.example

import com.example.core.network.SslTrustHelper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class SslTrustHelperTest {

    private val client: OkHttpClient = SslTrustHelper.configureOkHttpClient(
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(false)
    ).build()

    private fun testUrl(url: String, description: String) {
        val req = Request.Builder().url(url).get().build()
        try {
            client.newCall(req).execute().use { resp ->
                println("SUCCESS [$description] $url -> Code ${resp.code}")
                assertTrue("Expected 2xx or 3xx but got ${resp.code} for $url", resp.isSuccessful || resp.code in 200..399)
            }
        } catch (e: Throwable) {
            System.err.println("FAILED [$description] $url -> ${e.javaClass.simpleName}: ${e.message}")
            throw e
        }
    }

    @Test
    fun testElteSslConnection() {
        testUrl("https://neptun.elte.hu/Account/Login", "ELTE - GEANT/HARICA TLS RSA 2021")
    }

    @Test
    fun testBmeSslConnection() {
        testUrl("https://neptun.bme.hu/hallgatoi", "BME - GEANT/HARICA TLS RSA 2021")
    }

    @Test
    fun testCorvinusSslConnection() {
        testUrl("https://neptun3r.web.uni-corvinus.hu/Hallgatoi", "Corvinus - GEANT/HARICA TLS RSA 2021")
    }

    @Test
    fun testPpkeSslConnection() {
        testUrl("https://neptun2.ppke.hu/hallgato2_uj", "PPKE - Sectigo DV")
    }

    @Test
    fun testSemmelweisSslConnection() {
        testUrl("https://neptunweb.semmelweis.hu", "Semmelweis - Sectigo OV with bundled intermediate")
    }

    @Test
    fun testMiltonFriedmanSslConnection() {
        testUrl("https://neptun.uni-milton.hu", "Milton Friedman - Microsec e-Szigno 2023 link cert")
    }
}
