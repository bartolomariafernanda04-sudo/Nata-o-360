package com.example.data.repository

import com.example.model.LessonHistory
import com.example.model.Student
import kotlinx.coroutines.flow.Flow

/**
 * Interface de Repository para abstração e manipulação dos dados de Alunos
 * e histórico de evolução das aulas de natação adaptada.
 */
interface StudentRepository {

    /**
     * Fluxo reativo assíncrono com a lista completa de alunos cadastrados.
     */
    val allStudentsFlow: Flow<List<Student>>

    /**
     * Fluxo reativo para observar um aluno específico pelo seu ID.
     */
    fun getStudentFlow(studentId: String): Flow<Student?>

    /**
     * Busca um aluno específico de forma assíncrona.
     */
    suspend fun getStudentById(studentId: String): Student?

    /**
     * Salva ou atualiza os dados de um aluno no banco de dados local.
     */
    suspend fun saveStudent(student: Student)

    /**
     * Remove um aluno e seus respectivos históricos de aula.
     */
    suspend fun deleteStudent(studentId: String)

    /**
     * Registra uma nova sessão de aula no histórico de evolução do aluno.
     */
    suspend fun saveLessonHistory(studentId: String, history: LessonHistory)

    /**
     * Observa o histórico de evolução de um aluno específico.
     */
    fun getHistoryForStudentFlow(studentId: String): Flow<List<LessonHistory>>
}
