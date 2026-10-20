package com.example.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WatchConnectionState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatacaoTopAppBar(
    title: String,
    subtitle: String? = null,
    highContrast: Boolean,
    reducedStimuli: Boolean,
    teacherName: String? = null,
    onToggleHighContrast: () -> Unit,
    onToggleReducedStimuli: () -> Unit,
    onOpenSafetyModal: () -> Unit,
    onOpenWatchMode: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "360°",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        },
        actions = {
            // Botão Alto Contraste
            IconButton(
                onClick = onToggleHighContrast,
                modifier = Modifier.testTag("action_high_contrast")
            ) {
                Icon(
                    imageVector = if (highContrast) Icons.Default.Contrast else Icons.Default.InvertColors,
                    contentDescription = if (highContrast) "Desativar alto contraste" else "Ativar alto contraste",
                    tint = if (highContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Botão Baixo Estímulo
            IconButton(
                onClick = onToggleReducedStimuli,
                modifier = Modifier.testTag("action_reduced_stimuli")
            ) {
                Icon(
                    imageVector = if (reducedStimuli) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (reducedStimuli) "Desativar modo baixo estímulo" else "Ativar modo baixo estímulo",
                    tint = if (reducedStimuli) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Botão Diretrizes de Segurança
            IconButton(
                onClick = onOpenSafetyModal,
                modifier = Modifier.testTag("action_safety_info")
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Diretrizes de segurança na piscina",
                    tint = TurquoisePrimary
                )
            }

            // Atalho para Modo Relógio Independente
            if (onOpenWatchMode != null) {
                FilledTonalButton(
                    onClick = onOpenWatchMode,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CyanAccent.copy(alpha = 0.2f),
                        contentColor = OceanBluePrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .height(36.dp)
                        .testTag("action_open_standalone_watch")
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Relógio",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Botão Sair da Conta do Professor
            if (onLogout != null) {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("top_bar_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sair da Conta do Professor",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun WatchConnectionBadge(
    connectionState: WatchConnectionState,
    onClickChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        connectionState.isConnected -> GreenSuccess
        connectionState.connectionStatus.contains("Conectando", ignoreCase = true) -> YellowAttention
        else -> OrangeWarning
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = statusColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClickChange)
            .testTag("watch_connection_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Icon(
                imageVector = if (connectionState.isConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = connectionState.connectionStatus,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (connectionState.isConnected) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${connectionState.batteryPercent}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun AnimatedVibrationRipple(
    isVibrating: Boolean,
    modifier: Modifier = Modifier,
    color: Color = CyanAccent
) {
    if (!isVibrating) return

    val infiniteTransition = rememberInfiniteTransition(label = "VibrationRipple")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleAlpha"
    )

    Canvas(modifier = modifier) {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = (size.minDimension / 2f) * scale
        )
    }
}

@Composable
fun SpecificNeedChip(
    text: String,
    modifier: Modifier = Modifier
) {
    val (chipBg, chipFg) = when {
        text.contains("Autismo", ignoreCase = true) || text.contains("TEA", ignoreCase = true) ->
            Pair(PurpleSoft, PurpleDark)
        text.contains("Visual", ignoreCase = true) || text.contains("Visão", ignoreCase = true) ->
            Pair(YellowSoft, Color(0xFFB45309))
        text.contains("Down", ignoreCase = true) ->
            Pair(CyanGaze, OceanBlueDeep)
        text.contains("Cerebral", ignoreCase = true) || text.contains("Física", ignoreCase = true) ->
            Pair(PinkSoft, Color(0xFFBE185D))
        else ->
            Pair(TurquoiseSoft, Color(0xFF0F766E))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chipBg,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = chipFg,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
