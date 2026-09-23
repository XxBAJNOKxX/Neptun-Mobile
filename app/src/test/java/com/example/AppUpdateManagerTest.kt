package com.example

import com.example.core.update.AppUpdateManager
import com.example.core.update.ReleaseAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    private val updateManager = AppUpdateManager()

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
    fun `selectBestAsset chooses debug APK for debug app`() {
        val assets = listOf(
            ReleaseAsset(name = "neptun-mobile-0.2.33-dev-release.apk", downloadUrl = "https://example.com/release.apk"),
            ReleaseAsset(name = "app-debug.apk", downloadUrl = "https://example.com/app-debug.apk"),
            ReleaseAsset(name = "neptun-mobile-0.2.33-dev.apk", downloadUrl = "https://example.com/dev.apk")
        )

        val chosen = AppUpdateManager.selectBestAsset(assets, isCurrentAppDebug = true)
        assertNotNull(chosen)
        assertEquals("app-debug.apk", chosen?.name)
    }

    @Test
    fun `selectBestAsset chooses release APK for release app`() {
        val assets = listOf(
            ReleaseAsset(name = "app-debug.apk", downloadUrl = "https://example.com/app-debug.apk"),
            ReleaseAsset(name = "neptun-mobile-0.2.33-dev-release.apk", downloadUrl = "https://example.com/release.apk"),
            ReleaseAsset(name = "neptun-mobile-0.2.33-dev.apk", downloadUrl = "https://example.com/dev.apk")
        )

        val chosen = AppUpdateManager.selectBestAsset(assets, isCurrentAppDebug = false)
        assertNotNull(chosen)
        assertEquals("neptun-mobile-0.2.33-dev-release.apk", chosen?.name)
    }

    @Test
    fun `selectBestAsset falls back to any APK if preferred flavor not found`() {
        val assetsOnlyDebug = listOf(
            ReleaseAsset(name = "app-debug.apk", downloadUrl = "https://example.com/app-debug.apk")
        )
        val chosenForRelease = AppUpdateManager.selectBestAsset(assetsOnlyDebug, isCurrentAppDebug = false)
        assertNotNull(chosenForRelease)
        assertEquals("app-debug.apk", chosenForRelease?.name)

        val assetsOnlyRelease = listOf(
            ReleaseAsset(name = "app-release.apk", downloadUrl = "https://example.com/app-release.apk")
        )
        val chosenForDebug = AppUpdateManager.selectBestAsset(assetsOnlyRelease, isCurrentAppDebug = true)
        assertNotNull(chosenForDebug)
        assertEquals("app-release.apk", chosenForDebug?.name)
    }

    @Test
    fun `getSafeUpdateFileName sanitizes special characters and preserves valid base name`() {
        val name1 = AppUpdateManager.getSafeUpdateFileName("v0.2.33-dev", "app-debug.apk", "https://example.com/test")
        assertEquals("0.2.33-dev_app-debug", name1)

        val name2 = AppUpdateManager.getSafeUpdateFileName("v1.0.0", "neptun-release.apk", "https://example.com/test")
        assertEquals("1.0.0_neptun-release", name2)
    }
}
