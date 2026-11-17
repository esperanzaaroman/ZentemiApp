package com.zentemi.app.ui.learning.primerasfrases

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.audio.rememberPhrasePlayer
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File

/** Duración mínima de grabación para contar como intento (igual que iOS). */
private const val MIN_RECORDING_MS = 500L

/**
 * Amplitud mínima para considerar que hubo voz. iOS usa > -35 dBFS;
 * 32767 × 10^(-35/20) ≈ 580.
 */
private const val VOICE_AMPLITUDE_THRESHOLD = 580

/**
 * Ejercicio 3: escuchar «¿Cuántos años tienes?» y repetirlo con el micrófono.
 * No evalúa la pronunciación: basta con grabar voz al menos medio segundo.
 * Se puede omitir (no cuenta como acierto).
 */
@Composable
fun PronunciationExerciseScreen(
    appState: AppState,
    exerciseNumber: Int,
    totalExercises: Int,
    onClose: () -> Unit,
    onContinue: (answeredCorrectly: Boolean) -> Unit
) {
    val context = LocalContext.current
    val phrasePlayer = rememberPhrasePlayer()
    val shake = rememberShakeState()
    val scope = rememberCoroutineScope()
    val recorder = remember { VoiceRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var hasRecordedVoice by remember { mutableStateOf(false) }
    var recordedVoiceDetected by remember { mutableStateOf(false) }
    var verifiedAnswer by remember { mutableStateOf<Boolean?>(null) }
    var showSkipConfirmation by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { recorder.release() }
    }

    // Mientras se graba, revisa el volumen cada 100 ms.
    LaunchedEffect(isRecording) {
        while (isRecording) {
            recorder.pollAmplitude()
            delay(100)
        }
    }

    fun startRecording() {
        phrasePlayer.stop()
        if (recorder.start()) {
            hasRecordedVoice = false
            recordedVoiceDetected = false
            isRecording = true
        }
    }

    fun finishRecording() {
        val result = recorder.stop()
        isRecording = false
        hasRecordedVoice = result.durationMs >= MIN_RECORDING_MS
        recordedVoiceDetected = hasRecordedVoice && result.heardVoice
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startRecording() else showSkipConfirmation = true
    }

    fun toggleRecording() {
        if (isRecording) {
            finishRecording()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) startRecording() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun leave(answeredCorrectly: Boolean) {
        recorder.release()
        isRecording = false
        phrasePlayer.stop()
        onContinue(answeredCorrectly)
    }

    fun verifyOrContinue() {
        if (verifiedAnswer == true) {
            leave(true)
            return
        }
        if (recordedVoiceDetected) {
            verifiedAnswer = true
            SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
            scope.flashConfetti { showConfetti = it }
        } else {
            SoundManager.play(context, R.raw.audiomalaseleccion, appState.soundVolume)
            shake.shake(scope)
            hasRecordedVoice = false
            verifiedAnswer = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .offset(x = shake.offset.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ExerciseHeader(exerciseNumber, totalExercises) {
                recorder.release()
                isRecording = false
                phrasePlayer.stop()
                onClose()
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 170.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Pronuncia la frase en Yuhmu",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                // Frase de referencia
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "¿Cuántos años tienes?",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Escucha la frase en Yuhmu e intenta repetirla.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    ListenButton(
                        isPlaying = phrasePlayer.isPlaying,
                        onClick = {
                            if (!isRecording) {
                                phrasePlayer.play(R.raw.cuantosaniostienes, appState.soundVolume)
                            }
                        }
                    )
                }

                // Micrófono
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(10.dp, CircleShape, spotColor = Purple.copy(alpha = 0.2f))
                            .clip(CircleShape)
                            .background(if (isRecording) Color.Red else Purple)
                            .clickable(enabled = verifiedAnswer == null) { toggleRecording() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isRecording) "Detener" else "Grabar",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = when {
                            isRecording -> "Grabando... toca para detener"
                            hasRecordedVoice -> "Grabación lista"
                            else -> "Toca el micrófono para hablar"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRecording) Color.Red else Purple
                    )

                    if (verifiedAnswer == true) {
                        ResultLabel(success = true, text = "¡Muy bien! Has practicado la frase.")
                    }
                }

                MaicitoTipCard(
                    appState = appState,
                    text = "¡Habla fuerte y claro! Acerca un poco el micrófono para que pueda escucharte."
                )
            }
        }

        // Botones inferiores
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChunkyButton(
                text = if (verifiedAnswer == true) "Continuar" else "Verificar",
                onClick = { verifyOrContinue() },
                enabled = hasRecordedVoice && !isRecording,
                height = 54.dp,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { showSkipConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "Omitir",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
        }

        if (showConfetti) ConfettiOverlay()
    }

    if (showSkipConfirmation) {
        AlertDialog(
            onDismissRequest = { showSkipConfirmation = false },
            title = { Text("¿Seguro que quieres omitir?") },
            text = {
                Text(
                    "Si no cuentas con micrófono o no puedes hablar, puedes omitir este ejercicio. No contará como acierto."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSkipConfirmation = false
                    leave(false)
                }) {
                    Text("Sí, omitir ejercicio", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/** Resultado de una grabación. */
private data class RecordingResult(val durationMs: Long, val heardVoice: Boolean)

/** Envoltura de MediaRecorder que solo mide duración y si hubo voz. */
private class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var startedAt = 0L
    private var heardVoice = false

    private val outputFile: File
        get() = File(context.cacheDir, "thirdPhraseRecording.m4a")

    fun start(): Boolean {
        release()
        return try {
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            newRecorder.setAudioSamplingRate(44100)
            newRecorder.setAudioChannels(1)
            newRecorder.setOutputFile(outputFile.absolutePath)
            newRecorder.prepare()
            newRecorder.start()
            recorder = newRecorder
            startedAt = System.currentTimeMillis()
            heardVoice = false
            true
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error al grabar: ${e.message}")
            release()
            false
        }
    }

    fun pollAmplitude() {
        val amplitude = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)
        if (amplitude > VOICE_AMPLITUDE_THRESHOLD) heardVoice = true
    }

    fun stop(): RecordingResult {
        val duration = if (startedAt > 0) System.currentTimeMillis() - startedAt else 0L
        val result = RecordingResult(duration, heardVoice)
        release()
        return result
    }

    fun release() {
        recorder?.let {
            // stop() lanza excepción si se detiene antes de capturar datos.
            runCatching { it.stop() }
            it.release()
        }
        recorder = null
        startedAt = 0L
    }
}
