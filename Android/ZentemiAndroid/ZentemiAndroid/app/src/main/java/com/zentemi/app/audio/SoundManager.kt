package com.zentemi.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Efectos cortos (acierto, error, selección). Equivalente a SoundManager.swift.
 * Cada efecto usa su propio MediaPlayer y se libera al terminar.
 */
object SoundManager {

    fun play(context: Context, @RawRes resId: Int, volume: Float) {
        if (volume <= 0f) return
        try {
            val player = MediaPlayer.create(context.applicationContext, resId) ?: return
            player.setVolume(volume, volume)
            player.setOnCompletionListener { it.release() }
            player.start()
        } catch (e: Exception) {
            Log.e("SoundManager", "No se pudo reproducir el sonido: ${e.message}")
        }
    }
}

/**
 * Reproductor de frases en Yuhmu (uno a la vez), con estado `isPlaying`
 * para los botones "Escuchar audio". Se detiene solo al salir de la pantalla.
 */
@Stable
class PhrasePlayer(private val context: Context) {

    private var player: MediaPlayer? = null

    var isPlaying by mutableStateOf(false)
        private set

    fun play(@RawRes resId: Int, volume: Float) {
        stop()
        try {
            val newPlayer = MediaPlayer.create(context.applicationContext, resId) ?: return
            newPlayer.setVolume(volume, volume)
            newPlayer.setOnCompletionListener {
                isPlaying = false
                it.release()
                if (player === it) player = null
            }
            player = newPlayer
            isPlaying = true
            newPlayer.start()
        } catch (e: Exception) {
            Log.e("PhrasePlayer", "Error al reproducir audio: ${e.message}")
            isPlaying = false
        }
    }

    fun stop() {
        player?.let {
            runCatching { if (it.isPlaying) it.stop() }
            it.release()
        }
        player = null
        isPlaying = false
    }
}

@Composable
fun rememberPhrasePlayer(): PhrasePlayer {
    val context = LocalContext.current
    val phrasePlayer = remember { PhrasePlayer(context) }
    DisposableEffect(Unit) {
        onDispose { phrasePlayer.stop() }
    }
    return phrasePlayer
}
