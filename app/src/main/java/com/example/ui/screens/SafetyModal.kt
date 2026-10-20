package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.TurquoisePrimary

@Composable
fun SafetyGuidelinesModal(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = OrangeWarning.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = OrangeWarning
                        )
                    }
                }
                Column {
                    Text(
                        text = "Segurança e Uso na Piscina",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Diretrizes e Responsabilidade Pedagógica",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SafetyRuleItem(
                    icon = Icons.Default.Visibility,
                    title = "Supervisão Presencial Ininterrupta",
                    description = "O aplicativo é estritamente uma ferramenta de apoio pedagógico. Os comandos e vibrações jamais substituem o olhar, a presença física e a intervenção direta do professor de Educação Física."
                )

                SafetyRuleItem(
                    icon = Icons.Default.WaterDrop,
                    title = "Especificação Técnica de Relógio Hídrico",
                    description = "Utilize somente smartwatches com certificação à prova d'água homologada (mínimo 5 ATM / 50m ou padrão IP68 náutico). Nunca utilize smartwatches convencionais na piscina."
                )

                SafetyRuleItem(
                    icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                    title = "Atenuação de Sinal de Rádio na Água",
                    description = "A água absorve sinais de rádio (Bluetooth LE 2.4GHz) quando o braço está totalmente submerso. Os comandos devem ser testados na superfície ou sincronizados durante as fases aéreas da braçada."
                )

                SafetyRuleItem(
                    icon = Icons.Default.Hearing,
                    title = "Respeito aos Limites Sensoriais",
                    description = "Para crianças autistas ou com hipersensibilidade tátil, inicie sempre no nível mínimo de vibração e aumente apenas se houver conforto e assentimento da criança."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                modifier = Modifier.testTag("dismiss_safety_modal_button")
            ) {
                Text("Compreendi as Diretrizes", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SafetyRuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OceanBluePrimary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}
