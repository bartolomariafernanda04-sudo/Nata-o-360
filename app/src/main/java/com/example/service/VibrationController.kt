package com.example.service

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.model.SignalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VibrationController(private val context: Context) : TextToSpeech.OnInitListener {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isVibrating = MutableStateFlow(false)
    val isVibrating = _isVibrating.asStateFlow()

    private val _lastSignalTriggered = MutableStateFlow<SignalType?>(null)
    val lastSignalTriggered = _lastSignalTriggered.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VibrationController", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            isTtsReady = true
        }
    }

    fun vibrateSignal(
        signalType: SignalType,
        customDurations: List<Long>? = null,
        intensityFactor: Float = 1.0f,
        speakVoice: Boolean = true
    ) {
        _lastSignalTriggered.value = signalType
        _isVibrating.value = true

        val basePattern = customDurations ?: when (signalType) {
            SignalType.ATENCAO -> listOf(0L, 200L) // 1 curta
            SignalType.TROCA -> listOf(0L, 160L, 100L, 160L) // 2 curtas
            SignalType.VIRADA -> listOf(0L, 650L) // 1 longa e firme
            SignalType.RESPIRAR -> listOf(0L, 250L, 120L, 250L) // Rítmico
            SignalType.PAUSA -> listOf(0L, 200L, 150L, 200L) // Pausa dupla
            SignalType.FINALIZAR -> listOf(0L, 180L, 100L, 180L, 100L, 400L) // Sequência
            SignalType.INCENTIVO -> listOf(0L, 100L, 80L, 150L, 80L, 250L) // Crescente
        }

        val scaledPattern = basePattern.mapIndexed { index, ms ->
            if (index % 2 == 1) (ms * intensityFactor).toLong().coerceAtLeast(50L) else ms
        }.toLongArray()

        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(scaledPattern, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(scaledPattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e("VibrationController", "Hardware vibration failed", e)
        }

        // Voice announcement for accessibility
        if (speakVoice && isTtsReady) {
            val phrase = when (signalType) {
                SignalType.ATENCAO -> "Atenção ao professor"
                SignalType.TROCA -> "Trocar exercício"
                SignalType.VIRADA -> "Virada! Atenção à borda"
                SignalType.RESPIRAR -> "Respirar agora"
                SignalType.PAUSA -> "Pausa para descanso"
                SignalType.FINALIZAR -> "Parabéns, treino concluído!"
                SignalType.INCENTIVO -> "Muito bem, continue assim!"
            }
            tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "SignalTTS_${System.currentTimeMillis()}")
        }

        // Reset vibrating state after pattern duration
        val totalDuration = scaledPattern.sum()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _isVibrating.value = false
        }, totalDuration.coerceAtLeast(300L))
    }

    fun stop() {
        try {
            vibrator?.cancel()
            tts?.stop()
            _isVibrating.value = false
        } catch (e: Exception) {
            Log.e("VibrationController", "Error stopping vibrator", e)
        }
    }

    fun release() {
        try {
            stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VibrationController", "Error releasing TTS", e)
        }
    }
}
