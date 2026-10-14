package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.notification.AlarmScheduler
import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.domain.usecase.CalculateAveragesUseCase
import com.example.presentation.viewmodel.DashboardViewModel
import com.example.presentation.viewmodel.TimetableViewModel
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiStateConsistencyTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var repository: FakeNeptunRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var calculateAveragesUseCase: CalculateAveragesUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        repository = FakeNeptunRepository()
        alarmScheduler = AlarmScheduler(context)
        calculateAveragesUseCase = CalculateAveragesUseCase()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTimetableViewModel_initialSyncSuccess_setsHasSyncedAndLastSyncTime() = runTest(testDispatcher) {
        repository.refreshCalendarResult = Result.success(Unit)

        val viewModel = TimetableViewModel(repository, alarmScheduler)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("hasSyncedSuccessfully should be true on success", state.hasSyncedSuccessfully)
        assertTrue("lastSyncTime should be > 0", state.lastSyncTime > 0L)
        assertNull("errorMessage should be null on success", state.errorMessage)
    }

    @Test
    fun testTimetableViewModel_syncFailure_preservesSyncTimeAndSetsError() = runTest(testDispatcher) {
        repository.refreshCalendarResult = Result.failure(Exception("Network offline"))

        val viewModel = TimetableViewModel(repository, alarmScheduler)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertFalse("hasSyncedSuccessfully should remain false if initial fetch failed", state.hasSyncedSuccessfully)
        assertEquals("lastSyncTime should be 0", 0L, state.lastSyncTime)
        assertEquals("errorMessage should be TIMETABLE_LOAD_FAILED", "TIMETABLE_LOAD_FAILED", state.errorMessage)

        // Now simulate a successful refresh
        repository.refreshCalendarResult = Result.success(Unit)
        viewModel.refreshCalendar(isManual = true)
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue("hasSyncedSuccessfully should be true after successful refresh", state.hasSyncedSuccessfully)
        val successfulSyncTime = state.lastSyncTime
        assertTrue("lastSyncTime should be > 0", successfulSyncTime > 0L)
        assertNull("errorMessage should be null", state.errorMessage)

        // Now simulate a subsequent failure
        repository.refreshCalendarResult = Result.failure(Exception("Timeout"))
        viewModel.refreshCalendar(isManual = true)
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue("hasSyncedSuccessfully should be preserved as true", state.hasSyncedSuccessfully)
        assertEquals("lastSyncTime should be preserved from last successful sync", successfulSyncTime, state.lastSyncTime)
        assertEquals("errorMessage should be set on failure", "TIMETABLE_LOAD_FAILED", state.errorMessage)
    }

    @Test
    fun testDashboardViewModel_initialSyncSuccessWithNoClasses_isLoadedAndSyncNotFailed() = runTest(testDispatcher) {
        repository.syncAllDataResult = Result.success(Unit)

        val viewModel = DashboardViewModel(repository, calculateAveragesUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("hasSyncedSuccessfully should be true on sync success", state.hasSyncedSuccessfully)
        assertTrue("isCalendarLoaded should be true after successful sync even if empty", state.isCalendarLoaded)
        assertFalse("isCalendarSyncFailed should be false", state.isCalendarSyncFailed)
        assertNull("errorMessage should be null", state.errorMessage)
    }

    @Test
    fun testDashboardViewModel_initialSyncFailureWithEmptyCache_isCalendarSyncFailedTrueAndNotLoaded() = runTest(testDispatcher) {
        repository.syncAllDataResult = Result.failure(Exception("Server 500"))

        val viewModel = DashboardViewModel(repository, calculateAveragesUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("hasSyncedSuccessfully should be false", state.hasSyncedSuccessfully)
        assertFalse("isCalendarLoaded should be false when cache is empty and sync failed", state.isCalendarLoaded)
        assertTrue("isCalendarSyncFailed should be true when sync fails on empty cache", state.isCalendarSyncFailed)
        assertEquals("errorMessage should be SYNC_FAILED", "SYNC_FAILED", state.errorMessage)
    }

    @Test
    fun testDashboardViewModel_syncFailureWithCachedData_displaysCacheAndNotSyncFailed() = runTest(testDispatcher) {
        val todayIso = LocalDate.now().toString()
        val sampleEvent = CalendarEvent(
            id = "event_1",
            subjectName = "Adatbázisok",
            subjectCode = "DB101",
            courseCode = "G1",
            location = "I épület",
            room = "IB028",
            teacherName = "Kovács Béla",
            startHour = 8,
            startMinute = 15,
            endHour = 10,
            endMinute = 0,
            dayOfWeek = LocalDate.now().dayOfWeek.value,
            courseType = CourseType.PRACTICE,
            dateString = todayIso
        )

        // Seed cached events in repository before DashboardViewModel is initialized
        repository.calendarEventsFlow.value = listOf(sampleEvent)
        repository.syncAllDataResult = Result.failure(Exception("No connection"))

        val viewModel = DashboardViewModel(repository, calculateAveragesUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("isCalendarLoaded should be true when cached events exist", state.isCalendarLoaded)
        assertFalse("isCalendarSyncFailed should be false when cached data is available", state.isCalendarSyncFailed)
        assertEquals("todayClasses should contain the cached event", 1, state.todayClasses.size)
        assertEquals("errorMessage should be SYNC_FAILED for error banner display", "SYNC_FAILED", state.errorMessage)
    }

    @Test
    fun testEmptyStateVsErrorState_logicVerification() = runTest(testDispatcher) {
        // Scenario 1: Fresh install, sync fails, cache empty
        // Verify UI states ensure NO false empty state is triggered.
        repository.refreshCalendarResult = Result.failure(Exception("Fail"))
        val timetableVm = TimetableViewModel(repository, alarmScheduler)
        advanceUntilIdle()

        val tState = timetableVm.uiState.value
        // When tState.errorMessage != null && tState.events.isEmpty(), error retry view MUST be shown, NOT EmptyDayNotice
        assertTrue("Timetable error with empty cache: should trigger error retry view",
            tState.errorMessage != null && tState.events.isEmpty())

        repository.syncAllDataResult = Result.failure(Exception("Fail"))
        val dashboardVm = DashboardViewModel(repository, calculateAveragesUseCase)
        advanceUntilIdle()

        val dState = dashboardVm.uiState.value
        // When !isLoaded || (todayClasses.isEmpty() && isCalendarSyncFailed), retry card MUST be shown, NOT free messages
        val shouldShowDashboardRetry = !dState.isCalendarLoaded || (dState.todayClasses.isEmpty() && dState.isCalendarSyncFailed)
        assertTrue("Dashboard error with empty cache: should show subtle retry card", shouldShowDashboardRetry)

        val canShowFreeMessages = dState.isCalendarLoaded && !dState.isCalendarSyncFailed
        assertFalse("Dashboard error with empty cache: MUST NOT show free day messages", canShowFreeMessages)

        // Scenario 2: Successful sync with genuine 0 events (e.g. holidays)
        repository.syncAllDataResult = Result.success(Unit)
        dashboardVm.refresh()
        advanceUntilIdle()

        val dSuccessState = dashboardVm.uiState.value
        assertTrue("Dashboard genuine empty: isCalendarLoaded is true", dSuccessState.isCalendarLoaded)
        assertFalse("Dashboard genuine empty: isCalendarSyncFailed is false", dSuccessState.isCalendarSyncFailed)
        val canShowFreeMessagesNow = dSuccessState.isCalendarLoaded && !dSuccessState.isCalendarSyncFailed
        assertTrue("Dashboard genuine empty: CAN show free day messages", canShowFreeMessagesNow)
    }
}
