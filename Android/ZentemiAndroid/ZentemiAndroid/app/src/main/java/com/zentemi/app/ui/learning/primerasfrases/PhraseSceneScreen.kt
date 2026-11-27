package com.zentemi.app.ui.learning.primerasfrases

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.rememberPhrasePlayer
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.DarkText
import com.zentemi.app.ui.theme.Purple

/** Contenido de cada escena de diálogo antes de un ejercicio. */
data class PhraseScene(
    @param:DrawableRes val background: Int,
    val dialogues: List<String>,
    @param:RawRes val audio: Int
)

/** Paso del diálogo en el que aparece el botón "Escuchar audio" (igual que iOS). */
private const val AUDIO_STEP = 3

/**
 * Escena de diálogo entre Maicito y el personaje. Los pasos pares los dice
 * Maicito y los impares el personaje. Equivalente a *PhraseSceneView.swift.
 */
@Composable
fun PhraseSceneScreen(
    appState: AppState,
    scene: PhraseScene,
    onClose: () -> Unit,
    onSceneCompleted: () -> Unit
) {
    var dialogueStep by remember(scene) { mutableIntStateOf(0) }
    val phrasePlayer = rememberPhrasePlayer()
    val isMaicitoSpeaking = dialogueStep % 2 == 0
    val isLastStep = dialogueStep == scene.dialogues.lastIndex

    val breathing = rememberInfiniteTransition(label = "breathing")
    val breath by breathing.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    val maicitoScaleY = 0.985f + 0.06f * breath
    val characterScaleY = 1.045f - 0.06f * breath

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Image(
            painter = painterResource(scene.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            phrasePlayer.stop()
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Purple)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Primeras frases",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Purple,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.92f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Globo de diálogo animado
            AnimatedContent(
                targetState = dialogueStep,
                transitionSpec = {
                    val fromLeft = targetState % 2 == 0
                    (slideInHorizontally(tween(400)) { if (fromLeft) -it / 5 else it / 5 } +
                        fadeIn(tween(400)) +
                        scaleIn(tween(400), initialScale = 0.9f))
                        .togetherWith(fadeOut(tween(200)))
                },
                label = "dialogue"
            ) { step ->
                val maicito = step % 2 == 0
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (!maicito) Spacer(modifier = Modifier.width(45.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.96f))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = scene.dialogues[step],
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (!maicito) Spacer(modifier = Modifier.weight(1f))
                            if (maicito) {
                                PetView(appState = appState, size = 48.dp)
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.personaje00),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            if (maicito) Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    if (maicito) Spacer(modifier = Modifier.width(45.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Personajes
            val characterHeight = minOf(screenHeight * 0.28f, 245.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(characterHeight),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    PetView(
                        appState = appState,
                        size = minOf(screenWidth * 0.42f, 190.dp),
                        modifier = Modifier.graphicsLayer {
                            scaleY = maicitoScaleY
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                    )
                }

                Image(
                    painter = painterResource(R.drawable.personaje00),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleY = characterScaleY
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                )
            }

            if (dialogueStep == AUDIO_STEP) {
                ListenButton(
                    isPlaying = phrasePlayer.isPlaying,
                    onClick = { phrasePlayer.play(scene.audio, appState.soundVolume) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ChunkyButton(
                text = if (isLastStep) "Continuar" else "Siguiente",
                onClick = {
                    phrasePlayer.stop()
                    if (isLastStep) onSceneCompleted() else dialogueStep += 1
                },
                height = 54.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
