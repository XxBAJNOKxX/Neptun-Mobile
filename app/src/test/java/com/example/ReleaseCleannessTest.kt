package com.example

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Automatizált release tisztaság-ellenőrzés.
 * Elbukik, ha a release forráskészletben vagy no-op csonkokban demó adatok,
 * demó hallgatói nevek vagy aktív debug jelzők találhatók.
 */
class ReleaseCleannessTest {

    private val forbiddenDemoKeywords = listOf(
        "BMEVIIIM01",
        "Demó Hallgató",
        "demo-token",
        "IB025",
        "cal_1",
        "BMESZITM02",
        "QBF11",
        "Dr. Szabó Péter"
    )

    @Test
    fun `verify release source set does not contain any mock or demo data`() {
        val projectRoot = sequenceOf(
            File("."),
            File(".."),
            File("../../..")
        ).map { it.canonicalFile }.firstOrNull { File(it, "app/src/release").exists() }
            ?: File(".").canonicalFile

        val releaseDir = File(projectRoot, "app/src/release")
        assertTrue("A release source set mappának léteznie kell: ${releaseDir.absolutePath}", releaseDir.exists())

        val releaseFiles = releaseDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        assertTrue("A release forrásfájloknak létezniük kell!", releaseFiles.isNotEmpty())

        for (file in releaseFiles) {
            val content = file.readText()
            for (keyword in forbiddenDemoKeywords) {
                assertFalse(
                    "TILTOTT DEMÓ ADAT TALÁLHATÓ a release forrásfájlban (${file.name}): '$keyword'",
                    content.contains(keyword, ignoreCase = true)
                )
            }
        }
    }

    @Test
    fun `verify release stubs define false for debug flags`() {
        val projectRoot = sequenceOf(
            File("."),
            File("..")
        ).map { it.canonicalFile }.firstOrNull { File(it, "app/src/release").exists() }
            ?: File(".").canonicalFile

        val debugFeaturesReleaseFile = File(projectRoot, "app/src/release/java/com/example/core/debug/DebugFeatures.kt")
        assertTrue("A release DebugFeatures.kt fájlnak léteznie kell!", debugFeaturesReleaseFile.exists())

        val content = debugFeaturesReleaseFile.readText()
        assertTrue(
            "A release DebugFeatures-ben az isDebug értékének kötelezően 'false'-nak kell lennie!",
            content.contains("val isDebug: Boolean = false")
        )
        assertTrue(
            "A release DebugFeatures-ben az isDemoAllowed értékének kötelezően 'false'-nak kell lennie!",
            content.contains("val isDemoAllowed: Boolean = false")
        )
        assertFalse(
            "A release DebugFeatures nem tartalmazhat StrictMode inicializálást!",
            content.contains("StrictMode.setThreadPolicy")
        )
    }
}
