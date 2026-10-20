package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Natacao360Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastColorScheme
        darkTheme -> createNatacaoDarkM3Scheme()
        else -> createNatacaoLightM3Scheme()
    }

    val natacaoColorTokens = NatacaoColorTokens(
        azul = if (highContrast) HighContrastCyanTone else NatacaoBlue,
        azulTurquesa = if (highContrast) HighContrastCyanTone else NatacaoTurquoise,
        ciano = if (highContrast) HighContrastCyanTone else NatacaoCyan,
        roxo = if (highContrast) HighContrastYellowTone else NatacaoPurple,
        verde = if (highContrast) HighContrastYellowTone else NatacaoGreen,
        amarelo = if (highContrast) HighContrastYellowTone else NatacaoYellow,
        rosa = if (highContrast) HighContrastYellowTone else NatacaoPink,
        laranja = if (highContrast) HighContrastYellowTone else NatacaoOrange,
        isDark = darkTheme,
        highContrastActive = highContrast
    )

    CompositionLocalProvider(LocalNatacaoColors provides natacaoColorTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
