package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonHistoryDao {

    @Query("SELECT * FROM lesson_history WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getHistoryForStudentFlow(studentId: String): Flow<List<LessonHistoryEntity>>

    @Query("SELECT * FROM lesson_history ORDER BY timestamp DESC")
    fun getAllHistoriesFlow(): Flow<List<LessonHistoryEntity>>

    @Query("SELECT * FROM lesson_history WHERE studentId = :studentId ORDER BY timestamp DESC")
    suspend fun getHistoriesForStudent(studentId: String): List<LessonHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: LessonHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<LessonHistoryEntity>)

    @Query("DELETE FROM lesson_history WHERE studentId = :studentId")
    suspend fun deleteHistoriesByStudentId(studentId: String)

    @Query("DELETE FROM lesson_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)
}
