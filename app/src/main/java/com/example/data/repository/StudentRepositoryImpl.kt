package com.example.data.repository

import com.example.data.local.LessonHistoryDao
import com.example.data.local.LessonHistoryEntity
import com.example.data.local.StudentDao
import com.example.data.local.StudentEntity
import com.example.model.LessonHistory
import com.example.model.Student
import com.example.model.StudentAccessibility
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StudentRepositoryImpl(
    private val studentDao: StudentDao,
    private val lessonHistoryDao: LessonHistoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StudentRepository {

    override val allStudentsFlow: Flow<List<Student>> = combine(
        studentDao.getAllStudentsFlow(),
        lessonHistoryDao.getAllHistoriesFlow()
    ) { studentEntities, historyEntities ->
        val historiesByStudent = historyEntities.groupBy { it.studentId }
        studentEntities.map { sEntity ->
            val histories = historiesByStudent[sEntity.id]?.map { it.toDomainModel() } ?: emptyList()
            sEntity.toDomainModel(histories)
        }
    }

    override fun getStudentFlow(studentId: String): Flow<Student?> {
        return combine(
            studentDao.getAllStudentsFlow(),
            lessonHistoryDao.getHistoryForStudentFlow(studentId)
        ) { students, histories ->
            val entity = students.find { it.id == studentId } ?: return@combine null
            entity.toDomainModel(histories.map { it.toDomainModel() })
        }
    }

    override suspend fun getStudentById(studentId: String): Student? = withContext(ioDispatcher) {
        val entity = studentDao.getStudentById(studentId) ?: return@withContext null
        val histories = lessonHistoryDao.getHistoriesForStudent(studentId).map { it.toDomainModel() }
        entity.toDomainModel(histories)
    }

    override suspend fun saveStudent(student: Student) = withContext(ioDispatcher) {
        studentDao.insertStudent(StudentEntity.fromDomainModel(student))
    }

    override suspend fun deleteStudent(studentId: String) = withContext(ioDispatcher) {
        studentDao.deleteStudentById(studentId)
        lessonHistoryDao.deleteHistoriesByStudentId(studentId)
    }

    override suspend fun saveLessonHistory(studentId: String, history: LessonHistory) = withContext(ioDispatcher) {
        lessonHistoryDao.insertHistory(LessonHistoryEntity.fromDomainModel(studentId, history))
    }

    override fun getHistoryForStudentFlow(studentId: String): Flow<List<LessonHistory>> {
        return lessonHistoryDao.getHistoryForStudentFlow(studentId).map { list ->
            list.map { it.toDomainModel() }
        }
    }
}
