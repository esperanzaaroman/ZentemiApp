package com.zentemi.app.ui.learning.lessons

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Colores de las lecciones (iguales a los de iOS: morado, turquesa y naranja). */
val LessonTeal = Color(0xFF30B0C7)
val LessonOrange = Color(0xFFFF9500)
val LessonMint = Color(0xFF00C7BE)

/** Sonidos cortos de las lecciones. */
class LessonSounds(private val context: android.content.Context, private val appState: AppState) {
    fun good() = SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
    fun bad(volumeFactor: Float = 1f) =
        SoundManager.play(context, R.raw.audiomalaseleccion, appState.soundVolume * volumeFactor)
    fun select() = SoundManager.play(context, R.raw.audioseleccion, appState.soundVolume)
}

@Composable
fun rememberLessonSounds(appState: AppState): LessonSounds {
    val context = LocalContext.current
    return remember(appState) { LessonSounds(context, appState) }
}

/** true mientras la app está en primer plano (igual que scenePhase == .active en iOS). */
@Composable
fun rememberAppActive(): Boolean {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state by lifecycle.currentStateAsState()
    return state.isAtLeast(Lifecycle.State.RESUMED)
}

/**
 * Cronómetro de la lección: solo avanza con la app abierta y sin el diálogo
 * de salir, para que el tiempo de los resultados sea el tiempo real de juego.
 */
@Stable
class LessonClock {
    var elapsedSeconds by mutableDoubleStateOf(0.0)
        private set
    var stopped by mutableStateOf(false)
        private set

    fun add(seconds: Double) {
        if (!stopped && seconds > 0) elapsedSeconds += seconds
    }

    fun stop() {
        stopped = true
    }
}

@Composable
fun rememberLessonClock(paused: Boolean): LessonClock {
    val clock = remember { LessonClock() }
    val active = rememberAppActive()
    LaunchedEffect(active, paused, clock.stopped) {
        if (!active || paused || clock.stopped) return@LaunchedEffect
        var last = System.nanoTime()
        while (true) {
            delay(250)
            val now = System.nanoTime()
            clock.add((now - last) / 1_000_000_000.0)
            last = now
        }
    }
    return clock
}

/**
 * Estado común de las lecciones de 5 ejercicios: paso actual, si se está en la
 * explicación, si ya se verificó y cuántos aciertos lleva.
 */
@Stable
class LessonProgress(val total: Int = 5) {
    var step by mutableIntStateOf(0)
        private set
    var teaching by mutableStateOf(true)
    var checked by mutableStateOf(false)
        private set
    var lastCorrect by mutableStateOf(false)
        private set
    var score by mutableIntStateOf(0)
        private set
    val isLastStep: Boolean get() = step == total - 1

    /** Registra la respuesta del ejercicio actual (una sola vez). Devuelve false si ya estaba verificado. */
    fun answer(correct: Boolean): Boolean {
        if (checked) return false
        lastCorrect = correct
        checked = true
        if (correct) score += 1
        return true
    }

    /** Pasa al siguiente ejercicio. [teachEachStep]: muestra la explicación otra vez (Familia). */
    fun next(teachEachStep: Boolean = false) {
        if (!checked || isLastStep) return
        step += 1
        checked = false
        if (teachEachStep) teaching = true
    }
}

/**
 * Marco de una lección: botón de salir (con confirmación), barra de progreso,
 * título y contenido desplazable. El contenido vuelve arriba al cambiar de paso.
 */
@Composable
fun LessonFrame(
    lessonTitle: String,
    stepTitle: String,
    step: Int,
    total: Int,
    showExitDialog: Boolean,
    onShowExitDialog: (Boolean) -> Unit,
    onExitConfirmed: () -> Unit,
    exitTitle: String = "¿Salir de la lección?",
    background: List<Color> = listOf(Purple.copy(alpha = 0.07f), LessonOrange.copy(alpha = 0.09f)),
    content: @Composable ColumnScope.() -> Unit
) {
    val scroll = rememberScrollState()
    LaunchedEffect(step) { scroll.animateScrollTo(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .background(Brush.verticalGradient(background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scroll)
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .widthIn(max = 620.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onShowExitDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Salir de $lessonTitle", tint = Purple)
                }
                Spacer(modifier = Modifier.width(12.dp))
                LinearProgressIndicator(
                    progress = { (step + 1f) / total },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = LessonTeal,
                    trackColor = LessonTeal.copy(alpha = 0.18f),
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("${step + 1}/$total", fontWeight = FontWeight.Bold, color = LessonTeal, fontSize = 16.sp)
            }

            Text(
                text = "$lessonTitle · $stepTitle",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Purple,
                textAlign = TextAlign.Center
            )

            content()

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { onShowExitDialog(false) },
            title = { Text(exitTitle, fontWeight = FontWeight.Bold) },
            text = { Text("Las respuestas de esta partida no se guardarán.") },
            confirmButton = {
                TextButton(onClick = {
                    onShowExitDialog(false)
                    onExitConfirmed()
                }) { Text("Salir sin terminar", color = ErrorRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { onShowExitDialog(false) }) { Text("Continuar", color = Purple) }
            }
        )
    }
}

/** Pantalla cuando se abre una lección que aún no se desbloquea. */
@Composable
fun LessonLockedMessage(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, fontSize = 17.sp, color = DarkText, textAlign = TextAlign.Center)
        ChunkyButton(text = "Volver", onClick = onBack, modifier = Modifier.width(200.dp))
    }
}

