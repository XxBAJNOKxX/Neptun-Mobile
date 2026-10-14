package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.EncryptedPreferencesManager
import com.example.domain.model.Neptun2FASession
import com.example.domain.model.StudentCredentials
import com.example.domain.model.TwoFactorMethod
import com.example.presentation.viewmodel.AuthViewModel
import com.example.test.FakeAuthRepository
import com.example.test.FakeNeptunRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionExpiredSnoozeTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var prefsManager: EncryptedPreferencesManager
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var neptunRepository: FakeNeptunRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefsManager = EncryptedPreferencesManager(context)
        authRepository = FakeAuthRepository()
        neptunRepository = FakeNeptunRepository()
        authViewModel = AuthViewModel(authRepository, neptunRepository)
    }

    @Test
    fun `setting 15m snooze causes isSessionExpiredSnoozed to return true`() {
        assertFalse(prefsManager.isSessionExpiredSnoozed())

        prefsManager.setSessionExpiredSnooze(15 * 60 * 1000L)
        assertTrue(prefsManager.isSessionExpiredSnoozed())

        prefsManager.clearSessionExpiredSnooze()
        assertFalse(prefsManager.isSessionExpiredSnoozed())
    }

    @Test
    fun `markSessionExpired does not set session expired flag when snoozed`() {
        assertFalse(prefsManager.isSessionExpired())

        // Snooze for 15 minutes
        prefsManager.setSessionExpiredSnooze(15 * 60 * 1000L)
        assertTrue(prefsManager.isSessionExpiredSnoozed())

        // Try marking session as expired while snoozed
        prefsManager.markSessionExpired()
        assertFalse("Session must not be marked expired when snoozed", prefsManager.isSessionExpired())
        assertFalse(prefsManager.sessionExpiredFlow.value)

        // Clear snooze and verify marking as expired works
        prefsManager.clearSessionExpiredSnooze()
        assertFalse(prefsManager.isSessionExpiredSnoozed())

        prefsManager.markSessionExpired()
        assertTrue("Session must be marked expired when snooze is cleared", prefsManager.isSessionExpired())
        assertTrue(prefsManager.sessionExpiredFlow.value)
    }

    @Test
    fun `authViewModel snoozeSessionExpired sets snooze and clears expired flag`() {
        authViewModel.snoozeSessionExpired(minutes = 15)

        assertTrue(authRepository.isSessionExpiredSnoozed())
        assertTrue(authRepository.sessionExpiredCleared)
    }

    @Test
    fun `quick re-auth triggers data reload and sync`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val savedCreds = StudentCredentials(
            neptunCode = "TEST01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu",
            studentName = "Teszt Hallgató",
            trainingProgram = "BSc",
            isLoggedIn = true,
            lastSyncTime = 1000L
        )
        authRepository.credentialsFlow.value = savedCreds
        authRepository.savePassword("savedSecret")
        authRepository.setSessionExpiredSnooze(15 * 60 * 1000L)

        val initialSyncCount = neptunRepository.syncAllDataCallCount

        authViewModel.initiateQuickReAuth()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(
            "Sync count must increase after quick re-auth",
            neptunRepository.syncAllDataCallCount > initialSyncCount
        )
        assertEquals("TEST01", neptunRepository.lastSyncAllDataNeptunCode)
        assertFalse("Snooze must be cleared on successful quick re-auth", authRepository.isSessionExpiredSnoozed())
    }

    @Test
    fun `submitTwoFactor completion triggers data reload and sync`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val session = Neptun2FASession(
            neptunCode = "TEST01",
            key = "KEY_456",
            phase = "VerifyCode",
            rendered = "",
            verificationToken = "token_456",
            hasTotp = true,
            hasEmail = false,
            codePrefix = "",
            cookies = emptyMap(),
            baseUrl = "https://neptun.bme.hu"
        )

        authViewModel.onTwoFactorMethodChange(TwoFactorMethod.TOTP)
        authViewModel.onTwoFactorCodeChange("123456")

        // Set two-factor session in ViewModel state
        val method = AuthViewModel::class.java.getDeclaredField("_uiState")
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = method.get(authViewModel) as kotlinx.coroutines.flow.MutableStateFlow<com.example.presentation.viewmodel.AuthUiState>
        stateFlow.value = stateFlow.value.copy(
            twoFactorSession = session,
            isTwoFactorRequired = true,
            twoFactorCode = "123456"
        )

        authRepository.setSessionExpiredSnooze(15 * 60 * 1000L)
        val initialSyncCount = neptunRepository.syncAllDataCallCount

        authViewModel.submitTwoFactor()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(
            "Sync count must increase after 2FA submission",
            neptunRepository.syncAllDataCallCount > initialSyncCount
        )
        assertEquals("TEST01", neptunRepository.lastSyncAllDataNeptunCode)
        assertFalse("Snooze must be cleared on successful 2FA", authRepository.isSessionExpiredSnoozed())
    }
}
