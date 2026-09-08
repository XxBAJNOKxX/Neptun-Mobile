package com.example

import com.example.core.update.AppUpdateManager
import org.junit.Assert.assertFalse
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
}
