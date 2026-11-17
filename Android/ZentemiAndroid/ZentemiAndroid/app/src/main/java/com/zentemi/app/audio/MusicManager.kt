package com.zentemi.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import com.zentemi.app.R

/** Pistas de música de fondo (mismas que iOS). */
enum class MusicTrack(@param:RawRes val resId: Int) {
    INICIO(R.raw.musicainicio),     // onboarding e inicio de sesión
    LOBBY(R.raw.audiolobby),        // pantallas principales
    CAMINO(R.raw.musicacamino)      // camino de aprendizaje
}

/**
 * Música de fondo en bucle. Recuerda en qué parte iba cada pista para
 * continuar desde ahí, como MusicManager.swift. Se pausa al salir de la app.
 */
object MusicManager {

    private var appContext: Context? = null
    private var player: MediaPlayer? = null
    private var currentTrack: MusicTrack? = null
    private var wantedTrack: MusicTrack? = null
    private val savedPositions = mutableMapOf<MusicTrack, Int>()
    private var volume = 0.35f
    private var appInForeground = true

    fun init(context: Context, initialVolume: Float) {
        appContext = context.applicationContext
        volume = initialVolume
    }

    /** Pide una pista (o ninguna con null). Si el volumen es 0 solo la recuerda. */
    fun play(track: MusicTrack?) {
        wantedTrack = track
        if (track == null) {
            pause()
            return
        }
        if (volume <= 0f || !appInForeground) return

        val current = player
        if (currentTrack == track && current != null) {
            current.setVolume(volume, volume)
            if (!current.isPlaying) current.start()
            return
        }

        releaseCurrent()
        val context = appContext ?: return
        try {
            val newPlayer = MediaPlayer.create(context, track.resId) ?: return
            newPlayer.isLooping = true
            newPlayer.setVolume(volume, volume)
            savedPositions[track]?.let { newPlayer.seekTo(it) }
            newPlayer.start()
            player = newPlayer
            currentTrack = track
        } catch (e: Exception) {
            Log.e("MusicManager", "Error al reproducir música: ${e.message}")
        }
    }

    fun pause() {
        val current = player ?: return
        currentTrack?.let { savedPositions[it] = runCatching { current.currentPosition }.getOrDefault(0) }
        if (runCatching { current.isPlaying }.getOrDefault(false)) current.pause()
    }

    /** Volumen 0 pausa; cualquier otro valor reanuda la pista pedida. */
    fun onVolumeChanged(newVolume: Float) {
        volume = newVolume
        if (newVolume <= 0f) {
            pause()
        } else {
            play(wantedTrack)
        }
    }

    fun onAppBackground() {
        appInForeground = false
        pause()
    }

    fun onAppForeground() {
        appInForeground = true
        play(wantedTrack)
    }

    private fun releaseCurrent() {
        val current = player ?: return
        currentTrack?.let { savedPositions[it] = runCatching { current.currentPosition }.getOrDefault(0) }
        runCatching { current.stop() }
        current.release()
        player = null
        currentTrack = null
    }
}
