package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.NatacaoTopAppBar
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.TurquoisePrimary
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.NatacaoViewModel
import com.example.viewmodel.TeacherTab

@Composable
fun TeacherDashboardScreen(
    viewModel: NatacaoViewModel,
    onOpenWatchActivity: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTeacherTab.collectAsState()
    val highContrast by viewModel.globalHighContrast.collectAsState()
    val reducedStimuli by viewModel.globalReducedStimuli.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val currentTeacher by viewModel.currentTeacher.collectAsState()
    val feedbackMsg by viewModel.feedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMsg) {
        feedbackMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearFeedbackMessage()
        }
    }

    val (screenTitle, screenSubtitle) = when (currentTab) {
        TeacherTab.OVERVIEW -> Pair("Visão Geral & Metas", "Resumo dos alunos ativos e progresso semanal")
        TeacherTab.WATCH_HUB -> Pair("Central do Relógio", "Aluno: ${selectedStudent.name}")
        TeacherTab.LESSON_PLANNER -> Pair("Montar Aula", "Organização das etapas da aula")
        TeacherTab.STUDENTS -> Pair("Cadastro de Alunos", "Necessidades, acessibilidade e histórico")
        TeacherTab.SIGNALS_CONFIG -> Pair("Sinais e Vibrações", "Configuração háptica e de borda")
        TeacherTab.TRACKING_HISTORY -> Pair("Acompanhamento", "Métricas e avaliação pedagógica")
    }

    Scaffold(
        topBar = {
            NatacaoTopAppBar(
                title = screenTitle,
                subtitle = screenSubtitle,
                highContrast = highContrast,
                reducedStimuli = reducedStimuli,
                teacherName = currentTeacher?.name,
                onToggleHighContrast = { viewModel.toggleHighContrast() },
                onToggleReducedStimuli = { viewModel.toggleReducedStimuli() },
                onOpenSafetyModal = { viewModel.setSafetyModalVisible(true) },
                onOpenWatchMode = {
                    if (onOpenWatchActivity != null) {
                        onOpenWatchActivity()
                    } else {
                        viewModel.navigateTo(AppNavDestination.STANDALONE_WATCH)
                    }
                },
                onLogout = { viewModel.logout() },
                onBack = {
                    if (currentTab != TeacherTab.OVERVIEW) {
                        viewModel.selectTeacherTab(TeacherTab.OVERVIEW)
                    } else {
                        viewModel.navigateTo(AppNavDestination.WELCOME)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("teacher_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = currentTab == TeacherTab.OVERVIEW,
                    onClick = { viewModel.selectTeacherTab(TeacherTab.OVERVIEW) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == TeacherTab.OVERVIEW) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Resumo e Metas"
                        )
                    },
                    label = { Text("Resumo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_overview")
                )

                NavigationBarItem(
                    selected = currentTab == TeacherTab.WATCH_HUB,
                    onClick = { viewModel.selectTeacherTab(TeacherTab.WATCH_HUB) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == TeacherTab.WATCH_HUB) Icons.Filled.Watch else Icons.Outlined.Watch,
                            contentDescription = "Central do Relógio"
                        )
                    },
                    label = { Text("Relógio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_watch_hub")
                )

                NavigationBarItem(
                    selected = currentTab == TeacherTab.LESSON_PLANNER,
                    onClick = { viewModel.selectTeacherTab(TeacherTab.LESSON_PLANNER) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == TeacherTab.LESSON_PLANNER) Icons.Filled.Pool else Icons.Outlined.Pool,
                            contentDescription = "Montar Aula"
                        )
                    },
                    label = { Text("Montar", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_lesson_planner")
                )

                NavigationBarItem(
                    selected = currentTab == TeacherTab.STUDENTS,
                    onClick = { viewModel.selectTeacherTab(TeacherTab.STUDENTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == TeacherTab.STUDENTS) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = "Alunos"
                        )
                    },
                    label = { Text("Alunos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_students")
                )

                NavigationBarItem(
                    selected = currentTab == TeacherTab.TRACKING_HISTORY,
                    onClick = { viewModel.selectTeacherTab(TeacherTab.TRACKING_HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == TeacherTab.TRACKING_HISTORY) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "Acompanhamento"
                        )
                    },
                    label = { Text("Métricas", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_tracking")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                TeacherTab.OVERVIEW -> TeacherOverviewScreen(
                    viewModel = viewModel,
                    onOpenWatchActivity = onOpenWatchActivity
                )
                TeacherTab.WATCH_HUB -> WatchHubScreen(
                    viewModel = viewModel,
                    onOpenWatchActivity = onOpenWatchActivity
                )
                TeacherTab.LESSON_PLANNER -> LessonPlannerScreen(viewModel = viewModel)
                TeacherTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                TeacherTab.SIGNALS_CONFIG -> SignalsConfigScreen(viewModel = viewModel)
                TeacherTab.TRACKING_HISTORY -> LessonTrackingScreen(viewModel = viewModel)
            }
        }
    }
}
