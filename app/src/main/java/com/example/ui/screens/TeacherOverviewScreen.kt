package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveStudentProgress
import com.example.ui.components.ActiveStudentCard
import com.example.ui.components.WeeklyGoalsChart
import com.example.ui.theme.*
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.NatacaoViewModel
import com.example.viewmodel.TeacherTab

/**
 * Tela Principal do Natação 360° para o Professor (Material 3).
 * Exibe o resumo visual com cards coloridos dos alunos ativos e o gráfico de
 * visualização do progresso das metas semanais, integrando a paleta de cores
 * primárias (azul, turquesa, ciano) e complementares (roxo, verde, amarelo, rosa, laranja).
 */
@Composable
fun TeacherOverviewScreen(
    viewModel: NatacaoViewModel,
    onOpenWatchActivity: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val weeklySummary by viewModel.classWeeklySummary.collectAsState()
    val activeStudents by viewModel.activeStudentsProgress.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val highContrast by viewModel.globalHighContrast.collectAsState()
    val currentTeacher by viewModel.currentTeacher.collectAsState()
    val watchState by viewModel.watchConnectionState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_overview_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Bloco de Boas-Vindas e Resumo do Dia
        item {
            WelcomeBannerCard(
                teacherName = currentTeacher?.name ?: "Professor(a)",
                selectedStudentName = selectedStudent.name,
                isWatchConnected = watchState.isConnected,
                highContrast = highContrast,
                onStartQuickSession = {
                    viewModel.selectTeacherTab(TeacherTab.WATCH_HUB)
                }
            )
        }

        // 2. Linha de Métricas Rápidas (KPIs Coloridos)
        item {
            QuickKpiRow(
                overallProgress = weeklySummary.overallClassProgress,
                activeMinutes = weeklySummary.totalWaterMinutes,
                completedLessons = weeklySummary.completedLessonsCount,
                targetLessons = weeklySummary.targetLessonsCount,
                alertsCount = weeklySummary.hapticAlertsDispatched,
                highContrast = highContrast
            )
        }

        // 3. Componente de Gráfico de Progresso das Metas Semanais
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Acompanhamento Pedagógico Semanal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = if (highContrast) HighContrastYellowTone else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Metas de adaptação, respiração, viradas e autonomia com feedback háptico.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                WeeklyGoalsChart(
                    summary = weeklySummary,
                    highContrast = highContrast
                )
            }
        }

        // 4. Seção dos Cards Coloridos dos Alunos Ativos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Alunos Ativos na Piscina",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = if (highContrast) HighContrastYellowTone else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${activeStudents.size} alunos em acompanhamento personalizado",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.selectTeacherTab(TeacherTab.STUDENTS) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("manage_students_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gerenciar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Itens de Cards de Alunos Coloridos
        items(activeStudents, key = { it.student.id }) { item ->
            ActiveStudentCard(
                studentProgress = item,
                isSelected = item.student.id == selectedStudent.id,
                onSelectStudent = {
                    viewModel.selectStudent(item.student.id)
                },
                onStartLesson = {
                    viewModel.selectStudent(item.student.id)
                    viewModel.selectTeacherTab(TeacherTab.WATCH_HUB)
                },
                onOpenWatch = {
                    viewModel.selectStudent(item.student.id)
                    if (onOpenWatchActivity != null) {
                        onOpenWatchActivity()
                    } else {
                        viewModel.selectTeacherTab(TeacherTab.WATCH_HUB)
                    }
                },
                highContrast = highContrast
            )
        }

        // 5. Bloco de Ações Rápidas do Professor
        item {
            QuickActionsSection(
                onOpenWatchHub = { viewModel.selectTeacherTab(TeacherTab.WATCH_HUB) },
                onOpenPlanner = { viewModel.selectTeacherTab(TeacherTab.LESSON_PLANNER) },
                onOpenSignals = { viewModel.selectTeacherTab(TeacherTab.SIGNALS_CONFIG) },
                onOpenMetrics = { viewModel.selectTeacherTab(TeacherTab.TRACKING_HISTORY) },
                highContrast = highContrast
            )
        }
    }
}

/**
 * Card de Boas-Vindas Superior com Gradiente Aquático
 */
