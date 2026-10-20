package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.StudentRepository
import com.example.data.repository.StudentRepositoryImpl
import com.example.data.repository.WorkoutRepository
import com.example.data.repository.WorkoutRepositoryImpl
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RepositoryLayersTest {

    private lateinit var database: AppDatabase
    private lateinit var studentRepository: StudentRepository
    private lateinit var workoutRepository: WorkoutRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        studentRepository = StudentRepositoryImpl(
            studentDao = database.studentDao(),
            lessonHistoryDao = database.lessonHistoryDao(),
            ioDispatcher = Dispatchers.Unconfined
        )

        workoutRepository = WorkoutRepositoryImpl(
            lessonPlanDao = database.lessonPlanDao(),
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testStudentRepositorySaveAndFlow() = runBlocking {
        val student = Student(
            id = "std_test_1",
            name = "Juliana Prado",
            age = 10,
            avatarEmoji = "👧🏽",
            specificNeeds = listOf("Baixa Visão", "Dificuldade de Orientação Espacial"),
            communicationPreference = "Vibrações Longas",
            accessibility = StudentAccessibility(
                highContrast = true,
                reducedStimuli = false,
                largeFonts = true,
                vibrationIntensity = 1.2f,
                wallProximityAlerts = true,
                poolLengthMeters = 25,
                voiceAnnouncements = true
            ),
            adaptationLevel = "Propulsão e Viradas",
            importantNotes = "Avisar borda com antecedência"
        )

        studentRepository.saveStudent(student)

        val students = studentRepository.allStudentsFlow.first()
        assertEquals(1, students.size)
        assertEquals("Juliana Prado", students.first().name)
        assertTrue(students.first().accessibility.highContrast)

        val retrieved = studentRepository.getStudentById("std_test_1")
        assertNotNull(retrieved)
        assertEquals("Juliana Prado", retrieved?.name)
    }

    @Test
    fun testStudentRepositoryLessonHistory() = runBlocking {
        val studentId = "std_test_2"
        val student = Student(
            id = studentId,
            name = "Thiago Neves",
            age = 7,
            avatarEmoji = "👦🏼",
            specificNeeds = listOf("Autismo (TEA)"),
            communicationPreference = "Pictogramas",
            accessibility = StudentAccessibility(),
            adaptationLevel = "Adaptação ao Meio Líquido",
            importantNotes = "Sensibilidade a barulhos altos"
        )
        studentRepository.saveStudent(student)

        val historyEntry = LessonHistory(
            id = "hist_test_1",
            date = "19/10/2026 16:00",
            lessonTitle = "Adaptação e Respiração",
            durationMinutes = 40,
            completedExercises = 5,
            totalExercises = 6,
            commandsSent = 12,
            evolutionNotes = "Ótima resposta aos estímulos visuais na borda.",
            engagementRating = 5
        )

        studentRepository.saveLessonHistory(studentId, historyEntry)

        val histories = studentRepository.getHistoryForStudentFlow(studentId).first()
        assertEquals(1, histories.size)
        assertEquals("Adaptação e Respiração", histories.first().lessonTitle)
        assertEquals(5, histories.first().engagementRating)
    }

    @Test
    fun testWorkoutRepositorySaveAndModifyExercises() = runBlocking {
        val plan = LessonPlan(
            id = "plan_test_1",
            title = "Treino de Propulsão Adaptada",
            targetAudience = "Alunos nível intermediário",
            objective = "Desenvolver pernada e coordenação motora",
            exercises = listOf(
                ExerciseItem(
                    id = "ex_test_1",
                    title = "Aquecimento Articular",
                    category = ExerciseCategory.AQUECIMENTO,
                    durationSeconds = 240,
                    repetitions = 3,
                    instructions = "Movimentos circulares com braços",
                    signalType = SignalType.TROCA,
                    visualCueSymbol = "🤸"
                )
            )
        )

        workoutRepository.saveLessonPlan(plan)

        val plans = workoutRepository.allLessonPlansFlow.first()
        assertEquals(1, plans.size)
        assertEquals("Treino de Propulsão Adaptada", plans.first().title)
        assertEquals(1, plans.first().exercises.size)

        // Adicionar novo exercício
        val newExercise = ExerciseItem(
            id = "ex_test_2",
            title = "Pernada com Prancha",
            category = ExerciseCategory.PERNADA,
            durationSeconds = 300,
            repetitions = 4,
            instructions = "Bater pernas continuamente na superfície",
            signalType = SignalType.TROCA,
            visualCueSymbol = "🏊"
        )
        workoutRepository.addExerciseToPlan("plan_test_1", newExercise)

        val updatedPlan = workoutRepository.getLessonPlanById("plan_test_1")
        assertNotNull(updatedPlan)
        assertEquals(2, updatedPlan?.exercises?.size)
        assertEquals("Pernada com Prancha", updatedPlan?.exercises?.get(1)?.title)

        // Remover exercício
        workoutRepository.removeExerciseFromPlan("plan_test_1", "ex_test_1")
        val planAfterRemoval = workoutRepository.getLessonPlanById("plan_test_1")
        assertEquals(1, planAfterRemoval?.exercises?.size)
        assertEquals("Pernada com Prancha", planAfterRemoval?.exercises?.first()?.title)
    }
}
