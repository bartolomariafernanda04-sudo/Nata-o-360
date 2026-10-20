package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExerciseItem
import com.example.model.QuickCommand
import com.example.model.SignalType
import com.example.model.WatchConnectionState
import com.example.ui.components.AnimatedVibrationRipple
import com.example.ui.theme.*

@Composable
fun SmartwatchView(
    studentName: String,
    currentCommand: QuickCommand?,
    currentExercise: ExerciseItem?,
    nextExercise: ExerciseItem?,
    isVibrating: Boolean,
    watchConnectionState: WatchConnectionState,
    highContrast: Boolean = false,
    reducedStimuli: Boolean = false,
    sizeDp: Dp = 280.dp,
    modifier: Modifier = Modifier
) {
    // Cores dinâmicas para o display do smartwatch
    val bezelColor = if (highContrast) Color(0xFF1E293B) else Color(0xFF0F172A)
    val displayBg = if (highContrast) HighContrastBlack else Color(0xFF07111E)
    val activeAccentColor = if (highContrast) {
        HighContrastYellow
    } else {
        currentCommand?.colorHex?.let { Color(it) } ?: CyanAccent
    }

    val displaySymbol = currentCommand?.symbol ?: currentExercise?.visualCueSymbol ?: "🏊"
    val displayTitle = currentCommand?.title ?: currentExercise?.title ?: "Aguardando Treino"
    val displaySubtext = currentCommand?.subtext ?: currentExercise?.instructions ?: "Sincronizado com o Professor"

    val vibrationPatternDots = when (currentCommand?.signalType ?: currentExercise?.signalType) {
        SignalType.ATENCAO -> "• (Curta)"
        SignalType.TROCA -> "•  • (2 Curtas)"
        SignalType.VIRADA -> "━━━━ (Longa e Firme)"
        SignalType.RESPIRAR -> "• ━ • (Rítmica)"
        SignalType.PAUSA -> "•   • (Pausa Dupla)"
        SignalType.FINALIZAR -> "•  •  ━━━━ (Final)"
        SignalType.INCENTIVO -> "• • • ⚡ (Incentivo)"
        null -> "• • •"
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .shadow(16.dp, CircleShape, spotColor = activeAccentColor.copy(alpha = 0.5f))
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    listOf(
                        Color(0xFF334155),
                        Color(0xFF64748B),
                        Color(0xFF1E293B),
                        Color(0xFF334155)
                    )
                )
            )
            .padding(10.dp) // Aro metálico / Bezel
            .clip(CircleShape)
            .background(bezelColor)
            .border(2.dp, activeAccentColor.copy(alpha = if (isVibrating) 0.9f else 0.35f), CircleShape)
            .testTag("smartwatch_display_container"),
        contentAlignment = Alignment.Center
    ) {
        // Ondas visuais de vibração em volta do display
        AnimatedVibrationRipple(
            isVibrating = isVibrating,
            modifier = Modifier.fillMaxSize(),
            color = activeAccentColor
        )

        // Conteúdo Interno da Tela do Smartwatch
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(displayBg)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Topo do Relógio: Status de Conexão, Aluno e Bloqueio Aquático
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de bateria / sinal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = if (watchConnectionState.isConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                        contentDescription = null,
                        tint = if (watchConnectionState.isConnected) GreenLight else OrangeLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${watchConnectionState.batteryPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }

                // Nome do Aluno
                Text(
                    text = studentName.split(" ").firstOrNull() ?: "Aluno",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanLight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Bloqueio de Água
                Icon(
                    imageVector = if (watchConnectionState.isWaterLocked) Icons.Default.WaterDrop else Icons.Default.Waves,
                    contentDescription = "Bloqueio Aquático",
                    tint = if (watchConnectionState.isWaterLocked) CyanAccent else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
            }

            // Centro: Pictograma Gigante e Comando Atual
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Símbolo / Pictograma com transição animada
                AnimatedContent(
                    targetState = displaySymbol,
                    transitionSpec = {
                        scaleIn(tween(250)) + fadeIn() togetherWith scaleOut(tween(200)) + fadeOut()
                    },
                    label = "WatchSymbol"
                ) { symbol ->
                    Surface(
                        shape = CircleShape,
                        color = activeAccentColor.copy(alpha = 0.18f),
                        border = BorderStroke(
                            2.dp,
                            if (isVibrating) activeAccentColor else activeAccentColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(if (sizeDp > 260.dp) 72.dp else 56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = symbol,
                                fontSize = if (sizeDp > 260.dp) 38.sp else 28.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Título do comando em alto destaque
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (sizeDp > 260.dp) 17.sp else 14.sp,
                        color = if (highContrast) HighContrastYellow else Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                // Representação visual do padrão de vibração
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = activeAccentColor.copy(alpha = 0.22f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = activeAccentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = vibrationPatternDots,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (highContrast) HighContrastYellow else Color.White
                            )
                        )
                    }
                }
            }

            // Rodapé do Relógio: Antecipação / Próxima Atividade
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "A SEGUIR: ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent
                        )
                    )
                    Text(
                        text = nextExercise?.title ?: "Pausa / Conclusão",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
