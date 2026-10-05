package com.example

import com.example.data.network.NeptunApiClient
import com.example.data.network.NeptunUnauthorizedException
import com.example.domain.model.University
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SessionSyncAndKeepAliveTest {

    private fun createMockClient(
        statusCode: Int = 200,
        responseBody: String = "{}",
        contentType: String = "application/json",
        redirectUrl: String? = null
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request()
                val targetUrl = redirectUrl ?: req.url.toString()
                val builder = Response.Builder()
                    .request(req.newBuilder().url(targetUrl).build())
                    .protocol(Protocol.HTTP_1_1)
                    .code(statusCode)
                    .message(if (statusCode == 200) "OK" else "Error")
                    .body(responseBody.toResponseBody(contentType.toMediaType()))

                if (redirectUrl != null) {
                    builder.addHeader("Location", redirectUrl)
                }
                builder.build()
            }
            .build()
    }

    @Test
    fun `getMessages throws NeptunUnauthorizedException on HTTP 401`() = runTest {
        val client = NeptunApiClient(createMockClient(statusCode = 401, responseBody = "Unauthorized"))
        assertThrows(NeptunUnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.getMessages(
                    baseUrl = "https://neptun.elte.hu",
                    token = "expired_token",
                    username = "ELTE01",
                    password = "",
                    isModern = true
                )
            }
        }
    }

    @Test
    fun `getMessages throws NeptunUnauthorizedException when redirected to login HTML`() = runTest {
        val loginHtml = "<html><head><title>Neptun Login</title></head><body><form action=\"/Account/Login\"></body></html>"
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = loginHtml, contentType = "text/html"))
        assertThrows(NeptunUnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.getMessages(
                    baseUrl = "https://neptun.elte.hu",
                    token = "invalid_token",
                    username = "ELTE01",
                    password = "",
                    isModern = true
                )
            }
        }
    }

    @Test
    fun `getCalendarEvents throws NeptunUnauthorizedException on HTTP 401`() = runTest {
        val client = NeptunApiClient(createMockClient(statusCode = 401, responseBody = "Unauthorized"))
        assertThrows(NeptunUnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.getCalendarEvents(
                    baseUrl = "https://neptun.elte.hu",
                    token = "expired_token",
                    username = "ELTE01",
                    password = "",
                    isModern = true
                )
            }
        }
    }

    @Test
    fun `getGrades throws NeptunUnauthorizedException on HTTP 401`() = runTest {
        val client = NeptunApiClient(createMockClient(statusCode = 401, responseBody = "Unauthorized"))
        assertThrows(NeptunUnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.getGrades(
                    baseUrl = "https://neptun.elte.hu",
                    token = "expired_token",
                    username = "ELTE01",
                    password = "",
                    isModern = true
                )
            }
        }
    }

    @Test
    fun `getFinances throws NeptunUnauthorizedException on HTTP 401`() = runTest {
        val client = NeptunApiClient(createMockClient(statusCode = 401, responseBody = "Unauthorized"))
        assertThrows(NeptunUnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.getFinances(
                    baseUrl = "https://neptun.elte.hu",
                    token = "expired_token",
                    username = "ELTE01",
                    password = "",
                    isModern = true
                )
            }
        }
    }

    @Test
    fun `getExams throws NeptunUnauthorizedException on HTTP 401`() = runTest {
        val client = NeptunApiClient(createMockClient(statusCode = 401, responseBody = "Unauthorized"))
        assertThrows(NeptunUnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.getExams(
                    baseUrl = "https://neptun.elte.hu",
                    token = "expired_token",
                    username = "ELTE01",
                    password = "",
                    isModern = true
                )
            }
        }
    }

    @Test
    fun `getMessageContent throws NeptunUnauthorizedException on HTTP 401`() = runTest {
        val client = NeptunApiClient(createMockClient(statusCode = 401, responseBody = "Unauthorized"))
        assertThrows(NeptunUnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.getMessageContent(
                    baseUrl = "https://neptun.elte.hu",
                    token = "expired_token",
                    messageId = "12345",
                    isModern = true
                )
            }
        }
    }

    @Test
    fun `getMessages parses empty list correctly with 200 OK`() = runTest {
        val emptyJson = """{"data":{"receivedMessages":[]}}"""
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = emptyJson))
        val messages = client.getMessages(
            baseUrl = "https://neptun2.ppke.hu/hallgato_uj",
            token = "valid_token",
            username = "PPKE01",
            password = "",
            isModern = true
        )
        assertNotNull(messages)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `getCalendarEvents parses empty list correctly with 200 OK`() = runTest {
        val emptyJson = """{"data":[]}"""
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = emptyJson))
        val events = client.getCalendarEvents(
            baseUrl = "https://neptun2.ppke.hu/hallgato_uj",
            token = "valid_token",
            username = "PPKE01",
            password = "",
            isModern = true
        )
        assertNotNull(events)
        assertTrue(events.isEmpty())
    }

    @Test
    fun `getGrades parses empty list correctly with 200 OK`() = runTest {
        val emptyJson = """{"data":[]}"""
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = emptyJson))
        val grades = client.getGrades(
            baseUrl = "https://neptun2.ppke.hu/hallgato_uj",
            token = "valid_token",
            username = "PPKE01",
            password = "",
            isModern = true
        )
        assertNotNull(grades)
        assertTrue(grades.isEmpty())
    }

    @Test
    fun `getFinances parses empty list correctly with 200 OK`() = runTest {
        val emptyJson = """{"data":[]}"""
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = emptyJson))
        val finances = client.getFinances(
            baseUrl = "https://neptun2.ppke.hu/hallgato_uj",
            token = "valid_token",
            username = "PPKE01",
            password = "",
            isModern = true
        )
        assertNotNull(finances)
        assertTrue(finances.isEmpty())
    }

    @Test
    fun `getExams parses empty list correctly with 200 OK`() = runTest {
        val emptyJson = """{"data":[]}"""
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = emptyJson))
        val exams = client.getExams(
            baseUrl = "https://neptun2.ppke.hu/hallgato_uj",
            token = "valid_token",
            username = "PPKE01",
            password = "",
            isModern = true
        )
        assertNotNull(exams)
        assertTrue(exams.isEmpty())
    }

    @Test
    fun `university capabilities verify ELTE requires 2FA and keepalive whereas PPKE does not`() {
        val elte = University(
            id = "elte",
            name = "Eötvös Loránd Tudományegyetem",
            shortName = "ELTE",
            city = "Budapest",
            neptunUrl = "https://neptun.elte.hu",
            sessionKeepAlive = true,
            requiresInteractiveReauth = true
        )
        assertTrue(elte.sessionKeepAlive)
        assertTrue(elte.requiresInteractiveReauth)

        val ppke = University(
            id = "ppke",
            name = "Pázmány Péter Katolikus Egyetem",
            shortName = "PPKE",
            city = "Budapest",
            neptunUrl = "https://neptun2.ppke.hu/hallgato_uj",
            sessionKeepAlive = false,
            requiresInteractiveReauth = false
        )
        assertFalse(ppke.sessionKeepAlive)
        assertFalse(ppke.requiresInteractiveReauth)
    }

    @Test
    fun `pingSession correctly reports alive for valid session`() = runTest {
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = "{\"alive\":true}"))
        val ping = client.pingSession("https://neptun.elte.hu", "cookie=123", "ELTE01")
        assertTrue(ping.isAlive)
        assertFalse(ping.isRedirectToLogin)
    }

    @Test
    fun `pingSession correctly detects expired session on redirect to login`() = runTest {
        val loginHtml = "<html><body><form action=\"/Account/Login\">Login</form></body></html>"
        val client = NeptunApiClient(createMockClient(statusCode = 200, responseBody = loginHtml, contentType = "text/html"))
        val ping = client.pingSession("https://neptun.elte.hu", "cookie=123", "ELTE01")
        assertFalse(ping.isAlive)
        assertTrue(ping.isRedirectToLogin)
    }
}
