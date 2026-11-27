package com.zentemi.app.ui.learning.lessons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.LessonReward
import com.zentemi.app.data.PathLesson
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*
import kotlin.math.abs

/** Grafías del documento Zentemi4.pdf, tabla «Números del 1 al 10» (NumbersLessonView.swift). */
private object NumberVocabulary {
    val words = listOf("N’á", "Yóho", "Hyǘ", "Gǒho", "Kú̱t’a")
    val spanish = listOf("Uno", "Dos", "Tres", "Cuatro", "Cinco")
    fun word(value: Int) = words[value - 1]
}

private val numbersTitles = listOf("Toca y cuenta", "¿Cuántos hay?", "Llena la canasta", "El número perdido", "Conecta las cantidades")
private val numbersExplanations = listOf(
    "Contaste cinco objetos: Kú̱t’a significa cinco.",
    "Hay tres flores. Tres se escribe Hyǘ.",
    "Yóho significa dos: la canasta necesita dos objetos.",
    "Después de Hyǘ (tres) viene Gǒho (cuatro).",
    "N’á: uno · Yóho: dos · Hyǘ: tres · Gǒho: cuatro · Kú̱t’a: cinco."
)

/** Lección 3: Números. */
@Composable
fun NumbersLessonScreen(
    appState: AppState,
    onExit: () -> Unit,
    onFinished: (LessonReward, Int, Double) -> Unit
) {
    if (!appState.isLessonUnlocked(PathLesson.NUMBERS)) {
        LessonLockedMessage("Completa Familia para comenzar Números.", onExit)
        return
    }
    val sounds = rememberLessonSounds(appState)
    val progress = remember { LessonProgress() }
    var showExit by remember { mutableStateOf(false) }
    val clock = rememberLessonClock(paused = showExit)
    val active = rememberAppActive()

    var tapped by remember { mutableStateOf(setOf<Int>()) }
    var choice by remember { mutableStateOf<Int?>(null) }
    var basket by remember { mutableIntStateOf(0) }
    val step = progress.step
    val enabled = !progress.checked && !showExit && active

    fun finish(correct: Boolean) {
        if (!active || !progress.answer(correct)) return
        if (correct) sounds.good() else sounds.bad()
    }

    LessonFrame(
        lessonTitle = "Números",
        stepTitle = numbersTitles[step],
        step = step,
        total = 5,
        showExitDialog = showExit,
        onShowExitDialog = { showExit = it },
        onExitConfirmed = onExit,
        exitTitle = "¿Salir de Números?"
    ) {
        if (progress.teaching) {
            NumbersDiscovery(appState, sounds) { progress.teaching = false }
            return@LessonFrame
        }
        when (step) {
            0 -> {
                LessonText("Toca cada hoja una sola vez para contar hasta cinco.")
                (1..5).forEach { value ->
                    val done = value in tapped
                    LessonCard(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled && !done,
                        onClick = {
                            tapped = tapped + value
                            sounds.select()
                        }
                    ) {
                        Icon(
                            if (done) Icons.Filled.CheckCircle else Icons.Filled.Eco,
                            contentDescription = "Hoja $value",
                            tint = if (done) LessonTeal else SuccessGreen,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                LessonText(
                    if (tapped.isEmpty()) "¡Empieza a contar!" else "${tapped.size} · ${NumberVocabulary.word(tapped.size)}",
                    size = 22, bold = true, color = Purple
                )
            }
            1 -> {
                LessonText("Cuenta las flores. ¿Qué palabra corresponde?")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Icon(Icons.Filled.LocalFlorist, contentDescription = null, tint = Color(0xFFFF2D55), modifier = Modifier.size(52.dp))
                    }
                }
                listOf(2, 4, 3).forEach { value ->
                    ChoiceRow(NumberVocabulary.word(value), choice == value, enabled = enabled) { choice = value }
                }
            }
            2 -> {
                LessonText("Pon Yóho objetos en la canasta. Puedes agregar o quitar.")
                Icon(Icons.Filled.ShoppingBasket, contentDescription = "Canasta", tint = LessonOrange, modifier = Modifier.size(75.dp))
                NumberObjects(basket)
                Text("$basket objetos", fontWeight = FontWeight.Bold, color = DarkText)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    ChunkyButton(
                        text = "Quitar",
                        onClick = { basket -= 1 },
                        enabled = enabled && basket > 0,
                        modifier = Modifier.weight(1f),
                        leading = { Icon(Icons.Filled.RemoveCircle, contentDescription = null, tint = Color.White) }
                    )
                    ChunkyButton(
                        text = "Agregar",
                        onClick = { basket += 1 },
                        enabled = enabled && basket < 5,
                        modifier = Modifier.weight(1f),
                        leading = { Icon(Icons.Filled.AddCircle, contentDescription = null, tint = Color.White) }
                    )
                }
            }
            3 -> {
                LessonText("Sigue el orden del uno al cinco. ¿Qué palabra falta?")
                (1..5).forEach { value ->
                    Text(
                        if (value == 4) (choice?.let { NumberVocabulary.word(it) } ?: "¿…?") else NumberVocabulary.word(value),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (value == 4) LessonOrange else Purple,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    )
                }
                listOf(5, 1, 4).forEach { value ->
                    ChoiceRow(NumberVocabulary.word(value), choice == value, enabled = enabled) { choice = value }
                }
            }
            else -> {
                LessonText("Une cada palabra con su cantidad. Desliza desde la palabra hasta las hojas o toca ambos cuadros.")
                LessonText("Completa las cinco conexiones sin equivocarte para ganar el acierto.", size = 12, color = SubText)
                NumbersConnections(enabled = enabled, sounds = sounds) { mistakes -> finish(mistakes == 0) }
            }
        }

        val canVerify = when (step) {
            0 -> tapped.size == 5
            1, 3 -> choice != null
            2 -> true
            else -> false
        }
        LessonCheckArea(
            progress = progress,
            explanation = numbersExplanations[step],
            canVerify = canVerify,
            showVerify = step < 4,
            onVerify = {
                finish(
                    when (step) {
                        0 -> tapped.size == 5
                        1 -> choice == 3
                        2 -> basket == 2
                        else -> choice == 4
                    }
                )
            },
            onNext = {
                if (progress.isLastStep) {
                    clock.stop()
                    appState.completeLesson(PathLesson.NUMBERS, progress.score, clock.elapsedSeconds)?.let {
                        onFinished(it, progress.score, clock.elapsedSeconds)
                    }
                } else {
                    progress.next()
                    choice = null
                }
            }
        )
    }
}

