package com.example.data.repository

import com.example.model.ExerciseItem
import com.example.model.LessonPlan
import kotlinx.coroutines.flow.Flow

/**
 * Interface de Repository para abstração e manipulação dos dados de Treinos
 * e Montagem de Aulas de Natação Adaptada.
 */
interface WorkoutRepository {

    /**
     * Fluxo reativo assíncrono com todos os planos de treino cadastrados.
     */
    val allLessonPlansFlow: Flow<List<LessonPlan>>

    /**
     * Observa um plano de aula específico pelo seu ID.
     */
    fun getLessonPlanFlow(planId: String): Flow<LessonPlan?>

    /**
     * Busca um plano de aula de forma assíncrona.
     */
    suspend fun getLessonPlanById(planId: String): LessonPlan?

    /**
     * Salva ou atualiza um plano de aula completo no Room.
     */
    suspend fun saveLessonPlan(plan: LessonPlan)

    /**
     * Remove um plano de aula pelo seu identificador.
     */
    suspend fun deleteLessonPlan(planId: String)

    /**
     * Adiciona uma nova etapa de exercício a um treino existente.
     */
    suspend fun addExerciseToPlan(planId: String, exercise: ExerciseItem)

    /**
     * Remove uma etapa de exercício de um plano de treino.
     */
    suspend fun removeExerciseFromPlan(planId: String, exerciseId: String)

    /**
     * Atualiza os dados de uma etapa de exercício existente.
     */
    suspend fun updateExerciseInPlan(planId: String, exercise: ExerciseItem)
}
