package com.example

import com.example.core.update.AppUpdateManager
import com.example.core.update.ReleaseAsset
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AppUpdateManagerTest {

    private val updateManager = AppUpdateManager()

    @Before
    fun setUp() {
        AppUpdateManager.clearApiCache()
    }

    @Test
    fun `isNewerVersion returns true when stable version is newer than dev version`() {
        assertTrue(updateManager.isNewerVersion("v0.2.0", "0.1.45-dev"))
        assertTrue(updateManager.isNewerVersion("0.2.0", "0.1.45-dev"))
    }

    @Test
    fun `isNewerVersion returns true when dev run number increases`() {
        assertTrue(updateManager.isNewerVersion("v0.1.46-dev", "0.1.45-dev"))
    }

    @Test
    fun `isNewerVersion returns false when dev run number is lower or equal`() {
        assertFalse(updateManager.isNewerVersion("v0.1.44-dev", "0.1.45-dev"))
        assertFalse(updateManager.isNewerVersion("v0.1.45-dev", "0.1.45-dev"))
    }

    @Test
    fun `isNewerVersion handles stable patch increments correctly`() {
        assertTrue(updateManager.isNewerVersion("v0.2.1", "0.2.0"))
        assertFalse(updateManager.isNewerVersion("v0.2.0", "0.2.1"))
    }

    @Test
    fun `isNewerVersion considers stable version newer than dev version with same numbers`() {
        assertTrue(updateManager.isNewerVersion("v0.2.0", "0.2.0-dev"))
    }

    @Test
    fun `isNewerVersion handles debug suffix with build numbers correctly`() {
        // Magasabb build szám -> újabb
        assertTrue(updateManager.isNewerVersion("v0.1.0-debug+106", "0.1.0-debug+105"))
        assertFalse(updateManager.isNewerVersion("v0.1.0-debug+105", "0.1.0-debug+106"))
        assertFalse(updateManager.isNewerVersion("v0.1.0-debug+105", "0.1.0-debug+105"))

        // Stabil release újabb, mint az azonos számú debug build
        assertTrue(updateManager.isNewerVersion("v0.1.0", "0.1.0-debug+105"))
        assertFalse(updateManager.isNewerVersion("v0.1.0-debug+105", "0.1.0"))
    }

    @Test
    fun `parsedVersion correctly parses complex version strings`() {
        val v1 = AppUpdateManager.ParsedVersion.parse("v1.2.3")
        assertEquals(1, v1.major)
        assertEquals(2, v1.minor)
        assertEquals(3, v1.patch)
        assertFalse(v1.isDebugOrDev)
        assertEquals(0L, v1.buildNumber)

        val v2 = AppUpdateManager.ParsedVersion.parse("0.5.0-debug+1234")
        assertEquals(0, v2.major)
        assertEquals(5, v2.minor)
        assertEquals(0, v2.patch)
        assertTrue(v2.isDebugOrDev)
        assertEquals(1234L, v2.buildNumber)

        val v3 = AppUpdateManager.ParsedVersion.parse("0.5.0-dev.99")
        assertEquals(99L, v3.buildNumber)
        assertTrue(v3.isDebugOrDev)
    }

    @Test
    fun `parsedVersion handles major and minor version bumps`() {
        val oldMajor = AppUpdateManager.ParsedVersion.parse("1.99.99")
        val newMajor = AppUpdateManager.ParsedVersion.parse("2.0.0")
        assertTrue(newMajor.isNewerThan(oldMajor))
        assertFalse(oldMajor.isNewerThan(newMajor))

        val oldMinor = AppUpdateManager.ParsedVersion.parse("1.2.99")
        val newMinor = AppUpdateManager.ParsedVersion.parse("1.3.0")
        assertTrue(newMinor.isNewerThan(oldMinor))
        assertFalse(oldMinor.isNewerThan(newMinor))
    }

    @Test
    fun `selectBestAsset chooses debug APK for debug app`() {
        val assets = listOf(
            ReleaseAsset(name = "NeptunMobile-0.2.0-release.apk", downloadUrl = "https://example.com/release.apk"),
            ReleaseAsset(name = "NeptunMobile-0.2.0-debug.apk", downloadUrl = "https://example.com/debug.apk")
        )

        val chosen = AppUpdateManager.selectBestAsset(assets, isCurrentAppDebug = true)
        assertNotNull(chosen)
        assertEquals("NeptunMobile-0.2.0-debug.apk", chosen?.name)
    }

    @Test
    fun `selectBestAsset chooses release APK for release app`() {
        val assets = listOf(
            ReleaseAsset(name = "NeptunMobile-0.2.0-debug.apk", downloadUrl = "https://example.com/debug.apk"),
            ReleaseAsset(name = "NeptunMobile-0.2.0-release.apk", downloadUrl = "https://example.com/release.apk")
        )

        val chosen = AppUpdateManager.selectBestAsset(assets, isCurrentAppDebug = false)
        assertNotNull(chosen)
        assertEquals("NeptunMobile-0.2.0-release.apk", chosen?.name)
    }

    @Test
    fun `selectBestAsset never offers debug APK to release app and returns null`() {
        val assetsOnlyDebug = listOf(
            ReleaseAsset(name = "NeptunMobile-0.2.0-debug.apk", downloadUrl = "https://example.com/debug.apk"),
            ReleaseAsset(name = "app-debug.apk", downloadUrl = "https://example.com/app-debug.apk")
        )
        val chosenForRelease = AppUpdateManager.selectBestAsset(assetsOnlyDebug, isCurrentAppDebug = false)
        assertNull("A release app soha nem kaphat debug APK-t!", chosenForRelease)
    }

    @Test
    fun `selectBestAsset never offers release APK to debug app and returns null`() {
        val assetsOnlyRelease = listOf(
            ReleaseAsset(name = "NeptunMobile-0.2.0-release.apk", downloadUrl = "https://example.com/release.apk"),
            ReleaseAsset(name = "app-release.apk", downloadUrl = "https://example.com/app-release.apk")
        )
        val chosenForDebug = AppUpdateManager.selectBestAsset(assetsOnlyRelease, isCurrentAppDebug = true)
        assertNull("A debug app soha nem kaphat release APK-t!", chosenForDebug)
    }

    @Test
    fun `selectBestAsset returns null when no APK assets are available`() {
        val nonApkAssets = listOf(
            ReleaseAsset(name = "source-code.zip", downloadUrl = "https://example.com/src.zip"),
            ReleaseAsset(name = "mapping.txt", downloadUrl = "https://example.com/map.txt")
        )
        assertNull(AppUpdateManager.selectBestAsset(nonApkAssets, isCurrentAppDebug = true))
        assertNull(AppUpdateManager.selectBestAsset(nonApkAssets, isCurrentAppDebug = false))
    }

    @Test
    fun `api cache stores response and can be cleared`() {
        AppUpdateManager.apiCache["test_url"] = AppUpdateManager.Companion.CachedApiResponse(
            body = "{\"tag_name\":\"v1.0.0\"}",
            etag = "W/\"12345\"",
            timestamp = System.currentTimeMillis()
        )
        assertEquals(1, AppUpdateManager.apiCache.size)
        assertEquals("{\"tag_name\":\"v1.0.0\"}", AppUpdateManager.apiCache["test_url"]?.body)

        AppUpdateManager.clearApiCache()
        assertEquals(0, AppUpdateManager.apiCache.size)
    }

    @Test
    fun `getSafeUpdateFileName sanitizes special characters and preserves valid base name`() {
        val name1 = AppUpdateManager.getSafeUpdateFileName("v0.2.33-dev", "app-debug.apk", "https://example.com/test")
        assertEquals("0.2.33-dev_app-debug", name1)

        val name2 = AppUpdateManager.getSafeUpdateFileName("v1.0.0", "neptun-release.apk", "https://example.com/test")
        assertEquals("1.0.0_neptun-release", name2)
    }

    @Test
    fun `calculateSha256 computes expected hash for file`() {
        val tempFile = java.io.File.createTempFile("sha_test", ".txt").apply {
            writeText("hello world")
            deleteOnExit()
        }
        val hash = updateManager.calculateSha256(tempFile)
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash)
    }
}
