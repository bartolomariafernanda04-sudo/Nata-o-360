package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonPlanDao {

    @Query("SELECT * FROM lesson_plans ORDER BY updatedAt DESC")
    fun getAllLessonPlansFlow(): Flow<List<LessonPlanEntity>>

    @Query("SELECT * FROM lesson_plans WHERE id = :id LIMIT 1")
    fun getLessonPlanByIdFlow(id: String): Flow<LessonPlanEntity?>

    @Query("SELECT * FROM lesson_plans WHERE id = :id LIMIT 1")
    suspend fun getLessonPlanById(id: String): LessonPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonPlan(plan: LessonPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonPlans(plans: List<LessonPlanEntity>)

    @Query("DELETE FROM lesson_plans WHERE id = :id")
    suspend fun deleteLessonPlanById(id: String)

    @Query("SELECT COUNT(*) FROM lesson_plans")
    suspend fun getCount(): Int
}
