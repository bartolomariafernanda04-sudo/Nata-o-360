package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveStudentProgress
import com.example.ui.theme.*

/**
 * Card colorido para exibição de alunos ativos no painel principal.
 * Utiliza o padrão de cores solicitado: primárias (azul, turquesa, ciano) e
 * complementares (roxo, verde, amarelo, rosa, laranja) com alto acabamento Material 3.
 */
@Composable
fun ActiveStudentCard(
    studentProgress: ActiveStudentProgress,
    isSelected: Boolean,
    onSelectStudent: () -> Unit,
    onStartLesson: () -> Unit,
    onOpenWatch: () -> Unit,
    highContrast: Boolean = false,
    modifier: Modifier = Modifier
) {
    val student = studentProgress.student
    val primaryColor = if (highContrast) HighContrastYellowTone else Color(studentProgress.primaryColorHex)
    val secondaryColor = if (highContrast) HighContrastCyanTone else Color(studentProgress.secondaryColorHex)

    val animatedProgress by animateFloatAsState(
        targetValue = studentProgress.weeklyGoalPercent / 100f,
        label = "studentGoalProgress"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) {
                Color(0xFF141414)
            } else if (isSelected) {
                primaryColor.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            brush = if (highContrast) {
                Brush.horizontalGradient(listOf(HighContrastYellowTone, HighContrastCyanTone))
            } else if (isSelected) {
                Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
            } else {
                Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.4f), secondaryColor.copy(alpha = 0.2f)))
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectStudent() }
            .testTag("active_student_card_${student.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Faixa Superior: Avatar + Nome + Tag de Status Colorida
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar com Foto do Aluno ou Emoji inclusivo
                    StudentAvatar(
                        student = student,
                        size = 54.dp,
                        borderColor = primaryColor,
                        borderWidth = 2.dp,
                        showEmojiBadge = true
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (highContrast) HighContrastYellowTone else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor
                                ) {
                                    Text(
                                        text = "ATIVO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${student.age} anos • ${student.adaptationLevel}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Tag de Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (highContrast) Color.DarkGray else secondaryColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = studentProgress.statusTag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (highContrast) HighContrastCyanTone else secondaryColor
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Tags de Necessidades Específicas / Acessibilidade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                student.specificNeeds.forEach { need ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (highContrast) Color(0xFF222222) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("student_need_chip_${need.take(6)}")
                    ) {
                        Text(
                            text = need,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (highContrast) HighContrastWhiteTone else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Bloco de Progresso da Meta Semanal
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Meta Semanal: ${studentProgress.mainPillar}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Text(
                        text = "${studentProgress.weeklyGoalPercent}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = primaryColor
                        )
                    )
                }

                // Barra Colorida de Progresso da Meta
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.15f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Frequência: ${studentProgress.attendanceDaysThisWeek}/${studentProgress.totalWeeklyTargetDays} aulas cumpridas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = if (studentProgress.weeklyGoalPercent >= 85) "Superou a meta ⭐" else "Em evolução 🏊",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (studentProgress.weeklyGoalPercent >= 85) NatacaoGreenDark else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Linha de Ações: Selecionar / Iniciar Aula e Atalho do Relógio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onSelectStudent()
                        onStartLesson()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("start_lesson_student_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSelected) "Conectar na Aula" else "Selecionar Aluno",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = {
                        onSelectStudent()
                        onOpenWatch()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, secondaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = secondaryColor),
                    modifier = Modifier.testTag("open_watch_student_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = "Relógio do Aluno",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Relógio",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
