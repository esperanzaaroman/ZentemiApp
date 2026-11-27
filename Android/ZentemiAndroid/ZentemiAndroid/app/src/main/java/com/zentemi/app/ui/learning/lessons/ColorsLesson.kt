package com.zentemi.app.ui.learning.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.LessonReward
import com.zentemi.app.data.PathLesson
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*

/**
 * Manual para la escritura de los sonidos del yuhmu: pp. 74, 75, 81 y 85.
 * El léxico consultado confirma estos cuatro colores; no se infieren otros.
 */
private enum class LessonPaint(val word: String, val spanish: String, val color: Color) {
    YELLOW("K’áxt’i", "Amarillo", Color(0xFFFFCC00)),
    GREEN("Nk’àngi", "Verde", Color(0xFF34C759)),
    WHITE("T’àxi", "Blanco", Color.White),
    BLACK("Mbòi", "Negro", Color.Black)
}

private val colorsTitles = listOf("Descubre la paleta", "Busca el color", "Pinta la figura", "Completa el patrón", "El taller de Maicito")
private val colorsExplanations = listOf(
    "K’áxt’i: amarillo · Nk’àngi: verde · T’àxi: blanco · Mbòi: negro.",
    "Nk’àngi significa verde.",
    "T’àxi significa blanco: ese era el color del pedido.",
    "El patrón alterna amarillo y negro. Faltaba Mbòi: negro.",
    "¡Terminaste los cuatro pedidos del taller!"
)

/** Lección 4: Colores (ColorsLessonView.swift). */
@Composable
fun ColorsLessonScreen(
    appState: AppState,
    onExit: () -> Unit,
    onFinished: (LessonReward, Int, Double) -> Unit
) {
    if (!appState.isLessonUnlocked(PathLesson.COLORS)) {
        LessonLockedMessage("Completa Números para comenzar Colores.", onExit)
        return
    }
    val sounds = rememberLessonSounds(appState)
    val progress = remember { LessonProgress() }
    var showExit by remember { mutableStateOf(false) }
    val clock = rememberLessonClock(paused = showExit)
    val active = rememberAppActive()

    var discovered by remember { mutableStateOf(setOf<LessonPaint>()) }
    var choice by remember { mutableStateOf<LessonPaint?>(null) }
    var painted by remember { mutableStateOf<LessonPaint?>(null) }
    val step = progress.step
    val enabled = !progress.checked && !showExit && active

    fun finish(correct: Boolean) {
        if (!active || !progress.answer(correct)) return
        if (correct) sounds.good() else sounds.bad()
    }

    LessonFrame(
        lessonTitle = "Colores",
        stepTitle = colorsTitles[step],
        step = step,
        total = 5,
        showExitDialog = showExit,
        onShowExitDialog = { showExit = it },
        onExitConfirmed = onExit,
        exitTitle = "¿Salir de Colores?"
    ) {
        if (progress.teaching) {
            PetView(appState = appState, size = 90.dp)
            LessonText("¡Bienvenido al taller! Descubre cuatro colores, practica con ellos y ayuda a ${appState.petName} a pintar sus pedidos.")
            LessonButton("¡Comenzar!") { progress.teaching = false }
            return@LessonFrame
        }
        when (step) {
            0 -> {
                LessonText("Toca cada mancha para descubrir su nombre en Yuhmu.")
                LessonPaint.entries.forEach { paint ->
                    val known = paint in discovered
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(22.dp), ambientColor = Purple.copy(alpha = 0.15f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .clickable(enabled = enabled) {
                                discovered = discovered + paint
                                sounds.select()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        PaintBlob(Icons.Filled.WaterDrop, paint.color, 44)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (known) paint.word else "Descubrir", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
                            Text(paint.spanish, fontSize = 14.sp, color = Purple)
                        }
                        if (known) Icon(Icons.Filled.CheckCircle, contentDescription = "Descubierto", tint = Purple)
                    }
                }
                Text("${discovered.size} de 4 colores descubiertos", fontSize = 12.sp, color = SubText)
            }
            1 -> {
                LessonText("Busca el objeto de color", bold = true)
                Text(LessonPaint.GREEN.word, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Purple)
                TwoColumnGrid(LessonPaint.entries) { paint, modifier ->
                    LessonCard(modifier = modifier, enabled = enabled, onClick = { choice = paint }) {
                        PaintBlob(Icons.Filled.Eco, paint.color, 50)
                        Text(paint.spanish, color = Purple)
                        SelectionMark(choice == paint)
                    }
                }
            }
            2 -> {
                Text("Pedido: ${LessonPaint.WHITE.word}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
                LessonText("Elige una pintura y toca la nube. Puedes cambiarla antes de verificar.")
                PaintedDrawing(
                    icon = Icons.Filled.Cloud,
                    paint = painted,
                    modifier = Modifier.clickable(enabled = enabled && choice != null) {
                        painted = choice
                        sounds.select()
                    }
                )
                PaintPalette(choice, enabled) { choice = it }
            }
            3 -> {
                LessonText("Amarillo, negro, amarillo… ¿qué color sigue?")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(4) { index ->
                        val paint = if (index == 3) choice else if (index == 1) LessonPaint.BLACK else LessonPaint.YELLOW
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(paint?.color ?: Color.Gray.copy(alpha = 0.3f))
                                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                            )
                            Text(paint?.word ?: "¿…?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        }
                    }
                }
                PaintPalette(choice, enabled) { choice = it }
            }
            else -> ColorsWorkshop(appState, enabled, sounds) { mistakes -> finish(mistakes == 0) }
        }

        val canVerify = when (step) {
            0 -> discovered.size == LessonPaint.entries.size
            1, 3 -> choice != null
            2 -> painted != null
            else -> false
        }
        LessonCheckArea(
            progress = progress,
            explanation = colorsExplanations[step],
            canVerify = canVerify,
            showVerify = step < 4,
            onVerify = {
                finish(
                    when (step) {
                        0 -> discovered.size == 4
                        1 -> choice == LessonPaint.GREEN
                        2 -> painted == LessonPaint.WHITE
                        else -> choice == LessonPaint.BLACK
                    }
                )
            },
            onNext = {
                if (progress.isLastStep) {
                    clock.stop()
                    appState.completeLesson(PathLesson.COLORS, progress.score, clock.elapsedSeconds)?.let {
                        onFinished(it, progress.score, clock.elapsedSeconds)
                    }
                } else {
                    progress.next()
                    choice = null
                    painted = null
                }
            }
        )
    }
}

