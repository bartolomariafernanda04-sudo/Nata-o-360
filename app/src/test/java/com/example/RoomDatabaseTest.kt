package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.LessonHistoryEntity
import com.example.data.local.StudentEntity
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
class RoomDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveStudent() = runBlocking {
        val studentEntity = StudentEntity(
            id = "test_aluno_1",
            name = "Enzo Gabriel",
            age = 8,
            avatarEmoji = "👦🏻",
            specificNeedsCsv = "Autismo (TEA);;Hipersensibilidade Auditiva",
            communicationPreference = "Pictogramas e Vibração Curta",
            adaptationLevel = "Flutuação e Respiração",
            importantNotes = "Avisar com antecedência de troca",
            highContrast = false,
            reducedStimuli = true,
            largeFonts = true,
            vibrationIntensity = 1.0f,
            wallProximityAlerts = true,
            poolLengthMeters = 25,
            voiceAnnouncements = false
        )

        database.studentDao().insertStudent(studentEntity)

        val retrieved = database.studentDao().getStudentById("test_aluno_1")
        assertNotNull(retrieved)
        assertEquals("Enzo Gabriel", retrieved?.name)
        assertEquals(8, retrieved?.age)

        val allStudents = database.studentDao().getAllStudentsFlow().first()
        assertEquals(1, allStudents.size)
        val domain = allStudents.first().toDomainModel()
        assertEquals(2, domain.specificNeeds.size)
        assertTrue(domain.specificNeeds.contains("Autismo (TEA)"))
    }

    @Test
    fun testInsertAndRetrieveLessonHistory() = runBlocking {
        val studentId = "test_aluno_2"
        val history1 = LessonHistoryEntity(
            id = "hist_1",
            studentId = studentId,
            date = "19/10/2026 14:00",
            lessonTitle = "Adaptação e Respiração",
            durationMinutes = 40,
            completedExercises = 6,
            totalExercises = 6,
            commandsSent = 15,
            evolutionNotes = "Excelente resposta ao sinal de virada e bolhas na água.",
            engagementRating = 5,
            timestamp = 1000L
        )

        val history2 = LessonHistoryEntity(
            id = "hist_2",
            studentId = studentId,
            date = "19/10/2026 15:00",
            lessonTitle = "Pernada com Prancha",
            durationMinutes = 45,
            completedExercises = 5,
            totalExercises = 6,
            commandsSent = 20,
            evolutionNotes = "Conseguiu manter o alinhamento com apoio das vibrações.",
            engagementRating = 4,
            timestamp = 2000L
        )

        database.lessonHistoryDao().insertHistories(listOf(history1, history2))

        val studentHistories = database.lessonHistoryDao().getHistoriesForStudent(studentId)
        assertEquals(2, studentHistories.size)
        // Ordered by timestamp DESC
        assertEquals("hist_2", studentHistories.first().id)

        database.lessonHistoryDao().deleteHistoriesByStudentId(studentId)
        val afterDelete = database.lessonHistoryDao().getHistoriesForStudent(studentId)
        assertTrue(afterDelete.isEmpty())
    }
}