@Composable
private fun NumberObjects(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(count) {
            Icon(Icons.Filled.Eco, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun NumbersDiscovery(appState: AppState, sounds: LessonSounds, onContinue: () -> Unit) {
    var discovered by remember { mutableStateOf(setOf<Int>()) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PetView(appState = appState, size = 90.dp)
        LessonText("¡Contemos juntos! Toca cada cantidad para descubrir cómo se escribe en Yuhmu.")
        (1..5).forEach { value ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = Purple.copy(alpha = 0.15f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable {
                        discovered = discovered + value
                        sounds.select()
                    }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberObjects(value)
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (value in discovered) NumberVocabulary.word(value) else "Descubrir",
                        fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Purple
                    )
                    Text("$value · ${NumberVocabulary.spanish[value - 1]}", fontSize = 12.sp, color = Purple)
                }
            }
        }
        LessonButton("¡A practicar!", enabled = discovered.size == 5, onClick = onContinue)
    }
}

/** Unir palabras con cantidades, tocando o arrastrando (NumbersConnectionsView). */
@Composable
private fun NumbersConnections(enabled: Boolean, sounds: LessonSounds, onCompleted: (Int) -> Unit) {
    val targets = listOf(3, 5, 1, 4, 2)
    var matched by remember { mutableStateOf(setOf<Int>()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var dragPoint by remember { mutableStateOf<Offset?>(null) }
    var mistakes by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("0 de 5 conexiones") }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val rowPx = with(density) { 84.dp.toPx() }

    fun point(row: Int, right: Boolean) = Offset(boardSize.width * (if (right) 0.79f else 0.21f), row * rowPx + rowPx / 2)

    fun connect(word: Int, target: Int) {
        if (word in matched || target in matched) return
        if (word == target) {
            matched = matched + word
            feedback = "¡Bien! ${matched.size} de 5 conexiones"
            sounds.good()
            if (matched.size == 5) onCompleted(mistakes)
        } else {
            mistakes += 1
            feedback = "Cuenta las hojas y vuelve a intentarlo."
            sounds.bad()
        }
        selected = null
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .onSizeChanged { boardSize = it }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                matched.forEach { value ->
                    val row = targets.indexOf(value)
                    drawLine(LessonTeal, point(value - 1, false), point(row, true), 4.dp.toPx(), StrokeCap.Round)
                }
                val from = selected
                val to = dragPoint
                if (from != null && to != null) {
                    drawLine(Purple.copy(alpha = 0.5f), point(from - 1, false), to, 3.dp.toPx(), StrokeCap.Round)
                }
            }
            val boxWidth = with(density) { (boardSize.width * 0.39f).toDp() }
            (1..5).forEach { value ->
                val done = value in matched
                Box(
                    modifier = Modifier
                        .offset(x = with(density) { (boardSize.width * 0.21f).toDp() } - boxWidth / 2, y = (84 * (value - 1) + 11).dp)
                        .size(boxWidth, 62.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (done) LessonMint else Color.White)
                        .border(2.dp, if (selected == value) Purple else LessonTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable(enabled = enabled && !done) { selected = value }
                        .pointerInput(enabled, done, boardSize) {
                            if (!enabled || done) return@pointerInput
                            // Esquina de esta palabra dentro del tablero, para medir el gesto en el mismo espacio
                            val topLeft = Offset(boardSize.width * 0.21f - boxWidth.toPx() / 2, (84 * (value - 1) + 11).dp.toPx())
                            var current = Offset.Zero
                            detectDragGestures(
                                onDragStart = {
                                    selected = value
                                    current = topLeft + it
                                    dragPoint = current
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    current += amount
                                    dragPoint = current
                                },
                                onDragEnd = {
                                    val end = current
                                    dragPoint = null
                                    if (end.x >= boardSize.width * 0.58f && end.x <= boardSize.width) {
                                        val row = (end.y / rowPx).toInt()
                                        if (row in targets.indices && end.y >= 0 && abs(end.y - (row * rowPx + rowPx / 2)) <= 31.dp.toPx()) {
                                            connect(value, targets[row])
                                        }
                                    }
                                },
                                onDragCancel = { dragPoint = null }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(NumberVocabulary.word(value), fontWeight = FontWeight.Bold, color = Purple, fontSize = 17.sp)
                }
            }
            targets.forEachIndexed { row, value ->
                val done = value in matched
                Box(
                    modifier = Modifier
                        .offset(x = with(density) { (boardSize.width * 0.79f).toDp() } - boxWidth / 2, y = (84 * row + 11).dp)
                        .size(boxWidth, 62.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (done) LessonMint else Color.White)
                        .clickable(enabled = enabled && !done) { selected?.let { connect(it, value) } },
                    contentAlignment = Alignment.Center
                ) {
                    NumberObjects(value)
                }
            }
        }
        Text(feedback, fontWeight = FontWeight.Bold, color = Purple, fontSize = 16.sp)
    }
}
