package com.example

import com.example.data.NatacaoRepository
import com.example.model.ExerciseCategory
import com.example.model.ExerciseItem
import com.example.model.QuickCommand
import com.example.model.SignalType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NatacaoRepositoryTest {

    private lateinit var repository: NatacaoRepository

    @Before
    fun setup() {
        repository = NatacaoRepository()
    }

    @Test
    fun testInitialStudentsLoaded() {
        val students = repository.students.value
        assertTrue("Students list should not be empty", students.isNotEmpty())
        assertTrue("Lucas should be present", students.any { it.name.contains("Lucas") })
        assertTrue("Beatriz should be present", students.any { it.name.contains("Beatriz") })
    }

    @Test
    fun testDispatchCommandUpdatesStateAndLog() {
        repository.dispatchCommand(QuickCommand.VIRADA)

        assertEquals(QuickCommand.VIRADA, repository.lastCommand.value)
        val log = repository.sessionCommandsLog.value
        assertEquals(1, log.size)
        assertEquals(QuickCommand.VIRADA, log.first().first)
    }

    @Test
    fun testNextAndPreviousExercise() {
        val total = repository.activeLessonPlan.value.exercises.size
        assertEquals(0, repository.currentExerciseIndex.value)

        repository.nextExercise()
        assertEquals(1, repository.currentExerciseIndex.value)

        repository.previousExercise()
        assertEquals(0, repository.currentExerciseIndex.value)
    }

    @Test
    fun testSaveCompletedSessionUpdatesStudentHistory() {
        val selectedId = repository.selectedStudent.value.id
        val initialHistoryCount = repository.selectedStudent.value.history.size

        repository.dispatchCommand(QuickCommand.INICIAR_TREINO)
        repository.dispatchCommand(QuickCommand.VIRADA)
        repository.updateSessionNotes("Aluno teve excelente adaptação ao sinal de virada.")
        repository.saveCompletedSession(rating = 5)

        val updatedStudent = repository.students.value.first { it.id == selectedId }
        assertEquals(initialHistoryCount + 1, updatedStudent.history.size)
        assertEquals(5, updatedStudent.history.first().engagementRating)
        assertTrue(updatedStudent.history.first().evolutionNotes.contains("virada"))
    }

    @Test
    fun testAddExerciseToLessonPlan() {
        val initialCount = repository.activeLessonPlan.value.exercises.size
        val newEx = ExerciseItem(
            id = "test_ex",
            title = "Flutuação Estrela",
            category = ExerciseCategory.FLUTUACAO,
            durationSeconds = 180,
            repetitions = 3,
            instructions = "Relaxar e flutuar de costas",
            signalType = SignalType.TROCA,
            visualCueSymbol = "⭐"
        )

        repository.addExerciseToCurrentPlan(newEx)
        assertEquals(initialCount + 1, repository.activeLessonPlan.value.exercises.size)
    }

    @Test
    fun testClassWeeklySummaryAndActiveStudentsProgress() {
        val summary = repository.classWeeklySummary.value
        assertNotNull(summary)
        assertTrue("Weekly progress should be > 0", summary.overallClassProgress > 0)
        assertEquals(6, summary.dailyProgressList.size)
        assertTrue(summary.competenceGoals.isNotEmpty())

        val activeList = repository.getActiveStudentsProgressList()
        assertEquals(repository.students.value.size, activeList.size)
        assertTrue(activeList.any { it.weeklyGoalPercent > 70 })
    }
}
