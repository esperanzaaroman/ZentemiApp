package com.zentemi.app.ui.learning.primerasfrases

import androidx.annotation.RawRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.audio.rememberPhrasePlayer
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.ErrorRed
import com.zentemi.app.ui.theme.Purple
import com.zentemi.app.ui.theme.Turquoise
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

/** Velocidad de Maicito en fracciones de pantalla por segundo (igual que iOS). */
private const val MOVEMENT_SPEED = 0.22f

/** Distancia máxima (en fracción de pantalla) para recoger o entregar. */
private const val INTERACTION_DISTANCE = 0.085f

private data class PhrasePair(
    val id: Int,
    val spanish: String,
    @param:RawRes val audio: Int,
    val yuhmuPosition: Offset,
    val spanishPosition: Offset
)

private val phrasePairs = listOf(
    PhrasePair(0, "Buenos días", R.raw.buenosdias, Offset(0.17f, 0.24f), Offset(0.79f, 0.71f)),
    PhrasePair(1, "¿Cómo te llamas?", R.raw.comotellamas, Offset(0.80f, 0.23f), Offset(0.22f, 0.73f)),
    PhrasePair(2, "¿Cuántos años tienes?", R.raw.cuantosaniostienes, Offset(0.19f, 0.48f), Offset(0.79f, 0.45f)),
    PhrasePair(3, "Ayúdame por favor", R.raw.ayudameporfavor, Offset(0.78f, 0.58f), Offset(0.22f, 0.59f))
)

/**
 * Ejercicio 5 "Repaso final": Maicito se mueve con un joystick, recoge
 * cada audio en Yuhmu y lo lleva a su traducción. Equivalente a
 * FifthPhraseExerciseView.swift + MaicitoJoystick.swift.
 */
