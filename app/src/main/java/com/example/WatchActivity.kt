package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.StandaloneWatchScreen
import com.example.ui.theme.Natacao360Theme
import com.example.viewmodel.NatacaoViewModel

/**
 * Activity independente para o Aplicativo do Aluno / Relógio (Smartwatch).
 * Permite que o relógio ou celular do aluno execute a interface simplificada
 * com comandos visuais, auditivos e vibrações 100% offline, conectando-se
 * ao aplicativo do professor via Bluetooth LE / RFCOMM.
 */
class WatchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NatacaoViewModel = viewModel()
            val highContrast by viewModel.globalHighContrast.collectAsState()

            Natacao360Theme(highContrast = highContrast) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StandaloneWatchScreen(
                        viewModel = viewModel,
                        onOpenTeacherApp = {
                            val intent = Intent(this@WatchActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
