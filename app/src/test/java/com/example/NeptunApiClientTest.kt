package com.example

import com.example.data.network.NeptunApiClient
import com.example.domain.model.AcademicPeriod
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NeptunApiClientTest {

    private val client = NeptunApiClient()

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

    @Test
    fun testMultiFormParsingWithThemeAndLanguageForms() {
        val elteHtml = """
            <!DOCTYPE html>
            <html>
            <body>
                <form id="selectLanguageForm" action="/Home/SetLanguage" method="post">
                    <input type="hidden" name="returnUrl" value="/Account/Login" />
                    <input type="hidden" name="culture" value="en" />
                    <input type="hidden" name="__RequestVerificationToken" value="token_lang_111" />
                </form>
                <form id="selectThemeForm" action="/Home/SetTheme" method="post">
                    <input type="hidden" name="returnUrl" value="/Account/Login" />
                    <input type="hidden" name="theme" value="dark" />
                    <input type="hidden" name="__RequestVerificationToken" value="token_theme_222" />
                </form>
                <form action="/Account/Login" method="post">
                    <input type="text" name="LoginName" value="" />
                    <input type="password" name="Password" value="" />
                    <input type="hidden" name="ReturnUrl" value="" />
                    <input type="hidden" name="__RequestVerificationToken" value="token_real_login_333" />
                </form>
            </body>
            </html>
        """.trimIndent()

        // When specifying "Login" target, it should isolate the actual login form
        val loginInputs = client.parseFormInputs(elteHtml, "Login")
        assertEquals("token_real_login_333", loginInputs["__RequestVerificationToken"])
        assertEquals("", loginInputs["ReturnUrl"])
        assertTrue(!loginInputs.containsKey("theme"))
        assertTrue(!loginInputs.containsKey("culture"))

        // Even without specifying target, the auto-fallback should prioritize the Login form over theme/language switchers
        val autoInputs = client.parseFormInputs(elteHtml)
        assertEquals("token_real_login_333", autoInputs["__RequestVerificationToken"])
        assertEquals("", autoInputs["ReturnUrl"])
        assertTrue(!autoInputs.containsKey("theme"))
    }

    @Test
    fun testExtractValidationErrorsWithElteAlerts() {
        val elteAlertHtml = """
            <div class="alert alert-danger alert-dismissible fade show PotlapHTMLMessageEntry" role="alert">
                <span style="white-space: pre-line">Nemrég küldtünk e-mailt belépéshez. Amennyiben nem érkezik meg, néhány perc múlva próbálhatja újra.</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <div class="form-group">
                <span class="field-validation-error">Az e-mail kód kötőjel utáni része csak számjegyekből állhat!</span>
            </div>
        """.trimIndent()

        val errors = client.extractValidationErrors(elteAlertHtml)
        assertEquals(2, errors.size)
        assertTrue(errors[0].contains("Nemrég küldtünk e-mailt belépéshez"))
        assertTrue(!errors[0].contains("Close"))
        assertTrue(!errors[0].contains("button"))
        assertEquals("Az e-mail kód kötőjel utáni része csak számjegyekből állhat!", errors[1])
    }

    @Test
    fun testDegreeProgressSerialization() {
        val original = com.example.domain.model.DegreeProgress(
            completedCredits = 105,
            totalRequiredCredits = 210,
            completedCurriculums = 2,
            totalCurriculums = 3,
            compulsoryCompleted = 60,
            compulsoryTotal = 120,
            templates = listOf(
                com.example.domain.model.CurriculumTemplateItem(
                    id = "T1",
                    name = "Alapozó tárgyak",
                    completedCredits = 30,
                    totalCredits = 30,
                    isCompleted = true
                )
            )
        )
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.encodeToString(
            com.example.domain.model.DegreeProgress.serializer(),
            original
        )
        val decoded = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString(
            com.example.domain.model.DegreeProgress.serializer(),
            json
        )
        assertEquals(105, decoded.completedCredits)
        assertEquals(210, decoded.totalRequiredCredits)
        assertEquals(1, decoded.templates.size)
        assertEquals("Alapozó tárgyak", decoded.templates[0].name)
        assertEquals(50, decoded.progressPercentage)
    }

    @Test
    fun testTargetCreditsDynamicCalculation() {
        val progress = com.example.domain.model.DegreeProgress(
            completedCredits = 120,
            totalRequiredCredits = 240
        )
        assertEquals(0.5f, progress.progressFraction, 0.001f)
        assertEquals(50, progress.progressPercentage)

        // When user overrides target credits in settings to 180:
        val updated = progress.copy(totalRequiredCredits = 180)
        assertEquals(0.666f, updated.progressFraction, 0.01f)
        assertEquals(66, updated.progressPercentage)
    }

    @Test
    fun testTimetableMessagesI18n() {
        val hu = com.example.core.i18n.AppStringsProvider.HUNGARIAN
        val en = com.example.core.i18n.AppStringsProvider.ENGLISH
        val de = com.example.core.i18n.AppStringsProvider.GERMAN

        assertTrue(hu.weekendFreeMessages.size >= 4)
        assertTrue(hu.weekdayFreeMessages.size >= 4)
        assertTrue(hu.classesDoneMessages.size >= 4)

        assertTrue(en.weekendFreeMessages.size >= 4)
        assertTrue(en.weekdayFreeMessages.size >= 4)
        assertTrue(en.classesDoneMessages.size >= 4)

        assertTrue(de.weekendFreeMessages.size >= 4)
        assertTrue(de.weekdayFreeMessages.size >= 4)
        assertTrue(de.classesDoneMessages.size >= 4)

        assertTrue(hu.targetCreditsDesc.contains("Kezdőlap"))
        assertTrue(en.targetCreditsDesc.contains("Dashboard"))
        assertTrue(de.targetCreditsDesc.contains("Startseiten"))
    }

    @Test
    fun testSafeParseJsonWithBom() {
        val rawWithBom = "\uFEFF{\"data\":{\"posts\":[{\"postId\":\"1001\",\"isRead\":false}]}}"
        val parsed = client.safeParseJsonObject(rawWithBom)
        assertNotNull(parsed)
        val data = parsed?.get("data") as? JsonObject
        val posts = data?.get("posts") as? JsonArray
        assertEquals(1, posts?.size)
        val firstPost = posts?.get(0) as? JsonObject
        assertEquals("1001", (firstPost?.get("postId") as? JsonPrimitive)?.content)
    }

    @Test
    fun testMessagesI18nStrings() {
        val hu = com.example.core.i18n.AppStringsProvider.HUNGARIAN
        val en = com.example.core.i18n.AppStringsProvider.ENGLISH
        val de = com.example.core.i18n.AppStringsProvider.GERMAN

        assertEquals("Az üzenetek betöltése nem sikerült.", hu.messagesLoadFailed)
        assertEquals("Az üzenet betöltése nem sikerült.", hu.messageDetailLoadFailed)

        assertEquals("Failed to load messages.", en.messagesLoadFailed)
        assertEquals("Failed to load message.", en.messageDetailLoadFailed)

        assertEquals("Nachrichten konnten nicht geladen werden.", de.messagesLoadFailed)
        assertEquals("Nachricht konnte nicht geladen werden.", de.messageDetailLoadFailed)
    }

    @Test
    fun testPostsProcessedJsonFormat() {
        val postIds = listOf("12345", "67890")
        val idsJson = postIds.joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
        val jsonPayload = """{"postIds":$idsJson}"""
        val parsed = client.safeParseJsonObject(jsonPayload)
        val array = parsed?.get("postIds") as? JsonArray
        assertEquals(2, array?.size)
        assertEquals("12345", (array?.get(0) as? JsonPrimitive)?.content)
        assertEquals("67890", (array?.get(1) as? JsonPrimitive)?.content)
    }

    @Test
    fun testNormalizeDateIso() {
        assertEquals("2026-09-10", client.normalizeDateIso("2026-09-10T08:00:00"))
        assertEquals("2026-09-10", client.normalizeDateIso("2026.09.10."))
        assertEquals("2026-09-10", client.normalizeDateIso("2026. 9. 10."))
        assertEquals("2026-09-10", client.normalizeDateIso("2026/09/10"))
        assertEquals("2026-09-10", client.normalizeDateIso("2026-09-10"))
    }

    @Test
    fun testMapPeriodType() {
        assertEquals("COURSE_REG", client.mapPeriodType("Kurzus", "Végleges kurzusfelvétel"))
        assertEquals("REGISTRATION", client.mapPeriodType("Adminisztrációs", "Féléves bejelentkezési időszak"))
        assertEquals("EXAM", client.mapPeriodType("Vizsga", "Keresztféléves vizsgaidőszak"))
        assertEquals("EDUCATION", client.mapPeriodType("Oktatás", "Szorgalmi időszak"))
        assertEquals("BREAK", client.mapPeriodType("Szünet", "Tavaszi szünet"))
        assertEquals("FINANCE", client.mapPeriodType("Pénzügy", "Önköltség befizetési határidő"))
    }

    @Test
    fun testParseAcademicPeriods() {
        val json = """
            {
                "data": [
                    {
                        "periodId": "p_101",
                        "periodName": "Végleges kurzusfelvétel",
                        "periodType": "Kurzus",
                        "fromDate": "2026-09-01T08:00:00",
                        "toDate": "2026-10-31T23:59:59"
                    },
                    {
                        "periodId": "p_102",
                        "periodName": "Vizsgaidőszak",
                        "periodType": "Vizsga",
                        "fromDate": "2026-12-15T08:00:00",
                        "toDate": "2027-01-28T23:59:59"
                    }
                ]
            }
        """.trimIndent()

        val parsed = client.safeParseJsonObject(json)
        assertNotNull(parsed)
        val periods = client.parseAcademicPeriods(parsed!!)
        assertEquals(2, periods.size)

        val courseReg = periods.firstOrNull { it.id == "p_101" }
        assertNotNull(courseReg)
        assertEquals("Végleges kurzusfelvétel", courseReg?.name)
        assertEquals("COURSE_REG", courseReg?.type)
        assertEquals("2026-09-01", courseReg?.startDate)
        assertEquals("2026-10-31", courseReg?.endDate)

        val exam = periods.firstOrNull { it.id == "p_102" }
        assertNotNull(exam)
        assertEquals("Vizsgaidőszak", exam?.name)
        assertEquals("EXAM", exam?.type)
        assertEquals("2026-12-15", exam?.startDate)
        assertEquals("2027-01-28", exam?.endDate)
    }

    @Test
    fun testAcademicPeriodSerialization() {
        val periods = listOf(
            AcademicPeriod(
                id = "p1",
                name = "Szorgalmi időszak",
                startDate = "2026-09-07",
                endDate = "2026-12-11",
                type = "EDUCATION",
                isActive = true
            )
        )
        val jsonStr = Json.encodeToString(ListSerializer(AcademicPeriod.serializer()), periods)
        val decoded = Json.decodeFromString(ListSerializer(AcademicPeriod.serializer()), jsonStr)
        assertEquals(1, decoded.size)
        assertEquals("p1", decoded[0].id)
        assertEquals("Szorgalmi időszak", decoded[0].name)
        assertTrue(decoded[0].isActive)
    }
}

