package com.example

import com.example.data.network.ServerTextParser
import com.example.domain.model.CourseType
import com.example.domain.model.FinanceStatus
import com.example.domain.model.NeptunLanguage
import com.example.domain.model.NeptunLanguages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NeptunLanguageTest {

    // Valós `EnvironmentData` válasz a neptun3.ppke.hu HAR-naplóból.
    private val ppkeEnvironmentData = """
        {"data":{"loginUrl":null,"isSessionTimeoutCounterVisible":false,"logoutUrl":null,
        "version":"2026.2.13","generationDate":"2026-09-07T16:46:30","instituteCode":"PPKE",
        "instituteNames":[{"name":"Pázmány Péter Katolikus Egyetem","lcid":1038},
        {"name":"Pázmány Péter Catholic University","lcid":1033},
        {"name":"Pázmány Péter Katolikus Egyetem","lcid":1031}],
        "testSystem":false,"logUIErrors":false,"isResetPasswordLinkVisible":true,
        "neptunDebugEnabled":false,
        "systemParameters":{"isVirtualspaceFolderDescriptionRequired":false,"isSubstitutionReadonly":false},
        "serverName":"PPKEHWEBUJ7","remainingSession":1383,"sessionTimeoutInMinutes":15,
        "accessTokenExpirationInMinutes":5,
        "supportedLanguages":[{"code":"hu","name":"magyar (Magyarország)","lcid":1038,"selected":true},
        {"code":"en","name":"English (United States)","lcid":1033,"selected":false},
        {"code":"de","name":"Deutsch (Deutschland)","lcid":1031,"selected":false}],
        "isADAuthenticationInInstitute":false},"notification":[]}
    """.trimIndent()

    @Test
    fun testParseSupportedLanguagesFromRealHarPayload() {
        val languages = NeptunLanguages.parseSupportedLanguages(ppkeEnvironmentData)

        assertEquals(3, languages.size)
        assertEquals(NeptunLanguage("hu", "magyar (Magyarország)", 1038, selected = true), languages[0])
        assertEquals(NeptunLanguage("en", "English (United States)", 1033, selected = false), languages[1])
        assertEquals(NeptunLanguage("de", "Deutsch (Deutschland)", 1031, selected = false), languages[2])
    }

    @Test
    fun testParseSupportedLanguagesToleratesUnexpectedShapes() {
        assertTrue(NeptunLanguages.parseSupportedLanguages("").isEmpty())
        assertTrue(NeptunLanguages.parseSupportedLanguages("<html>nem json</html>").isEmpty())
        assertTrue(NeptunLanguages.parseSupportedLanguages("""{"data":{}}""").isEmpty())
        assertTrue(NeptunLanguages.parseSupportedLanguages("""{"data":{"supportedLanguages":[]}}""").isEmpty())
        // Érvénytelen LCID-jű elemek kiesnek, az ép elem megmarad.
        val mixed = """{"data":{"supportedLanguages":[
            {"code":"hu","name":"magyar","lcid":0,"selected":true},
            {"code":"en","name":"English","lcid":1033,"selected":false}]}}"""
        val parsed = NeptunLanguages.parseSupportedLanguages(mixed)
        assertEquals(1, parsed.size)
        assertEquals(1033, parsed[0].lcid)
    }

    @Test
    fun testResolveLcidPrefersUserChoiceThenServerDefault() {
        val languages = NeptunLanguages.parseSupportedLanguages(ppkeEnvironmentData)

        assertEquals(1033, NeptunLanguages.resolveLcid(languages, 1033))
        assertEquals(1031, NeptunLanguages.resolveLcid(languages, 1031))
        // Nem támogatott választás → a szerver által megjelölt (magyar).
        assertEquals(1038, NeptunLanguages.resolveLcid(languages, 1051))
    }

    @Test
    fun testResolveLcidWithoutHungarianFallsBackToServerSelected() {
        // Kivételes intézmény: csak angol + szlovák, a szerver az angolt jelöli.
        val languages = listOf(
            NeptunLanguage("en", "English (United States)", 1033, selected = true),
            NeptunLanguage("sk", "slovenčina (Slovensko)", 1051, selected = false)
        )
        assertEquals(1033, NeptunLanguages.resolveLcid(languages, 1038))
        assertEquals(1051, NeptunLanguages.resolveLcid(languages, 1051))
    }

    @Test
    fun testAcceptLanguageHeader() {
        assertEquals(
            "hu-HU,hu;q=0.9,en;q=0.8,de;q=0.7",
            NeptunLanguages.acceptLanguageHeader(1038)
        )
        assertEquals(
            "en-US,en;q=0.9,hu;q=0.8,de;q=0.7",
            NeptunLanguages.acceptLanguageHeader(1033)
        )
        assertEquals(
            "de-DE,de;q=0.9,hu;q=0.8,en;q=0.7",
            NeptunLanguages.acceptLanguageHeader(1031)
        )
        // Ismeretlen LCID kód alapján, végső esetben magyar tartalékkal.
        assertTrue(NeptunLanguages.acceptLanguageHeader(1051).startsWith("sk-SK,sk;q=0.9"))
        assertTrue(NeptunLanguages.acceptLanguageHeader(9999).startsWith("hu-HU,hu;q=0.9"))
    }

    @Test
    fun testDisplayLabelAndCodeBadge() {
        val hu = NeptunLanguage("hu", "magyar (Magyarország)", 1038, selected = true)
        val en = NeptunLanguage("en", "English (United States)", 1033)
        assertEquals("Magyar", NeptunLanguages.displayLabel(hu))
        assertEquals("English", NeptunLanguages.displayLabel(en))
        assertEquals("HU", NeptunLanguages.codeBadge(hu))
        assertEquals("EN", NeptunLanguages.codeBadge(en))
    }

    @Test
    fun testGradeFromTextHungarian() {
        assertEquals(5, ServerTextParser.gradeFromText("Jeles (5)"))
        assertEquals(4, ServerTextParser.gradeFromText("Jó (4)"))
        assertEquals(3, ServerTextParser.gradeFromText("Közepes (3)"))
        assertEquals(2, ServerTextParser.gradeFromText("Elégséges (2)"))
        assertEquals(1, ServerTextParser.gradeFromText("Elégtelen (1)"))
        assertEquals(5, ServerTextParser.gradeFromText("Megfelelt"))
        assertEquals(0, ServerTextParser.gradeFromText("Aláírva"))
        assertEquals(0, ServerTextParser.gradeFromText(""))
    }

    @Test
    fun testGradeFromTextEnglishAndGerman() {
        assertEquals(5, ServerTextParser.gradeFromText("Excellent"))
        assertEquals(4, ServerTextParser.gradeFromText("Good"))
        assertEquals(3, ServerTextParser.gradeFromText("Average"))
        assertEquals(2, ServerTextParser.gradeFromText("Sufficient"))
        assertEquals(1, ServerTextParser.gradeFromText("Failed"))
        assertEquals(1, ServerTextParser.gradeFromText("Fail"))
        assertEquals(5, ServerTextParser.gradeFromText("Passed"))

        assertEquals(5, ServerTextParser.gradeFromText("Sehr gut"))
        assertEquals(4, ServerTextParser.gradeFromText("Gut"))
        assertEquals(3, ServerTextParser.gradeFromText("Befriedigend"))
        assertEquals(2, ServerTextParser.gradeFromText("Ausreichend"))
        assertEquals(1, ServerTextParser.gradeFromText("Nicht genügend"))
        assertEquals(1, ServerTextParser.gradeFromText("Ungenügend"))
        assertEquals(5, ServerTextParser.gradeFromText("Bestanden"))
        assertEquals(1, ServerTextParser.gradeFromText("Nicht bestanden"))
    }

    @Test
    fun testGradeFromTextPrefersDigitsAndNegativeForms() {
        assertEquals(2, ServerTextParser.gradeFromText("Grade: 2"))
        // A tagadó alakok nem keverhetők össze az állító kulcsszavakkal.
        assertEquals(1, ServerTextParser.gradeFromText("Insufficient"))
        assertEquals(2, ServerTextParser.gradeFromText("Genügend"))
        assertEquals(1, ServerTextParser.gradeFromText("Not passed"))
        assertEquals(1, ServerTextParser.gradeFromText("Did not pass"))
        assertEquals(1, ServerTextParser.gradeFromText("Nem felelt meg"))
    }

    @Test
    fun testFinanceStatusFromText() {
        assertEquals(FinanceStatus.COMPLETED, ServerTextParser.financeStatusFromText("Teljesítve"))
        assertEquals(FinanceStatus.COMPLETED, ServerTextParser.financeStatusFromText("Befizetve"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Aktív"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Fizetendő"))

        assertEquals(FinanceStatus.COMPLETED, ServerTextParser.financeStatusFromText("Completed"))
        assertEquals(FinanceStatus.COMPLETED, ServerTextParser.financeStatusFromText("Paid"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Pending"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Unpaid"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Overdue"))

        assertEquals(FinanceStatus.COMPLETED, ServerTextParser.financeStatusFromText("Bezahlt"))
        assertEquals(FinanceStatus.COMPLETED, ServerTextParser.financeStatusFromText("Abgeschlossen"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Offen"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Fällig"))
        // Tagadó alakok nem lehetnek COMPLETED státuszúak.
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Not completed"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Not paid"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Nicht abgeschlossen"))
        assertEquals(FinanceStatus.PENDING, ServerTextParser.financeStatusFromText("Teljesítetlen"))

        assertNull(ServerTextParser.financeStatusFromText(""))
        assertNull(ServerTextParser.financeStatusFromText("???"))
    }

    @Test
    fun testIsSignedStatus() {
        assertTrue(ServerTextParser.isSignedStatus("Aláírva"))
        assertTrue(ServerTextParser.isSignedStatus("Teljesítve"))
        assertTrue(ServerTextParser.isSignedStatus("Signed"))
        assertTrue(ServerTextParser.isSignedStatus("Unterschrieben"))
        assertFalse(ServerTextParser.isSignedStatus("Folyamatban"))
        assertFalse(ServerTextParser.isSignedStatus("Not signed"))
        assertFalse(ServerTextParser.isSignedStatus("Not completed"))
        assertFalse(ServerTextParser.isSignedStatus("Nicht unterschrieben"))
        assertFalse(ServerTextParser.isSignedStatus("Nincs aláírva"))
        assertFalse(ServerTextParser.isSignedStatus(""))
    }

    @Test
    fun testIsOfficialSender() {
        assertTrue(ServerTextParser.isOfficialSender("Tanulmányi Hivatal"))
        assertTrue(ServerTextParser.isOfficialSender("Academic Registry Office"))
        assertTrue(ServerTextParser.isOfficialSender("Studienabteilung"))
        assertTrue(ServerTextParser.isOfficialSender(""))
        assertFalse(ServerTextParser.isOfficialSender("Dr. Kovács István"))
        assertFalse(ServerTextParser.isOfficialSender("John Smith"))
    }

    @Test
    fun testCourseTypeFromTextMultilingual() {
        assertEquals(CourseType.LECTURE, ServerTextParser.courseTypeFromText("előadás matematika"))
        assertEquals(CourseType.LECTURE, ServerTextParser.courseTypeFromText("mathematics lecture"))
        assertEquals(CourseType.LECTURE, ServerTextParser.courseTypeFromText("mathematik vorlesung"))
        assertEquals(CourseType.PRACTICE, ServerTextParser.courseTypeFromText("gyakorlat"))
        assertEquals(CourseType.PRACTICE, ServerTextParser.courseTypeFromText("practice session"))
        assertEquals(CourseType.PRACTICE, ServerTextParser.courseTypeFromText("übung"))
        assertEquals(CourseType.LAB, ServerTextParser.courseTypeFromText("laboratory"))
        assertEquals(CourseType.SEMINAR, ServerTextParser.courseTypeFromText("seminar"))
        assertEquals(CourseType.EXAM, ServerTextParser.courseTypeFromText("written exam"))
        assertEquals(CourseType.EXAM, ServerTextParser.courseTypeFromText("klausur"))
    }
}
