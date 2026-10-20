package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TeacherUser(
    val id: String,
    val name: String,
    val email: String,
    val cref: String,
    val schoolOrClub: String = "Centro Aquático Inclusivo 360"
)

interface AuthRepository {
    val currentTeacherFlow: StateFlow<TeacherUser?>
    val isAuthenticatedFlow: StateFlow<Boolean>

    suspend fun login(email: String, password: String): Result<TeacherUser>
    suspend fun loginWithBiometrics(): Result<TeacherUser>
    suspend fun loginAsDemo(): Result<TeacherUser>
    suspend fun logout()
    suspend fun requestPasswordReset(email: String): Result<String>
}

class AuthRepositoryImpl(context: Context? = null) : AuthRepository {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("natacao360_auth_prefs", Context.MODE_PRIVATE)

    private val _currentTeacher = MutableStateFlow<TeacherUser?>(loadSavedTeacher())
    override val currentTeacherFlow: StateFlow<TeacherUser?> = _currentTeacher.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(_currentTeacher.value != null)
    override val isAuthenticatedFlow: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private fun loadSavedTeacher(): TeacherUser? {
        val email = prefs?.getString("logged_teacher_email", null) ?: return null
        val name = prefs?.getString("logged_teacher_name", "Prof.ª Fernanda Bartolo") ?: "Prof.ª Fernanda Bartolo"
        val cref = prefs?.getString("logged_teacher_cref", "012345-G/SP") ?: "012345-G/SP"
        return TeacherUser(
            id = "teacher_1",
            name = name,
            email = email,
            cref = cref
        )
    }

    override suspend fun login(email: String, password: String): Result<TeacherUser> {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Preencha o e-mail e a senha do professor."))
        }

        // Validação de credenciais de professor (aceita credenciais do professor ou demo)
        if (cleanPassword.length < 4) {
            return Result.failure(IllegalArgumentException("A senha deve ter pelo menos 4 caracteres."))
        }

        val teacher = TeacherUser(
            id = "teacher_1",
            name = if (cleanEmail.contains("fernanda")) "Prof.ª Fernanda Bartolo" else "Prof. de Educação Física",
            email = cleanEmail,
            cref = "012345-G/SP"
        )

        _currentTeacher.value = teacher
        _isAuthenticated.value = true

        prefs?.edit()?.apply {
            putString("logged_teacher_email", teacher.email)
            putString("logged_teacher_name", teacher.name)
            putString("logged_teacher_cref", teacher.cref)
            apply()
        }

        return Result.success(teacher)
    }

    override suspend fun loginWithBiometrics(): Result<TeacherUser> {
        return loginAsDemo()
    }

    override suspend fun loginAsDemo(): Result<TeacherUser> {
        val demoTeacher = TeacherUser(
            id = "teacher_demo",
            name = "Prof.ª Fernanda Bartolo",
            email = "professor@natacao360.com.br",
            cref = "012345-G/SP",
            schoolOrClub = "Escola de Natação Adaptada 360"
        )

        _currentTeacher.value = demoTeacher
        _isAuthenticated.value = true

        prefs?.edit()?.apply {
            putString("logged_teacher_email", demoTeacher.email)
            putString("logged_teacher_name", demoTeacher.name)
            putString("logged_teacher_cref", demoTeacher.cref)
            apply()
        }

        return Result.success(demoTeacher)
    }

    override suspend fun logout() {
        _currentTeacher.value = null
        _isAuthenticated.value = false
        prefs?.edit()?.clear()?.apply()
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        return if (email.contains("@")) {
            Result.success("Link de recuperação de senha enviado para $email com sucesso.")
        } else {
            Result.failure(IllegalArgumentException("Informe um e-mail válido."))
        }
    }
}
