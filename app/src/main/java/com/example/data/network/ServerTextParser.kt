package com.example.data.network

import com.example.domain.model.CourseType
import com.example.domain.model.FinanceStatus

/**
 * Szerverről érkező, lokalizált szövegek értelmezése.
 *
 * A Neptun a bejelentkezéskor küldött LCID alapján lokalizálva adja vissza
 * az állapot- és eredmény-szövegeket (pl. jegy-megnevezések, pénzügyi
 * státuszok), ezért a felismerésnek a magyar mellett legalább az angol és
 * német változatokat is kezelnie kell. Tisztán lokális logika, unit-tesztelve.
 */
object ServerTextParser {

    // ------------------------------------------------------------------
    // Jegyek (1..5, 0 = nem felismerhető)
    // ------------------------------------------------------------------

    /**
     * Jegy kinyerése szabad szövegből. Először arab számjegyet keres
     * (1-5), majd magyar/angol/német kulcsszavakat. A sorrend szándékos:
     * az elégtelen-csoport van elöl, mert a "failed" tartalmazza a "fail",
     * az "insufficient" a "sufficient", a "nicht genügend" pedig a
     * "genügend" kifejezést.
     */
    fun gradeFromText(text: String): Int {
        if (text.isBlank()) return 0
        val digitMatch = Regex("""\b([1-5])\b""").find(text)
        if (digitMatch != null) {
            return digitMatch.groupValues[1].toInt()
        }
        val lower = text.lowercase()
        // 1 – elégtelen / fail (először, lásd a fenti megjegyzést)
        if (lower.contains("elégtelen") ||
            lower.contains("failed") ||
            lower.contains("fail") ||
            lower.contains("insufficient") ||
            lower.contains("unsatisfactory") ||
            lower.contains("nicht bestanden") ||
            lower.contains("nicht genügend") ||
            lower.contains("nicht genuegend") ||
            lower.contains("ungenügend") ||
            lower.contains("ungenuegend")
        ) {
            return 1
        }
        // 5 – jeles
        if (lower.contains("jeles") ||
            lower.contains("excellent") ||
            lower.contains("outstanding") ||
            lower.contains("sehr gut")
        ) {
            return 5
        }
        // 4 – jó (a "gut" részstringje a "sehr gut"-nak, de azt már kezeltük)
        if (lower.contains("jó") ||
            lower.contains("good") ||
            lower.contains("gut")
        ) {
            return 4
        }
        // 3 – közepes
        if (lower.contains("közepes") ||
            lower.contains("average") ||
            lower.contains("fair") ||
            lower.contains("befriedigend")
        ) {
            return 3
        }
        // 2 – elégséges
        if (lower.contains("elégséges") ||
            lower.contains("sufficient") ||
            lower.contains("ausreichend") ||
            lower.contains("genügend") ||
            lower.contains("genuegend")
        ) {
            return 2
        }
        // megfelelt-jellegű (nem osztályzatos) teljesítés → 5
        if (lower.contains("megfelelt") ||
            lower.contains("passed") ||
            lower.contains("pass") ||
            lower.contains("bestanden")
        ) {
            return 5
        }
        return 0
    }

    // ------------------------------------------------------------------
    // Pénzügyi státuszok
    // ------------------------------------------------------------------

    private val UNPAID_KEYWORDS = listOf(
        "unpaid", "incomplete", "unsettled",
        "nicht bezahlt", "unbezahlt", "offen"
    )

    private val COMPLETED_KEYWORDS = listOf(
        // magyar
        "teljesít", "befizet", "kifizet",
        // angol
        "completed", "complete", "paid", "settled", "fulfilled", "done",
        // német
        "bezahlt", "abgeschlossen", "erfüllt", "erfuellt",
        "ausgeglichen", "beglichen", "erledigt"
    )

    private val PENDING_KEYWORDS = listOf(
        // magyar
        "aktív", "aktiv", "fizetendő", "fizetendo", "kiírt", "kiirt",
        // angol
        "active", "pending", "payable", "overdue", "outstanding", "open", "due",
        // német
        "fällig", "faellig", "zahlbar", "rückständig", "rueckstaendig"
    )

