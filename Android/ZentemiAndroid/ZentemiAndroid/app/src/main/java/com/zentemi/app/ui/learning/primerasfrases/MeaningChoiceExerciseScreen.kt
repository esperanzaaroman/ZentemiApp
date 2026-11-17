package com.zentemi.app.ui.learning.primerasfrases

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.audio.rememberPhrasePlayer
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.theme.*

/**
 * Ejercicio 4: escuchar «Ayúdame por favor» y elegir su significado.
 * Como en iOS, se puede reintentar hasta acertar.
 */
@Composable
fun MeaningChoiceExerciseScreen(
    appState: AppState,
    exerciseNumber: Int,
    totalExercises: Int,
    onClose: () -> Unit,
    onContinue: (answeredCorrectly: Boolean) -> Unit
) {
    val answers = listOf(
        "Buenos días",
        "¿Cómo te llamas?",
        "Ayúdame por favor",
        "¿Cuántos años tienes?"
    )
    val correctAnswerIndex = 2

    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var verifiedAnswer by remember { mutableStateOf<Boolean?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val phrasePlayer = rememberPhrasePlayer()
    val shake = rememberShakeState()
    val scope = rememberCoroutineScope()

    fun verifyOrContinue() {
        if (verifiedAnswer == true) {
            phrasePlayer.stop()
            // iOS cuenta este ejercicio como acierto al continuar.
            onContinue(true)
            return
        }
        val selected = selectedAnswer ?: return
        if (selected == correctAnswerIndex) {
            verifiedAnswer = true
            SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
            scope.flashConfetti { showConfetti = it }
        } else {
            SoundManager.play(context, R.raw.audiomalaseleccion, appState.soundVolume)
            verifiedAnswer = false
            shake.shake(scope)
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
                phrasePlayer.stop()
                onClose()
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "¿Qué significa esta frase en Yuhmu?",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    lineHeight = 28.sp
                )

                // Tarjeta de audio
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Escucha la frase",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    Text(
                        text = "Selecciona su significado en español.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    ListenButton(
                        isPlaying = phrasePlayer.isPlaying,
                        onClick = { phrasePlayer.play(R.raw.ayudameporfavor, appState.soundVolume) }
                    )
                }

                // Opciones
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    answers.forEachIndexed { index, answer ->
                        val isSelected = selectedAnswer == index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Purple.copy(alpha = 0.10f) else Color.White)
                                .border(
                                    2.dp,
                                    if (isSelected) Purple else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = verifiedAnswer != true) {
                                    SoundManager.play(context, R.raw.audioseleccion, appState.soundVolume)
                                    selectedAnswer = index
                                    verifiedAnswer = null
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Purple.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + index).toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = answer,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkText,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (verifiedAnswer == true) Turquoise else Purple
                                )
                            }
                        }
                    }
                }

                verifiedAnswer?.let { correct ->
                    ResultLabel(
                        success = correct,
                        text = if (correct) "¡Correcto! Has aprendido a pedir ayuda."
                        else "Respuesta incorrecta. Inténtalo de nuevo."
                    )
                }

                MaicitoTipCard(
                    appState = appState,
                    text = "¡Escucha con atención! Recuerda lo que aprendimos durante nuestra conversación."
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            ChunkyButton(
                text = if (verifiedAnswer == true) "Continuar" else "Verificar",
                onClick = { verifyOrContinue() },
                enabled = selectedAnswer != null,
                height = 54.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showConfetti) ConfettiOverlay()
    }
}
