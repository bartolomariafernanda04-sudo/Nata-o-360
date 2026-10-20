package com.example.model

enum class SignalType(val label: String, val defaultPatternDescription: String) {
    ATENCAO("Atenção", "1 vibração curta (200ms)"),
    TROCA("Troca de Exercício", "2 vibrações curtas (150ms cada)"),
    VIRADA("Virada / Borda", "1 vibração longa e firme (600ms)"),
    RESPIRAR("Respiração", "Pulso rítmico suave (300ms)"),
    PAUSA("Pausa / Descanso", "Vibração dupla suave (200ms + 200ms)"),
    FINALIZAR("Finalizar Aula", "Sequência festiva de 3 pulsos"),
    INCENTIVO("Muito Bem! / Parabéns", "Pulso crescente de incentivo")
}

data class VibrationSetting(
    val signalType: SignalType,
    val name: String,
    val patternType: String,
    val durations: List<Long>,
    val intensityPercent: Int = 100,
    val repetitions: Int = 1,
    val description: String
)

enum class ExerciseCategory(val displayName: String, val iconKey: String) {
    ACOLHIMENTO("Acolhimento e Adaptação", "favorite"),
    AQUECIMENTO("Aquecimento Articular", "fitness"),
    RESPIRACAO("Exercícios de Respiração", "air"),
    PERNADA("Pernada e Propulsão", "directions_run"),
    BRACADA("Braçada e Apoio", "waves"),
    COORDENACAO("Coordenação Motora", "sync"),
    DESLOCAMENTO("Deslocamento na Piscina", "navigation"),
    FLUTUACAO("Flutuação Ventral/Dorsal", "spa"),
    VIRADA("Viradas e Rotações", "rotate_right"),
    PAUSA("Pausa e Hidratação", "local_drink"),
    ENCERRAMENTO("Encerramento e Relaxamento", "check_circle")
}

data class ExerciseItem(
    val id: String,
    val title: String,
    val category: ExerciseCategory,
    val durationSeconds: Int,
    val repetitions: Int,
    val instructions: String,
    val signalType: SignalType,
    val isCompleted: Boolean = false,
    val visualCueSymbol: String = "🏊"
)

enum class QuickCommand(
    val title: String,
    val subtext: String,
    val signalType: SignalType,
    val symbol: String,
    val colorHex: Long
) {
    INICIAR_TREINO("INICIAR TREINO", "Largada da aula", SignalType.INCENTIVO, "🏁", 0xFF0D9488),
    INICIAR_EXERCICIO("INICIAR EXERCÍCIO", "Começar atividade", SignalType.TROCA, "▶️", 0xFF0284C7),
    TROCAR_TREINO("TROCAR TREINO", "Mudar modalidade", SignalType.TROCA, "🔄", 0xFF8B5CF6),
    PROXIMA_ATIVIDADE("PRÓXIMA ATIVIDADE", "Antecipação de etapa", SignalType.TROCA, "⏭️", 0xFF06B6D4),
    VIRADA("VIRADA", "Toque ou giro na borda", SignalType.VIRADA, "🔁", 0xFFF97316),
    ROTACAO("ROTAÇÃO", "Troca decúbito ventral/dorsal", SignalType.VIRADA, "🔃", 0xFFEC4899),
    ATENCAO("ATENÇÃO", "Olhe para o professor", SignalType.ATENCAO, "👀", 0xFFF59E0B),
    RESPIRAR("RESPIRAR", "Soltar bolhas / inspirar", SignalType.RESPIRAR, "🫧", 0xFF06B6D4),
    PAUSA("PAUSA", "Ir até a borda descansar", SignalType.PAUSA, "⏸️", 0xFF64748B),
    RETOMAR("RETOMAR", "Reiniciar o movimento", SignalType.INCENTIVO, "⚡", 0xFF10B981),
    FINALIZAR_TREINO("FINALIZAR TREINO", "Parabéns! Aula concluída", SignalType.FINALIZAR, "🎉", 0xFF10B981)
}

data class StudentAccessibility(
    val highContrast: Boolean = false,
    val reducedStimuli: Boolean = false,
    val largeFonts: Boolean = false,
    val vibrationIntensity: Float = 1.0f,
    val wallProximityAlerts: Boolean = true,
    val poolLengthMeters: Int = 25,
    val voiceAnnouncements: Boolean = true
)

data class LessonHistory(
    val id: String,
    val date: String,
    val lessonTitle: String,
    val durationMinutes: Int,
    val completedExercises: Int,
    val totalExercises: Int,
    val commandsSent: Int,
    val evolutionNotes: String,
    val engagementRating: Int
)

data class Student(
    val id: String,
    val name: String,
    val age: Int,
    val avatarEmoji: String,
    val specificNeeds: List<String>,
    val communicationPreference: String,
    val accessibility: StudentAccessibility,
    val adaptationLevel: String,
    val importantNotes: String,
    val photoUri: String? = null,
    val history: List<LessonHistory> = emptyList()
)

data class LessonPlan(
    val id: String,
    val title: String,
    val targetAudience: String,
    val objective: String,
    val exercises: List<ExerciseItem>
)

data class WatchConnectionState(
    val isConnected: Boolean = true,
    val connectionStatus: String = "Conectado via Bluetooth LE",
    val deviceName: String = "Garmin Swim 2 (Natação 360)",
    val batteryPercent: Int = 92,
    val rssiSignalDbm: Int = -58,
    val isWaterLocked: Boolean = false,
    val lastPingMs: Long = 18
)

data class WeeklyGoalProgress(
    val id: String,
    val title: String,
    val categoryName: String,
    val targetUnit: String,
    val targetValue: Int,
    val currentValue: Int,
    val percentCompleted: Int,
    val hexColor: Long,
    val iconKey: String,
    val description: String
)

data class DayProgressItem(
    val dayLabel: String,      // "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"
    val fullDateLabel: String, // "19 Out"
    val percentAchieved: Int,  // 0..100
    val exercisesCompleted: Int,
    val activeMinutes: Int,
    val isToday: Boolean = false
)

data class ActiveStudentProgress(
    val student: Student,
    val weeklyGoalPercent: Int,
    val attendanceDaysThisWeek: Int,
    val totalWeeklyTargetDays: Int,
    val mainPillar: String,
    val statusTag: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
)

data class ClassWeeklySummary(
    val weekTitle: String = "Semana 42 • Outubro de 2026",
    val overallClassProgress: Int = 84,
    val totalActiveStudentsCount: Int = 4,
    val completedLessonsCount: Int = 11,
    val targetLessonsCount: Int = 14,
    val totalWaterMinutes: Int = 480,
    val hapticAlertsDispatched: Int = 67,
    val dailyProgressList: List<DayProgressItem>,
    val competenceGoals: List<WeeklyGoalProgress>
)

