package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuickCommand
import com.example.ui.theme.*
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.NatacaoViewModel

@Composable
fun StandaloneWatchScreen(
    viewModel: NatacaoViewModel,
    onOpenTeacherApp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val lastCommand by viewModel.lastCommand.collectAsState()
    val isVibrating by viewModel.isVibrating.collectAsState()
    val watchState by viewModel.watchConnectionState.collectAsState()
    val highContrast by viewModel.globalHighContrast.collectAsState()
    val reducedStimuli by viewModel.globalReducedStimuli.collectAsState()
    val pairingPin by viewModel.bluetoothSyncManager.pairingPin.collectAsState()

    val currentExercise = viewModel.getCurrentExercise()
    val nextExercise = viewModel.getNextExercise()

    var studentCalledProfessor by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (highContrast) HighContrastBlack else Color(0xFF030712)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar do Relógio com Botão de Retorno e Identificação de Modo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (onOpenTeacherApp != null) {
                            onOpenTeacherApp()
                        } else {
                            viewModel.navigateTo(AppNavDestination.TEACHER_DASHBOARD)
                        }
                    },
                    modifier = Modifier.testTag("watch_screen_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsScore,
                        contentDescription = "Abrir Aplicativo do Professor",
                        tint = CyanAccent
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, if (watchState.isConnected) GreenSuccess.copy(alpha = 0.5f) else OrangeWarning)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (watchState.isConnected) GreenSuccess else OrangeWarning)
                        )
                        Text(
                            text = if (watchState.isConnected) "Bluetooth LE • PIN $pairingPin" else "Modo Autônomo",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleWaterLock() },
                    modifier = Modifier.testTag("watch_toggle_water_lock")
                ) {
                    Icon(
                        imageVector = if (watchState.isWaterLocked) Icons.Default.WaterDrop else Icons.Default.Waves,
                        contentDescription = "Bloqueio Aquático",
                        tint = if (watchState.isWaterLocked) CyanAccent else Color.Gray
                    )
                }
            }

            // Exibição Central em formato de Smartwatch Hídrico
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                SmartwatchView(
                    studentName = selectedStudent.name,
                    currentCommand = lastCommand,
                    currentExercise = currentExercise,
                    nextExercise = nextExercise,
                    isVibrating = isVibrating,
                    watchConnectionState = watchState,
                    highContrast = highContrast,
                    reducedStimuli = reducedStimuli,
                    sizeDp = 300.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (studentCalledProfessor) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YellowAttention.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, YellowAttention)
                    ) {
                        Text(
                            text = "🙋 Alerta transmitido via Bluetooth ao Professor! Aguarde na borda.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = YellowAttention
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Barra Inferior do Relógio: Botões do Aluno
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botão de Emergência / Chamar Professor via Bluetooth
                    Button(
                        onClick = {
                            studentCalledProfessor = true
                            viewModel.bluetoothSyncManager.sendAlertFromWatch(selectedStudent.name, "EMERGENCY_HELP")
                            viewModel.executeQuickCommand(QuickCommand.ATENCAO)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (studentCalledProfessor) YellowAttention else Color(0xFFDC2626)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("student_call_teacher_button")
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (studentCalledProfessor) "Professor Avisado" else "Chamar Professor",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Confirmar Recepção do Comando via Bluetooth
                    Button(
                        onClick = {
                            viewModel.bluetoothSyncManager.sendAlertFromWatch(selectedStudent.name, "ACKNOWLEDGE")
                            viewModel.executeQuickCommand(QuickCommand.RETOMAR)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TurquoisePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("student_confirm_command_button")
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Entendido! 👍", fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Natação 360 • Aplicativo do Relógio (Aluno) • Conectado via Bluetooth",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
