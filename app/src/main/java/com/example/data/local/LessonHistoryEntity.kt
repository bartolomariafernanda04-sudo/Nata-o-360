package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.LessonHistory

@Entity(
    tableName = "lesson_history",
    indices = [Index(value = ["studentId"])]
)
data class LessonHistoryEntity(
    @PrimaryKey
    val id: String,
    val studentId: String,
    val date: String,
    val lessonTitle: String,
    val durationMinutes: Int,
    val completedExercises: Int,
    val totalExercises: Int,
    val commandsSent: Int,
    val evolutionNotes: String,
    val engagementRating: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): LessonHistory {
        return LessonHistory(
            id = id,
            date = date,
            lessonTitle = lessonTitle,
            durationMinutes = durationMinutes,
            completedExercises = completedExercises,
            totalExercises = totalExercises,
            commandsSent = commandsSent,
            evolutionNotes = evolutionNotes,
            engagementRating = engagementRating
        )
    }

    companion object {
        fun fromDomainModel(studentId: String, history: LessonHistory): LessonHistoryEntity {
            return LessonHistoryEntity(
                id = history.id,
                studentId = studentId,
                date = history.date,
                lessonTitle = history.lessonTitle,
                durationMinutes = history.durationMinutes,
                completedExercises = history.completedExercises,
                totalExercises = history.totalExercises,
                commandsSent = history.commandsSent,
                evolutionNotes = history.evolutionNotes,
                engagementRating = history.engagementRating
            )
        }
    }
}
