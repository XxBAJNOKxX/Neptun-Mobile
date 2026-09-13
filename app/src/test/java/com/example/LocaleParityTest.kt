package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A felületi szövegek (values/ + values-hu/ + values-de/) összhangját őrzi:
 * azonos kulcskészlet, azonos formátum-argumentumok, hivatkozott kulcsok létezése.
 * Tisztán JVM-alapú teszt, Android-környezet nélkül fut.
 */
class LocaleParityTest {

    private val resDir: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (!File(dir, "src/main/res").isDirectory) {
            dir = dir.parentFile ?: error("src/main/res nem található innen: ${System.getProperty("user.dir")}")
        }
        File(dir, "src/main/res")
    }

    private data class ResEntry(
        val name: String,
        /** Formátum-argumentumok, pl. ["%1$s", "%2$d"]. Plurals esetén az összes item uniója. */
        val args: Set<String>
    )

    private fun parseEntries(file: File): List<ResEntry> {
        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
        doc.documentElement.normalize()
        val out = mutableListOf<ResEntry>()
        val strings = doc.getElementsByTagName("string")
        for (i in 0 until strings.length) {
            val el = strings.item(i) as Element
            out += ResEntry(el.getAttribute("name"), formatArgs(el.textContent))
        }
        val plurals = doc.getElementsByTagName("plurals")
        for (i in 0 until plurals.length) {
            val el = plurals.item(i) as Element
            val items = el.getElementsByTagName("item")
            val args = mutableSetOf<String>()
            for (j in 0 until items.length) {
                args += formatArgs(items.item(j).textContent)
            }
            out += ResEntry(el.getAttribute("name"), args)
        }
        return out
    }

    private fun formatArgs(text: String): Set<String> {
        val noEscapedPercents = text.replace("%%", "")
        return Regex("%(?:\\d+\\$)?[sdfoxXeEgGcCaA]")
            .findAll(noEscapedPercents)
            .map { it.value }
            .toSet()
    }

    @Test
    fun `all locales define the same keys`() {
        val base = parseEntries(File(resDir, "values/strings.xml")).associate { it.name to it }
        assertTrue("Az alap values/ üres?", base.isNotEmpty())
        for (locale in listOf("values-hu", "values-de")) {
            val other = parseEntries(File(resDir, "$locale/strings.xml")).associate { it.name to it }
            assertEquals(
                "Hiányzó kulcsok itt: $locale",
                emptySet<String>(),
                base.keys - other.keys
            )
            assertEquals(
                "Többlet kulcsok itt: $locale",
                emptySet<String>(),
                other.keys - base.keys
            )
        }
    }

    @Test
    fun `no duplicate keys within a locale`() {
        for (locale in listOf("values", "values-hu", "values-de")) {
            val names = parseEntries(File(resDir, "$locale/strings.xml")).map { it.name }
            val dupes = names.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            assertEquals("Dupla kulcsok itt: $locale", emptySet<String>(), dupes)
        }
    }

    @Test
    fun `format arguments match across locales`() {
        val base = parseEntries(File(resDir, "values/strings.xml")).associate { it.name to it.args }
        for (locale in listOf("values-hu", "values-de")) {
            val other = parseEntries(File(resDir, "$locale/strings.xml")).associate { it.name to it.args }
            for ((name, baseArgs) in base) {
                assertEquals(
                    "Eltérő formátum-argumentumok: $name ($locale)",
                    baseArgs,
                    other[name] ?: emptySet()
                )
            }
        }
    }

    @Test
    fun `referenced string and plural keys exist`() {
        val definedKeys = parseEntries(File(resDir, "values/strings.xml"))
            .map { it.name }.toSet()
        val srcDir = resDir.resolve("../java").normalize()
        val stringRefs = mutableSetOf<String>()
        val pluralRefs = mutableSetOf<String>()
        srcDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            Regex("R\\.string\\.([A-Za-z0-9_]+)").findAll(text).forEach { stringRefs += it.groupValues[1] }
            Regex("R\\.plurals\\.([A-Za-z0-9_]+)").findAll(text).forEach { pluralRefs += it.groupValues[1] }
        }
        assertTrue("Nincs R.string-hivatkozás a forrásokban?", stringRefs.isNotEmpty())
        assertEquals(
            "Hiányzó string-kulcsok",
            emptySet<String>(),
            (stringRefs + pluralRefs) - definedKeys
        )
    }

    @Test
    fun `every locale file is well-formed XML`() {
        // A parseEntries kivételt dob hibás XML esetén; itt explicit is ellenőrizzük.
        for (locale in listOf("values", "values-hu", "values-de")) {
            parseEntries(File(resDir, "$locale/strings.xml"))
        }
    }
}
