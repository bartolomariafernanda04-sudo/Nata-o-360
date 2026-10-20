package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.StudentAvatar
import com.example.ui.components.WatchConnectionBadge
import com.example.ui.theme.*
import com.example.viewmodel.NatacaoViewModel

@Composable
fun WatchHubScreen(
    viewModel: NatacaoViewModel,
    onOpenWatchActivity: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val students by viewModel.students.collectAsState()
    val activeLesson by viewModel.activeLessonPlan.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val isRunning by viewModel.isLessonRunning.collectAsState()
    val isPaused by viewModel.isLessonPaused.collectAsState()
    val elapsedSeconds by viewModel.lessonElapsedSeconds.collectAsState()
    val lastCommand by viewModel.lastCommand.collectAsState()
    val lastCommandTimestamp by viewModel.lastCommandTimestamp.collectAsState()
    val watchState by viewModel.watchConnectionState.collectAsState()
    val isVibrating by viewModel.isVibrating.collectAsState()
    val highContrast by viewModel.globalHighContrast.collectAsState()
    val reducedStimuli by viewModel.globalReducedStimuli.collectAsState()
    val discoveredDevices by viewModel.bluetoothSyncManager.discoveredDevices.collectAsState()
    val pairingPin by viewModel.bluetoothSyncManager.pairingPin.collectAsState()

    val currentExercise = viewModel.getCurrentExercise()
    val nextExercise = viewModel.getNextExercise()

    var showConnectionDialog by remember { mutableStateOf(false) }
    var showStudentSelector by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 760.dp

        if (isWide) {
            // Layout Lado a Lado (Tablet / Tela Ampla)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lado Esquerdo — Painel do Professor
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                ) {
                    TeacherHubContent(
                        selectedStudent = selectedStudent,
                        activeLesson = activeLesson,
                        currentExercise = currentExercise,
                        nextExercise = nextExercise,
                        currentExerciseIndex = currentExerciseIndex,
                        elapsedSeconds = elapsedSeconds,
                        isRunning = isRunning,
                        isPaused = isPaused,
                        lastCommand = lastCommand,
                        watchState = watchState,
                        highContrast = highContrast,
                        onOpenStudentPicker = { showStudentSelector = true },
                        onOpenConnectionDialog = { showConnectionDialog = true },
                        onExecuteCommand = { viewModel.executeQuickCommand(it) },
                        onNextExercise = { viewModel.nextExercise() },
                        onPreviousExercise = { viewModel.previousExercise() }
                    )
                }

                // Lado Direito — Prévia do Relógio do Aluno
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (highContrast) HighContrastBlack else SurfaceVariantDark,
                    border = BorderStroke(
                        1.dp,
                        if (isVibrating) CyanAccent else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                ) {
                    WatchPreviewPanel(
                        studentName = selectedStudent.name,
                        currentCommand = lastCommand,
                        currentExercise = currentExercise,
                        nextExercise = nextExercise,
                        isVibrating = isVibrating,
                        watchState = watchState,
                        highContrast = highContrast,
                        reducedStimuli = reducedStimuli,
                        onToggleWaterLock = { viewModel.toggleWaterLock() },
                        onOpenStandaloneWatch = {
                            if (onOpenWatchActivity != null) {
                                onOpenWatchActivity()
                            } else {
                                viewModel.navigateTo(com.example.viewmodel.AppNavDestination.STANDALONE_WATCH)
                            }
                        }
                    )
                }
            }
        } else {
            // Layout Vertical (Mobile): Prévia do Relógio integrada + Painel do Professor com rolagem
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header com Status de Conexão e Seleção Rápida de Aluno
                item {
                    HubTopStatusBar(
                        selectedStudent = selectedStudent,
                        watchState = watchState,
                        onOpenStudentPicker = { showStudentSelector = true },
                        onOpenConnectionDialog = { showConnectionDialog = true }
                    )
                }

                // Prévia do Relógio do Aluno (Destaque visual interativo e responsivo)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (highContrast) HighContrastBlack else SurfaceVariantDark
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isVibrating) CyanAccent else Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Watch,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Prévia do Relógio do Aluno",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isVibrating) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = CyanAccent.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "VIBRANDO AGORA",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Black,
                                                    color = CyanLight,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            if (onOpenWatchActivity != null) {
                                                onOpenWatchActivity()
                                            } else {
                                                viewModel.navigateTo(com.example.viewmodel.AppNavDestination.STANDALONE_WATCH)
                                            }
                                        },
                                        modifier = Modifier.testTag("preview_expand_watch_mobile_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Abrir Aplicativo do Relógio",
                                            tint = CyanAccent
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SmartwatchView(
                                studentName = selectedStudent.name,
                                currentCommand = lastCommand,
                                currentExercise = currentExercise,
                                nextExercise = nextExercise,
                                isVibrating = isVibrating,
                                watchConnectionState = watchState,
                                highContrast = highContrast,
                                reducedStimuli = reducedStimuli,
                                sizeDp = 240.dp
                            )
                        }
                    }
                }

                // Informações da Atividade Atual e Próxima
                item {
                    ActivityOverviewCard(
                        activeLesson = activeLesson,
                        currentExercise = currentExercise,
                        nextExercise = nextExercise,
                        currentExerciseIndex = currentExerciseIndex,
                        elapsedSeconds = elapsedSeconds,
                        isRunning = isRunning,
                        isPaused = isPaused,
                        onNext = { viewModel.nextExercise() },
                        onPrev = { viewModel.previousExercise() }
                    )
                }

                // Indicador do Último Comando Enviado
                item {
                    LastCommandSentBadge(lastCommand = lastCommand, isVibrating = isVibrating)
                }

                // Grade de Comandos Rápidos
                item {
                    Text(
                        text = "Comandos Rápidos Imediatos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                item {
                    QuickCommandsGrid(
                        onCommandClick = { viewModel.executeQuickCommand(it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Modal seletor de Aluno
    if (showStudentSelector) {
        AlertDialog(
            onDismissRequest = { showStudentSelector = false },
            title = {
                Text(
                    text = "Selecionar Aluno da Aula",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    students.forEach { std ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (std.id == selectedStudent.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectStudent(std.id)
                                    showStudentSelector = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StudentAvatar(student = std, size = 36.dp, borderWidth = 1.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = std.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = std.specificNeeds.joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (std.id == selectedStudent.id) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentSelector = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal de Conexão com o Relógio
    if (showConnectionDialog) {
        ConnectionSettingsDialog(
            currentStatus = watchState.connectionStatus,
            pairingPin = pairingPin,
            discoveredDevices = discoveredDevices,
            onScan = { viewModel.startBluetoothScan() },
            onConnectDevice = { deviceName -> viewModel.connectBluetoothWatch(deviceName) },
            onSelectMode = { mode ->
                viewModel.setWatchConnection(mode)
            },
            onDismiss = { showConnectionDialog = false }
        )
    }
}

@Composable
private fun HubTopStatusBar(
    selectedStudent: Student,
    watchState: WatchConnectionState,
    onOpenStudentPicker: () -> Unit,
    onOpenConnectionDialog: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Aluno ativo
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenStudentPicker)
                .padding(end = 8.dp)
                .testTag("select_active_student_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudentAvatar(student = selectedStudent, size = 34.dp, borderWidth = 1.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aluno em Aula",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    )
                    Text(
                        text = selectedStudent.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Trocar Aluno",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Status do Relógio
        WatchConnectionBadge(
            connectionState = watchState,
            onClickChange = onOpenConnectionDialog
        )
    }
}

@Composable
private fun ActivityOverviewCard(
    activeLesson: LessonPlan,
    currentExercise: ExerciseItem?,
    nextExercise: ExerciseItem?,
    currentExerciseIndex: Int,
    elapsedSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header do Treino e Cronômetro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TREINO ATUAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = OceanBluePrimary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = activeLesson.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Cronômetro da Aula
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                val timeStr = String.format("%02d:%02d", minutes, seconds)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isRunning && !isPaused) TurquoiseSoft else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isRunning && !isPaused) TurquoisePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isRunning && !isPaused) TurquoisePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Exercício em Andamento
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = OceanBluePrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentExercise?.visualCueSymbol ?: "🏊",
                            fontSize = 22.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXERCÍCIO EM ANDAMENTO (${currentExerciseIndex + 1}/${activeLesson.exercises.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OceanBluePrimary,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = currentExercise?.title ?: "Nenhum exercício ativo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentExercise?.instructions ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Exercício anterior")
                    }
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Próximo exercício")
                    }
                }
            }

            // Próxima Atividade
            if (nextExercise != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PRÓXIMA:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "${nextExercise.visualCueSymbol} ${nextExercise.title}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LastCommandSentBadge(
    lastCommand: QuickCommand?,
    isVibrating: Boolean
) {
    if (lastCommand == null) return

    val cmdColor = Color(lastCommand.colorHex)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cmdColor.copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, cmdColor.copy(alpha = if (isVibrating) 0.9f else 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(cmdColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = lastCommand.symbol, fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ÚLTIMO COMANDO TRANSMITIDO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = cmdColor,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = lastCommand.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "✓ Confirmado no Relógio",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GreenSuccess
                )
            )
        }
    }
}

@Composable
private fun QuickCommandsGrid(
    onCommandClick: (QuickCommand) -> Unit
) {
    val commands = QuickCommand.values().toList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Exibir comandos em pares (2 colunas) com botões grandes e acessíveis
        for (i in commands.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cmd1 = commands[i]
                QuickCommandButton(
                    command = cmd1,
                    modifier = Modifier.weight(1f),
                    onClick = { onCommandClick(cmd1) }
                )

                if (i + 1 < commands.size) {
                    val cmd2 = commands[i + 1]
                    QuickCommandButton(
                        command = cmd2,
                        modifier = Modifier.weight(1f),
                        onClick = { onCommandClick(cmd2) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickCommandButton(
    command: QuickCommand,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cmdColor = Color(command.colorHex)

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cmdColor,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
        modifier = modifier
            .heightIn(min = 54.dp)
            .testTag("quick_command_${command.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = command.symbol,
                fontSize = 20.sp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = command.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.3.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = command.subtext,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TeacherHubContent(
    selectedStudent: Student,
    activeLesson: LessonPlan,
    currentExercise: ExerciseItem?,
    nextExercise: ExerciseItem?,
    currentExerciseIndex: Int,
    elapsedSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean,
    lastCommand: QuickCommand?,
    watchState: WatchConnectionState,
    highContrast: Boolean,
    onOpenStudentPicker: () -> Unit,
    onOpenConnectionDialog: () -> Unit,
    onExecuteCommand: (QuickCommand) -> Unit,
    onNextExercise: () -> Unit,
    onPreviousExercise: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HubTopStatusBar(
                selectedStudent = selectedStudent,
                watchState = watchState,
                onOpenStudentPicker = onOpenStudentPicker,
                onOpenConnectionDialog = onOpenConnectionDialog
            )
        }

        item {
            ActivityOverviewCard(
                activeLesson = activeLesson,
                currentExercise = currentExercise,
                nextExercise = nextExercise,
                currentExerciseIndex = currentExerciseIndex,
                elapsedSeconds = elapsedSeconds,
                isRunning = isRunning,
                isPaused = isPaused,
                onNext = onNextExercise,
                onPrev = onPreviousExercise
            )
        }

        item {
            LastCommandSentBadge(lastCommand = lastCommand, isVibrating = false)
        }

        item {
            Text(
                text = "Comandos Rápidos Imediatos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            QuickCommandsGrid(onCommandClick = onExecuteCommand)
        }
    }
}

@Composable
private fun WatchPreviewPanel(
    studentName: String,
    currentCommand: QuickCommand?,
    currentExercise: ExerciseItem?,
    nextExercise: ExerciseItem?,
    isVibrating: Boolean,
    watchState: WatchConnectionState,
    highContrast: Boolean,
    reducedStimuli: Boolean,
    onToggleWaterLock: () -> Unit,
    onOpenStandaloneWatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Título da Prévia e Indicador de Vibração
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Watch,
                    contentDescription = null,
                    tint = CyanAccent
                )
                Text(
                    text = "Prévia do Relógio do Aluno",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            IconButton(
                onClick = onOpenStandaloneWatch,
                modifier = Modifier.testTag("preview_expand_watch_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Abrir Relógio em Tela Cheia",
                    tint = Color.White
                )
            }
        }

        // Simulação do Smartwatch
        SmartwatchView(
            studentName = studentName,
            currentCommand = currentCommand,
            currentExercise = currentExercise,
            nextExercise = nextExercise,
            isVibrating = isVibrating,
            watchConnectionState = watchState,
            highContrast = highContrast,
            reducedStimuli = reducedStimuli,
            sizeDp = 290.dp
        )

        // Controles de Demonstração e Bloqueio Aquático
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onToggleWaterLock,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (watchState.isWaterLocked) CyanAccent else Color.White
                ),
                border = BorderStroke(1.dp, if (watchState.isWaterLocked) CyanAccent else Color.Gray),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (watchState.isWaterLocked) Icons.Default.WaterDrop else Icons.Default.Waves,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (watchState.isWaterLocked) "Bloqueio Ativo" else "Bloqueio Água",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Button(
                onClick = onOpenStandaloneWatch,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = OceanBlueDeep),
                modifier = Modifier.weight(1f)
            ) {
                Text("Modo Relógio", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun ConnectionSettingsDialog(
    currentStatus: String,
    pairingPin: String,
    discoveredDevices: List<com.example.service.DiscoveredBluetoothDevice>,
    onScan: () -> Unit,
    onConnectDevice: (String) -> Unit,
    onSelectMode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = CyanAccent
                )
                Text(
                    text = "Central Bluetooth Natação 360",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Comunicação sem fio direta entre o Painel do Professor e o Relógio do Aluno via Bluetooth Low Energy (BLE) e RFCOMM.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Status e PIN de Pareamento
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: $currentStatus",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = OceanBluePrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "PIN: $pairingPin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = OceanBluePrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Canal Seguro: Criptografia local 100% offline (sem internet)",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                // Botão de Busca / Escanear Dispositivos
                item {
                    Button(
                        onClick = onScan,
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar Relógios Bluetooth na Piscina")
                    }
                }

                // Dispositivos Descobertos
                item {
                    Text(
                        text = "Dispositivos Bluetooth Encontrados:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(discoveredDevices) { device ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (device.isPaired) CyanAccent.copy(alpha = 0.7f) else Color.LightGray.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = null,
                                tint = if (device.isPaired) TurquoisePrimary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${device.deviceType} • Sinal: ${device.rssiDbm} dBm",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                )
                            }
                            FilledTonalButton(
                                onClick = {
                                    onConnectDevice(device.name)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Parear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Alerta Técnico de Atenuação Aquática
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Waves,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Atenuação de Rádio na Água: O sinal 2.4 GHz atenua ~4 dB/cm em meio aquoso. Oriente o aluno a elevar o pulso ou nadar próximo à borda durante o envio de instruções.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }
                    }
                }

                // Botão de Desconexão
                item {
                    OutlinedButton(
                        onClick = { onSelectMode("Desconectado") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeWarning),
                        border = BorderStroke(1.dp, OrangeWarning),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desconectar Relógio")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Concluir")
            }
        }
    )
}
