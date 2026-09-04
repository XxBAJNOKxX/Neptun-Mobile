package com.example.data.network

import android.util.Base64
import android.util.Log
import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

sealed class NeptunAuthResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String?,
        val deviceCookie: String?,
        val studentName: String,
        val trainingProgram: String,
        val isModernApi: Boolean,
        val normalizedBaseUrl: String
    ) : NeptunAuthResult()

    data class TwoFactorRequired(
        val twoFactorToken: String,
        val normalizedBaseUrl: String
    ) : NeptunAuthResult()

    data class Failure(val message: String) : NeptunAuthResult()
}

class NeptunApiClient {

    private val tag = "NeptunApiClient"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private val okHttpClient: OkHttpClient = try {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    } catch (e: Exception) {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    fun normalizeBaseUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }
        url = url.replace(Regex("""/login(\.aspx)?$""", RegexOption.IGNORE_CASE), "")
        url = url.replace(Regex("""/MobileService\.svc$""", RegexOption.IGNORE_CASE), "")
        return url
    }

    fun getLegacyBaseUrl(baseUrl: String): String {
        var clean = baseUrl.trim()
        while (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length - 1)
        }
        return if (clean.endsWith("/MobileService.svc", ignoreCase = true)) {
            clean
        } else {
            "$clean/MobileService.svc"
        }
    }

    fun getLegacyServiceUrl(baseUrl: String, method: String): String {
        val legacyBase = getLegacyBaseUrl(baseUrl)
        val cleanMethod = method.trimStart('/')
        return if (cleanMethod.isEmpty()) legacyBase else "$legacyBase/$cleanMethod"
    }

    private fun safeParseJson(raw: String?): JsonElement? {
        if (raw == null) return null
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("<") || (!trimmed.startsWith("{") && !trimmed.startsWith("["))) {
            return null
        }
        return try {
            json.parseToJsonElement(trimmed)
        } catch (e: Exception) {
            null
        }
    }

    private fun safeParseJsonObject(raw: String?): JsonObject? {
        return safeParseJson(raw) as? JsonObject
    }

    suspend fun authenticate(
        rawUrl: String,
        neptunCode: String,
        password: String,
        twoFactorCode: String = "",
        savedDeviceCookie: String = ""
    ): NeptunAuthResult = withContext(Dispatchers.IO) {
        val candidateUrls = if (rawUrl.contains("ppke.hu", ignoreCase = true)) {
            listOf(
                rawUrl,
                "https://neptun2.ppke.hu/hallgato_uj",
                "https://neptun3.ppke.hu/hallgato_uj",
                "https://neptun3.ppke.hu/hallgato3_uj",
                "https://neptun3.ppke.hu/hallgato2_uj"
            ).distinct()
        } else {
            listOf(rawUrl)
        }

        var lastFailure: NeptunAuthResult? = null
        for (targetUrl in candidateUrls) {
            val result = authenticateSingle(targetUrl, neptunCode, password, twoFactorCode, savedDeviceCookie)
            if (result is NeptunAuthResult.Success || result is NeptunAuthResult.TwoFactorRequired) {
                return@withContext result
            }
            lastFailure = result
        }
        return@withContext lastFailure ?: NeptunAuthResult.Failure("Nem sikerült kapcsolódni a Neptun szerverhez!")
    }

    private suspend fun authenticateSingle(
        rawUrl: String,
        neptunCode: String,
        password: String,
        twoFactorCode: String = "",
        savedDeviceCookie: String = ""
    ): NeptunAuthResult = withContext(Dispatchers.IO) {
        val username = neptunCode.trim().uppercase()
        val pwd = password.trim()
        val baseUrl = normalizeBaseUrl(rawUrl)
        val containsAspx = rawUrl.contains(".aspx", ignoreCase = true)

        if (containsAspx) {
            val legacySuccess = tryLegacyLogin(baseUrl, username, pwd)
            return@withContext if (legacySuccess) {
                NeptunAuthResult.Success(
                    accessToken = "legacy-token-$username",
                    refreshToken = null,
                    deviceCookie = null,
                    studentName = "Hallgató ($username)",
                    trainingProgram = "Egyetemi képzés",
                    isModernApi = false,
                    normalizedBaseUrl = getLegacyBaseUrl(baseUrl)
                )
            } else {
                NeptunAuthResult.Failure("Nem sikerült a bejelentkezés a Neptun kiszolgálóra!")
            }
        }

        // Modern API Login
        try {
            val modernUrl = "$baseUrl/api/Account/Authenticate"
            val requestJson = """
                {
                    "userName": "$username",
                    "password": "$pwd",
                    "captcha": "",
                    "captchaIdentifier": "",
                    "token": "$twoFactorCode",
                    "LCID": 1038
                }
            """.trimIndent()

            val reqBuilder = Request.Builder()
                .url(modernUrl)
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")

            if (savedDeviceCookie.isNotEmpty()) {
                val b64User = Base64.encodeToString(username.toByteArray(), Base64.NO_WRAP)
                reqBuilder.addHeader("Cookie", "devicecookie-$b64User=$savedDeviceCookie")
            }

            val response = okHttpClient.newCall(reqBuilder.build()).execute()
            val responseBody = response.body?.string() ?: "{}"

            val setCookieHeader = response.header("Set-Cookie") ?: ""
            val extractedCookie = extractDeviceCookie(setCookieHeader)
            val extractedRefreshToken = extractRefreshToken(setCookieHeader)

            if (responseBody.trim().startsWith("<")) {
                // Fallback to legacy if modern API route returned webpage
                val fallbackLegacy = tryLegacyLogin(baseUrl, username, pwd)
                if (fallbackLegacy) {
                    return@withContext NeptunAuthResult.Success(
                        accessToken = "legacy-token-$username",
                        refreshToken = null,
                        deviceCookie = null,
                        studentName = "Hallgató ($username)",
                        trainingProgram = "Egyetemi képzés",
                        isModernApi = false,
                        normalizedBaseUrl = getLegacyBaseUrl(baseUrl)
                    )
                }
                return@withContext NeptunAuthResult.Failure("A Neptun szervere nem elérhető (HTML válasz).")
            }

            val parsed = safeParseJsonObject(responseBody)
            if (parsed == null) {
                val fallbackLegacy = tryLegacyLogin(baseUrl, username, pwd)
                if (fallbackLegacy) {
                    return@withContext NeptunAuthResult.Success(
                        accessToken = "legacy-token-$username",
                        refreshToken = null,
                        deviceCookie = null,
                        studentName = "Hallgató ($username)",
                        trainingProgram = "Egyetemi képzés",
                        isModernApi = false,
                        normalizedBaseUrl = getLegacyBaseUrl(baseUrl)
                    )
                }
                return@withContext NeptunAuthResult.Failure("Érvénytelen válasz a szervertől.")
            }

            val dataObj = parsed["data"]?.jsonObject
            val is2Fa = dataObj != null && (
                    dataObj["isTwoFactorRequired"]?.jsonPrimitive?.booleanOrNull == true ||
                            dataObj["requiresTwoFactor"]?.jsonPrimitive?.booleanOrNull == true
                    )

            if (is2Fa) {
                val twoFactorToken = dataObj["twoFactorLoginToken"]?.jsonPrimitive?.contentOrNull ?: ""
                return@withContext NeptunAuthResult.TwoFactorRequired(
                    twoFactorToken = twoFactorToken,
                    normalizedBaseUrl = baseUrl
                )
            }

            val accessToken = dataObj?.get("accessToken")?.jsonPrimitive?.contentOrNull
            if (!accessToken.isNullOrEmpty()) {
                val trainingName = try {
                    getStudentTrainingInfo(baseUrl, accessToken)?.second ?: "Egyetemi képzés"
                } catch (e: Exception) {
                    "Egyetemi képzés"
                }

                return@withContext NeptunAuthResult.Success(
                    accessToken = accessToken,
                    refreshToken = extractedRefreshToken,
                    deviceCookie = extractedCookie,
                    studentName = "Hallgató ($username)",
                    trainingProgram = trainingName,
                    isModernApi = true,
                    normalizedBaseUrl = baseUrl
                )
            }

            val errMsg = parsed["errorMessage"]?.jsonPrimitive?.contentOrNull
                ?: parsed["ErrorMessage"]?.jsonPrimitive?.contentOrNull
                ?: parsed["message"]?.jsonPrimitive?.contentOrNull
                ?: "Hibás felhasználónév vagy jelszó!"

            if (response.code == 404) {
                val legacySuccess = tryLegacyLogin(baseUrl, username, pwd)
                if (legacySuccess) {
                    return@withContext NeptunAuthResult.Success(
                        accessToken = "legacy-token-$username",
                        refreshToken = null,
                        deviceCookie = null,
                        studentName = "Hallgató ($username)",
                        trainingProgram = "Egyetemi képzés",
                        isModernApi = false,
                        normalizedBaseUrl = getLegacyBaseUrl(baseUrl)
                    )
                }
            }

            NeptunAuthResult.Failure(errMsg)
        } catch (e: Exception) {
            Log.e(tag, "Modern login failed, attempting legacy fallback: ${e.message}")
            val fallbackLegacy = tryLegacyLogin(baseUrl, username, pwd)
            if (fallbackLegacy) {
                NeptunAuthResult.Success(
                    accessToken = "legacy-token-$username",
                    refreshToken = null,
                    deviceCookie = null,
                    studentName = "Hallgató ($username)",
                    trainingProgram = "Egyetemi képzés",
                    isModernApi = false,
                    normalizedBaseUrl = getLegacyBaseUrl(baseUrl)
                )
            } else {
                NeptunAuthResult.Failure("Hálózati hiba a Neptunhoz kapcsolódáskor: ${e.localizedMessage}")
            }
        }
    }

    suspend fun refreshAccessToken(baseUrl: String, refreshToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/api/Account/GetNewTokens"
            val req = Request.Builder()
                .url(url)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $refreshToken")
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string()
                val parsed = safeParseJsonObject(body)
                return@withContext parsed?.get("data")?.jsonObject?.get("accessToken")?.jsonPrimitive?.contentOrNull
            }
        } catch (e: Exception) {
            Log.e(tag, "Refresh token error: ${e.message}")
        }
        null
    }

    private fun extractDeviceCookie(cookieHeader: String): String? {
        val regex = Regex("""devicecookie-[a-zA-Z0-9+/=]+=([a-zA-Z0-9+/=]+)""")
        return regex.find(cookieHeader)?.groupValues?.getOrNull(1)
    }

    private fun extractRefreshToken(cookieHeader: String): String? {
        val regex = Regex("""[^=;\s,]+=(eyJ[a-zA-Z0-9\-_\.]+)""")
        return regex.find(cookieHeader)?.groupValues?.getOrNull(1)
    }

    private suspend fun tryLegacyLogin(baseUrl: String, username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = getLegacyServiceUrl(baseUrl, "GetTrainings")
            val body = """{"UserLogin":"$username","Password":"$password"}"""
            val req = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext false
            val respBody = resp.body?.string() ?: ""
            val parsed = safeParseJsonObject(respBody) ?: return@withContext false
            return@withContext parsed["ErrorMessage"] == null || parsed["ErrorMessage"]?.jsonPrimitive?.contentOrNull.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e(tag, "Legacy login error: ${e.message}")
        }
        false
    }

    // --- STUDENT TRAINING INFO ---
    suspend fun getStudentTrainingInfo(baseUrl: String, token: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/api/Calendar/GetStudentTrainings"
            val req = Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val parsed = safeParseJsonObject(body) ?: return@withContext null
                val dataArr = parsed["data"]?.jsonArray
                if (!dataArr.isNullOrEmpty()) {
                    var selected = dataArr.first().jsonObject
                    for (item in dataArr) {
                        val obj = item.jsonObject
                        if (obj["actualStudentTraining"]?.jsonPrimitive?.booleanOrNull == true) {
                            selected = obj
                            break
                        }
                    }
                    val id = selected["studentTrainingId"]?.jsonPrimitive?.contentOrNull ?: ""
                    val name = selected["trainingName"]?.jsonPrimitive?.contentOrNull ?: "Mérnökinformatikus képzés"
                    return@withContext Pair(id, name)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "GetStudentTrainings error: ${e.message}")
        }
        null
    }

    // --- CALENDAR & TIMETABLE ---
    suspend fun getCalendarEvents(
        baseUrl: String,
        token: String,
        trainingId: String?,
        username: String,
        password: String,
        isModern: Boolean
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        if (!isModern) {
            return@withContext getLegacyCalendarEvents(baseUrl, username, password)
        }

        try {
            val now = LocalDate.now()
            val startWindow = now.minusMonths(2).withDayOfMonth(1)
            val endWindow = now.plusMonths(5).let { it.withDayOfMonth(it.lengthOfMonth()) }

            val startIso = startWindow.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00"))
            val endIso = endWindow.atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'23:59:59"))

            val trainId = trainingId ?: getStudentTrainingInfo(baseUrl, token)?.first ?: ""

            var events = fetchModernCalendarEvents(baseUrl, token, trainId, startIso, endIso)

            if (events.isEmpty() && trainId.isNotEmpty()) {
                events = fetchModernCalendarEvents(baseUrl, token, "", startIso, endIso)
            }

            if (events.isEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                events = getLegacyCalendarEvents(baseUrl, username, password)
            }

            return@withContext events
        } catch (e: Exception) {
            Log.e(tag, "Failed to get modern calendar events: ${e.message}")
            if (username.isNotEmpty() && password.isNotEmpty()) {
                return@withContext getLegacyCalendarEvents(baseUrl, username, password)
            }
            return@withContext emptyList()
        }
    }

    private fun extractAllText(element: JsonElement?): String {
        if (element == null) return ""
        return try {
            when (element) {
                is JsonPrimitive -> element.contentOrNull ?: element.toString()
                is JsonObject -> element.values.joinToString(" ") { extractAllText(it) }
                is JsonArray -> element.joinToString(" ") { extractAllText(it) }
                else -> ""
            }
        } catch (e: Exception) {
            element.toString()
        }
    }

    private fun detectCourseType(item: JsonElement, courseCode: String = "", name: String = ""): CourseType {
        val fullText = (extractAllText(item) + " " + courseCode + " " + name).lowercase()
        return when {
            fullText.contains("labor") || fullText.contains("lab") || courseCode.contains("lab", ignoreCase = true) || courseCode.startsWith("L", ignoreCase = true) -> CourseType.LAB
            fullText.contains("gyakorlat") || fullText.contains("gyak") || courseCode.contains("gyak", ignoreCase = true) || courseCode.startsWith("G", ignoreCase = true) -> CourseType.PRACTICE
            fullText.contains("szeminárium") || fullText.contains("szem") -> CourseType.SEMINAR
            fullText.contains("vizsga") -> CourseType.EXAM
            fullText.contains("előadás") || fullText.contains("elmélet") || fullText.contains("ea") -> CourseType.LECTURE
            else -> CourseType.LECTURE
        }
    }

    private suspend fun fetchModernCalendarEvents(
        baseUrl: String,
        token: String,
        trainId: String,
        startIso: String,
        endIso: String
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = StringBuilder("$baseUrl/api/Calendar/GetCalendarEvents")
            urlBuilder.append("?startDate=").append(startIso)
            urlBuilder.append("&endDate=").append(endIso)
            urlBuilder.append("&isClassesVisible=true")
            urlBuilder.append("&isExamsVisible=true")
            urlBuilder.append("&isFinalExamsVisible=true")
            urlBuilder.append("&isOtherEventsVisible=true")
            urlBuilder.append("&isTasksVisible=true")
            urlBuilder.append("&isPeriodsVisible=false")
            urlBuilder.append("&isOnlineMeetingsVisible=false")
            urlBuilder.append("&displayClasses=true")
            urlBuilder.append("&displayExams=true")
            urlBuilder.append("&displayOtherEvents=true")
            urlBuilder.append("&displayTasks=true")
            if (trainId.isNotEmpty()) {
                urlBuilder.append("&studentTrainingIds[0]=").append(trainId)
                urlBuilder.append("&studentTrainingIds=").append(trainId)
            }

            val req = Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext emptyList()
            val respBody = resp.body?.string() ?: ""
            val parsed = safeParseJsonObject(respBody) ?: return@withContext emptyList()
            val dataPart = parsed["data"] ?: parsed["calendarData"] ?: parsed["events"]

            val items: List<JsonElement> = when (dataPart) {
                is JsonArray -> dataPart
                is JsonObject -> listOf(dataPart)
                else -> emptyList()
            }

            val list = mutableListOf<CalendarEvent>()
            for (item in items) {
                val obj = item.jsonObject
                val startRaw = obj["startDate"]?.jsonPrimitive?.contentOrNull
                    ?: obj["start"]?.jsonPrimitive?.contentOrNull
                    ?: ""
                val endRaw = obj["endDate"]?.jsonPrimitive?.contentOrNull
                    ?: obj["end"]?.jsonPrimitive?.contentOrNull
                    ?: ""
                val name = obj["name"]?.jsonPrimitive?.contentOrNull
                    ?: obj["subjectName"]?.jsonPrimitive?.contentOrNull
                    ?: obj["title"]?.jsonPrimitive?.contentOrNull
                    ?: "Óra"
                val subjectCode = obj["subjectCode"]?.jsonPrimitive?.contentOrNull ?: ""
                val courseCode = obj["courseCode"]?.jsonPrimitive?.contentOrNull ?: ""
                val room = obj["rooms"]?.jsonPrimitive?.contentOrNull
                    ?: obj["room"]?.jsonPrimitive?.contentOrNull
                    ?: obj["location"]?.jsonPrimitive?.contentOrNull
                    ?: "Nincs megadva"
                val tutor = obj["courseTutor"]?.jsonPrimitive?.contentOrNull
                    ?: obj["teacher"]?.jsonPrimitive?.contentOrNull
                    ?: "Oktató nincs megadva"

                val startDt = try {
                    LocalDateTime.parse(startRaw.substringBefore("."))
                } catch (e: Exception) {
                    LocalDateTime.now()
                }

                val endDt = try {
                    LocalDateTime.parse(endRaw.substringBefore("."))
                } catch (e: Exception) {
                    startDt.plusHours(2)
                }

                val dayOfWeekNum = startDt.dayOfWeek.value
                val courseType = detectCourseType(item, courseCode, name)

                list.add(
                    CalendarEvent(
                        id = obj["id"]?.jsonPrimitive?.contentOrNull ?: "evt_${System.nanoTime()}_${list.size}",
                        subjectName = name,
                        subjectCode = subjectCode.ifEmpty { courseCode }.ifEmpty { "-" },
                        courseCode = courseCode,
                        location = room,
                        room = room,
                        teacherName = tutor,
                        startHour = startDt.hour,
                        startMinute = startDt.minute,
                        endHour = endDt.hour,
                        endMinute = endDt.minute,
                        dayOfWeek = dayOfWeekNum,
                        courseType = courseType,
                        dateString = startDt.toLocalDate().toString()
                    )
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(tag, "fetchModernCalendarEvents error: ${e.message}")
            return@withContext emptyList()
        }
    }

    private suspend fun getLegacyCalendarEvents(baseUrl: String, user: String, pass: String): List<CalendarEvent> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val startEpoch = now - 7 * 86400000L
            val endEpoch = now + 21 * 86400000L

            val body = """
                {
                    "UserLogin": "$user",
                    "Password": "$pass",
                    "Time": true,
                    "Exam": true,
                    "startDate": "/Date($startEpoch)/",
                    "endDate": "/Date($endEpoch)/",
                    "TotalRowCount": -1
                }
            """.trimIndent()

            val url = getLegacyServiceUrl(baseUrl, "GetCalendarData")
            val req = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext emptyList()
            val respBody = resp.body?.string() ?: ""
            val parsed = safeParseJsonObject(respBody) ?: return@withContext emptyList()
            val calData = parsed["calendarData"]?.jsonArray ?: parsed["CalendarData"]?.jsonArray ?: return@withContext emptyList()

            val list = mutableListOf<CalendarEvent>()
            for (item in calData) {
                val obj = item.jsonObject
                val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "Óra"
                val location = obj["location"]?.jsonPrimitive?.contentOrNull ?: "Nincs megadva"
                val cCode = obj["courseCode"]?.jsonPrimitive?.contentOrNull ?: ""
                val teacher = obj["teacher"]?.jsonPrimitive?.contentOrNull
                    ?: obj["tutor"]?.jsonPrimitive?.contentOrNull
                    ?: "Oktató"

                val rawStart = obj["start"]?.jsonPrimitive?.contentOrNull?.replace(Regex("""\D"""), "")?.toLongOrNull() ?: now
                val rawEnd = obj["end"]?.jsonPrimitive?.contentOrNull?.replace(Regex("""\D"""), "")?.toLongOrNull() ?: (now + 5400000L)

                val startInstant = Instant.ofEpochMilli(rawStart).atZone(ZoneId.systemDefault()).toLocalDateTime()
                val endInstant = Instant.ofEpochMilli(rawEnd).atZone(ZoneId.systemDefault()).toLocalDateTime()

                val courseType = detectCourseType(item, cCode, title)

                list.add(
                    CalendarEvent(
                        id = "leg_${startInstant.toLocalDate()}_${startInstant.hour}_${title.hashCode()}",
                        subjectName = title,
                        subjectCode = if (cCode.isNotBlank()) cCode else "-",
                        courseCode = if (cCode.isNotBlank()) cCode else "-",
                        location = location,
                        room = location,
                        teacherName = teacher,
                        startHour = startInstant.hour,
                        startMinute = startInstant.minute,
                        endHour = endInstant.hour,
                        endMinute = endInstant.minute,
                        dayOfWeek = startInstant.dayOfWeek.value,
                        courseType = courseType,
                        dateString = startInstant.toLocalDate().toString()
                    )
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(tag, "Legacy calendar error: ${e.message}")
            return@withContext emptyList()
        }
    }

    // --- GRADES & MARKBOOK ---
    suspend fun getGrades(
        baseUrl: String,
        token: String,
        username: String,
        password: String,
        isModern: Boolean
    ): List<SubjectGrade> = withContext(Dispatchers.IO) {
        if (!isModern) {
            return@withContext getLegacyGrades(baseUrl, username, password)
        }

        try {
            val candidateTerms = mutableListOf<Pair<String, String>>()

            // 1. Fetch terms
            try {
                val termsUrl = "$baseUrl/api/TakenSubjects/Terms"
                val termsReq = Request.Builder()
                    .url(termsUrl)
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val termsResp = okHttpClient.newCall(termsReq).execute()
                if (termsResp.isSuccessful) {
                    val termsBody = termsResp.body?.string() ?: ""
                    val termsData = safeParseJsonObject(termsBody)?.get("data")?.jsonArray

                    if (!termsData.isNullOrEmpty()) {
                        for (termItem in termsData.reversed()) {
                            val tObj = termItem.jsonObject
                            val tidRaw = when (val v = tObj["value"] ?: tObj["termId"] ?: tObj["id"] ?: tObj["Id"]) {
                                is JsonObject -> v["id"]?.jsonPrimitive?.contentOrNull ?: v["termId"]?.jsonPrimitive?.contentOrNull ?: v["name"]?.jsonPrimitive?.contentOrNull ?: v.toString()
                                is JsonElement -> v.jsonPrimitive.contentOrNull ?: ""
                                else -> ""
                            }
                            val tnameRaw = when (val t = tObj["text"] ?: tObj["termName"] ?: tObj["name"] ?: tObj["Text"]) {
                                is JsonObject -> t["name"]?.jsonPrimitive?.contentOrNull ?: t["text"]?.jsonPrimitive?.contentOrNull ?: t.toString()
                                is JsonElement -> t.jsonPrimitive.contentOrNull ?: ""
                                else -> ""
                            }
                            val tid = cleanTermString(tidRaw)
                            val tname = cleanTermString(tnameRaw).ifEmpty { tid }
                            if (tid.isNotEmpty() && candidateTerms.none { it.first == tid }) {
                                candidateTerms.add(tid to tname)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Terms fetch failed: ${e.message}")
            }

            val allGrades = mutableListOf<SubjectGrade>()

            if (candidateTerms.isEmpty()) {
                candidateTerms.add("" to "Aktuális félév")
            }

            for ((termId, termName) in candidateTerms) {
                val urlsToTry = if (termId.isNotEmpty()) {
                    listOf(
                        "$baseUrl/api/TakenSubjects?request.termId=$termId&sortAndPage.firstRow=0&sortAndPage.lastRow=100",
                        "$baseUrl/api/TakenSubjects?termId=$termId&sortAndPage.firstRow=0&sortAndPage.lastRow=100",
                        "$baseUrl/api/TakenSubjects/GetTakenSubjects?termId=$termId"
                    )
                } else {
                    listOf(
                        "$baseUrl/api/TakenSubjects?sortAndPage.firstRow=0&sortAndPage.lastRow=100",
                        "$baseUrl/api/TakenSubjects",
                        "$baseUrl/api/SubjectCourse/GetTakenSubjects"
                    )
                }

                for (subjectsUrl in urlsToTry) {
                    try {
                        val subReq = Request.Builder()
                            .url(subjectsUrl)
                            .get()
                            .addHeader("Authorization", "Bearer $token")
                            .addHeader("Content-Type", "application/json")
                            .build()

                        val subResp = okHttpClient.newCall(subReq).execute()
                        if (!subResp.isSuccessful) continue
                        val subBody = subResp.body?.string() ?: ""
                        val parsed = safeParseJson(subBody) ?: continue
                        val subjectsData = when {
                            parsed is JsonObject && parsed["data"] is JsonArray -> parsed["data"]?.jsonArray
                            parsed is JsonObject && parsed["data"] is JsonObject && parsed["data"]?.jsonObject?.get("takenSubjects") is JsonArray ->
                                parsed["data"]?.jsonObject?.get("takenSubjects")?.jsonArray
                            parsed is JsonObject && parsed["data"] is JsonObject && parsed["data"]?.jsonObject?.get("subjects") is JsonArray ->
                                parsed["data"]?.jsonObject?.get("subjects")?.jsonArray
                            parsed is JsonObject && parsed["takenSubjects"] is JsonArray -> parsed["takenSubjects"]?.jsonArray
                            parsed is JsonArray -> parsed
                            else -> null
                        }

                        if (!subjectsData.isNullOrEmpty()) {
                            for (item in subjectsData) {
                                val obj = item.jsonObject
                                val subjectId = obj["subjectId"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["id"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["ID"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["subjectCode"]?.jsonPrimitive?.contentOrNull
                                    ?: "sub_${System.nanoTime()}_${allGrades.size}"
                                val subjectName = obj["subjectName"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["objectName"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["name"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["title"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["SubjectName"]?.jsonPrimitive?.contentOrNull
                                    ?: "Tantárgy"
                                val subjectCode = obj["subjectCode"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["objectCode"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["code"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["SubjectCode"]?.jsonPrimitive?.contentOrNull
                                    ?: "KÓD"
                                val credit = obj["subjectCredit"]?.jsonPrimitive?.intOrNull
                                    ?: obj["credit"]?.jsonPrimitive?.intOrNull
                                    ?: obj["creditValue"]?.jsonPrimitive?.intOrNull
                                    ?: obj["Credit"]?.jsonPrimitive?.intOrNull
                                    ?: 0

                                var grade: Int? = obj["resultValue"]?.jsonPrimitive?.intOrNull
                                    ?: obj["mark"]?.jsonPrimitive?.intOrNull
                                    ?: obj["grade"]?.jsonPrimitive?.intOrNull
                                var gradeText = obj["result"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["resultName"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["statusText"]?.jsonPrimitive?.contentOrNull
                                    ?: ""
                                if (grade == null && gradeText.isNotEmpty()) {
                                    val parsedG = parseTextToGrade(gradeText)
                                    if (parsedG > 0) grade = parsedG
                                }

                                val statusStr = obj["status"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["subjectStatus"]?.jsonPrimitive?.contentOrNull
                                    ?: obj["statusText"]?.jsonPrimitive?.contentOrNull
                                    ?: ""
                                var isSigned = obj["isSigned"]?.jsonPrimitive?.booleanOrNull
                                    ?: obj["completed"]?.jsonPrimitive?.booleanOrNull
                                    ?: obj["passed"]?.jsonPrimitive?.booleanOrNull
                                    ?: false
                                if (statusStr.contains("Teljesít", ignoreCase = true) || statusStr.contains("Aláír", ignoreCase = true)) {
                                    isSigned = true
                                }

                                val rawTermId = termId.ifEmpty {
                                    obj["termId"]?.jsonPrimitive?.contentOrNull ?: "2025/26/1"
                                }
                                val rawTermName = termName.ifEmpty {
                                    obj["termName"]?.jsonPrimitive?.contentOrNull ?: "2025/26/1 félév"
                                }
                                val effectiveTermId = cleanTermString(rawTermId)
                                val effectiveTermName = cleanTermString(rawTermName)

                                val itemUniqueId = "${effectiveTermId}_${subjectCode}_$subjectId"
                                if (allGrades.none { it.id == itemUniqueId || (it.subjectCode == subjectCode && it.termId == effectiveTermId) }) {
                                    allGrades.add(
                                        SubjectGrade(
                                            id = itemUniqueId,
                                            termId = effectiveTermId,
                                            termName = effectiveTermName,
                                            subjectName = subjectName,
                                            subjectCode = subjectCode,
                                            credit = credit,
                                            grade = grade,
                                            gradeText = gradeText,
                                            isSigned = isSigned
                                        )
                                    )
                                }
                            }
                            break
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed URL $subjectsUrl: ${e.message}")
                    }
                }
            }

            // Also check OfferedGrades
            try {
                val offeredUrl = "$baseUrl/api/OfferedGrades/GetOfferedGrades?sortAndPage.firstRow=0&sortAndPage.lastRow=50"
                val offReq = Request.Builder()
                    .url(offeredUrl)
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()
                val offResp = okHttpClient.newCall(offReq).execute()
                if (offResp.isSuccessful) {
                    val offBody = offResp.body?.string() ?: ""
                    val offData = safeParseJsonObject(offBody)?.get("data")?.jsonArray
                    if (!offData.isNullOrEmpty()) {
                        for (offItem in offData) {
                            val offObj = offItem.jsonObject
                            val sCode = offObj["subjectCode"]?.jsonPrimitive?.contentOrNull ?: ""
                            val resVal = offObj["resultValue"]?.jsonPrimitive?.intOrNull
                            val resName = offObj["resultName"]?.jsonPrimitive?.contentOrNull ?: ""
                            if (sCode.isNotEmpty() && (resVal != null || resName.isNotEmpty())) {
                                val idx = allGrades.indexOfFirst { it.subjectCode.equals(sCode, ignoreCase = true) }
                                if (idx >= 0 && allGrades[idx].grade == null) {
                                    val cur = allGrades[idx]
                                    allGrades[idx] = cur.copy(
                                        grade = resVal ?: parseTextToGrade(resName),
                                        gradeText = resName.ifEmpty { "Megajánlott ($resVal)" },
                                        isSigned = true
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "OfferedGrades optional check: ${e.message}")
            }

            if (allGrades.isEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                getLegacyGrades(baseUrl, username, password)
            } else {
                allGrades
            }
        } catch (e: Exception) {
            Log.e(tag, "Modern grades fetch error: ${e.message}")
            if (username.isNotEmpty() && password.isNotEmpty()) {
                getLegacyGrades(baseUrl, username, password)
            } else {
                emptyList()
            }
        }
    }

    private suspend fun getLegacyGrades(baseUrl: String, user: String, pass: String): List<SubjectGrade> = withContext(Dispatchers.IO) {
        try {
            val body = """{"UserLogin":"$user","Password":"$pass","CurrentPage":1,"filter":{"TermID":0},"TotalRowCount":-1}"""
            val url = getLegacyServiceUrl(baseUrl, "GetMarkbookData")
            val req = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext emptyList()
            val respBody = resp.body?.string() ?: ""
            val parsed = safeParseJsonObject(respBody) ?: return@withContext emptyList()
            val markbookList = parsed["MarkBookList"]?.jsonArray ?: parsed["markBookList"]?.jsonArray ?: return@withContext emptyList()

            val list = mutableListOf<SubjectGrade>()
            for (item in markbookList) {
                val obj = item.jsonObject
                val subName = obj["SubjectName"]?.jsonPrimitive?.contentOrNull
                    ?: obj["subjectName"]?.jsonPrimitive?.contentOrNull
                    ?: "Tantárgy"
                val subId = obj["ID"]?.jsonPrimitive?.contentOrNull
                    ?: obj["id"]?.jsonPrimitive?.contentOrNull
                    ?: "mb_${System.nanoTime()}"
                val credit = obj["Credit"]?.jsonPrimitive?.intOrNull
                    ?: obj["credit"]?.jsonPrimitive?.intOrNull
                    ?: 0
                val values = obj["Values"]?.jsonPrimitive?.contentOrNull
                    ?: obj["values"]?.jsonPrimitive?.contentOrNull
                    ?: ""
                val completed = obj["Completed"]?.jsonPrimitive?.booleanOrNull
                    ?: obj["completed"]?.jsonPrimitive?.booleanOrNull
                    ?: false
                val parsedGrade = parseTextToGrade(values)

                list.add(
                    SubjectGrade(
                        id = subId,
                        termId = "term_legacy",
                        termName = "Félévi jegyek",
                        subjectName = subName,
                        subjectCode = subId,
                        credit = credit,
                        grade = if (parsedGrade > 0) parsedGrade else null,
                        gradeText = values,
                        isSigned = completed
                    )
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(tag, "Legacy grades error: ${e.message}")
            emptyList()
        }
    }

    private fun parseTextToGrade(text: String): Int {
        val lower = text.lowercase()
        val digitMatch = Regex("""\b([1-5])\b""").find(text)
        if (digitMatch != null) {
            return digitMatch.groupValues[1].toInt()
        }
        return when {
            lower.contains("jeles") -> 5
            lower.contains("jó") -> 4
            lower.contains("közepes") -> 3
            lower.contains("elégséges") -> 2
            lower.contains("elégtelen") -> 1
            lower.contains("megfelelt") -> 5
            else -> 0
        }
    }

    // --- MESSAGES ---
    suspend fun getMessages(
        baseUrl: String,
        token: String,
        username: String,
        password: String,
        isModern: Boolean,
        page: Int = 1
    ): List<NeptunMessage> = withContext(Dispatchers.IO) {
        if (!isModern) {
            return@withContext getLegacyMessages(baseUrl, username, password, page)
        }

        try {
            val firstRow = (page - 1) * 25
            val lastRow = firstRow + 25
            val url = "$baseUrl/api/Message/GetReceivedMessages?firstRow=$firstRow&lastRow=$lastRow&filterType=0"

            val req = Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext emptyList()
            val respBody = resp.body?.string() ?: ""
            val parsed = safeParseJsonObject(respBody) ?: return@withContext emptyList()
            val recMessages = parsed["data"]?.jsonObject?.get("receivedMessages")?.jsonArray ?: return@withContext emptyList()

            val list = mutableListOf<NeptunMessage>()
            for (item in recMessages) {
                val obj = item.jsonObject
                val subject = obj["subject"]?.jsonPrimitive?.contentOrNull ?: "Nincs tárgy"
                val sender = obj["senderName"]?.jsonPrimitive?.contentOrNull ?: "Ismeretlen feladó"
                val dateStr = obj["lastPostDate"]?.jsonPrimitive?.contentOrNull ?: ""
                val unreadCount = obj["unreadedPostCount"]?.jsonPrimitive?.intOrNull ?: 0

                val id = obj["messageId"]?.jsonPrimitive?.contentOrNull
                    ?: obj["MessageID"]?.jsonPrimitive?.contentOrNull
                    ?: obj["id"]?.jsonPrimitive?.contentOrNull
                    ?: obj["ID"]?.jsonPrimitive?.contentOrNull
                    ?: "msg_${sender.hashCode()}_${subject.hashCode()}_${dateStr.hashCode()}"

                val formattedDate = try {
                    val ldt = LocalDateTime.parse(dateStr.substringBefore("."))
                    ldt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                } catch (e: Exception) {
                    dateStr
                }

                list.add(
                    NeptunMessage(
                        id = id,
                        subject = subject,
                        sender = sender,
                        sendDate = formattedDate,
                        previewText = "Koppints a teljes üzenet megtekintéséhez...",
                        bodyHtml = "",
                        isRead = unreadCount == 0,
                        isOfficial = sender.contains("hivatal", ignoreCase = true) || sender.contains("tanulmányi", ignoreCase = true)
                    )
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(tag, "Modern messages error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMessageContent(
        baseUrl: String,
        token: String,
        messageId: String,
        isModern: Boolean,
        username: String = "",
        password: String = ""
    ): String = withContext(Dispatchers.IO) {
        if (isModern && token.isNotBlank()) {
            val candidateUrls = listOf(
                "$baseUrl/api/Messages/$messageId/Posts?messageId=$messageId",
                "$baseUrl/api/Message/GetMessagePosts?messageId=$messageId",
                "$baseUrl/api/Messages/$messageId/Posts",
                "$baseUrl/api/Message/GetReceivedMessageDetail?messageId=$messageId",
                "$baseUrl/api/Messages/GetMessagePosts?messageId=$messageId",
                "$baseUrl/api/Messages/$messageId"
            )

            for (url in candidateUrls) {
                try {
                    val req = Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    var resp = okHttpClient.newCall(req).execute()
                    var bodyStr = resp.body?.string() ?: ""

                    if (resp.code >= 500 || bodyStr.contains("Hiba történt") || bodyStr.contains("\"statusCode\":500")) {
                        kotlinx.coroutines.delay(200)
                        resp = okHttpClient.newCall(req).execute()
                        bodyStr = resp.body?.string() ?: ""
                    }

                    if (!resp.isSuccessful || bodyStr.isBlank()) continue

                    val parsed = safeParseJson(bodyStr)
                    if (parsed is JsonObject) {
                        val dataObj = parsed["data"]
                        if (dataObj is JsonObject) {
                            val posts = dataObj["posts"]?.jsonArray
                            if (!posts.isNullOrEmpty()) {
                                for (postItem in posts) {
                                    if (postItem is JsonObject) {
                                        val html = postItem["htmlText"]?.jsonPrimitive?.contentOrNull
                                            ?: postItem["text"]?.jsonPrimitive?.contentOrNull
                                            ?: postItem["detail"]?.jsonPrimitive?.contentOrNull
                                            ?: postItem["body"]?.jsonPrimitive?.contentOrNull
                                        if (!html.isNullOrBlank()) {
                                            val cleaned = cleanHtml(html)
                                            if (cleaned.isNotBlank()) return@withContext cleaned
                                        }
                                    }
                                }
                            }
                        } else if (dataObj is JsonArray) {
                            for (postItem in dataObj) {
                                if (postItem is JsonObject) {
                                    val html = postItem["htmlText"]?.jsonPrimitive?.contentOrNull
                                        ?: postItem["text"]?.jsonPrimitive?.contentOrNull
                                    if (!html.isNullOrBlank()) {
                                        val cleaned = cleanHtml(html)
                                        if (cleaned.isNotBlank()) return@withContext cleaned
                                    }
                                }
                            }
                        }

                        val directHtml = (parsed["data"]?.jsonObject ?: parsed).let { obj ->
                            obj["htmlText"]?.jsonPrimitive?.contentOrNull
                                ?: obj["detail"]?.jsonPrimitive?.contentOrNull
                                ?: obj["body"]?.jsonPrimitive?.contentOrNull
                                ?: obj["messageText"]?.jsonPrimitive?.contentOrNull
                                ?: obj["text"]?.jsonPrimitive?.contentOrNull
                        }

                        if (!directHtml.isNullOrBlank()) {
                            val cleaned = cleanHtml(directHtml)
                            if (cleaned.isNotBlank()) return@withContext cleaned
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed URL $url: ${e.message}")
                }
            }
        }

        // If legacy or modern failed, try legacy /GetMessages with message ID
        if (username.isNotBlank() && password.isNotBlank()) {
            try {
                val numId = messageId.toLongOrNull() ?: 0L
                val body = """{"UserLogin":"$username","Password":"$password","CurrentPage":0,"TotalRowCount":-1,"MessageID":$numId,"MessageSortEnum":0}"""
                val url = getLegacyServiceUrl(baseUrl, "GetMessages")
                val req = Request.Builder()
                    .url(url)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .build()

                val resp = okHttpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val respBody = resp.body?.string() ?: ""
                    val parsed = safeParseJsonObject(respBody)
                    val msgs = parsed?.get("MessagesList")?.jsonArray ?: parsed?.get("messagesList")?.jsonArray
                    if (!msgs.isNullOrEmpty()) {
                        for (item in msgs) {
                            val obj = item.jsonObject
                            val id = obj["PersonMessageId"]?.jsonPrimitive?.contentOrNull
                                ?: obj["personMessageId"]?.jsonPrimitive?.contentOrNull
                                ?: ""
                            if (id == messageId || numId == 0L) {
                                val detail = cleanHtml(obj["Detail"]?.jsonPrimitive?.contentOrNull ?: obj["detail"]?.jsonPrimitive?.contentOrNull ?: "")
                                if (detail.isNotBlank()) return@withContext detail
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Legacy detail error: ${e.message}")
            }
        }

        ""
    }

    private suspend fun getLegacyMessages(baseUrl: String, user: String, pass: String, page: Int): List<NeptunMessage> = withContext(Dispatchers.IO) {
        try {
            val body = """{"UserLogin":"$user","Password":"$pass","CurrentPage":$page,"TotalRowCount":-1,"MessageID":0,"MessageSortEnum":0}"""
            val url = getLegacyServiceUrl(baseUrl, "GetMessages")
            val req = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext emptyList()
            val respBody = resp.body?.string() ?: ""
            val parsed = safeParseJsonObject(respBody) ?: return@withContext emptyList()
            val msgs = parsed["MessagesList"]?.jsonArray ?: parsed["messagesList"]?.jsonArray ?: return@withContext emptyList()

            val list = mutableListOf<NeptunMessage>()
            for (item in msgs) {
                val obj = item.jsonObject
                val subject = obj["Subject"]?.jsonPrimitive?.contentOrNull
                    ?: obj["subject"]?.jsonPrimitive?.contentOrNull
                    ?: "Tárgy"
                val sender = obj["Name"]?.jsonPrimitive?.contentOrNull
                    ?: obj["name"]?.jsonPrimitive?.contentOrNull
                    ?: "Feladó"
                val sendDateRaw = obj["SendDate"]?.jsonPrimitive?.contentOrNull?.replace(Regex("""\D"""), "")?.toLongOrNull() ?: System.currentTimeMillis()

                val id = obj["PersonMessageId"]?.jsonPrimitive?.contentOrNull
                    ?: obj["personMessageId"]?.jsonPrimitive?.contentOrNull
                    ?: obj["MessageID"]?.jsonPrimitive?.contentOrNull
                    ?: obj["ID"]?.jsonPrimitive?.contentOrNull
                    ?: "lmsg_${sender.hashCode()}_${subject.hashCode()}_${sendDateRaw.hashCode()}"

                val detail = cleanHtml(obj["Detail"]?.jsonPrimitive?.contentOrNull ?: obj["detail"]?.jsonPrimitive?.contentOrNull ?: "")
                val isNew = obj["IsNew"]?.jsonPrimitive?.booleanOrNull ?: obj["isNew"]?.jsonPrimitive?.booleanOrNull ?: false

                val formattedDate = try {
                    val ldt = Instant.ofEpochMilli(sendDateRaw).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    ldt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                } catch (e: Exception) {
                    ""
                }

                list.add(
                    NeptunMessage(
                        id = id,
                        subject = subject,
                        sender = sender,
                        sendDate = formattedDate,
                        previewText = detail.take(120),
                        bodyHtml = detail,
                        isRead = !isNew,
                        isOfficial = sender.contains("hivatal", ignoreCase = true) || sender.contains("tanulmányi", ignoreCase = true)
                    )
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(tag, "Legacy messages error: ${e.message}")
            emptyList()
        }
    }

    suspend fun markMessageAsReadOnServer(
        baseUrl: String,
        token: String,
        messageId: String,
        isModern: Boolean,
        username: String = "",
        password: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        var success = false
        try {
            if (isModern && token.isNotBlank()) {
                val urls = listOf(
                    "$baseUrl/api/Message/SetMessageRead?messageId=$messageId",
                    "$baseUrl/api/Message/SetReadState?messageId=$messageId",
                    "$baseUrl/api/Message/MarkAsRead?messageId=$messageId",
                    "$baseUrl/api/Messages/$messageId/Read"
                )
                for (url in urls) {
                    try {
                        val req = Request.Builder()
                            .url(url)
                            .post("""{"messageId":"$messageId","isRead":true}""".toRequestBody("application/json".toMediaType()))
                            .addHeader("Authorization", "Bearer $token")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        val resp = okHttpClient.newCall(req).execute()
                        if (resp.isSuccessful) success = true
                    } catch (e: Exception) {
                        Log.e(tag, "Failed URL $url: ${e.message}")
                    }
                }
            }

            if (username.isNotBlank() && password.isNotBlank()) {
                val numId = messageId.toLongOrNull() ?: 0L
                val legacyServices = listOf("SetMessageRead", "ReadMessage", "GetMessageDetail")
                for (svc in legacyServices) {
                    try {
                        val body = """{"UserLogin":"$username","Password":"$password","MessageID":$numId,"PersonMessageID":$numId,"PersonMessageId":$numId,"IsRead":true,"isRead":true}"""
                        val url = getLegacyServiceUrl(baseUrl, svc)
                        val req = Request.Builder()
                            .url(url)
                            .post(body.toRequestBody("application/json".toMediaType()))
                            .addHeader("Content-Type", "application/json")
                            .build()
                        val resp = okHttpClient.newCall(req).execute()
                        if (resp.isSuccessful) success = true
                    } catch (e: Exception) {
                        Log.e(tag, "Failed legacy $svc: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "markMessageAsReadOnServer error: ${e.message}")
        }
        return@withContext success
    }

    private fun cleanHtml(raw: String): String {
        return raw
            .replace(Regex("""<style[^>]*>[\s\S]*?</style>"""), "")
            .replace(Regex("""<script[^>]*>[\s\S]*?</script>"""), "")
            .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""</p>""", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("""<[^>]*>"""), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    // --- FINANCES & TRANSACTIONS ---
    suspend fun getFinances(
        baseUrl: String,
        token: String,
        username: String,
        password: String,
        isModern: Boolean
    ): List<FinanceItem> = withContext(Dispatchers.IO) {
        if (!isModern) {
            return@withContext getLegacyFinances(baseUrl, username, password)
        }

        val list = mutableListOf<FinanceItem>()

        // 1. Fetch Items To Be Paid
        try {
            val toPayUrl = "$baseUrl/api/FinancialItem/GetItemsToBePayed?sortAndPage.firstRow=0&sortAndPage.lastRow=50"
            val toPayReq = Request.Builder()
                .url(toPayUrl)
                .get()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            val toPayResp = okHttpClient.newCall(toPayReq).execute()
            if (toPayResp.isSuccessful) {
                val toPayBody = toPayResp.body?.string() ?: ""
                val toPayData = safeParseJsonObject(toPayBody)?.get("data")?.jsonArray
                if (toPayData != null) {
                    for (item in toPayData) {
                        val obj = item.jsonObject
                        val id = obj["impositionId"]?.jsonPrimitive?.contentOrNull
                            ?: obj["id"]?.jsonPrimitive?.contentOrNull
                            ?: "pay_${System.nanoTime()}_${list.size}"
                        val title = obj["itemTitle"]?.jsonPrimitive?.contentOrNull
                            ?: obj["impositionName"]?.jsonPrimitive?.contentOrNull
                            ?: obj["title"]?.jsonPrimitive?.contentOrNull
                            ?: "Befizetendő tétel"
                        val amount = (obj["amount"]?.jsonPrimitive?.doubleOrNull
                            ?: obj["itemValue"]?.jsonPrimitive?.doubleOrNull
                            ?: obj["price"]?.jsonPrimitive?.doubleOrNull
                            ?: 0.0).toInt()
                        val dueDateRaw = obj["deadline"]?.jsonPrimitive?.contentOrNull
                            ?: obj["dueDate"]?.jsonPrimitive?.contentOrNull
                            ?: ""
                        val dueDateFormatted = try {
                            val ldt = LocalDateTime.parse(dueDateRaw.substringBefore("."))
                            ldt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        } catch (e: Exception) {
                            dueDateRaw
                        }

                        list.add(
                            FinanceItem(
                                id = id,
                                title = title,
                                termName = "Aktuális",
                                amountHuf = amount,
                                status = FinanceStatus.PENDING,
                                dueDate = dueDateFormatted,
                                paymentDate = null,
                                transactionId = id
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "GetItemsToBePayed error: ${e.message}")
        }

        // 2. Fetch Student Impositions
        try {
            val impositionsUrl = "$baseUrl/api/FinancialItem/GetStudentImpositions?sortAndPage.firstRow=0&sortAndPage.lastRow=50"
            val impReq = Request.Builder()
                .url(impositionsUrl)
                .get()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            val impResp = okHttpClient.newCall(impReq).execute()
            if (impResp.isSuccessful) {
                val impBody = impResp.body?.string() ?: ""
                val impData = safeParseJsonObject(impBody)?.get("data")?.jsonArray
                if (impData != null) {
                    for (item in impData) {
                        val obj = item.jsonObject
                        val id = obj["impositionId"]?.jsonPrimitive?.contentOrNull
                            ?: obj["id"]?.jsonPrimitive?.contentOrNull
                            ?: "imp_${System.nanoTime()}_${list.size}"
                        if (list.any { it.id == id }) continue

                        val title = obj["itemTitle"]?.jsonPrimitive?.contentOrNull
                            ?: obj["impositionName"]?.jsonPrimitive?.contentOrNull
                            ?: obj["title"]?.jsonPrimitive?.contentOrNull
                            ?: "Kiírt tétel"
                        val amount = (obj["amount"]?.jsonPrimitive?.doubleOrNull
                            ?: obj["itemValue"]?.jsonPrimitive?.doubleOrNull
                            ?: obj["price"]?.jsonPrimitive?.doubleOrNull
                            ?: 0.0).toInt()
                        val statusText = obj["status"]?.jsonPrimitive?.contentOrNull
                            ?: obj["statusName"]?.jsonPrimitive?.contentOrNull
                            ?: "Aktív"
                        val status = if (statusText.contains("teljesít", ignoreCase = true) || statusText.contains("befizet", ignoreCase = true)) {
                            FinanceStatus.COMPLETED
                        } else {
                            FinanceStatus.PENDING
                        }
                        val dateRaw = obj["deadline"]?.jsonPrimitive?.contentOrNull
                            ?: obj["dueDate"]?.jsonPrimitive?.contentOrNull
                            ?: obj["impositionDate"]?.jsonPrimitive?.contentOrNull
                            ?: ""
                        val dateFormatted = try {
                            val ldt = LocalDateTime.parse(dateRaw.substringBefore("."))
                            ldt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        } catch (e: Exception) {
                            dateRaw
                        }

                        list.add(
                            FinanceItem(
                                id = id,
                                title = title,
                                termName = "Aktuális",
                                amountHuf = amount,
                                status = status,
                                dueDate = dateFormatted,
                                paymentDate = if (status == FinanceStatus.COMPLETED) dateFormatted else null,
                                transactionId = id
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "GetStudentImpositions error: ${e.message}")
        }

        // 3. Fetch Completed Previous Transactions
        try {
            val url = "$baseUrl/api/Transactions/GetStudentPreviousTransactions?sortAndPage.firstRow=0&sortAndPage.lastRow=50&sortAndPage.transferDate=desc"
            val req = Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (resp.isSuccessful) {
                val respBody = resp.body?.string() ?: ""
                val parsed = safeParseJsonObject(respBody)
                val dataPart = parsed?.get("data")?.jsonArray
                if (dataPart != null) {
                    for (item in dataPart) {
                        val obj = item.jsonObject
                        val transId = obj["transactionId"]?.jsonPrimitive?.contentOrNull ?: "tx_${System.nanoTime()}_${list.size}"
                        if (list.any { it.id == transId }) continue

                        var amount = (obj["transactionValue"]?.jsonPrimitive?.doubleOrNull ?: 0.0).toInt()
                        val sign = obj["sign"]?.jsonPrimitive?.contentOrNull ?: "+"
                        if (sign == "-") {
                            amount = -amount
                        }

                        val title = obj["transactionPayingType"]?.jsonPrimitive?.contentOrNull ?: "Tranzakció"
                        val statusText = obj["transactionStatus"]?.jsonPrimitive?.contentOrNull ?: "Teljesített"
                        val dateStr = obj["transferDate"]?.jsonPrimitive?.contentOrNull ?: ""

                        val formattedDate = try {
                            val ldt = LocalDateTime.parse(dateStr.substringBefore("."))
                            ldt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        } catch (e: Exception) {
                            dateStr
                        }

                        val status = when {
                            statusText.contains("teljesít", ignoreCase = true) -> FinanceStatus.COMPLETED
                            statusText.contains("aktív", ignoreCase = true) || statusText.contains("fizetendő", ignoreCase = true) -> FinanceStatus.PENDING
                            else -> FinanceStatus.COMPLETED
                        }

                        list.add(
                            FinanceItem(
                                id = transId,
                                title = title,
                                termName = "Aktuális",
                                amountHuf = amount,
                                status = status,
                                dueDate = formattedDate,
                                paymentDate = if (status == FinanceStatus.COMPLETED) formattedDate else null,
                                transactionId = transId
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Modern transactions error: ${e.message}")
        }

        if (list.isEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
            getLegacyFinances(baseUrl, username, password)
        } else {
            list
        }
    }

    private suspend fun getLegacyFinances(baseUrl: String, user: String, pass: String): List<FinanceItem> = withContext(Dispatchers.IO) {
        try {
            val body = """{"UserLogin":"$user","Password":"$pass","TotalRowCount":-1}"""
            val url = getLegacyServiceUrl(baseUrl, "GetCashinData")
            val req = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val resp = okHttpClient.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext emptyList()
            val respBody = resp.body?.string() ?: ""
            val parsed = safeParseJsonObject(respBody) ?: return@withContext emptyList()
            val rows = parsed["CashinDataRows"]?.jsonArray ?: parsed["cashinDataRows"]?.jsonArray ?: return@withContext emptyList()

            val list = mutableListOf<FinanceItem>()
            for (item in rows) {
                val obj = item.jsonObject
                val amount = obj["amount"]?.jsonPrimitive?.intOrNull ?: 0
                val title = obj["appellation"]?.jsonPrimitive?.contentOrNull ?: "Befizetés"
                val id = obj["ID"]?.jsonPrimitive?.contentOrNull ?: "leg_cash_${System.nanoTime()}"
                val statusName = obj["status_name"]?.jsonPrimitive?.contentOrNull ?: "aktív"
                val deadlineRaw = obj["deadline"]?.jsonPrimitive?.contentOrNull?.replace(Regex("""\D"""), "")?.toLongOrNull() ?: System.currentTimeMillis()

                val formattedDate = try {
                    val ldt = Instant.ofEpochMilli(deadlineRaw).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    ldt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                } catch (e: Exception) {
                    ""
                }

                val status = if (statusName.contains("teljesít", ignoreCase = true)) FinanceStatus.COMPLETED else FinanceStatus.PENDING

                list.add(
                    FinanceItem(
                        id = id,
                        title = title,
                        termName = "Félév",
                        amountHuf = amount,
                        status = status,
                        dueDate = formattedDate,
                        paymentDate = if (status == FinanceStatus.COMPLETED) formattedDate else null,
                        transactionId = id
                    )
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(tag, "Legacy finances error: ${e.message}")
            emptyList()
        }
    }

    fun cleanTermString(raw: String?): String {
        if (raw == null || raw.isBlank()) return "2025/26/1"
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") || trimmed.contains("{")) {
            try {
                val element = json.parseToJsonElement(trimmed)
                if (element is JsonObject) {
                    val extracted = element["id"]?.jsonPrimitive?.contentOrNull
                        ?: element["termId"]?.jsonPrimitive?.contentOrNull
                        ?: element["value"]?.jsonPrimitive?.contentOrNull
                        ?: element["name"]?.jsonPrimitive?.contentOrNull
                        ?: element["termName"]?.jsonPrimitive?.contentOrNull
                        ?: element["text"]?.jsonPrimitive?.contentOrNull
                    if (!extracted.isNullOrBlank() && !extracted.startsWith("{")) {
                        return extracted.trim()
                    }
                }
            } catch (e: Exception) {
                val regex = Regex(""""(?:id|termId|value|name|termName|text)"\s*:\s*"([^"]+)"""")
                val match = regex.find(trimmed)
                if (match != null) {
                    return match.groupValues[1].trim()
                }
            }
        }
        return trimmed
    }
}