    /**
     * Pénzügyi státusz felismerése szabad szövegből, vagy `null`, ha nem
     * felismerhető (ilyenkor a hívó oldali alapértelmezés érvényesül).
     * A "kifizetetlen"-jellegű kifejezések élveznek elsőbbséget, mert pl. az
     * "unpaid" tartalmazza a "paid" szót.
     */
    fun financeStatusFromText(text: String): FinanceStatus? {
        if (text.isBlank()) return null
        val lower = text.lowercase()
        if (UNPAID_KEYWORDS.any { lower.contains(it) }) return FinanceStatus.PENDING
        if (COMPLETED_KEYWORDS.any { lower.contains(it) }) return FinanceStatus.COMPLETED
        if (PENDING_KEYWORDS.any { lower.contains(it) }) return FinanceStatus.PENDING
        return null
    }

    fun isCompletedStatus(text: String): Boolean {
        return financeStatusFromText(text) == FinanceStatus.COMPLETED
    }

    // ------------------------------------------------------------------
    // Aláírás / teljesítés (tantárgy-státusz)
    // ------------------------------------------------------------------

    /**
     * "Aláírva / teljesítve"-jellegű tantárgy-státusz felismerése.
     */
    fun isSignedStatus(text: String): Boolean {
        if (text.isBlank()) return false
        val lower = text.lowercase()
        return lower.contains("teljesít") ||
            lower.contains("aláír") ||
            lower.contains("alair") ||
            lower.contains("signed") ||
            lower.contains("completed") ||
            lower.contains("passed") ||
            lower.contains("unterschrieben") ||
            lower.contains("unterzeichnet") ||
            lower.contains("abgeschlossen") ||
            lower.contains("bestanden")
    }

    // ------------------------------------------------------------------
    // Hivatalos feladó
    // ------------------------------------------------------------------

    /**
     * Annak eldöntése, hogy a feladó neve alapján hivatalos/tanulmányi
     * üzenetről van-e szó.
     */
    fun isOfficialSender(sender: String): Boolean {
        if (sender.isBlank()) return true
        val lower = sender.lowercase()
        return lower.contains("hivatal") ||
            lower.contains("tanulmányi") ||
            lower.contains("tanulmanyi") ||
            lower.contains("official") ||
            lower.contains("academic") ||
            lower.contains("registry") ||
            lower.contains("student affairs") ||
            lower.contains("study office") ||
            lower.contains("studien") ||
            lower.contains("prüfung") ||
            lower.contains("pruefung") ||
            lower.contains("sekretariat") ||
            lower.contains("dekanat")
    }

    // ------------------------------------------------------------------
    // Óratípus
    // ------------------------------------------------------------------

    /**
     * Óratípus felismerése a naptárelem összes szövegéből (magyar, angol,
     * német kulcsszavakkal). A [courseCode] előtagját is figyelembe veszi
     * (L = labor, G = gyakorlat), a korábbi viselkedéssel megegyezően.
     */
    fun courseTypeFromText(fullTextLowercase: String, courseCode: String = ""): CourseType {
        val text = fullTextLowercase.lowercase()
        return when {
            text.contains("labor") || text.contains("lab") ||
                text.contains("laboratory") ||
                courseCode.contains("lab", ignoreCase = true) ||
                courseCode.startsWith("L", ignoreCase = true) -> CourseType.LAB

            text.contains("gyakorlat") || text.contains("gyak") ||
                text.contains("practice") || text.contains("practical") ||
                text.contains("übung") || text.contains("uebung") ||
                courseCode.contains("gyak", ignoreCase = true) ||
                courseCode.startsWith("G", ignoreCase = true) -> CourseType.PRACTICE

            text.contains("szeminárium") || text.contains("szem") ||
                text.contains("seminar") -> CourseType.SEMINAR

            text.contains("vizsga") ||
                text.contains("exam") ||
                text.contains("prüfung") || text.contains("pruefung") ||
                text.contains("klausur") -> CourseType.EXAM

            text.contains("előadás") || text.contains("eloadas") ||
                text.contains("elmélet") || text.contains("elmelet") ||
                text.contains("lecture") ||
                text.contains("vorlesung") ||
                text.contains("ea") -> CourseType.LECTURE

            else -> CourseType.LECTURE
        }
    }
}
