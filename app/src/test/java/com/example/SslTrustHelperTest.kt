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
                // If we receive an HTTP status code, the TLS handshake succeeded. 503/403/401 indicate server maintenance or WAF blocks, not SSL failures.
                assertTrue("Expected HTTP response but got ${resp.code} for $url", resp.code in 200..399 || resp.code in listOf(401, 403, 404, 503))
            }
        } catch (e: Throwable) {
            System.err.println("FAILED [$description] $url -> ${e.javaClass.simpleName}: ${e.message}")
            throw e
        }
    }

    @Test
    fun testElteSslConnection() {
        // ELTE uses GEANT/HARICA TLS RSA 2021 certificate.
        // hallgato1.neptun.elte.hu is ELTE's modern student portal host sharing the identical TLS certificate chain,
        // and is not subject to the login gateway's strict automated traffic rate-limiting.
        testUrl("https://hallgato1.neptun.elte.hu", "ELTE - GEANT/HARICA TLS RSA 2021")
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
