package com.zentemi.app.ui.learning.primerasfrases

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.audio.rememberPhrasePlayer
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.theme.*
import java.text.Normalizer

/**
 * Ejercicio 2: escuchar «¿Cómo te llamas?» en Yuhmu y escribir la traducción.
 * Un solo intento, igual que iOS.
 */
@Composable
fun WriteTranslationExerciseScreen(
    appState: AppState,
    exerciseNumber: Int,
    totalExercises: Int,
    onClose: () -> Unit,
    onContinue: (answeredCorrectly: Boolean) -> Unit
) {
    val expectedAnswer = "como te llamas"

    var writtenAnswer by remember { mutableStateOf("") }
    var verifiedAnswer by remember { mutableStateOf<Boolean?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val phrasePlayer = rememberPhrasePlayer()
    val shake = rememberShakeState()
    val scope = rememberCoroutineScope()

    val hasText = writtenAnswer.isNotBlank()

    fun verifyOrContinue() {
        if (verifiedAnswer == null) {
            if (!hasText) return
            focusManager.clearFocus()
            val correct = normalizeAnswer(writtenAnswer) == expectedAnswer
            verifiedAnswer = correct
            if (correct) {
                SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
                scope.flashConfetti { showConfetti = it }
            } else {
                SoundManager.play(context, R.raw.audiomalaseleccion, appState.soundVolume)
                shake.shake(scope)
            }
        } else {
            phrasePlayer.stop()
            onContinue(verifiedAnswer == true)
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
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Escucha la frase en Yuhmu y escribe su significado en español.",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    lineHeight = 28.sp
                )

                // Botón de audio
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (phrasePlayer.isPlaying) Purple else Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        phrasePlayer.play(R.raw.comotellamas, appState.soundVolume)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (phrasePlayer.isPlaying) Color.White.copy(alpha = 0.18f) else Purple
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (phrasePlayer.isPlaying) "Reproduciendo..." else "Escuchar audio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (phrasePlayer.isPlaying) Color.White else Purple
                        )
                    }
                }

                // Respuesta escrita
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Escribe la traducción",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )

                    val borderColor = when (verifiedAnswer) {
                        null -> Purple.copy(alpha = 0.25f)
                        true -> Turquoise
                        false -> ErrorRed
                    }

                    TextField(
                        value = writtenAnswer,
                        onValueChange = { writtenAnswer = it },
                        enabled = verifiedAnswer == null,
                        placeholder = { Text("Escribe tu respuesta aquí") },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 17.sp, color = DarkText),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            disabledTextColor = DarkText,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                    )

                    verifiedAnswer?.let { correct ->
                        ResultLabel(
                            success = correct,
                            text = if (correct) "¡Respuesta correcta!" else "Respuesta incorrecta"
                        )
                        if (!correct) {
                            Text(
                                text = "Respuesta correcta: ¿Cómo te llamas?",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            ChunkyButton(
                text = if (verifiedAnswer == null) "Verificar" else "Continuar",
                onClick = { verifyOrContinue() },
                enabled = verifiedAnswer != null || hasText,
                height = 54.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showConfetti) ConfettiOverlay()
    }
}

/**
 * Quita acentos, mayúsculas y signos: "¿Cómo te llamas?" → "como te llamas".
 * Misma regla que normalize() en SecondPhraseExerciseView.swift.
 */
internal fun normalizeAnswer(text: String): String {
    val withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
    return withoutAccents
        .map { if (it.isLetter() || it.isWhitespace()) it else ' ' }
        .joinToString("")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .joinToString(" ")
}
