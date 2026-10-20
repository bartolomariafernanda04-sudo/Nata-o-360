package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ExerciseCategory
import com.example.model.ExerciseItem
import com.example.model.LessonPlan
import com.example.model.SignalType
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "lesson_plans")
data class LessonPlanEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val targetAudience: String,
    val objective: String,
    val exercisesJson: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): LessonPlan {
        val exerciseList = mutableListOf<ExerciseItem>()
        try {
            val array = JSONArray(exercisesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val catName = obj.optString("category", ExerciseCategory.PERNADA.name)
                val sigName = obj.optString("signalType", SignalType.TROCA.name)
                val category = runCatching { ExerciseCategory.valueOf(catName) }.getOrDefault(ExerciseCategory.PERNADA)
                val signalType = runCatching { SignalType.valueOf(sigName) }.getOrDefault(SignalType.TROCA)

                exerciseList.add(
                    ExerciseItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        category = category,
                        durationSeconds = obj.getInt("durationSeconds"),
                        repetitions = obj.getInt("repetitions"),
                        instructions = obj.getString("instructions"),
                        signalType = signalType,
                        isCompleted = obj.optBoolean("isCompleted", false),
                        visualCueSymbol = obj.optString("visualCueSymbol", "🏊")
                    )
                )
            }
        } catch (_: Exception) {}

        return LessonPlan(
            id = id,
            title = title,
            targetAudience = targetAudience,
            objective = objective,
            exercises = exerciseList
        )
    }

    companion object {
        fun fromDomainModel(plan: LessonPlan): LessonPlanEntity {
            val array = JSONArray()
            for (ex in plan.exercises) {
                val obj = JSONObject()
                obj.put("id", ex.id)
                obj.put("title", ex.title)
                obj.put("category", ex.category.name)
                obj.put("durationSeconds", ex.durationSeconds)
                obj.put("repetitions", ex.repetitions)
                obj.put("instructions", ex.instructions)
                obj.put("signalType", ex.signalType.name)
                obj.put("isCompleted", ex.isCompleted)
                obj.put("visualCueSymbol", ex.visualCueSymbol)
                array.put(obj)
            }

            return LessonPlanEntity(
                id = plan.id,
                title = plan.title,
                targetAudience = plan.targetAudience,
                objective = plan.objective,
                exercisesJson = array.toString(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
