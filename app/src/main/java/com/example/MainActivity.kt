package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SafetyGuidelinesModal
import com.example.ui.screens.StandaloneWatchScreen
import com.example.ui.screens.TeacherDashboardScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.Natacao360Theme
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.NatacaoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NatacaoViewModel = viewModel()
            val highContrast by viewModel.globalHighContrast.collectAsState()
            val currentDestination by viewModel.currentDestination.collectAsState()
            val showSafetyModal by viewModel.showSafetyModal.collectAsState()
            val loginErrorMessage by viewModel.loginErrorMessage.collectAsState()

            Natacao360Theme(highContrast = highContrast) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "AppScreenNavigation"
                    ) { dest ->
                        when (dest) {
                            AppNavDestination.LOGIN -> {
                                LoginScreen(
                                    onLoginSuccess = {
                                        viewModel.navigateTo(AppNavDestination.WELCOME)
                                    },
                                    onOpenWatchApp = {
                                        val intent = Intent(this@MainActivity, WatchActivity::class.java)
                                        startActivity(intent)
                                    },
                                    onLoginSubmit = { email, pass ->
                                        viewModel.login(email, pass)
                                    },
                                    onLoginDemo = {
                                        viewModel.loginAsDemo()
                                    },
                                    highContrast = highContrast,
                                    errorMessage = loginErrorMessage
                                )
                            }
                            AppNavDestination.WELCOME -> {
                                BackHandler {
                                    viewModel.navigateTo(AppNavDestination.LOGIN)
                                }
                                WelcomeScreen(
                                    viewModel = viewModel,
                                    onOpenWatchActivity = {
                                        val intent = Intent(this@MainActivity, WatchActivity::class.java)
                                        startActivity(intent)
                                    }
                                )
                            }
                            AppNavDestination.TEACHER_DASHBOARD -> {
                                val currentTeacherTab by viewModel.currentTeacherTab.collectAsState()
                                BackHandler {
                                    if (currentTeacherTab != com.example.viewmodel.TeacherTab.OVERVIEW) {
                                        viewModel.selectTeacherTab(com.example.viewmodel.TeacherTab.OVERVIEW)
                                    } else {
                                        viewModel.navigateTo(AppNavDestination.WELCOME)
                                    }
                                }
                                TeacherDashboardScreen(
                                    viewModel = viewModel,
                                    onOpenWatchActivity = {
                                        val intent = Intent(this@MainActivity, WatchActivity::class.java)
                                        startActivity(intent)
                                    }
                                )
                            }
                            AppNavDestination.STANDALONE_WATCH -> {
                                BackHandler {
                                    viewModel.navigateTo(AppNavDestination.TEACHER_DASHBOARD)
                                }
                                StandaloneWatchScreen(
                                    viewModel = viewModel,
                                    onOpenTeacherApp = {
                                        viewModel.navigateTo(AppNavDestination.TEACHER_DASHBOARD)
                                    }
                                )
                            }
                        }
                    }

                    if (showSafetyModal) {
                        SafetyGuidelinesModal(
                            onDismiss = { viewModel.setSafetyModalVisible(false) }
                        )
                    }
                }
            }
        }
    }
}
