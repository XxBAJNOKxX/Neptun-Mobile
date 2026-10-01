package com.example

import com.example.domain.model.ExamItem
import com.example.domain.model.SubjectGrade
import com.example.domain.usecase.CalculateAveragesUseCase
import com.example.presentation.viewmodel.GradesViewModel
import com.example.test.FakeNeptunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GradesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var neptunRepository: FakeNeptunRepository
    private lateinit var calculateAveragesUseCase: CalculateAveragesUseCase
    private lateinit var viewModel: GradesViewModel

    private val sampleGrades = listOf(
        SubjectGrade(
            id = "subj_1",
            termId = "2025/26/1",
            termName = "2025/26/1",
            subjectName = "Matematika I",
            subjectCode = "MAT101",
            credit = 6,
            grade = 5,
            gradeText = "Jeles"
        ),
        SubjectGrade(
            id = "subj_2",
            termId = "2025/26/1",
            termName = "2025/26/1",
            subjectName = "Programozás I",
            subjectCode = "PROG1",
            credit = 5,
            grade = 4,
            gradeText = "Jó"
        ),
        SubjectGrade(
            id = "subj_3",
            termId = "2024/25/2",
            termName = "2024/25/2",
            subjectName = "Fizika I",
            subjectCode = "FIZ101",
            credit = 4,
            grade = 3,
            gradeText = "Közepes"
        )
    )

    @Before
    fun setUp() {
        neptunRepository = FakeNeptunRepository()
        calculateAveragesUseCase = CalculateAveragesUseCase()
        viewModel = GradesViewModel(
            neptunRepository = neptunRepository,
            calculateAveragesUseCase = calculateAveragesUseCase,
            prefsManager = null
        )
    }

    @Test
    fun `observing grades populates available terms and selects newest term`() = runTest {
        neptunRepository.subjectGradesFlow.value = sampleGrades
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.allGrades.size)
        assertEquals(listOf("2025/26/1", "2024/25/2"), state.availableTerms)
        assertEquals("2025/26/1", state.selectedTerm)
        assertEquals(2, state.termGrades.size)
    }

    @Test
    fun `term calculation correctly computes weighted average`() = runTest {
        neptunRepository.subjectGradesFlow.value = sampleGrades
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.calculation)
        // (5*6 + 4*5) / (6 + 5) = 50 / 11 ≈ 4.545
        assertEquals(4.55, state.calculation!!.weightedAverage, 0.02)
        assertEquals(11, state.calculation!!.completedCredits)
    }

    @Test
    fun `selecting different term updates termGrades and calculation`() = runTest {
        neptunRepository.subjectGradesFlow.value = sampleGrades
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTerm("2024/25/2")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("2024/25/2", state.selectedTerm)
        assertEquals(1, state.termGrades.size)
        assertEquals("FIZ101", state.termGrades.first().subjectCode)
        assertEquals(3.0, state.calculation!!.weightedAverage, 0.01)
    }

    @Test
    fun `ghost mark simulation updates calculation with simulated grade`() = runTest {
        neptunRepository.subjectGradesFlow.value = sampleGrades
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val subject = sampleGrades.first()
        viewModel.openGhostMarkDialog(subject)
        assertEquals("subj_1", viewModel.uiState.value.ghostMarkDialogSubject?.id)

        // Set ghost grade to 2 instead of 5
        viewModel.setGhostGrade(subject.id, 2)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.ghostMarkDialogSubject)
        // (2*6 + 4*5) / 11 = 32 / 11 ≈ 2.91
        val updatedCalculation = viewModel.uiState.value.calculation
        assertNotNull(updatedCalculation)
        assertEquals(2.91, updatedCalculation!!.ghostWeightedAverage, 0.02)
        assertEquals(1, updatedCalculation.ghostCount)
    }

    @Test
    fun `resetAllGhostGrades reverts calculation to actual grades`() = runTest {
        neptunRepository.subjectGradesFlow.value = sampleGrades
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val subject = sampleGrades.first()
        viewModel.setGhostGrade(subject.id, 1)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.calculation?.ghostCount)

        viewModel.resetAllGhostGrades()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(0, finalState.calculation?.ghostCount)
        assertEquals(4.55, finalState.calculation!!.weightedAverage, 0.02)
    }

    @Test
    fun `tab navigation and exam filter change ui state`() = runTest {
        viewModel.selectTab(1)
        assertEquals(1, viewModel.uiState.value.selectedTab)

        viewModel.setExamFilter(2)
        assertEquals(2, viewModel.uiState.value.examFilter)

        viewModel.selectTab(2)
        assertEquals(2, viewModel.uiState.value.selectedTab)
    }
}
