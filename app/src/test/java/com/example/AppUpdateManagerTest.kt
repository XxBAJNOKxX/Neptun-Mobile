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
