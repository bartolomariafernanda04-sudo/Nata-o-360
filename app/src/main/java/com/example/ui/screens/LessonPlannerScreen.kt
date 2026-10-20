package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.NatacaoViewModel
import com.example.viewmodel.TeacherTab
import java.util.UUID

@Composable
fun LessonPlannerScreen(
    viewModel: NatacaoViewModel,
    modifier: Modifier = Modifier
) {
    val activeLesson by viewModel.activeLessonPlan.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Adicionar Etapa", fontWeight = FontWeight.Bold) },
                containerColor = TurquoisePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_exercise_stage")
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                                imageVector = Icons.Default.Pool,
                                contentDescription = null,
                                tint = OceanBlueDark
                            )
                            Text(
                                text = "Montagem da Aula Aquática",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = activeLesson.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )

                        Text(
                            text = "Objetivo: ${activeLesson.objective}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeLesson.exercises.size} etapas programadas • Total: ~${activeLesson.exercises.sumOf { it.durationSeconds } / 60} min",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Button(
                                onClick = {
                                    viewModel.selectTeacherTab(TeacherTab.WATCH_HUB)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Iniciar Esta Aula")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Etapas e Exercícios da Aula",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            itemsIndexed(
                items = activeLesson.exercises,
                key = { _, item -> item.id }
            ) { index, exercise ->
                ExerciseStageCard(
                    index = index,
                    exercise = exercise,
                    onDelete = { viewModel.removeExercise(exercise.id) },
                    onSelectAsActive = {
                        viewModel.selectExercise(index)
                        viewModel.selectTeacherTab(TeacherTab.WATCH_HUB)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newExercise ->
                viewModel.addExercise(newExercise)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ExerciseStageCard(
    index: Int,
    exercise: ExerciseItem,
    onDelete: () -> Unit,
    onSelectAsActive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Número e Símbolo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = TurquoiseSoft,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = exercise.visualCueSymbol, fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = TurquoisePrimary)
                )
            }

            // Detalhes do Exercício
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = exercise.category.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = exercise.instructions,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${exercise.durationSeconds / 60}m ${if (exercise.durationSeconds % 60 > 0) "${exercise.durationSeconds % 60}s" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${exercise.repetitions}x",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(13.dp), tint = PurpleSignal)
                        Text(
                            text = exercise.signalType.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = PurpleDark)
                        )
                    }
                }
            }

            // Ações: Ativar ou Excluir
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onSelectAsActive,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Ativar este exercício na aula",
                        tint = OceanBluePrimary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remover etapa",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onAdd: (ExerciseItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf(4) }
    var repetitions by remember { mutableStateOf(3) }
    var selectedCategory by remember { mutableStateOf(ExerciseCategory.PERNADA) }
    var selectedSignal by remember { mutableStateOf(SignalType.TROCA) }
    var visualSymbol by remember { mutableStateOf("🏊") }

    val symbolOptions = listOf("🏊", "🫧", "🌊", "⭐", "🧱", "🤝", "🤸", "💧", "🔁", "🏆")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nova Etapa do Treino",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nome do Exercício") },
                        placeholder = { Text("Ex: Pernada com Prancha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instruções e Adaptação") },
                        placeholder = { Text("Ex: Manter pernas esticadas e pontas dos pés estendidas") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Categoria da Aula
                item {
                    Text("Categoria:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ExerciseCategory.values().take(6).forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategory = cat }
                            ) {
                                RadioButton(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                                Text(text = cat.displayName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Duração e Repetições
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Duração: $durationMinutes min", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Slider(
                                value = durationMinutes.toFloat(),
                                onValueChange = { durationMinutes = it.toInt() },
                                valueRange = 1f..15f,
                                steps = 13
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Repetições: $repetitions x", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Slider(
                                value = repetitions.toFloat(),
                                onValueChange = { repetitions = it.toInt() },
                                valueRange = 1f..10f,
                                steps = 8
                            )
                        }
                    }
                }

                // Sinal do Relógio
                item {
                    Text("Sinal Enviado ao Relógio:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(SignalType.TROCA, SignalType.ATENCAO, SignalType.VIRADA, SignalType.RESPIRAR, SignalType.PAUSA).forEach { sig ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSignal = sig }
                            ) {
                                RadioButton(
                                    selected = selectedSignal == sig,
                                    onClick = { selectedSignal = sig }
                                )
                                Text(text = "${sig.label} (${sig.defaultPatternDescription})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Símbolo Visual / Pictograma
                item {
                    Text("Símbolo do Relógio:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        symbolOptions.take(5).forEach { sym ->
                            Surface(
                                shape = CircleShape,
                                color = if (visualSymbol == sym) TurquoiseSoft else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (visualSymbol == sym) BorderStroke(2.dp, TurquoisePrimary) else null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { visualSymbol = sym }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = sym, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newEx = ExerciseItem(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            category = selectedCategory,
                            durationSeconds = durationMinutes * 60,
                            repetitions = repetitions,
                            instructions = instructions.trim().ifBlank { "Seguir orientação do professor." },
                            signalType = selectedSignal,
                            visualCueSymbol = visualSymbol
                        )
                        onAdd(newEx)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Salvar Etapa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