/** Ícono pintado sobre un círculo gris, para que el blanco también se vea. */
@Composable
private fun PaintBlob(icon: ImageVector, color: Color, size: Int) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.25f))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(size.dp))
    }
}

@Composable
private fun SelectionMark(selected: Boolean) {
    Icon(
        if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
        contentDescription = if (selected) "Seleccionado" else null,
        tint = LessonTeal
    )
}

@Composable
private fun PaintPalette(selection: LessonPaint?, enabled: Boolean, onSelect: (LessonPaint) -> Unit) {
    TwoColumnGrid(LessonPaint.entries) { paint, modifier ->
        LessonCard(
            modifier = modifier.border(
                2.dp,
                if (selection == paint) Purple else Purple.copy(alpha = 0.15f),
                RoundedCornerShape(22.dp)
            ),
            enabled = enabled,
            onClick = { onSelect(paint) }
        ) {
            PaintBlob(Icons.Filled.Brush, paint.color, 38)
            Text(paint.spanish, fontWeight = FontWeight.Bold, color = Purple)
            SelectionMark(selection == paint)
        }
    }
}

@Composable
private fun PaintedDrawing(icon: ImageVector, paint: LessonPaint?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Purple.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFC9D1E3))
            .border(5.dp, Color.White, RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = paint?.let { "Dibujo pintado: ${it.spanish}" } ?: "Dibujo sin pintar",
            tint = paint?.color ?: Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(140.dp)
        )
    }
}

/** Cuatro pedidos de pintura sin límite de tiempo (ColorsWorkshopView). */
@Composable
private fun ColorsWorkshop(appState: AppState, enabled: Boolean, sounds: LessonSounds, onCompleted: (Int) -> Unit) {
    val orders = listOf(LessonPaint.YELLOW, LessonPaint.GREEN, LessonPaint.BLACK, LessonPaint.WHITE)
    val icons = listOf(Icons.Filled.WbSunny, Icons.Filled.Park, Icons.Filled.Pets, Icons.Filled.Cloud)
    var order by remember { mutableIntStateOf(0) }
    var selection by remember { mutableStateOf<LessonPaint?>(null) }
    var painted by remember { mutableStateOf<LessonPaint?>(null) }
    var delivered by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var mistakes by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("Elige una pintura y toca el dibujo para pintarlo.") }

    fun deliver() {
        val current = painted ?: return
        if (delivered) return
        if (current == orders[order]) {
            delivered = true
            feedback = "¡Pedido listo! ${orders[order].word}: ${orders[order].spanish}."
            if (order == orders.lastIndex) {
                finished = true
                onCompleted(mistakes)
            } else {
                sounds.good()
            }
        } else {
            mistakes += 1
            feedback = "El pedido dice ${orders[order].word}, que significa ${orders[order].spanish}. ¡Vuelve a pintarlo!"
            painted = null
            selection = null
            sounds.bad()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PetView(appState = appState, size = 75.dp)
            Text("El taller de ${appState.petName}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
        }
        LessonText("Completa los cuatro pedidos. Sin prisas: no hay límite de tiempo.")
        LessonText("El ejercicio cuenta como acierto si entregas todos los pedidos sin equivocarte.", size = 12, color = SubText)
        LinearProgressIndicator(
            progress = { (if (finished) 4 else order) / 4f },
            modifier = Modifier.fillMaxWidth(),
            color = LessonTeal,
            trackColor = LessonTeal.copy(alpha = 0.2f)
        )
        Text("Pedido ${order + 1} de 4: ${orders[order].word}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
        Box {
            PaintedDrawing(
                icon = icons[order],
                paint = painted,
                modifier = Modifier
                    .scale(if (delivered) 1.05f else 1f)
                    .clickable(enabled = enabled && selection != null && !delivered) {
                        painted = selection
                        sounds.select()
                    }
            )
            if (delivered) {
                Icon(
                    Icons.Filled.Verified, contentDescription = "Pedido entregado", tint = LessonTeal,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        LessonText(feedback, bold = true, color = if (delivered) LessonTeal else Purple)
        if (!delivered) {
            PaintPalette(selection, enabled) { selection = it }
            LessonButton("Entregar dibujo", enabled = enabled && painted != null) { deliver() }
        } else if (!finished) {
            LessonButton("Siguiente pedido") {
                order += 1
                painted = null
                selection = null
                delivered = false
                feedback = "¡Vamos con otro dibujo!"
            }
        }
    }
}
