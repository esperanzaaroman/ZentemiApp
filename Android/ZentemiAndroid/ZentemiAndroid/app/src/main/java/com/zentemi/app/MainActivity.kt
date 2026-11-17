package com.zentemi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zentemi.app.audio.MusicManager
import com.zentemi.app.ui.theme.ZentemiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZentemiTheme {
                ZentemiApp()
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
}
