package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * =============================================================================
 * SISTEMA DE DESIGN - NATAÇÃO 360° (UI THEME MATERIAL 3)
 * =============================================================================
 *
 * Paleta de cores global desenvolvida para atender aos requisitos de:
 * 1. Cores Primárias: Azul, Azul-Turquesa e Ciano (identidade aquática, foco e clareza).
 * 2. Cores Complementares: Roxo, Verde, Amarelo, Rosa e Laranja (para sinalização háptica,
 *    diferenciação visual de alunos atípicos, categorias de treino e metas pedagógicas).
 * 3. Acessibilidade e Alto Contraste (WCAG 2.1 AA+ para alunos com baixa visão e neurodivergência).
 */

// -----------------------------------------------------------------------------
// 1. CORES PRIMÁRIAS (Aquático & Tecnológico)
// -----------------------------------------------------------------------------
val NatacaoBlue = Color(0xFF0284C7)             // Azul Oceano Principal
val NatacaoBlueDark = Color(0xFF0369A1)         // Azul Escuro Profundo
val NatacaoBlueDeep = Color(0xFF0C4A6E)         // Azul Noturno Abissal
val NatacaoBlueLight = Color(0xFF38BDF8)        // Azul Céu Claro
val NatacaoBlueContainer = Color(0xFFE0F2FE)    // Container suave para Azul

val NatacaoTurquoise = Color(0xFF0D9488)        // Azul-Turquesa Principal
val NatacaoTurquoiseDark = Color(0xFF0F766E)    // Turquesa Profundo
val NatacaoTurquoiseLight = Color(0xFF14B8A6)   // Turquesa Vibrante
val NatacaoTurquoiseSoft = Color(0xFFCCFBF1)    // Turquesa Claro / Container

val NatacaoCyan = Color(0xFF06B6D4)             // Ciano Vibrante
val NatacaoCyanBright = Color(0xFF22D3EE)       // Ciano Iluminado
val NatacaoCyanNeon = Color(0xFF00F0FF)         // Ciano Neon Alto Contraste
val NatacaoCyanContainer = Color(0xFFCFFAFE)    // Container Ciano

// -----------------------------------------------------------------------------
// 2. CORES COMPLEMENTARES (Sinais, Alunos, Metas e Categorias)
// -----------------------------------------------------------------------------
val NatacaoPurple = Color(0xFF8B5CF6)           // Roxo Vibrante (Troca / Transição)
val NatacaoPurpleDark = Color(0xFF6D28D9)       // Roxo Profundo
val NatacaoPurpleLight = Color(0xFFA78BFA)      // Roxo Claro
val NatacaoPurpleContainer = Color(0xFFEDE9FE)  // Container Roxo Suave

val NatacaoGreen = Color(0xFF10B981)            // Verde Sucesso (Respiração / Meta Concluída)
val NatacaoGreenDark = Color(0xFF059669)        // Verde Esmeralda
val NatacaoGreenLight = Color(0xFF34D399)       // Verde Menta
val NatacaoGreenContainer = Color(0xFFD1FAE5)   // Container Verde Suave

val NatacaoYellow = Color(0xFFF59E0B)           // Amarelo Atenção (Alerta / Foco)
val NatacaoYellowBright = Color(0xFFFBBF24)     // Amarelo Dourado Brilhante
val NatacaoYellowDark = Color(0xFFD97706)       // Âmbar Escuro
val NatacaoYellowContainer = Color(0xFFFEF3C7)  // Container Amarelo Suave

val NatacaoPink = Color(0xFFEC4899)             // Rosa Incentivo (Elogio / Comemoração)
val NatacaoPinkDark = Color(0xFFDB2777)         // Rosa Magenta
val NatacaoPinkLight = Color(0xFFF472B6)        // Rosa Suave
val NatacaoPinkContainer = Color(0xFFFCE7F3)    // Container Rosa Claro

val NatacaoOrange = Color(0xFFF97316)           // Laranja Aviso (Borda / Virada / Cuidado)
val NatacaoOrangeDark = Color(0xFFEA580C)       // Laranja Queimado
val NatacaoOrangeLight = Color(0xFFFB923C)      // Laranja Coral
val NatacaoOrangeContainer = Color(0xFFFFEDD5)  // Container Laranja Suave

// -----------------------------------------------------------------------------
// 3. CORES DE ALTO CONTRASTE (Acessibilidade Visual Especial)
// -----------------------------------------------------------------------------
val HighContrastYellowTone = Color(0xFFFFE600)
val HighContrastCyanTone = Color(0xFF00F0FF)
val HighContrastBlackTone = Color(0xFF000000)
val HighContrastWhiteTone = Color(0xFFFFFFFF)

