package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Student

/**
 * Componente unificado para exibição da foto ou avatar do aluno.
 * Suporta fotos locais da galeria, fotos capturadas pela câmera,
 * drawables de recursos do app e fallback gracioso para emojis inclusivos.
 */
@Composable
fun StudentAvatar(
    student: Student,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    borderWidth: Dp = 2.dp,
    showEmojiBadge: Boolean = true
) {
    StudentAvatar(
        name = student.name,
        avatarEmoji = student.avatarEmoji,
        photoUri = student.photoUri,
        modifier = modifier,
        size = size,
        borderColor = borderColor,
        borderWidth = borderWidth,
        showEmojiBadge = showEmojiBadge
    )
}

@Composable
fun StudentAvatar(
    name: String,
    avatarEmoji: String,
    photoUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    borderWidth: Dp = 2.dp,
    showEmojiBadge: Boolean = true
) {
    val context = LocalContext.current
    var isImageError by remember(photoUri) { mutableStateOf(false) }

    val hasValidPhoto = !photoUri.isNullOrBlank() && !isImageError

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(borderWidth, borderColor),
            modifier = Modifier.fillMaxSize()
        ) {
            if (hasValidPhoto) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de $name",
                    contentScale = ContentScale.Crop,
                    onError = { isImageError = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    borderColor.copy(alpha = 0.25f),
                                    borderColor.copy(alpha = 0.08f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarEmoji.isNotBlank()) {
                        Text(
                            text = avatarEmoji,
                            fontSize = (size.value * 0.48f).sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = name,
                            tint = borderColor,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                }
            }
        }

        // Mini badge com emoji na borda inferior direita quando há foto personalizada
        if (hasValidPhoto && showEmojiBadge && avatarEmoji.isNotBlank() && size >= 40.dp) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(size * 0.38f)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = avatarEmoji,
                        fontSize = (size.value * 0.22f).sp
                    )
                }
            }
        }
    }
}
