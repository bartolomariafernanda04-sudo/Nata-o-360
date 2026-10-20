package com.example.data.repository

import com.example.data.local.LessonPlanDao
import com.example.data.local.LessonPlanEntity
import com.example.model.ExerciseItem
import com.example.model.LessonPlan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WorkoutRepositoryImpl(
    private val lessonPlanDao: LessonPlanDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WorkoutRepository {

    override val allLessonPlansFlow: Flow<List<LessonPlan>> =
        lessonPlanDao.getAllLessonPlansFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getLessonPlanFlow(planId: String): Flow<LessonPlan?> {
        return lessonPlanDao.getLessonPlanByIdFlow(planId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun getLessonPlanById(planId: String): LessonPlan? = withContext(ioDispatcher) {
        lessonPlanDao.getLessonPlanById(planId)?.toDomainModel()
    }

    override suspend fun saveLessonPlan(plan: LessonPlan) = withContext(ioDispatcher) {
        lessonPlanDao.insertLessonPlan(LessonPlanEntity.fromDomainModel(plan))
    }

    override suspend fun deleteLessonPlan(planId: String) = withContext(ioDispatcher) {
        lessonPlanDao.deleteLessonPlanById(planId)
    }

    override suspend fun addExerciseToPlan(planId: String, exercise: ExerciseItem) = withContext(ioDispatcher) {
        val currentPlan = lessonPlanDao.getLessonPlanById(planId)?.toDomainModel() ?: return@withContext
        val updatedExercises = currentPlan.exercises.toMutableList().apply { add(exercise) }
        val updatedPlan = currentPlan.copy(exercises = updatedExercises)
        lessonPlanDao.insertLessonPlan(LessonPlanEntity.fromDomainModel(updatedPlan))
    }

    override suspend fun removeExerciseFromPlan(planId: String, exerciseId: String) = withContext(ioDispatcher) {
        val currentPlan = lessonPlanDao.getLessonPlanById(planId)?.toDomainModel() ?: return@withContext
        val updatedExercises = currentPlan.exercises.filter { it.id != exerciseId }
        val updatedPlan = currentPlan.copy(exercises = updatedExercises)
        lessonPlanDao.insertLessonPlan(LessonPlanEntity.fromDomainModel(updatedPlan))
    }

    override suspend fun updateExerciseInPlan(planId: String, exercise: ExerciseItem) = withContext(ioDispatcher) {
        val currentPlan = lessonPlanDao.getLessonPlanById(planId)?.toDomainModel() ?: return@withContext
        val updatedExercises = currentPlan.exercises.toMutableList()
        val index = updatedExercises.indexOfFirst { it.id == exercise.id }
        if (index >= 0) {
            updatedExercises[index] = exercise
            val updatedPlan = currentPlan.copy(exercises = updatedExercises)
            lessonPlanDao.insertLessonPlan(LessonPlanEntity.fromDomainModel(updatedPlan))
        }
    }
}
