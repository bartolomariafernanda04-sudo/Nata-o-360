package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NatacaoViewModel

@Composable
fun LessonTrackingScreen(
    viewModel: NatacaoViewModel,
    modifier: Modifier = Modifier
) {
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val activeLesson by viewModel.activeLessonPlan.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val elapsedSeconds by viewModel.lessonElapsedSeconds.collectAsState()
    val commandsLog by viewModel.sessionCommandsLog.collectAsState()
    val sessionNotes by viewModel.currentSessionNotes.collectAsState()

    var teacherNotes by remember(sessionNotes) { mutableStateOf(sessionNotes) }
    var rating by remember { mutableStateOf(5) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val completedCount = activeLesson.exercises.count { it.isCompleted }
    val totalCount = activeLesson.exercises.size

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card de Visão Geral do Aluno em Acompanhamento
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = selectedStudent.avatarEmoji, fontSize = 28.sp)
                            Column {
                                Text(
                                    text = "Acompanhamento em Tempo Real",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = OceanBlueDark)
                                )
                                Text(
                                    text = selectedStudent.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OceanBluePrimary
                        ) {
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Treino: ${activeLesson.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        // Estatísticas da Aula em Cards Rápidos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Tempo Ativo",
                    value = timeFormatted,
                    icon = Icons.Default.Timer,
                    color = OceanBluePrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Etapas Feitas",
                    value = "$completedCount / $totalCount",
                    icon = Icons.Default.CheckCircle,
                    color = TurquoisePrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Sinais Relógio",
                    value = "${commandsLog.size}",
                    icon = Icons.Default.Vibration,
                    color = PurpleSignal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Registro de Dificuldades e Adaptações Necessárias
        item {
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
                    Text(
                        text = "Registro Pedagógico e Adaptações",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Anote dificuldades motoras, respostas às vibrações, receptividade a pictogramas e progressos observados:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    )

                    OutlinedTextField(
                        value = teacherNotes,
                        onValueChange = {
                            teacherNotes = it
                            viewModel.updateSessionNotes(it)
                        },
                        placeholder = { Text("Ex: Respondeu prontamente ao sinal de virada. Demonstrou maior segurança na pernada de costas sem apoio...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_session_notes_input")
                    )

                    // Avaliação de Engajamento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Avaliação da Participação:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { rating = star },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(
                                        text = if (star <= rating) "⭐" else "☆",
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showSaveConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("save_session_history_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finalizar Aula e Salvar no Histórico", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Histórico de Comandos Emitidos na Sessão Atual
        item {
            Text(
                text = "Log de Comandos Transmitidos (${commandsLog.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (commandsLog.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nenhum comando acionado ainda na sessão. Clique nos botões na Central do Relógio para emitir sinais.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(commandsLog) { (cmd, time) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = cmd.symbol, fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cmd.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Sinal: ${cmd.signalType.label}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            )
                        }
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, color = OceanBluePrimary)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = {
                Text(
                    text = "Confirmar Finalização da Aula",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "A aula será arquivada no histórico de ${selectedStudent.name} com ${commandsLog.size} sinais registrados e tempo total de $timeFormatted. Deseja confirmar?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCompletedSession(rating)
                        showSaveConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                ) {
                    Text("Confirmar e Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = color)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}