/** Botón principal de las lecciones. */
@Composable
fun LessonButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    ChunkyButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        height = 52.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Botones "Verificar" / "Siguiente" / "Ver resultados" y la retroalimentación. */
@Composable
fun LessonCheckArea(
    progress: LessonProgress,
    explanation: String,
    canVerify: Boolean,
    showVerify: Boolean = true,
    correctTitle: String = "¡Muy bien!",
    wrongTitle: String = "Sigamos aprendiendo",
    onVerify: () -> Unit,
    onNext: () -> Unit
) {
    if (progress.checked) {
        AnswerFeedback(progress.lastCorrect, explanation, correctTitle, wrongTitle)
        LessonButton(if (progress.isLastStep) "Ver resultados" else "Siguiente", onClick = onNext)
    } else if (showVerify) {
        LessonButton("Verificar", enabled = canVerify, onClick = onVerify)
    }
}

@Composable
fun AnswerFeedback(
    correct: Boolean,
    explanation: String,
    correctTitle: String = "¡Muy bien!",
    wrongTitle: String = "Sigamos aprendiendo"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (correct) Icons.Filled.CheckCircle else Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = if (correct) LessonTeal else LessonOrange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (correct) correctTitle else wrongTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = if (correct) LessonTeal else LessonOrange
            )
        }
        Text(explanation, fontSize = 16.sp, color = DarkText)
    }
}

/** Opción seleccionable de texto (con palomita o círculo). */
@Composable
fun ChoiceRow(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp), ambientColor = Purple.copy(alpha = 0.12f), spotColor = Purple.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Purple.copy(alpha = 0.12f) else Color.White)
            .border(2.dp, if (selected) Purple else Purple.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Purple, modifier = Modifier.weight(1f))
        Icon(
            if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (selected) "Seleccionado" else null,
            tint = Purple
        )
    }
}

/** Tarjeta blanca con sombra suave, para dibujos y opciones grandes. */
@Composable
fun LessonCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    highlight: Color = LessonMint,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale by animateFloatAsState(if (selected) 1.02f else 1f, spring(), label = "cardScale")
    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Purple.copy(alpha = 0.15f), spotColor = Purple.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) highlight.copy(alpha = 0.2f) else Color.White)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/** Imagen de la lección con esquinas redondeadas. */
@Composable
fun LessonImage(@DrawableRes image: Int, description: String?, height: androidx.compose.ui.unit.Dp) {
    Image(
        painter = painterResource(image),
        contentDescription = description,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .height(height)
            .clip(RoundedCornerShape(18.dp))
    )
}

/** Texto centrado de instrucciones. */
@Composable
fun LessonText(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 16,
    bold: Boolean = false,
    color: Color = DarkText
) {
    Text(
        text = text,
        fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/** Dos columnas de elementos, para cuadrículas pequeñas (sin LazyGrid dentro de un scroll). */
@Composable
fun <T> TwoColumnGrid(
    items: List<T>,
    spacing: androidx.compose.ui.unit.Dp = 16.dp,
    cell: @Composable (T, Modifier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing), modifier = Modifier.fillMaxWidth()) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing), modifier = Modifier.fillMaxWidth()) {
                row.forEach { cell(it, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Palanca para mover a Maicito (MaicitoJoystick.swift). Entrega una dirección
 * de -1 a 1 en cada eje; al soltarla vuelve a cero.
 */
@Composable
fun MaicitoJoystick(
    onDirection: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val size = 110.dp
    val knob = 46.dp
    val density = LocalDensity.current
    val maxDistance = with(density) { ((size - knob) / 2).toPx() }
    val center = with(density) { (size / 2).toPx() }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(enabled) {
        if (!enabled) {
            knobOffset = Offset.Zero
            onDirection(Offset.Zero)
        }
        onDispose { onDirection(Offset.Zero) }
    }

    fun update(position: Offset) {
        val dx = position.x - center
        val dy = position.y - center
        val distance = sqrt(dx * dx + dy * dy)
        val scale = if (distance > maxDistance) maxDistance / distance else 1f
        knobOffset = Offset(dx * scale, dy * scale)
        onDirection(Offset(knobOffset.x / maxDistance, knobOffset.y / maxDistance))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .border(3.dp, Color.White.copy(alpha = 0.65f), CircleShape)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { update(it) },
                    onDrag = { change, _ ->
                        change.consume()
                        update(change.position)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onDirection(Offset.Zero)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onDirection(Offset.Zero)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(knob)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
        )
    }
}