@Composable
fun PairMatchingGameScreen(
    appState: AppState,
    exerciseNumber: Int,
    totalExercises: Int,
    onClose: () -> Unit,
    onContinue: (answeredCorrectly: Boolean) -> Unit
) {
    val context = LocalContext.current
    val phrasePlayer = rememberPhrasePlayer()
    val scope = rememberCoroutineScope()

    var joystickDirection by remember { mutableStateOf(Offset.Zero) }
    var maicitoPosition by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var carriedPhrase by remember { mutableStateOf<Int?>(null) }
    var completedPairs by remember { mutableStateOf(setOf<Int>()) }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var gameFinished by remember { mutableStateOf(false) }
    var areaSize by remember { mutableStateOf(IntSize.Zero) }
    var feedbackJob by remember { mutableStateOf<Job?>(null) }

    // Ciclo de movimiento: un paso por cuadro.
    LaunchedEffect(Unit) {
        var lastFrame = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val delta = minOf((now - lastFrame) / 1_000_000_000f, 0.05f)
                lastFrame = now
                val size = areaSize
                if (!gameFinished && size.width > 0 && size.height > 0) {
                    val aspectCorrection = size.height.toFloat() / size.width
                    val nextX = maicitoPosition.x +
                        joystickDirection.x * MOVEMENT_SPEED * delta * aspectCorrection
                    val nextY = maicitoPosition.y +
                        joystickDirection.y * MOVEMENT_SPEED * delta
                    maicitoPosition = Offset(
                        nextX.coerceIn(0.10f, 0.90f),
                        nextY.coerceIn(0.13f, 0.84f)
                    )
                }
            }
        }
    }

    fun showFeedback(success: Boolean) {
        feedbackJob?.cancel()
        showSuccess = success
        showError = !success
        feedbackJob = scope.launch {
            delay(1200)
            showSuccess = false
            showError = false
        }
    }

    fun distance(a: Offset, b: Offset) = hypot(a.x - b.x, a.y - b.y)

    fun interact() {
        if (gameFinished) return
        val pending = phrasePairs.filter { it.id !in completedPairs }
        val carried = carriedPhrase

        if (carried != null) {
            val nearest = pending.minByOrNull { distance(maicitoPosition, it.spanishPosition) } ?: return
            if (distance(maicitoPosition, nearest.spanishPosition) >= INTERACTION_DISTANCE) return

            if (nearest.id == carried) {
                completedPairs = completedPairs + carried
                carriedPhrase = null
                SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
                showFeedback(success = true)
                if (completedPairs.size == phrasePairs.size) {
                    joystickDirection = Offset.Zero
                    scope.launch {
                        delay(800)
                        gameFinished = true
                    }
                }
            } else {
                SoundManager.play(context, R.raw.audiomalaseleccion, appState.soundVolume)
                showFeedback(success = false)
            }
        } else {
            val nearest = pending.minByOrNull { distance(maicitoPosition, it.yuhmuPosition) } ?: return
            if (distance(maicitoPosition, nearest.yuhmuPosition) >= INTERACTION_DISTANCE) return
            carriedPhrase = nearest.id
            phrasePlayer.play(nearest.audio, appState.soundVolume)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { areaSize = it }
    ) {
        Image(
            painter = painterResource(R.drawable.fondopf_05),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Frases y traducciones
        phrasePairs.forEach { phrase ->
            if (phrase.id !in completedPairs) {
                if (carriedPhrase != phrase.id) {
                    PhraseLabel(
                        title = "Yuhmu ${phrase.id + 1}",
                        subtitle = "Recoger",
                        color = Purple,
                        modifier = Modifier.centeredAt(phrase.yuhmuPosition)
                    )
                }
                PhraseLabel(
                    title = phrase.spanish,
                    subtitle = "Traducción",
                    color = Turquoise,
                    modifier = Modifier.centeredAt(phrase.spanishPosition)
                )
            } else {
                PhraseLabel(
                    title = phrase.spanish,
                    subtitle = "✓ Completado",
                    color = Color(0xFF34C759),
                    modifier = Modifier.centeredAt(phrase.spanishPosition)
                )
            }
        }

        // Maicito
        Column(
            modifier = Modifier.centeredAt(maicitoPosition),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            carriedPhrase?.let {
                Text(
                    text = "Yuhmu ${it + 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Purple)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
            PetView(appState = appState, size = 65.dp)
        }

        // Encabezado y controles
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Repaso final", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Purple)
                    Text(
                        "${completedPairs.size} / ${phrasePairs.size} parejas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Purple
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "$exerciseNumber/$totalExercises",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                MaicitoJoystick(onDirectionChange = { joystickDirection = it })

                Spacer(modifier = Modifier.weight(1f))

                // Ancho fijo (el del botón) para que el aviso no mueva el botón.
                Column(
                    modifier = Modifier.width(70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Píldora blanca para que el texto se lea sobre el fondo.
                    // Se superpone arriba del botón para no moverlo de lugar.
                    if (showError || showSuccess) {
                        Text(
                            text = if (showSuccess) "¡Pareja correcta!" else "¡Intenta otra traducción!",
                            color = if (showSuccess) Color(0xFF1E9E47) else ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            modifier = Modifier
                                .wrapContentWidth(align = Alignment.End, unbounded = true)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.95f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Purple)
                            .clickable { interact() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.TouchApp,
                            contentDescription = "Interactuar",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = "Interactuar",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = Shadow(Color.Black, blurRadius = 6f)
                        )
                    )
                }
            }
        }

        // Final del juego
        if (gameFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color.White)
                        .padding(25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PetView(appState = appState, size = 110.dp)
                    Text("¡Repaso completado!", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Purple)
                    Text(
                        "¡Encontraste las cuatro parejas!",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    ChunkyButton(
                        text = "Ver resultados",
                        onClick = {
                            phrasePlayer.stop()
                            // Igual que iOS: el repaso siempre cuenta como acierto.
                            onContinue(true)
                        },
                        height = 54.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PhraseLabel(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(125.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.95f))
            .padding(horizontal = 9.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

/**
 * Coloca el elemento centrado en una posición relativa (0..1) del contenedor,
 * como `.position()` de SwiftUI.
 */
private fun Modifier.centeredAt(position: Offset) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(
            (position.x * constraints.maxWidth - placeable.width / 2f).roundToInt(),
            (position.y * constraints.maxHeight - placeable.height / 2f).roundToInt()
        )
    }
}

/** Joystick circular; reporta una dirección con largo de 0 a 1. */
@Composable
private fun MaicitoJoystick(onDirectionChange: (Offset) -> Unit) {
    val joystickSize = 110.dp
    val knobSize = 46.dp
    val density = LocalDensity.current
    val maxDistancePx = with(density) { ((joystickSize - knobSize) / 2).toPx() }
    val knobOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(joystickSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .border(3.dp, Color.White.copy(alpha = 0.65f), CircleShape)
            .pointerInput(Unit) {
                val center = Offset(size.width / 2f, size.height / 2f)
                awaitEachGesture {
                    fun update(position: Offset) {
                        val delta = position - center
                        val length = delta.getDistance()
                        val scale = if (length > maxDistancePx) maxDistancePx / length else 1f
                        val clamped = delta * scale
                        scope.launch { knobOffset.snapTo(clamped) }
                        onDirectionChange(clamped / maxDistancePx)
                    }

                    val down = awaitFirstDown()
                    update(down.position)
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            update(change.position)
                            change.consume()
                        }
                    } while (change.pressed)

                    onDirectionChange(Offset.Zero)
                    scope.launch { knobOffset.animateTo(Offset.Zero, tween(150)) }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.value.x.roundToInt(), knobOffset.value.y.roundToInt()) }
                .size(knobSize)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
        )
    }
}
