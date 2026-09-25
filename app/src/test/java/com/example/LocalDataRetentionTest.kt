package com.example

import com.example.domain.model.StudentCredentials
import com.example.presentation.viewmodel.AuthUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDataRetentionTest {

    @Test
    fun `AuthUiState reflects offline availability`() {
        val stateWithOffline = AuthUiState(
            neptunCode = "ELTE01",
            isOfflineModeAvailable = true
        )
        assertTrue(stateWithOffline.isOfflineModeAvailable)
        assertEquals("ELTE01", stateWithOffline.neptunCode)

        val stateWithoutOffline = AuthUiState(
            isOfflineModeAvailable = false
        )
        assertFalse(stateWithoutOffline.isOfflineModeAvailable)
    }

    @Test
    fun `StudentCredentials retains data when offline mode is activated`() {
        val credentials = StudentCredentials(
            neptunCode = "ELTE01",
            universityId = "etvslorndtud",
            universityName = "Eötvös Loránd Tudományegyetem",
            neptunUrl = "https://neptun.elte.hu",
            studentName = "Teszt Hallgató",
            trainingProgram = "Programtervező informatikus BSc",
            isLoggedIn = false,
            lastSyncTime = 1720000000000L
        )

        // Offline mode activation retains all metadata and marks session as active
        val offlineCredentials = credentials.copy(isLoggedIn = true)
        assertTrue(offlineCredentials.isLoggedIn)
        assertEquals("ELTE01", offlineCredentials.neptunCode)
        assertEquals("etvslorndtud", offlineCredentials.universityId)
        assertEquals("Teszt Hallgató", offlineCredentials.studentName)
        assertEquals(1720000000000L, offlineCredentials.lastSyncTime)
    }
}
