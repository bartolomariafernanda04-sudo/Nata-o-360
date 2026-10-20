package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NatacaoRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.StudentRepository
import com.example.data.repository.TeacherUser
import com.example.data.repository.WorkoutRepository
import com.example.model.*
import com.example.service.BluetoothSyncManager
import com.example.service.VibrationController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavDestination {
    LOGIN,
    WELCOME,
    TEACHER_DASHBOARD,
    STANDALONE_WATCH
}

enum class TeacherTab {
    OVERVIEW,
    WATCH_HUB,
    LESSON_PLANNER,
    STUDENTS,
    SIGNALS_CONFIG,
    TRACKING_HISTORY
}

class NatacaoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NatacaoRepository(application.applicationContext)
    private val vibrationController = VibrationController(application.applicationContext)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application.applicationContext)
    val bluetoothSyncManager = BluetoothSyncManager.getInstance(application.applicationContext)

    // Autenticação do Professor
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticatedFlow
    val currentTeacher: StateFlow<TeacherUser?> = authRepository.currentTeacherFlow

    private val _loginErrorMessage = MutableStateFlow<String?>(null)
    val loginErrorMessage: StateFlow<String?> = _loginErrorMessage.asStateFlow()

    // Navegação principal (se já autenticado vai para WELCOME/DASHBOARD, se não vai para LOGIN)
    private val _currentDestination = MutableStateFlow(
        if (authRepository.isAuthenticatedFlow.value) AppNavDestination.WELCOME else AppNavDestination.LOGIN
    )
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    private val _currentTeacherTab = MutableStateFlow(TeacherTab.OVERVIEW)
    val currentTeacherTab: StateFlow<TeacherTab> = _currentTeacherTab.asStateFlow()

    // Configurações Globais de Acessibilidade do App
    private val _globalHighContrast = MutableStateFlow(false)
    val globalHighContrast: StateFlow<Boolean> = _globalHighContrast.asStateFlow()

    private val _globalReducedStimuli = MutableStateFlow(false)
    val globalReducedStimuli: StateFlow<Boolean> = _globalReducedStimuli.asStateFlow()

    private val _showSafetyModal = MutableStateFlow(false)
    val showSafetyModal: StateFlow<Boolean> = _showSafetyModal.asStateFlow()

    // Fluxos do repositório expostos para a UI
    val students: StateFlow<List<Student>> = repository.students
    val selectedStudent: StateFlow<Student> = repository.selectedStudent
    val activeLessonPlan: StateFlow<LessonPlan> = repository.activeLessonPlan
    val currentExerciseIndex: StateFlow<Int> = repository.currentExerciseIndex
    val isLessonRunning: StateFlow<Boolean> = repository.isLessonRunning
    val isLessonPaused: StateFlow<Boolean> = repository.isLessonPaused
    val lessonElapsedSeconds: StateFlow<Int> = repository.lessonElapsedSeconds
    val lastCommand: StateFlow<QuickCommand?> = repository.lastCommand
    val lastCommandTimestamp: StateFlow<Long> = repository.lastCommandTimestamp
    val sessionCommandsLog: StateFlow<List<Pair<QuickCommand, String>>> = repository.sessionCommandsLog
    val watchConnectionState: StateFlow<WatchConnectionState> = repository.watchConnectionState
    val vibrationSettings: StateFlow<List<VibrationSetting>> = repository.vibrationSettings
    val currentSessionNotes: StateFlow<String> = repository.currentSessionNotes
    val classWeeklySummary: StateFlow<ClassWeeklySummary> = repository.classWeeklySummary

    private val _activeStudentsProgress = MutableStateFlow<List<ActiveStudentProgress>>(repository.getActiveStudentsProgressList())
    val activeStudentsProgress: StateFlow<List<ActiveStudentProgress>> = _activeStudentsProgress.asStateFlow()

    // Estados do controlador de vibração e feedback
    val isVibrating: StateFlow<Boolean> = vibrationController.isVibrating
    val lastSignalTriggered: StateFlow<SignalType?> = vibrationController.lastSignalTriggered

    // Mensagem de feedback temporária (Toast / Snack)
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    // Job para contagem do timer em segundo plano
    private var timerJob: Job? = null

    init {
        startTimerCoroutine()
        listenToBluetoothWatchAlerts()
    }

    private fun startTimerCoroutine() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                repository.tickTimer()
            }
        }
    }

    private fun listenToBluetoothWatchAlerts() {
        viewModelScope.launch {
            bluetoothSyncManager.watchIncomingAlerts.collect { alert ->
                if (alert.type == "EMERGENCY_HELP") {
                    _feedbackMessage.value = "⚠️ ALERTA DO RELÓGIO: ${alert.studentName} chamou o professor na borda!"
                    vibrationController.vibrateSignal(SignalType.ATENCAO, speakVoice = true)
                } else {
                    _feedbackMessage.value = "✓ Aluno confirmou recebimento do comando no relógio."
                }
            }
        }
    }

    // Métodos de Autenticação do Professor
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, pass)
            result.onSuccess {
                _loginErrorMessage.value = null
                _currentDestination.value = AppNavDestination.WELCOME
                _feedbackMessage.value = "Bem-vindo(a), ${it.name}!"
            }.onFailure { err ->
                _loginErrorMessage.value = err.message ?: "Erro ao realizar login."
            }
        }
    }

    fun loginAsDemo() {
        viewModelScope.launch {
            val result = authRepository.loginAsDemo()
            result.onSuccess {
                _loginErrorMessage.value = null
                _currentDestination.value = AppNavDestination.WELCOME
                _feedbackMessage.value = "Acesso Demo ativado: ${it.name}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentDestination.value = AppNavDestination.LOGIN
            _feedbackMessage.value = "Sessão encerrada com sucesso."
        }
    }

    // Métodos de Conexão Bluetooth com o Relógio
    fun startBluetoothScan() {
        bluetoothSyncManager.startScanning()
        repository.setConnectionMode("Conectando")
        _feedbackMessage.value = "Buscando Smartwatch Hídrico via Bluetooth LE..."
    }

    fun connectBluetoothWatch(deviceName: String = "Smartwatch Hídrico 360") {
        bluetoothSyncManager.connectToWatch(deviceName)
        repository.setConnectionMode("Conectado")
        _feedbackMessage.value = "Pareado com sucesso via Bluetooth LE!"
    }

    fun disconnectBluetoothWatch() {
        bluetoothSyncManager.disconnect()
        repository.setConnectionMode("Desconectado")
        _feedbackMessage.value = "Relógio desconectado do painel do professor."
    }

    // Navegação
    fun navigateTo(destination: AppNavDestination) {
        _currentDestination.value = destination
    }

    fun selectTeacherTab(tab: TeacherTab) {
        _currentTeacherTab.value = tab
    }

    fun toggleHighContrast() {
        _globalHighContrast.value = !_globalHighContrast.value
    }

    fun toggleReducedStimuli() {
        _globalReducedStimuli.value = !_globalReducedStimuli.value
    }

    fun setSafetyModalVisible(visible: Boolean) {
        _showSafetyModal.value = visible
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    // Ações de Comando Rápido com Disparo Háptico e Sincronização Bluetooth Imediata
    fun executeQuickCommand(command: QuickCommand) {
        repository.dispatchCommand(command)

        // Transmissão em tempo real via Bluetooth LE para o relógio
        bluetoothSyncManager.sendCommandToWatch(command)

        val student = selectedStudent.value
        val settings = vibrationSettings.value.find { it.signalType == command.signalType }
        val customDurations = settings?.durations
        val intensity = (settings?.intensityPercent ?: 100) / 100f * student.accessibility.vibrationIntensity
        val speak = student.accessibility.voiceAnnouncements && !_globalReducedStimuli.value

        vibrationController.vibrateSignal(
            signalType = command.signalType,
            customDurations = customDurations,
            intensityFactor = intensity,
            speakVoice = speak
        )

        _feedbackMessage.value = "Sinal '${command.title}' transmitido via Bluetooth ao relógio de ${student.name}!"
    }

    // Testar vibração específica
    fun testVibration(signalType: SignalType) {
        val student = selectedStudent.value
        val settings = vibrationSettings.value.find { it.signalType == signalType }
        val durations = settings?.durations
        val intensity = (settings?.intensityPercent ?: 100) / 100f * student.accessibility.vibrationIntensity

        vibrationController.vibrateSignal(
            signalType = signalType,
            customDurations = durations,
            intensityFactor = intensity,
            speakVoice = true
        )
        _feedbackMessage.value = "Testando padrão '${signalType.label}' no dispositivo..."
    }

    fun stopVibration() {
        vibrationController.stop()
    }

    // Controle da Aula
    fun nextExercise() {
        repository.nextExercise()
        val curEx = getCurrentExercise()
        if (curEx != null) {
            executeQuickCommand(QuickCommand.INICIAR_EXERCICIO)
        }
    }

    fun previousExercise() {
        repository.previousExercise()
    }

    fun selectExercise(index: Int) {
        repository.setExerciseIndex(index)
    }

    fun getCurrentExercise(): ExerciseItem? {
        val plan = activeLessonPlan.value
        val idx = currentExerciseIndex.value
        return if (idx in plan.exercises.indices) plan.exercises[idx] else null
    }

    fun getNextExercise(): ExerciseItem? {
        val plan = activeLessonPlan.value
        val nextIdx = currentExerciseIndex.value + 1
        return if (nextIdx in plan.exercises.indices) plan.exercises[nextIdx] else null
    }

    fun resetLesson() {
        repository.resetLessonTimer()
        _feedbackMessage.value = "Aula e cronômetros reiniciados com sucesso."
    }

    fun updateSessionNotes(notes: String) {
        repository.updateSessionNotes(notes)
    }

    fun saveCompletedSession(rating: Int) {
        repository.saveCompletedSession(rating)
        _feedbackMessage.value = "Aula finalizada e registrada no histórico do aluno!"
    }

    // Repositories expostos para consumo assíncrono
    val studentRepository: StudentRepository? = repository.studentRepository
    val workoutRepository: WorkoutRepository? = repository.workoutRepository

    // Alunos
    fun selectStudent(studentId: String) {
        repository.selectStudent(studentId)
        val student = selectedStudent.value
        // Aplicar preferências de acessibilidade do aluno selecionado
        _globalHighContrast.value = student.accessibility.highContrast
        _globalReducedStimuli.value = student.accessibility.reducedStimuli
        _feedbackMessage.value = "Aluno alterado para ${student.name}."
    }

    fun saveStudent(student: Student) {
        viewModelScope.launch {
            repository.saveStudent(student)
            _activeStudentsProgress.value = repository.getActiveStudentsProgressList()
            _feedbackMessage.value = "Cadastro de ${student.name} salvo com sucesso!"
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            _activeStudentsProgress.value = repository.getActiveStudentsProgressList()
            _feedbackMessage.value = "Aluno removido do sistema."
        }
    }

    // Montar Aula (Manipulação de Treinos)
    fun addExercise(exercise: ExerciseItem) {
        viewModelScope.launch {
            repository.addExerciseToCurrentPlan(exercise)
            _feedbackMessage.value = "Exercício '${exercise.title}' adicionado à aula."
        }
    }

    fun removeExercise(exerciseId: String) {
        viewModelScope.launch {
            repository.removeExerciseFromCurrentPlan(exerciseId)
            _feedbackMessage.value = "Exercício removido da aula."
        }
    }

    fun updateLessonPlan(plan: LessonPlan) {
        viewModelScope.launch {
            repository.updateLessonPlan(plan)
            _feedbackMessage.value = "Plano de treino atualizado com sucesso."
        }
    }

    // Conexão do Relógio
    fun setWatchConnection(status: String) {
        repository.setConnectionMode(status)
        _feedbackMessage.value = "Status do relógio atualizado: $status"
    }

    fun toggleWaterLock() {
        repository.toggleWaterLock()
        val locked = repository.watchConnectionState.value.isWaterLocked
        _feedbackMessage.value = if (locked) "Bloqueio aquático ativado no relógio 💧" else "Bloqueio aquático desativado"
    }

    fun updateVibrationSetting(setting: VibrationSetting) {
        repository.updateVibrationSetting(setting)
        _feedbackMessage.value = "Configuração do sinal '${setting.name}' salva."
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        vibrationController.release()
    }
}