// -----------------------------------------------------------------------------
// 4. TOKENS GLOBAIS DE DESIGN (Disponíveis em Todo o Aplicativo)
// -----------------------------------------------------------------------------
@Immutable
data class NatacaoColorTokens(
    // Cores Primárias Obrigatórias
    val azul: Color = NatacaoBlue,
    val azulTurquesa: Color = NatacaoTurquoise,
    val ciano: Color = NatacaoCyan,

    // Cores Complementares Obrigatórias
    val roxo: Color = NatacaoPurple,
    val verde: Color = NatacaoGreen,
    val amarelo: Color = NatacaoYellow,
    val rosa: Color = NatacaoPink,
    val laranja: Color = NatacaoOrange,

    // Variações Suaves para Containers de Cards
    val azulContainer: Color = NatacaoBlueContainer,
    val azulTurquesaContainer: Color = NatacaoTurquoiseSoft,
    val cianoContainer: Color = NatacaoCyanContainer,
    val roxoContainer: Color = NatacaoPurpleContainer,
    val verdeContainer: Color = NatacaoGreenContainer,
    val amareloContainer: Color = NatacaoYellowContainer,
    val rosaContainer: Color = NatacaoPinkContainer,
    val laranjaContainer: Color = NatacaoOrangeContainer,

    // Superfícies e Textos
    val isDark: Boolean = false,
    val highContrastActive: Boolean = false,
    val surfaceHighlight: Color = Color(0xFFF8FAFC),
    val borderSubtle: Color = Color(0xFFE2E8F0)
) {
    // Gradientes Temáticos Reutilizáveis
    val aquaticGradient: Brush
        get() = Brush.horizontalGradient(listOf(azul, azulTurquesa, ciano))

    val neonWaterGradient: Brush
        get() = Brush.verticalGradient(listOf(Color(0xFF0F172A), NatacaoBlueDark, azul, ciano))

    val goalSuccessGradient: Brush
        get() = Brush.horizontalGradient(listOf(azulTurquesa, verde))

    val purpleEnergyGradient: Brush
        get() = Brush.horizontalGradient(listOf(roxo, rosa))

    val alertGradient: Brush
        get() = Brush.horizontalGradient(listOf(amarelo, laranja))
}

// -----------------------------------------------------------------------------
// 5. COMPOSITION LOCAL E EXTENSÕES DO MATERIAL 3
// -----------------------------------------------------------------------------
val LocalNatacaoColors = staticCompositionLocalOf { NatacaoColorTokens() }

val MaterialTheme.natacaoColors: NatacaoColorTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalNatacaoColors.current

// -----------------------------------------------------------------------------
// 6. SCHEMES DO MATERIAL 3 COMPATÍVEIS
// -----------------------------------------------------------------------------
fun createNatacaoLightM3Scheme(): ColorScheme = lightColorScheme(
    primary = NatacaoBlue,
    onPrimary = Color.White,
    primaryContainer = NatacaoBlueContainer,
    onPrimaryContainer = NatacaoBlueDeep,

    secondary = NatacaoTurquoise,
    onSecondary = Color.White,
    secondaryContainer = NatacaoTurquoiseSoft,
    onSecondaryContainer = Color(0xFF134E4A),

    tertiary = NatacaoCyan,
    onTertiary = Color.White,
    tertiaryContainer = NatacaoCyanContainer,
    onTertiaryContainer = Color(0xFF164E63),

    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),

    error = NatacaoOrangeDark,
    onError = Color.White,
    errorContainer = NatacaoOrangeContainer,
    onErrorContainer = Color(0xFF7C2D12)
)

fun createNatacaoDarkM3Scheme(): ColorScheme = darkColorScheme(
    primary = NatacaoCyanBright,
    onPrimary = NatacaoBlueDeep,
    primaryContainer = NatacaoBlueDark,
    onPrimaryContainer = NatacaoCyanContainer,

    secondary = NatacaoTurquoiseLight,
    onSecondary = Color.Black,
    secondaryContainer = NatacaoTurquoiseDark,
    onSecondaryContainer = NatacaoTurquoiseSoft,

    tertiary = NatacaoPurpleLight,
    onTertiary = Color.White,
    tertiaryContainer = NatacaoPurpleDark,
    onTertiaryContainer = NatacaoPurpleContainer,

    background = Color(0xFF0A111E),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131D2F),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),

    error = NatacaoOrangeLight,
    onError = Color.Black,
    errorContainer = Color(0xFF431407),
    onErrorContainer = NatacaoOrangeContainer
)

val HighContrastColorScheme: ColorScheme = darkColorScheme(
    primary = HighContrastYellowTone,
    onPrimary = HighContrastBlackTone,
    primaryContainer = HighContrastBlackTone,
    onPrimaryContainer = HighContrastYellowTone,
    secondary = HighContrastCyanTone,
    onSecondary = HighContrastBlackTone,
    tertiary = Color(0xFFFF5555),
    onTertiary = HighContrastBlackTone,
    background = HighContrastBlackTone,
    onBackground = HighContrastWhiteTone,
    surface = Color(0xFF121212),
    onSurface = HighContrastWhiteTone,
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = HighContrastYellowTone
)

