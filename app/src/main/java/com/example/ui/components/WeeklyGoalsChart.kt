package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClassWeeklySummary
import com.example.model.DayProgressItem
import com.example.model.WeeklyGoalProgress
import com.example.ui.theme.*

/**
 * Componente de visualização de dados e gráficos para o progresso das metas semanais
 * da turma e dos alunos na natação adaptada.
 * Utiliza o padrão de cores primárias (azul, turquesa, ciano) e complementares (roxo, verde, amarelo, rosa, laranja).
 */
@Composable
fun WeeklyGoalsChart(
    summary: ClassWeeklySummary,
    highContrast: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedChartMode by remember { mutableStateOf(0) } // 0: Barras por Dia, 1: Pilares/Competências
    var selectedDayIndex by remember { mutableStateOf<Int?>(summary.dailyProgressList.indexOfFirst { it.isToday }.takeIf { it >= 0 } ?: 4) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color(0xFF121212) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (highContrast) Brush.linearGradient(listOf(HighContrastYellowTone, HighContrastCyanTone))
                    else Brush.horizontalGradient(listOf(NatacaoBlue.copy(alpha = 0.3f), NatacaoTurquoise.copy(alpha = 0.3f)))
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_goals_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabeçalho do Gráfico com Seletor de Modo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progresso das Metas Semanais",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (highContrast) HighContrastYellowTone else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = summary.weekTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Badge de Meta Geral Atingida
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (highContrast) HighContrastYellowTone else NatacaoTurquoise.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (highContrast) HighContrastYellowTone else NatacaoTurquoise)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (highContrast) HighContrastBlackTone else NatacaoTurquoise,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${summary.overallClassProgress}% Concluído",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (highContrast) HighContrastBlackTone else NatacaoTurquoiseDark
                            )
                        )
                    }
                }
            }

            // Alternância entre Visão por Dias e Visão por Competências
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = selectedChartMode == 0,
                    onClick = { selectedChartMode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("chart_mode_days_button")
                ) {
                    Text("Dias da Semana (Barras)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                SegmentedButton(
                    selected = selectedChartMode == 1,
                    onClick = { selectedChartMode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("chart_mode_competence_button")
                ) {
                    Text("Pilares de Adaptação", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Área de Conteúdo do Gráfico
            if (selectedChartMode == 0) {
                // MODO 1: Gráfico de Barras por Dia
                DailyProgressBarChart(
                    dailyData = summary.dailyProgressList,
                    selectedIndex = selectedDayIndex,
                    onSelectDay = { selectedDayIndex = it },
                    highContrast = highContrast
                )

                // Detalhe interativo do dia selecionado
                selectedDayIndex?.let { idx ->
                    if (idx in summary.dailyProgressList.indices) {
                        val dayItem = summary.dailyProgressList[idx]
                        DayDetailPill(dayItem = dayItem, highContrast = highContrast)
                    }
                }
            } else {
                // MODO 2: Progresso por Competências Adaptadas
                CompetencePillarsList(
                    goals = summary.competenceGoals,
                    highContrast = highContrast
                )
            }

            // Rodapé com Legenda Colorida do Padrão Solicitado
            ColorPaletteLegendRow(highContrast = highContrast)
        }
    }
}

/**
 * Gráfico de Barras com Canvas e Animação suave
 */
@Composable
private fun DailyProgressBarChart(
    dailyData: List<DayProgressItem>,
    selectedIndex: Int?,
    onSelectDay: (Int) -> Unit,
    highContrast: Boolean
) {
    val barProgressAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        barProgressAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("daily_progress_bar_chart")
    ) {
        // Linhas de grade e barras no Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = dailyData.size
                val barWidth = (canvasWidth / barCount) * 0.45f
                val spacing = canvasWidth / barCount

                // Linha de Meta Recomendada (80%)
                val targetY = canvasHeight * (1f - 0.80f)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

                drawLine(
                    color = if (highContrast) HighContrastYellowTone else NatacaoYellowDark.copy(alpha = 0.6f),
                    start = Offset(0f, targetY),
                    end = Offset(canvasWidth, targetY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashEffect
                )

                // Linha de base inferior
                drawLine(
                    color = if (highContrast) Color.DarkGray else Color(0xFFE2E8F0),
                    start = Offset(0f, canvasHeight),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Desenho das Barras Semanais
                dailyData.forEachIndexed { index, item ->
                    val centerX = spacing * index + spacing / 2f
                    val left = centerX - barWidth / 2f
                    val animatedPercentage = (item.percentAchieved / 100f) * barProgressAnimation.value
                    val barHeight = (canvasHeight * animatedPercentage).coerceAtLeast(6.dp.toPx())
                    val top = canvasHeight - barHeight

                    val isSelected = selectedIndex == index

                    // Gradientes das cores solicitadas (Azul, Turquesa, Ciano, Roxo, Rosa, Verde)
                    val barBrush = if (highContrast) {
                        Brush.verticalGradient(
                            listOf(
                                if (isSelected) HighContrastYellowTone else HighContrastCyanTone,
                                if (isSelected) HighContrastYellowTone.copy(alpha = 0.8f) else HighContrastCyanTone.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        when (index % 5) {
                            0 -> Brush.verticalGradient(listOf(NatacaoBlueLight, NatacaoBlue))
                            1 -> Brush.verticalGradient(listOf(NatacaoTurquoiseLight, NatacaoTurquoise))
                            2 -> Brush.verticalGradient(listOf(NatacaoCyanBright, NatacaoCyan))
                            3 -> Brush.verticalGradient(listOf(NatacaoPurpleLight, NatacaoPurple))
                            else -> Brush.verticalGradient(listOf(NatacaoGreenLight, NatacaoGreen))
                        }
                    }

                    // Sombra / Destaque de seleção
                    if (isSelected) {
                        drawRoundRect(
                            color = (if (highContrast) HighContrastYellowTone else NatacaoCyan).copy(alpha = 0.25f),
                            topLeft = Offset(left - 4.dp.toPx(), top - 4.dp.toPx()),
                            size = Size(barWidth + 8.dp.toPx(), barHeight + 4.dp.toPx()),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )
                    }

                    // Barra arredondada principal
                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            // Indicador de Texto da Linha de Meta (80%)
            Text(
                text = "Meta 80%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (highContrast) HighContrastYellowTone else NatacaoYellowDark
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 4.dp)
            )
        }

        // Rótulos dos Dias da Semana e Seleção por Clique
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dailyData.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectDay(index) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected || item.isToday) FontWeight.Black else FontWeight.Normal,
                            color = when {
                                highContrast && isSelected -> HighContrastYellowTone
                                isSelected -> NatacaoBlue
                                item.isToday -> NatacaoTurquoise
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                    Text(
                        text = "${item.percentAchieved}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}

/**
 * Detalhe do dia selecionado no gráfico
 */
@Composable
private fun DayDetailPill(
    dayItem: DayProgressItem,
    highContrast: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (highContrast) Color(0xFF1E1E1E) else NatacaoBlueContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (highContrast) HighContrastCyanTone else NatacaoBlue.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (dayItem.percentAchieved >= 80) NatacaoGreen else NatacaoYellow,
                    modifier = Modifier.size(10.dp)
                ) {}
                Column {
                    Text(
                        text = "${dayItem.dayLabel} (${dayItem.fullDateLabel})${if (dayItem.isToday) " • Hoje" else ""}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${dayItem.exercisesCompleted} exercícios executados • ${dayItem.activeMinutes} min na água",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (dayItem.percentAchieved >= 80) NatacaoGreenContainer else NatacaoYellowContainer
            ) {
                Text(
                    text = if (dayItem.percentAchieved >= 80) "Meta Batida 🎯" else "Em Progresso ⏳",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (dayItem.percentAchieved >= 80) NatacaoGreenDark else NatacaoYellowDark
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Lista de Barras de Progresso por Competência Pedagógica
 */
@Composable
private fun CompetencePillarsList(
    goals: List<WeeklyGoalProgress>,
    highContrast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("competence_pillars_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        goals.forEach { goal ->
            val color = if (highContrast) HighContrastYellowTone else Color(goal.hexColor)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        Surface(
                            shape = CircleShape,
                            color = color.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (goal.iconKey) {
                                        "air" -> Icons.Default.Air
                                        "waves" -> Icons.Default.Waves
                                        "rotate_right" -> Icons.Default.RotateRight
                                        "directions_run" -> Icons.Default.DirectionsRun
                                        else -> Icons.Default.Shield
                                    },
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "${goal.currentValue}/${goal.targetValue} ${goal.targetUnit} (${goal.percentCompleted}%)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                }

                // Barra de Progresso M3 Estilizada com a cor da competência
                LinearProgressIndicator(
                    progress = { goal.percentCompleted / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f)
                )

                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Linha de Legenda Colorida destacando as cores do projeto
 */
@Composable
private fun ColorPaletteLegendRow(highContrast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = NatacaoBlue, label = "Azul (Respiração)")
        LegendItem(color = NatacaoTurquoise, label = "Turquesa (Propulsão)")
        LegendItem(color = NatacaoCyan, label = "Ciano (Flutuação)")
        LegendItem(color = NatacaoPurple, label = "Roxo (Viradas)")
        LegendItem(color = NatacaoGreen, label = "Verde (Meta)")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(8.dp)
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        )
    }
}
