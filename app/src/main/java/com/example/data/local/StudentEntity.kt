package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Student
import com.example.model.StudentAccessibility

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val age: Int,
    val avatarEmoji: String,
    val specificNeedsCsv: String,
    val communicationPreference: String,
    val adaptationLevel: String,
    val importantNotes: String,
    // Acessibilidade e adaptação sensorial
    val highContrast: Boolean,
    val reducedStimuli: Boolean,
    val largeFonts: Boolean,
    val vibrationIntensity: Float,
    val wallProximityAlerts: Boolean,
    val poolLengthMeters: Int,
    val voiceAnnouncements: Boolean,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(history: List<com.example.model.LessonHistory> = emptyList()): Student {
        val needs = specificNeedsCsv.split(";;").map { it.trim() }.filter { it.isNotBlank() }
        return Student(
            id = id,
            name = name,
            age = age,
            avatarEmoji = avatarEmoji,
            specificNeeds = needs,
            communicationPreference = communicationPreference,
            accessibility = StudentAccessibility(
                highContrast = highContrast,
                reducedStimuli = reducedStimuli,
                largeFonts = largeFonts,
                vibrationIntensity = vibrationIntensity,
                wallProximityAlerts = wallProximityAlerts,
                poolLengthMeters = poolLengthMeters,
                voiceAnnouncements = voiceAnnouncements
            ),
            adaptationLevel = adaptationLevel,
            importantNotes = importantNotes,
            photoUri = photoUri,
            history = history
        )
    }

    companion object {
        fun fromDomainModel(student: Student): StudentEntity {
            return StudentEntity(
                id = student.id,
                name = student.name,
                age = student.age,
                avatarEmoji = student.avatarEmoji,
                specificNeedsCsv = student.specificNeeds.joinToString(";;"),
                communicationPreference = student.communicationPreference,
                adaptationLevel = student.adaptationLevel,
                importantNotes = student.importantNotes,
                highContrast = student.accessibility.highContrast,
                reducedStimuli = student.accessibility.reducedStimuli,
                largeFonts = student.accessibility.largeFonts,
                vibrationIntensity = student.accessibility.vibrationIntensity,
                wallProximityAlerts = student.accessibility.wallProximityAlerts,
                poolLengthMeters = student.accessibility.poolLengthMeters,
                voiceAnnouncements = student.accessibility.voiceAnnouncements,
                photoUri = student.photoUri
            )
        }
    }
}