@Composable
private fun WelcomeBannerCard(
    teacherName: String,
    selectedStudentName: String,
    isWatchConnected: Boolean,
    highContrast: Boolean,
    onStartQuickSession: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (highContrast) Color(0xFF161616) else NatacaoBlue,
        border = BorderStroke(
            1.dp,
            if (highContrast) HighContrastYellowTone else NatacaoCyan.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overview_welcome_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!highContrast) {
                        Modifier.background(
                            Brush.linearGradient(
                                listOf(
                                    NatacaoBlueDark,
                                    NatacaoBlue,
                                    NatacaoTurquoise
                                )
                            )
                        )
                    } else Modifier
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Painel Principal",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (highContrast) HighContrastYellowTone else NatacaoCyanContainer,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Olá, $teacherName 👋",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }

                    // Status da Conexão Bluetooth
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, if (isWatchConnected) NatacaoCyanBright else NatacaoOrange)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isWatchConnected) NatacaoCyanBright else NatacaoOrange,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Text(
                                text = if (isWatchConnected) "Relógio Conectado" else "Buscando BLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "Aluno em foco na aula ativa: $selectedStudentName. Todos os comandos táteis e visuais estão prontos para envio.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onStartQuickSession,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (highContrast) HighContrastYellowTone else Color.White,
                            contentColor = if (highContrast) HighContrastBlackTone else NatacaoBlueDeep
                        ),
                        modifier = Modifier.testTag("banner_start_live_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsScore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acessar Central do Relógio",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Linha de Indicadores Chave de Desempenho (KPIs Coloridos)
 */
@Composable
private fun QuickKpiRow(
    overallProgress: Int,
    activeMinutes: Int,
    completedLessons: Int,
    targetLessons: Int,
    alertsCount: Int,
    highContrast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // KPI 1: Progresso Médio (Azul)
        KpiCard(
            title = "Metas da Turma",
            value = "$overallProgress%",
            subtitle = "Meta atingida",
            accentColor = NatacaoBlue,
            containerColor = NatacaoBlueContainer,
            icon = Icons.Default.TrendingUp,
            highContrast = highContrast,
            modifier = Modifier.weight(1f)
        )

        // KPI 2: Tempo na Água (Turquesa)
        KpiCard(
            title = "Tempo na Água",
            value = "${activeMinutes / 60}h ${activeMinutes % 60}m",
            subtitle = "$completedLessons/$targetLessons aulas",
            accentColor = NatacaoTurquoise,
            containerColor = NatacaoTurquoiseSoft,
            icon = Icons.Default.Pool,
            highContrast = highContrast,
            modifier = Modifier.weight(1f)
        )

        // KPI 3: Sinais Hápticos (Roxo)
        KpiCard(
            title = "Sinais Hápticos",
            value = "$alertsCount",
            subtitle = "Disparos no relógio",
            accentColor = NatacaoPurple,
            containerColor = NatacaoPurpleContainer,
            icon = Icons.Default.Vibration,
            highContrast = highContrast,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    containerColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highContrast: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (highContrast) Color(0xFF141414) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (highContrast) HighContrastCyanTone else accentColor.copy(alpha = 0.35f)),
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (highContrast) Color.Black else containerColor,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (highContrast) HighContrastYellowTone else accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (highContrast) HighContrastYellowTone else MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = if (highContrast) HighContrastCyanTone else accentColor
                )
            )
        }
    }
}

/**
 * Seção de Atalhos e Ações Rápidas do Professor
 */
@Composable
private fun QuickActionsSection(
    onOpenWatchHub: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenSignals: () -> Unit,
    onOpenMetrics: () -> Unit,
    highContrast: Boolean
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color(0xFF141414) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (highContrast) HighContrastYellowTone else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_actions_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Recursos Pedagógicos do Professor",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionPill(
                    icon = Icons.Default.Watch,
                    title = "Comandos BLE",
                    accentColor = NatacaoCyan,
                    onClick = onOpenWatchHub,
                    highContrast = highContrast,
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Default.Pool,
                    title = "Montar Aula",
                    accentColor = NatacaoTurquoise,
                    onClick = onOpenPlanner,
                    highContrast = highContrast,
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Default.Vibration,
                    title = "Config Sinais",
                    accentColor = NatacaoPurple,
                    onClick = onOpenSignals,
                    highContrast = highContrast,
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Default.Assessment,
                    title = "Histórico",
                    accentColor = NatacaoOrange,
                    onClick = onOpenMetrics,
                    highContrast = highContrast,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    accentColor: Color,
    onClick: () -> Unit,
    highContrast: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (highContrast) Color.Black else accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, if (highContrast) HighContrastCyanTone else accentColor.copy(alpha = 0.35f)),
        modifier = modifier.height(68.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highContrast) HighContrastYellowTone else accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (highContrast) HighContrastWhiteTone else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
