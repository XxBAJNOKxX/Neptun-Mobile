package com.example.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Egy Neptun szerver által támogatott felület/adatnyelv.
 *
 * Forrás: `GET {base}/api/General/EnvironmentData` →
 * `data.supportedLanguages: [{code, name, lcid, selected}]`
 * (lásd a `neptun3.ppke.hu.har.txt` hálózati naplót).
 *
 * A legtöbb Neptun a magyar (1038), angol (1033) és német (1031) nyelvet
 * kínálja, de intézményenként lehet eltérés (hiányzó vagy extra nyelv),
 * ezért a lista mindig dinamikusan, a szerverről töltődik.
 */
@Serializable
data class NeptunLanguage(
    /** ISO nyelvkód, pl. "hu", "en", "de". */
    val code: String,
    /** Szerver által küldött megnevezés, pl. "magyar (Magyarország)". */
    val name: String,
    /** Windows LCID, pl. 1038. Ezt várja az `Authenticate` kérés `LCID` mezője. */
    val lcid: Int,
    /** A szerver által megjelölt alapértelmezett nyelv-e. */
    val selected: Boolean = false
)

/**
 * A választható nyelvekhez tartozó segédfüggvények (tisztán lokális logika,
 * hálózat nélkül is unit-tesztelhető).
 */
object NeptunLanguages {

    const val DEFAULT_LCID = 1038

    const val LCID_HUNGARIAN = 1038
    const val LCID_ENGLISH = 1033
    const val LCID_GERMAN = 1031

    /**
     * Tartalék lista arra az esetre, ha az `EnvironmentData` végpont nem
     * érhető el (régi/legacy szerver, offline állapot). A három leggyakoribb
     * Neptun-nyelv.
     */
    val FALLBACK_LANGUAGES: List<NeptunLanguage> = listOf(
        NeptunLanguage(code = "hu", name = "magyar (Magyarország)", lcid = LCID_HUNGARIAN, selected = true),
        NeptunLanguage(code = "en", name = "English (United States)", lcid = LCID_ENGLISH),
        NeptunLanguage(code = "de", name = "Deutsch (Deutschland)", lcid = LCID_GERMAN)
    )

    private val lenientJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Ismert LCID → (BCP-47 címke, ISO nyelvkód) leképezés az
     * `Accept-Language` fejléc összeállításához. Ismeretlen LCID esetén a
     * [code] paraméterből próbálunk címkét gyártani, végső esetben a magyar
     * fejlécet használjuk (ez volt a korábbi, mindenhol hardkódolt viselkedés).
     */
    private val KNOWN_LCID_TAGS: Map<Int, Pair<String, String>> = mapOf(
        1026 to ("bg-BG" to "bg"),
        1029 to ("cs-CZ" to "cs"),
        1031 to ("de-DE" to "de"),
        1032 to ("el-GR" to "el"),
        1033 to ("en-US" to "en"),
        1034 to ("es-ES" to "es"),
        1035 to ("fi-FI" to "fi"),
        1036 to ("fr-FR" to "fr"),
        1038 to ("hu-HU" to "hu"),
        1040 to ("it-IT" to "it"),
        1043 to ("nl-NL" to "nl"),
        1045 to ("pl-PL" to "pl"),
        1046 to ("pt-BR" to "pt"),
        1048 to ("ro-RO" to "ro"),
        1049 to ("ru-RU" to "ru"),
        1050 to ("hr-HR" to "hr"),
        1051 to ("sk-SK" to "sk"),
        1053 to ("sv-SE" to "sv"),
        1055 to ("tr-TR" to "tr"),
        1059 to ("by-BY" to "by"),
        1060 to ("sl-SI" to "sl"),
        1061 to ("et-EE" to "et"),
        1062 to ("lv-LV" to "lv"),
        1063 to ("lt-LT" to "lt"),
        1067 to ("uz-UZ" to "uz"),
        1087 to ("kk-KZ" to "kk")
    )

