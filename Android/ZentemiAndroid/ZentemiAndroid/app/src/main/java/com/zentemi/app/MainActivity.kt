package com.zentemi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.zentemi.app.audio.MusicManager
import com.zentemi.app.data.AuthService
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.theme.ZentemiTheme

class MainActivity : ComponentActivity() {

    private val appState: AppState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Firebase se inicializa solo a partir de google-services.json
        AuthService.init(applicationContext)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZentemiTheme {
                ZentemiApp(appState)
            }
        }
    }

    // La música se pausa al salir de la app y continúa al volver.
    override fun onStart() {
        super.onStart()
        MusicManager.onAppForeground()
    }

    override fun onStop() {
        MusicManager.onAppBackground()
        super.onStop()
    }

    // Igual que iOS al volver a primer plano: reinicia el contador de
    // inactividad de la sesión. (La racha la revisa la navegación.)
    override fun onResume() {
        super.onResume()
        AuthService.registerUserActivity()
    }
}
