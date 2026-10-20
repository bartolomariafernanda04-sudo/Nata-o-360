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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalType
import com.example.model.VibrationSetting
import com.example.ui.theme.*
import com.example.viewmodel.NatacaoViewModel

@Composable
fun SignalsConfigScreen(
    viewModel: NatacaoViewModel,
    modifier: Modifier = Modifier
) {
    val vibrationSettings by viewModel.vibrationSettings.collectAsState()
    val isVibrating by viewModel.isVibrating.collectAsState()
    val lastSignal by viewModel.lastSignalTriggered.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()

    var editingSetting by remember { mutableStateOf<VibrationSetting?>(null) }
    var poolLengthMeters by remember { mutableStateOf(selectedStudent.accessibility.poolLengthMeters) }
    var proximityDistanceMeters by remember { mutableStateOf(5) }
    var wallProximityAlertEnabled by remember { mutableStateOf(selectedStudent.accessibility.wallProximityAlerts) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card de Informação e Diagnóstico Háptico
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = OceanBlueDark
                        )
                        Text(
                            text = "Configuração Háptica e Sensorial",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "Personalize o padrão de vibração transmitido ao relógio do aluno (${selectedStudent.name}). Cada necessidade requer respostas sensoriais distintas.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Motor háptico compatível: Vibrações em tempo real ativas no dispositivo.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }

        // Seção: Mapeamento de Piscina e Aviso de Parede (Deficiência Visual)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, YellowAttention.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = YellowAttention
                            )
                            Column {
                                Text(
                                    text = "Mapeamento da Piscina & Borda",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Recurso essencial para alunos com Baixa Visão e Cegueira",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                )
                            }
                        }

                        Switch(
                            checked = wallProximityAlertEnabled,
                            onCheckedChange = { wallProximityAlertEnabled = it }
                        )
                    }

                    if (wallProximityAlertEnabled) {
                        Text(
                            text = "Comprimento da Piscina: $poolLengthMeters metros",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = poolLengthMeters == 25,
                                onClick = { poolLengthMeters = 25 },
                                label = { Text("Semiolímpica (25m)") }
                            )
                            FilterChip(
                                selected = poolLengthMeters == 50,
                                onClick = { poolLengthMeters = 50 },
                                label = { Text("Olímpica (50m)") }
                            )
                        }

                        Text(
                            text = "Aviso antecipado de aproximação da parede: $proximityDistanceMeters metros antes",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = proximityDistanceMeters.toFloat(),
                            onValueChange = { proximityDistanceMeters = it.toInt() },
                            valueRange = 2f..8f,
                            steps = 5
                        )

                        Button(
                            onClick = {
                                viewModel.testVibration(SignalType.VIRADA)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YellowAttention, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testar Alerta de Virada (1 Longa)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Título da Lista de Sinais
        item {
            Text(
                text = "Padrões de Sinal por Comando",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Cards de cada sinal
        items(vibrationSettings, key = { it.signalType }) { setting ->
            VibrationSettingCard(
                setting = setting,
                isTestingThis = isVibrating && lastSignal == setting.signalType,
                onTest = { viewModel.testVibration(setting.signalType) },
                onEdit = { editingSetting = setting }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (editingSetting != null) {
        EditVibrationDialog(
            setting = editingSetting!!,
            onDismiss = { editingSetting = null },
            onSave = { updated ->
                viewModel.updateVibrationSetting(updated)
                editingSetting = null
            },
            onTest = { sig ->
                viewModel.testVibration(sig)
            }
        )
    }
}

@Composable
private fun VibrationSettingCard(
    setting: VibrationSetting,
    isTestingThis: Boolean,
    onTest: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTestingThis) CyanAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isTestingThis) 2.dp else 1.dp,
            if (isTestingThis) CyanAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TurquoiseSoft,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = TurquoisePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = setting.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Padrão: ${setting.patternType}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${setting.intensityPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = setting.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajustar", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onTest,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTestingThis) CyanAccent else TurquoisePrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("test_vibration_${setting.signalType.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = if (isTestingThis) Icons.Default.Sensors else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTestingThis) "Vibrando..." else "Testar Vibração",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun EditVibrationDialog(
    setting: VibrationSetting,
    onDismiss: () -> Unit,
    onSave: (VibrationSetting) -> Unit,
    onTest: (SignalType) -> Unit
) {
    var patternType by remember { mutableStateOf(setting.patternType) }
    var intensity by remember { mutableStateOf(setting.intensityPercent.toFloat()) }
    var repetitions by remember { mutableStateOf(setting.repetitions) }

    val patternOptions = listOf("1 Curta (180ms)", "2 Curtas (150ms)", "1 Longa (650ms)", "Pulso Rítmico (250ms)", "Sequência Festiva")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajustar Sinal: ${setting.name}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Escolha a cadência háptica que melhor se adapta à sensibilidade tátil da criança:",
                    style = MaterialTheme.typography.bodySmall
                )

                patternOptions.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = patternType.startsWith(opt.take(6)),
                            onClick = { patternType = opt }
                        )
                        Text(text = opt, style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Intensidade Háptica: ${intensity.toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    valueRange = 40f..100f,
                    steps = 5
                )

                Button(
                    onClick = { onTest(setting.signalType) },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sentir Padrão Agora no Aparelho")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = setting.copy(
                        patternType = patternType,
                        intensityPercent = intensity.toInt(),
                        repetitions = repetitions
                    )
                    onSave(updated)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