    /**
     * Az `EnvironmentData` JSON-válaszból kinyeri a támogatott nyelveket.
     * Üres listát ad, ha a válasz nem tartalmaz értelmezhető listát.
     */
    fun parseSupportedLanguages(environmentDataJson: String): List<NeptunLanguage> {
        if (environmentDataJson.isBlank()) return emptyList()
        return try {
            val root = lenientJson.parseToJsonElement(environmentDataJson).jsonObject
            val dataObj = root["data"]?.let {
                try {
                    it.jsonObject
                } catch (_: Exception) {
                    null
                }
            }
            val array = dataObj?.get("supportedLanguages")?.let {
                try {
                    it.jsonArray
                } catch (_: Exception) {
                    null
                }
            } ?: root["supportedLanguages"]?.let {
                try {
                    it.jsonArray
                } catch (_: Exception) {
                    null
                }
            } ?: return emptyList()

            array.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val lcid = obj["lcid"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
                    if (lcid <= 0) return@mapNotNull null
                    val code = obj["code"]?.jsonPrimitive?.content?.trim().orEmpty()
                    val name = obj["name"]?.jsonPrimitive?.content?.trim().orEmpty()
                    val selected = try {
                        obj["selected"]?.jsonPrimitive?.content == "true"
                    } catch (_: Exception) {
                        false
                    }
                    NeptunLanguage(
                        code = code.ifEmpty { guessCodeForLcid(lcid) },
                        name = name.ifEmpty { code.ifEmpty { "LCID $lcid" } },
                        lcid = lcid,
                        selected = selected
                    )
                } catch (_: Exception) {
                    null
                }
            }.distinctBy { it.lcid }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Kiválasztja az effektív LCID-t: ha a [preferredLcid] szerepel a
     * [languages] listában, azt adja vissza, különben a szerver által
     * megjelölt (`selected`) nyelvet, majd a magyart, végül az első elemet.
     */
    fun resolveLcid(languages: List<NeptunLanguage>, preferredLcid: Int): Int {
        if (languages.isEmpty()) return preferredLcid.takeIf { it > 0 } ?: DEFAULT_LCID
        if (languages.any { it.lcid == preferredLcid }) return preferredLcid
        languages.firstOrNull { it.selected }?.let { return it.lcid }
        languages.firstOrNull { it.lcid == DEFAULT_LCID }?.let { return it.lcid }
        return languages.first().lcid
    }

    /**
     * `Accept-Language` fejlécérték a megadott LCID-hez. Példa (1033):
     * `en-US,en;q=0.9,hu;q=0.8,de;q=0.7`.
     */
    fun acceptLanguageHeader(lcid: Int, code: String? = null): String {
        val (tag, lang) = KNOWN_LCID_TAGS[lcid] ?: run {
            val normalizedCode = (code ?: guessCodeForLcid(lcid)).lowercase().take(2)
            if (normalizedCode.length == 2 && normalizedCode.all { it.isLetter() } && normalizedCode != "hu") {
                ("$normalizedCode-${normalizedCode.uppercase()}" to normalizedCode)
            } else {
                ("hu-HU" to "hu")
            }
        }
        // A másodlagos nyelvek sorrendje: a magyar és az angol mindig szerepel,
        // hogy a csak részben lokalizált Neptun-oldalak is értelmesen essenek vissza.
        val fallbacks = listOf("hu", "en", "de").filter { it != lang }
        val header = StringBuilder("$tag,$lang;q=0.9")
        val qualities = listOf("0.8", "0.7")
        fallbacks.forEachIndexed { index, fallback ->
            header.append(",$fallback;q=").append(qualities.getOrElse(index) { "0.6" })
        }
        return header.toString()
    }

    /**
     * Rövid, UI-ra való címke a szerver által küldött névből:
     * "magyar (Magyarország)" → "Magyar".
     */
    fun displayLabel(language: NeptunLanguage): String {
        val short = language.name.substringBefore(" (").substringBefore("(").trim()
        if (short.isNotEmpty()) {
            return short.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase() else ch.toString()
            }
        }
        return language.code.uppercase().ifEmpty { "LCID ${language.lcid}" }
    }

    /** Kétbetűs kódcsík a választóba: "HU", "EN", "DE". */
    fun codeBadge(language: NeptunLanguage): String {
        val code = language.code.trim()
        if (code.isNotEmpty()) return code.uppercase().take(3)
        return "LC${language.lcid}"
    }

    private fun guessCodeForLcid(lcid: Int): String {
        return KNOWN_LCID_TAGS[lcid]?.second ?: ""
    }
}
