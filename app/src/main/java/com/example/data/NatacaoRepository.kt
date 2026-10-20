package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.LessonPlanEntity
import com.example.data.local.StudentEntity
import com.example.data.repository.StudentRepository
import com.example.data.repository.StudentRepositoryImpl
import com.example.data.repository.WorkoutRepository
import com.example.data.repository.WorkoutRepositoryImpl
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class NatacaoRepository(private val context: Context? = null) {

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database = context?.let { AppDatabase.getInstance(it) }

    // Camadas de Repository abstraindo o acesso ao Room
    val studentRepository: StudentRepository? = database?.let {
        StudentRepositoryImpl(it.studentDao(), it.lessonHistoryDao())
    }

    val workoutRepository: WorkoutRepository? = database?.let {
        WorkoutRepositoryImpl(it.lessonPlanDao())
    }

    // Lista de alunos cadastrados com perfis detalhados de inclusão
    private val _students = MutableStateFlow<List<Student>>(initialStudents())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    // Aluno selecionado para a aula ativa
    private val _selectedStudent = MutableStateFlow<Student>(_students.value.first())
    val selectedStudent: StateFlow<Student> = _selectedStudent.asStateFlow()

    // Planos de aula disponíveis
    private val _lessonPlans = MutableStateFlow<List<LessonPlan>>(initialLessonPlans())
    val lessonPlans: StateFlow<List<LessonPlan>> = _lessonPlans.asStateFlow()

    // Plano de aula atualmente carregado
    private val _activeLessonPlan = MutableStateFlow<LessonPlan>(_lessonPlans.value.first())
    val activeLessonPlan: StateFlow<LessonPlan> = _activeLessonPlan.asStateFlow()

    // Índice do exercício atual
    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    // Estado da aula: isRunning, isPaused, activeSeconds
    private val _isLessonRunning = MutableStateFlow(false)
    val isLessonRunning: StateFlow<Boolean> = _isLessonRunning.asStateFlow()

    private val _isLessonPaused = MutableStateFlow(false)
    val isLessonPaused: StateFlow<Boolean> = _isLessonPaused.asStateFlow()

    private val _lessonElapsedSeconds = MutableStateFlow(0)
    val lessonElapsedSeconds: StateFlow<Int> = _lessonElapsedSeconds.asStateFlow()

    // Último comando emitido pelo professor
    private val _lastCommand = MutableStateFlow<QuickCommand?>(null)
    val lastCommand: StateFlow<QuickCommand?> = _lastCommand.asStateFlow()

    private val _lastCommandTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastCommandTimestamp: StateFlow<Long> = _lastCommandTimestamp.asStateFlow()

    // Histórico de comandos enviados na sessão corrente
    private val _sessionCommandsLog = MutableStateFlow<List<Pair<QuickCommand, String>>>(emptyList())
    val sessionCommandsLog: StateFlow<List<Pair<QuickCommand, String>>> = _sessionCommandsLog.asStateFlow()

    // Estado da conexão com o smartwatch
    private val _watchConnectionState = MutableStateFlow(
        WatchConnectionState(
            isConnected = true,
            connectionStatus = "Conectado via Bluetooth LE",
            deviceName = "Smartwatch Hídrico 360 - Lucas",
            batteryPercent = 94,
            rssiSignalDbm = -52,
            isWaterLocked = false,
            lastPingMs = 14
        )
    )
    val watchConnectionState: StateFlow<WatchConnectionState> = _watchConnectionState.asStateFlow()

    // Configurações personalizadas de vibrações por tipo de sinal
    private val _vibrationSettings = MutableStateFlow<List<VibrationSetting>>(initialVibrationSettings())
    val vibrationSettings: StateFlow<List<VibrationSetting>> = _vibrationSettings.asStateFlow()

    // Resumo e Progresso das Metas Semanais
    private val _classWeeklySummary = MutableStateFlow<ClassWeeklySummary>(initialClassWeeklySummary())
    val classWeeklySummary: StateFlow<ClassWeeklySummary> = _classWeeklySummary.asStateFlow()

    // Notas de observação da aula atual
    private val _currentSessionNotes = MutableStateFlow("")
    val currentSessionNotes: StateFlow<String> = _currentSessionNotes.asStateFlow()

    init {
        // Inicialização e sincronização reativa com as camadas de Repository
        if (database != null && studentRepository != null && workoutRepository != null) {
            repoScope.launch {
                try {
                    // Pré-população inicial de alunos e históricos no Room caso esteja vazio
                    if (database.studentDao().getCount() == 0) {
                        val initList = initialStudents()
                        initList.forEach { std ->
                            studentRepository.saveStudent(std)
                            std.history.forEach { hist ->
                                studentRepository.saveLessonHistory(std.id, hist)
                            }
                        }
                    }

                    // Pré-população de treinos no Room caso esteja vazio
                    if (database.lessonPlanDao().getCount() == 0) {
                        initialLessonPlans().forEach { plan ->
                            workoutRepository.saveLessonPlan(plan)
                        }
                    }

                    // Coletar alunos reativamente a partir do StudentRepository
                    launch {
                        studentRepository.allStudentsFlow.collect { domainStudents ->
                            if (domainStudents.isNotEmpty()) {
                                _students.value = domainStudents
                                val curr = _selectedStudent.value
                                val matched = domainStudents.find { it.id == curr.id }
                                if (matched != null) {
                                    _selectedStudent.value = matched
                                } else {
                                    _selectedStudent.value = domainStudents.first()
                                }
                            }
                        }
                    }

                    // Coletar planos de aula reativamente a partir do WorkoutRepository
                    launch {
                        workoutRepository.allLessonPlansFlow.collect { domainPlans ->
                            if (domainPlans.isNotEmpty()) {
                                _lessonPlans.value = domainPlans
                                val currPlan = _activeLessonPlan.value
                                val matchedPlan = domainPlans.find { it.id == currPlan.id }
                                if (matchedPlan != null) {
                                    _activeLessonPlan.value = matchedPlan
                                } else {
                                    _activeLessonPlan.value = domainPlans.first()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NatacaoRepository", "Erro inicializando camadas de Repository", e)
                }
            }
        }
    }

    // Controles de Seleção e Aluno delegando para StudentRepository
    fun selectStudent(studentId: String) {
        val found = _students.value.find { it.id == studentId }
        if (found != null) {
            _selectedStudent.value = found
            _watchConnectionState.value = _watchConnectionState.value.copy(
                deviceName = "Smartwatch 360 - ${found.name.split(" ").first()}"
            )
        }
    }

    fun saveStudent(student: Student) {
        val currentList = _students.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == student.id }
        if (index >= 0) {
            currentList[index] = student
        } else {
            currentList.add(student)
        }
        _students.value = currentList
        if (_selectedStudent.value.id == student.id) {
            _selectedStudent.value = student
        }

        studentRepository?.let { repo ->
            repoScope.launch {
                try {
                    repo.saveStudent(student)
                } catch (e: Exception) {
                    Log.e("NatacaoRepository", "Erro salvando aluno via StudentRepository", e)
                }
            }
        }
    }

    fun deleteStudent(studentId: String) {
        val currentList = _students.value.filter { it.id != studentId }
        _students.value = currentList
        if (_selectedStudent.value.id == studentId && currentList.isNotEmpty()) {
            _selectedStudent.value = currentList.first()
        }

        studentRepository?.let { repo ->
            repoScope.launch {
                try {
                    repo.deleteStudent(studentId)
                } catch (e: Exception) {
                    Log.e("NatacaoRepository", "Erro excluindo aluno via StudentRepository", e)
                }
            }
        }
    }

    // Ações de Treino e Comandos
    fun dispatchCommand(command: QuickCommand) {
        _lastCommand.value = command
        _lastCommandTimestamp.value = System.currentTimeMillis()

        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val updatedLog = _sessionCommandsLog.value.toMutableList().apply {
            add(0, Pair(command, timeStr))
        }
        _sessionCommandsLog.value = updatedLog

        when (command) {
            QuickCommand.INICIAR_TREINO -> {
                _isLessonRunning.value = true
                _isLessonPaused.value = false
            }
            QuickCommand.PAUSA -> {
                _isLessonPaused.value = true
            }
            QuickCommand.RETOMAR -> {
                _isLessonRunning.value = true
                _isLessonPaused.value = false
            }
            QuickCommand.PROXIMA_ATIVIDADE -> {
                nextExercise()
            }
            QuickCommand.FINALIZAR_TREINO -> {
                _isLessonPaused.value = true
            }
            else -> { /* Mantém fluxo */ }
        }
    }

    fun nextExercise() {
        val total = _activeLessonPlan.value.exercises.size
        if (total > 0) {
            val nextIdx = (_currentExerciseIndex.value + 1) % total
            _currentExerciseIndex.value = nextIdx
            markExerciseCompleted(_currentExerciseIndex.value)
        }
    }

    fun previousExercise() {
        val total = _activeLessonPlan.value.exercises.size
        if (total > 0) {
            val prevIdx = if (_currentExerciseIndex.value > 0) _currentExerciseIndex.value - 1 else total - 1
            _currentExerciseIndex.value = prevIdx
        }
    }

    fun setExerciseIndex(index: Int) {
        val total = _activeLessonPlan.value.exercises.size
        if (index in 0 until total) {
            _currentExerciseIndex.value = index
        }
    }

    fun markExerciseCompleted(index: Int) {
        val currentExercises = _activeLessonPlan.value.exercises.toMutableList()
        if (index in currentExercises.indices) {
            val ex = currentExercises[index]
            currentExercises[index] = ex.copy(isCompleted = true)
            _activeLessonPlan.value = _activeLessonPlan.value.copy(exercises = currentExercises)
        }
    }

    fun tickTimer() {
        if (_isLessonRunning.value && !_isLessonPaused.value) {
            _lessonElapsedSeconds.value += 1
        }
    }

    fun resetLessonTimer() {
        _lessonElapsedSeconds.value = 0
        _isLessonRunning.value = false
        _isLessonPaused.value = false
        _sessionCommandsLog.value = emptyList()
    }

    fun updateSessionNotes(notes: String) {
        _currentSessionNotes.value = notes
    }

    fun saveCompletedSession(rating: Int = 5) {
        val student = _selectedStudent.value
        val historyEntry = LessonHistory(
            id = UUID.randomUUID().toString(),
            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            lessonTitle = _activeLessonPlan.value.title,
            durationMinutes = (_lessonElapsedSeconds.value / 60).coerceAtLeast(1),
            completedExercises = _activeLessonPlan.value.exercises.count { it.isCompleted },
            totalExercises = _activeLessonPlan.value.exercises.size,
            commandsSent = _sessionCommandsLog.value.size,
            evolutionNotes = _currentSessionNotes.value.ifBlank { "Excelente resposta aos sinais de vibração e aos pictogramas visuais na piscina." },
            engagementRating = rating
        )

        val updatedStudent = student.copy(
            history = listOf(historyEntry) + student.history
        )
        saveStudent(updatedStudent)

        studentRepository?.let { repo ->
            repoScope.launch {
                try {
                    repo.saveLessonHistory(student.id, historyEntry)
                } catch (e: Exception) {
                    Log.e("NatacaoRepository", "Erro salvando histórico de aula via StudentRepository", e)
                }
            }
        }

        _currentSessionNotes.value = ""
    }

    // Configuração de Montar Aula (Exercícios do Treino) delegando para WorkoutRepository
    fun updateLessonPlan(plan: LessonPlan) {
        _activeLessonPlan.value = plan
        val plans = _lessonPlans.value.toMutableList()
        val idx = plans.indexOfFirst { it.id == plan.id }
        if (idx >= 0) {
            plans[idx] = plan
        } else {
            plans.add(plan)
        }
        _lessonPlans.value = plans

        workoutRepository?.let { repo ->
            repoScope.launch {
                try {
                    repo.saveLessonPlan(plan)
                } catch (e: Exception) {
                    Log.e("NatacaoRepository", "Erro salvando plano de treino via WorkoutRepository", e)
                }
            }
        }
    }

    fun addExerciseToCurrentPlan(exercise: ExerciseItem) {
        val currentExercises = _activeLessonPlan.value.exercises.toMutableList()
        currentExercises.add(exercise)
        val updatedPlan = _activeLessonPlan.value.copy(exercises = currentExercises)
        updateLessonPlan(updatedPlan)
    }

    fun removeExerciseFromCurrentPlan(exerciseId: String) {
        val currentExercises = _activeLessonPlan.value.exercises.filter { it.id != exerciseId }
        val updatedPlan = _activeLessonPlan.value.copy(exercises = currentExercises)
        updateLessonPlan(updatedPlan)
        if (_currentExerciseIndex.value >= currentExercises.size && currentExercises.isNotEmpty()) {
            _currentExerciseIndex.value = currentExercises.size - 1
        }
    }

    // Controle de Conexão com o Relógio
    fun setConnectionMode(status: String) {
        when (status) {
            "Conectado" -> {
                _watchConnectionState.value = _watchConnectionState.value.copy(
                    isConnected = true,
                    connectionStatus = "Conectado via Bluetooth LE",
                    rssiSignalDbm = -54,
                    batteryPercent = 90
                )
            }
            "Conectando" -> {
                _watchConnectionState.value = _watchConnectionState.value.copy(
                    isConnected = false,
                    connectionStatus = "Conectando ao Relógio..."
                )
            }
            else -> {
                _watchConnectionState.value = _watchConnectionState.value.copy(
                    isConnected = false,
                    connectionStatus = "Desconectado (Modo Demonstração)"
                )
            }
        }
    }

    fun toggleWaterLock() {
        val current = _watchConnectionState.value.isWaterLocked
        _watchConnectionState.value = _watchConnectionState.value.copy(isWaterLocked = !current)
    }

    // Atualização de Configuração de Vibrações
    fun updateVibrationSetting(setting: VibrationSetting) {
        val list = _vibrationSettings.value.toMutableList()
        val idx = list.indexOfFirst { it.signalType == setting.signalType }
        if (idx >= 0) {
            list[idx] = setting
            _vibrationSettings.value = list
        }
    }

    // Dados Iniciais e Demos Realistas
    private fun initialStudents(): List<Student> {
        return listOf(
            Student(
                id = "std_lucas",
                name = "Lucas Rocha",
                age = 9,
                avatarEmoji = "👦🏻",
                specificNeeds = listOf("Autismo (TEA)", "Hipersensibilidade Auditiva"),
                communicationPreference = "Pictogramas Sequenciais e Vibrações Curtas",
                accessibility = StudentAccessibility(
                    highContrast = false,
                    reducedStimuli = true,
                    largeFonts = true,
                    vibrationIntensity = 1.0f,
                    wallProximityAlerts = true,
                    poolLengthMeters = 25,
                    voiceAnnouncements = false
                ),
                adaptationLevel = "Flutuação e Respiração Rítmica",
                importantNotes = "Evitar apitos sonoros estridentes perto da borda. Reage muito bem a avisos de transição com 30 segundos de antecedência pelo relógio.",
                photoUri = "android.resource://com.example/drawable/img_student_swimmer_boy",
                history = listOf(
                    LessonHistory(
                        id = "h1",
                        date = "18/10/2026 10:00",
                        lessonTitle = "Adaptação e Respiração",
                        durationMinutes = 40,
                        completedExercises = 6,
                        totalExercises = 7,
                        commandsSent = 14,
                        evolutionNotes = "Conseguiu realizar 5 imersões respiratórias soltando bolhas na água sem ansiedade após comando vibratório.",
                        engagementRating = 5
                    ),
                    LessonHistory(
                        id = "h2",
                        date = "15/10/2026 10:00",
                        lessonTitle = "Pernada com Prancha",
                        durationMinutes = 45,
                        completedExercises = 5,
                        totalExercises = 6,
                        commandsSent = 18,
                        evolutionNotes = "Manteve foco excelente com apoio dos pictogramas de início e troca de atividade.",
                        engagementRating = 4
                    )
                )
            ),
            Student(
                id = "std_beatriz",
                name = "Beatriz Mendonça",
                age = 11,
                avatarEmoji = "👧🏽",
                specificNeeds = listOf("Deficiência Visual", "Baixa Visão Severa"),
                communicationPreference = "Vibração Longa e Marcadores Hápticos de Parede",
                accessibility = StudentAccessibility(
                    highContrast = true,
                    reducedStimuli = false,
                    largeFonts = true,
                    vibrationIntensity = 1.3f,
                    wallProximityAlerts = true,
                    poolLengthMeters = 25,
                    voiceAnnouncements = true
                ),
                adaptationLevel = "Propulsão e Nado Livre com Orientação",
                importantNotes = "O sinal de virada longo deve ser disparado com 4 braçadas de antecedência da parede. Utilizar raia de referência à direita.",
                photoUri = "android.resource://com.example/drawable/img_student_swimmer_girl",
                history = listOf(
                    LessonHistory(
                        id = "h3",
                        date = "17/10/2026 14:30",
                        lessonTitle = "Nado Livre e Viradas",
                        durationMinutes = 45,
                        completedExercises = 7,
                        totalExercises = 7,
                        commandsSent = 22,
                        evolutionNotes = "Executou todas as 8 viradas com segurança total usando o alerta háptico de borda.",
                        engagementRating = 5
                    )
                )
            ),
            Student(
                id = "std_mateus",
                name = "Mateus Silveira",
                age = 8,
                avatarEmoji = "🧒🏼",
                specificNeeds = listOf("Síndrome de Down", "Hipotonia Muscular Leve"),
                communicationPreference = "Reforço Positivo Frequente e Estímulos Lúdicos",
                accessibility = StudentAccessibility(
                    highContrast = false,
                    reducedStimuli = false,
                    largeFonts = true,
                    vibrationIntensity = 1.0f,
                    wallProximityAlerts = false,
                    poolLengthMeters = 25,
                    voiceAnnouncements = true
                ),
                adaptationLevel = "Propulsão com Macarrão e Pernada Dorsal",
                importantNotes = "Gosta de saber quando está perto da pausa. Excelente resposta a sinais comemoração e incentivo.",
                history = listOf(
                    LessonHistory(
                        id = "h4",
                        date = "16/10/2026 09:15",
                        lessonTitle = "Propulsão e Equilíbrio",
                        durationMinutes = 35,
                        completedExercises = 5,
                        totalExercises = 5,
                        commandsSent = 11,
                        evolutionNotes = "Completou a travessia de 15 metros sem apoio do professor, muito animado com as vibrações de parabéns.",
                        engagementRating = 5
                    )
                )
            ),
            Student(
                id = "std_helena",
                name = "Helena Castro",
                age = 10,
                avatarEmoji = "👧🏻",
                specificNeeds = listOf("Paralisia Cerebral Hemiparética", "Deficiência Física"),
                communicationPreference = "Sinais Visuais e Vibração no Punho Esquerdo",
                accessibility = StudentAccessibility(
                    highContrast = false,
                    reducedStimuli = true,
                    largeFonts = false,
                    vibrationIntensity = 1.1f,
                    wallProximityAlerts = true,
                    poolLengthMeters = 25,
                    voiceAnnouncements = true
                ),
                adaptationLevel = "Equilíbrio Dorsal e Rotações em Bloco",
                importantNotes = "Monitorar cansaço muscular do hemicorpo direito. Intervalos de hidratação e pausa a cada 10 minutos.",
                history = emptyList()
            )
        )
    }

    private fun initialLessonPlans(): List<LessonPlan> {
        return listOf(
            LessonPlan(
                id = "lp_adaptacao",
                title = "Aula Inclusiva: Adaptação, Respiração e Propulsão",
                targetAudience = "Alunos em adaptação e desenvolvimento da autonomia aquática",
                objective = "Promover segurança na imersão, controle respiratório e propulsão de pernas com feedback tátil no relógio",
                exercises = listOf(
                    ExerciseItem(
                        id = "ex_1",
                        title = "Acolhimento e Contato com a Água",
                        category = ExerciseCategory.ACOLHIMENTO,
                        durationSeconds = 180,
                        repetitions = 1,
                        instructions = "Sentar na borda, molhar rosto, pernas e braços. Estabelecer vínculo e confiança.",
                        signalType = SignalType.ATENCAO,
                        visualCueSymbol = "🤝"
                    ),
                    ExerciseItem(
                        id = "ex_2",
                        title = "Aquecimento Articular Lúdico",
                        category = ExerciseCategory.AQUECIMENTO,
                        durationSeconds = 240,
                        repetitions = 3,
                        instructions = "Movimentos circulares com braços e pernas como se fossem moinhos de vento.",
                        signalType = SignalType.TROCA,
                        visualCueSymbol = "🤸"
                    ),
                    ExerciseItem(
                        id = "ex_3",
                        title = "Controle Respiratório: Soprar Bolhas",
                        category = ExerciseCategory.RESPIRACAO,
                        durationSeconds = 300,
                        repetitions = 8,
                        instructions = "Inspirar pela boca fora d'água e expirar devagar dentro d'água formando bolhas gigantes.",
                        signalType = SignalType.RESPIRAR,
                        visualCueSymbol = "🫧"
                    ),
                    ExerciseItem(
                        id = "ex_4",
                        title = "Flutuação Ventral (Estrela do Mar)",
                        category = ExerciseCategory.FLUTUACAO,
                        durationSeconds = 240,
                        repetitions = 4,
                        instructions = "Abrir braços e pernas de bruços com apoio suave na cintura. Relaxar musculatura.",
                        signalType = SignalType.TROCA,
                        visualCueSymbol = "⭐"
                    ),
                    ExerciseItem(
                        id = "ex_5",
                        title = "Pernada de Crawl com Prancha",
                        category = ExerciseCategory.PERNADA,
                        durationSeconds = 360,
                        repetitions = 4,
                        instructions = "Pernas esticadas, ponta dos pés estendida, batendo continuamente na superfície.",
                        signalType = SignalType.TROCA,
                        visualCueSymbol = "🏊‍♂️"
                    ),
                    ExerciseItem(
                        id = "ex_6",
                        title = "Aproximação e Virada na Borda",
                        category = ExerciseCategory.VIRADA,
                        durationSeconds = 300,
                        repetitions = 6,
                        instructions = "Ao sentir a vibração longa, tocar suavemente com as mãos na parede e apoiar os pés.",
                        signalType = SignalType.VIRADA,
                        visualCueSymbol = "🧱"
                    ),
                    ExerciseItem(
                        id = "ex_7",
                        title = "Pausa para Água e Regulação",
                        category = ExerciseCategory.PAUSA,
                        durationSeconds = 180,
                        repetitions = 1,
                        instructions = "Hidratação na borda da piscina, respiração profunda e conversa sobre a sensação.",
                        signalType = SignalType.PAUSA,
                        visualCueSymbol = "💧"
                    ),
                    ExerciseItem(
                        id = "ex_8",
                        title = "Encerramento e Relaxamento Dorsal",
                        category = ExerciseCategory.ENCERRAMENTO,
                        durationSeconds = 240,
                        repetitions = 1,
                        instructions = "Flutuação de costas relaxante com balanço suave da água e reforço positivo final.",
                        signalType = SignalType.FINALIZAR,
                        visualCueSymbol = "🏆"
                    )
                )
            ),
            LessonPlan(
                id = "lp_avancado",
                title = "Coordenação de Braçadas e Orientação Espacial",
                targetAudience = "Alunos com deficiência visual ou coordenação motora",
                objective = "Aprimorar alinhamento de corpo, contagem de braçadas e percepção háptica da borda",
                exercises = listOf(
                    ExerciseItem(
                        id = "ex_b1",
                        title = "Respiração Lateral com Prancha",
                        category = ExerciseCategory.RESPIRACAO,
                        durationSeconds = 300,
                        repetitions = 4,
                        instructions = "Pernada contínua, virando o queixo para o lado direito para respirar a cada 3 pernadas.",
                        signalType = SignalType.RESPIRAR,
                        visualCueSymbol = "🏊‍♀️"
                    ),
                    ExerciseItem(
                        id = "ex_b2",
                        title = "Braçada Unilateral Alternada",
                        category = ExerciseCategory.BRACADA,
                        durationSeconds = 360,
                        repetitions = 6,
                        instructions = "Puxada ampla, fase subaquática firme e cotovelo alto na recuperação.",
                        signalType = SignalType.TROCA,
                        visualCueSymbol = "🌊"
                    ),
                    ExerciseItem(
                        id = "ex_b3",
                        title = "Detecção Háptica da Borda e Virada Simples",
                        category = ExerciseCategory.VIRADA,
                        durationSeconds = 300,
                        repetitions = 8,
                        instructions = "Aguardar o sinal vibratório longo para antecipar a parede e posicionar o corpo.",
                        signalType = SignalType.VIRADA,
                        visualCueSymbol = "🔁"
                    ),
                    ExerciseItem(
                        id = "ex_b4",
                        title = "Soltura e Feedback do Professor",
                        category = ExerciseCategory.ENCERRAMENTO,
                        durationSeconds = 180,
                        repetitions = 1,
                        instructions = "Nado descontraído e avaliação conjunta dos estímulos recebidos.",
                        signalType = SignalType.FINALIZAR,
                        visualCueSymbol = "🎉"
                    )
                )
            )
        )
    }

    private fun initialVibrationSettings(): List<VibrationSetting> {
        return listOf(
            VibrationSetting(
                signalType = SignalType.ATENCAO,
                name = "Atenção ao Professor",
                patternType = "1 Curta",
                durations = listOf(0L, 200L),
                intensityPercent = 100,
                repetitions = 1,
                description = "Um toque rápido e sutil para solicitar contato visual com o professor."
            ),
            VibrationSetting(
                signalType = SignalType.TROCA,
                name = "Troca de Exercício",
                patternType = "2 Curtas",
                durations = listOf(0L, 160L, 100L, 160L),
                intensityPercent = 100,
                repetitions = 2,
                description = "Dois pulsos rápidos sinalizando transição para a próxima atividade programada."
            ),
            VibrationSetting(
                signalType = SignalType.VIRADA,
                name = "Virada / Aproximação de Parede",
                patternType = "1 Longa e Firme",
                durations = listOf(0L, 650L),
                intensityPercent = 100,
                repetitions = 1,
                description = "Vibração contínua e forte para indicar a chegada da borda da piscina."
            ),
            VibrationSetting(
                signalType = SignalType.RESPIRAR,
                name = "Comando de Respiração",
                patternType = "Pulso Rítmico",
                durations = listOf(0L, 250L, 120L, 250L),
                intensityPercent = 80,
                repetitions = 2,
                description = "Auxilia na sincronia de inspiração e expiração aquática."
            ),
            VibrationSetting(
                signalType = SignalType.PAUSA,
                name = "Pausa / Descanso",
                patternType = "Duplo Pulso Suave",
                durations = listOf(0L, 200L, 150L, 200L),
                intensityPercent = 70,
                repetitions = 1,
                description = "Indica desaceleração e repouso na borda ou ilha de segurança."
            ),
            VibrationSetting(
                signalType = SignalType.FINALIZAR,
                name = "Finalização de Treino",
                patternType = "Sequência Comemorativa",
                durations = listOf(0L, 180L, 100L, 180L, 100L, 400L),
                intensityPercent = 100,
                repetitions = 1,
                description = "Padrão festivo anunciando a conclusão com êxito da aula."
            ),
            VibrationSetting(
                signalType = SignalType.INCENTIVO,
                name = "Incentivo e Parabéns",
                patternType = "Crescente Positivo",
                durations = listOf(0L, 100L, 80L, 150L, 80L, 250L),
                intensityPercent = 90,
                repetitions = 1,
                description = "Reforço sensorial motivacional para o aluno continuar."
            )
        )
    }

    private fun initialClassWeeklySummary(): ClassWeeklySummary {
        return ClassWeeklySummary(
            weekTitle = "Semana 42 • Outubro de 2026",
            overallClassProgress = 84,
            totalActiveStudentsCount = 4,
            completedLessonsCount = 11,
            targetLessonsCount = 14,
            totalWaterMinutes = 480,
            hapticAlertsDispatched = 67,
            dailyProgressList = listOf(
                DayProgressItem("Seg", "19 Out", 85, 12, 45, isToday = false),
                DayProgressItem("Ter", "20 Out", 78, 10, 40, isToday = false),
                DayProgressItem("Qua", "21 Out", 95, 14, 50, isToday = false),
                DayProgressItem("Qui", "22 Out", 88, 11, 45, isToday = false),
                DayProgressItem("Sex", "23 Out", 92, 13, 45, isToday = true),
                DayProgressItem("Sáb", "24 Out", 70, 8, 35, isToday = false)
            ),
            competenceGoals = listOf(
                WeeklyGoalProgress(
                    id = "g_respiracao",
                    title = "Respiração Rítmica & Imersão",
                    categoryName = "Adaptação",
                    targetUnit = "séries",
                    targetValue = 20,
                    currentValue = 18,
                    percentCompleted = 90,
                    hexColor = 0xFF06B6D4, // Ciano
                    iconKey = "air",
                    description = "Imersão com expiração contínua soltando bolhas na água"
                ),
                WeeklyGoalProgress(
                    id = "g_flutuacao",
                    title = "Flutuação Ventral e Dorsal",
                    categoryName = "Equilíbrio",
                    targetUnit = "min",
                    targetValue = 60,
                    currentValue = 51,
                    percentCompleted = 85,
                    hexColor = 0xFF0284C7, // Azul
                    iconKey = "waves",
                    description = "Relaxamento corporal em decúbito com apoio e autonomia"
                ),
                WeeklyGoalProgress(
                    id = "g_viradas",
                    title = "Viradas com Alerta Háptico",
                    categoryName = "Orientação",
                    targetUnit = "viradas",
                    targetValue = 25,
                    currentValue = 19,
                    percentCompleted = 76,
                    hexColor = 0xFF8B5CF6, // Roxo
                    iconKey = "rotate_right",
                    description = "Toque na borda orientado por sinal vibratório no relógio"
                ),
                WeeklyGoalProgress(
                    id = "g_propulsao",
                    title = "Propulsão de Pernada com Prancha",
                    categoryName = "Pernada",
                    targetUnit = "metros",
                    targetValue = 400,
                    currentValue = 320,
                    percentCompleted = 80,
                    hexColor = 0xFF10B981, // Verde
                    iconKey = "directions_run",
                    description = "Movimento alternado de pernas sem flexão excessiva"
                ),
                WeeklyGoalProgress(
                    id = "g_seguranca",
                    title = "Autonomia e Segurança na Borda",
                    categoryName = "Segurança",
                    targetUnit = "sessões",
                    targetValue = 10,
                    currentValue = 9,
                    percentCompleted = 95,
                    hexColor = 0xFFF97316, // Laranja
                    iconKey = "shield",
                    description = "Entrada e saída seguras da piscina e resposta a comandos de atenção"
                )
            )
        )
    }

    fun getActiveStudentsProgressList(): List<ActiveStudentProgress> {
        val list = _students.value
        return list.mapIndexed { index, student ->
            when (index % 4) {
                0 -> ActiveStudentProgress(
                    student = student,
                    weeklyGoalPercent = 88,
                    attendanceDaysThisWeek = 3,
                    totalWeeklyTargetDays = 3,
                    mainPillar = "Respiração e Adaptação Sensorial",
                    statusTag = "Na Raia 1 • Relógio Ativo",
                    primaryColorHex = 0xFF0284C7, // Azul
                    secondaryColorHex = 0xFF06B6D4  // Ciano
                )
                1 -> ActiveStudentProgress(
                    student = student,
                    weeklyGoalPercent = 94,
                    attendanceDaysThisWeek = 3,
                    totalWeeklyTargetDays = 3,
                    mainPillar = "Orientação Espacial e Viradas Hápticas",
                    statusTag = "Próxima Aula • 14:30",
                    primaryColorHex = 0xFF8B5CF6, // Roxo
                    secondaryColorHex = 0xFFEC4899  // Rosa
                )
                2 -> ActiveStudentProgress(
                    student = student,
                    weeklyGoalPercent = 80,
                    attendanceDaysThisWeek = 2,
                    totalWeeklyTargetDays = 3,
                    mainPillar = "Propulsão com Macarrão e Pernada",
                    statusTag = "Meta Batida Hoje 🎯",
                    primaryColorHex = 0xFF10B981, // Verde
                    secondaryColorHex = 0xFFF59E0B  // Amarelo
                )
                else -> ActiveStudentProgress(
                    student = student,
                    weeklyGoalPercent = 72,
                    attendanceDaysThisWeek = 2,
                    totalWeeklyTargetDays = 2,
                    mainPillar = "Equilíbrio Dorsal e Pausas de Descanso",
                    statusTag = "Em Aquecimento",
                    primaryColorHex = 0xFFEC4899, // Rosa
                    secondaryColorHex = 0xFFF97316  // Laranja
                )
            }
        }
    }
}


