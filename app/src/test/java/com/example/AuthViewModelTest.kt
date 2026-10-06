package com.example

import com.example.domain.model.StudentCredentials
import com.example.domain.model.University
import com.example.presentation.viewmodel.AuthViewModel
import com.example.test.FakeAuthRepository
import com.example.test.FakeNeptunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var neptunRepository: FakeNeptunRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        neptunRepository = FakeNeptunRepository()
        viewModel = AuthViewModel(authRepository, neptunRepository)
    }

    @Test
    fun `initial data load sets universities and matched saved university`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.universities.size)
        assertEquals("bme", state.selectedUniversity?.id)
        assertEquals("TEST01", state.neptunCode)
        assertTrue(state.isOfflineModeAvailable)
    }

    @Test
    fun `search query filters universities list`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Eötvös")
        val state = viewModel.uiState.value
        assertEquals("Eötvös", state.searchQuery)
        assertEquals(1, state.filteredUniversities.size)
        assertEquals("elte", state.filteredUniversities.first().id)

        // Clear filter
        viewModel.onSearchQueryChange("")
        assertEquals(3, viewModel.uiState.value.filteredUniversities.size)
    }

    @Test
    fun `selecting university updates selectedUniversity and saves it`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val corvinus = University("corvinus", "Budapesti Corvinus Egyetem", "Corvinus", "Budapest", "https://neptun.uni-corvinus.hu")
        viewModel.onSelectUniversity(corvinus)

        val state = viewModel.uiState.value
        assertEquals("corvinus", state.selectedUniversity?.id)
        assertEquals("corvinus", authRepository.storedUniId)
    }

    @Test
    fun `input credentials changes neptunCode and password`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNeptunCodeChange("TEST99")
        viewModel.onPasswordChange("secretPass123")

        val state = viewModel.uiState.value
        assertEquals("TEST99", state.neptunCode)
        assertEquals("secretPass123", state.password)
    }

    @Test
    fun `quickDemoFill populates demo credentials`() = runTest {
        viewModel.quickDemoFill()

        val state = viewModel.uiState.value
        assertEquals("DEMO01", state.neptunCode)
        assertEquals("demo", state.password)
    }

    @Test
    fun `successful login updates credentials state`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNeptunCodeChange("USER01")
        viewModel.onPasswordChange("Pass123")
        viewModel.login()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.credentials)
        assertEquals("USER01", state.credentials?.neptunCode)
        assertTrue(state.credentials?.isLoggedIn == true)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `logout clears active credentials`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNeptunCodeChange("USER01")
        viewModel.onPasswordChange("Pass123")
        viewModel.login()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.credentials)

        viewModel.logout()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.credentials)
    }

    @Test
    fun `continueOffline enters offline mode with stored credentials`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.continueOffline()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.credentials)
        assertEquals("OFFLINE01", state.credentials?.neptunCode)
    }

    @Test
    fun `initiateQuickReAuth with saved credentials and password logs in directly`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        authRepository.storedPassword = "savedSecretPassword"
        authRepository.credentialsFlow.value = StudentCredentials(
            neptunCode = "TEST01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu",
            studentName = "Teszt Hallgató",
            trainingProgram = "BSc",
            isLoggedIn = true,
            lastSyncTime = System.currentTimeMillis()
        )

        viewModel.initiateQuickReAuth()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.credentials)
        assertEquals("TEST01", state.credentials?.neptunCode)
        assertFalse(state.isPasswordPromptRequired)
        assertFalse(state.isQuickReAuthOpen)
    }

    @Test
    fun `initiateQuickReAuth without saved password prompts for password`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        authRepository.storedPassword = ""
        authRepository.credentialsFlow.value = StudentCredentials(
            neptunCode = "TEST01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu",
            studentName = "Teszt Hallgató",
            trainingProgram = "BSc",
            isLoggedIn = true,
            lastSyncTime = System.currentTimeMillis()
        )

        viewModel.initiateQuickReAuth()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isQuickReAuthOpen)
        assertTrue(state.isPasswordPromptRequired)

        // Now submit password
        viewModel.submitReAuthPassword("enteredPass123")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertNotNull(finalState.credentials)
        assertFalse(finalState.isPasswordPromptRequired)
        assertFalse(finalState.isQuickReAuthOpen)
        assertEquals("enteredPass123", authRepository.storedPassword)
    }

    @Test
    fun `initiateQuickReAuth with 2FA requires user to request email code and shows prefix`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        authRepository.storedPassword = "savedSecretPassword"
        authRepository.shouldRequire2FASession = true
        authRepository.credentialsFlow.value = StudentCredentials(
            neptunCode = "TEST01",
            universityId = "elte",
            universityName = "ELTE",
            neptunUrl = "https://neptun.elte.hu",
            studentName = "Teszt Hallgató",
            trainingProgram = "BSc",
            isLoggedIn = true,
            lastSyncTime = System.currentTimeMillis()
        )

        viewModel.initiateQuickReAuth()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterReauth = viewModel.uiState.value
        assertTrue(stateAfterReauth.isTwoFactorRequired)
        assertFalse(stateAfterReauth.isEmailCodeRequested)
        assertEquals("", stateAfterReauth.codePrefix)

        // User clicks "Kód kérése e-mailben"
        viewModel.requestEmailCode()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterRequest = viewModel.uiState.value
        assertTrue(stateAfterRequest.isEmailCodeRequested)
        assertEquals("AZ", stateAfterRequest.codePrefix)
        assertNotNull(stateAfterRequest.twoFactorSuccessMessage)

        // Submitting invalid code fails and leaves dialog open
        viewModel.onTwoFactorCodeChange("999999")
        viewModel.submitTwoFactor()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterInvalid = viewModel.uiState.value
        assertTrue(stateAfterInvalid.isTwoFactorRequired)
        assertNotNull(stateAfterInvalid.twoFactorErrorMessage)

        // Submitting valid code succeeds and dismisses 2FA
        viewModel.onTwoFactorCodeChange("123456")
        viewModel.submitTwoFactor()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterValid = viewModel.uiState.value
        assertFalse(stateAfterValid.isTwoFactorRequired)
        assertFalse(stateAfterValid.isQuickReAuthOpen)
        assertNotNull(stateAfterValid.credentials)
    }

    @Test
    fun `standard login with 2FA starts with unrequested email code and populates prefix on request`() = runTest {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        authRepository.shouldRequire2FASession = true
        viewModel.onNeptunCodeChange("USER01")
        viewModel.onPasswordChange("Pass123")
        viewModel.login()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isTwoFactorRequired)
        assertFalse(state.isEmailCodeRequested)
        assertEquals("", state.codePrefix)

        viewModel.requestEmailCode()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.uiState.value
        assertTrue(updated.isEmailCodeRequested)
        assertEquals("AZ", updated.codePrefix)
    }
}
