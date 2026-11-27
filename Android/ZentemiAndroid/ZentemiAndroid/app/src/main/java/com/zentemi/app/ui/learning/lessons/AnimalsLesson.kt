package com.zentemi.app.ui.learning.lessons

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.data.LessonReward
import com.zentemi.app.data.PathLesson
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*
import kotlin.math.hypot
import kotlin.math.roundToInt

/** Manual para la escritura de los sonidos del yuhmu, páginas 74, 76 y 82. */
private enum class LessonAnimal(val word: String, val spanish: String, @DrawableRes val image: Int, val ending: String) {
    DOG("’Yó", "Perro", R.drawable.animal_lesson_dog, "yó"),
    DUCK("Bàdu", "Pato", R.drawable.animal_lesson_duck, "du"),
    RABBIT("Khwá", "Conejo", R.drawable.animal_lesson_rabbit, "wá"),
    BEE("Gáne", "Abeja", R.drawable.animal_lesson_bee, "ne")
}

private val animalsTitles = listOf("Álbum de exploración", "¿Quién se esconde?", "La palabra partida", "Encuentra a los dos", "El sendero de los animales")
private val animalsExplanations = listOf(
    "’Yó: perro · Bàdu: pato · Khwá: conejo · Gáne: abeja.",
    "La silueta era un conejo: Khwá.",
    "Bà + du forman Bàdu: pato.",
    "’Yó es perro y Gáne es abeja.",
    "¡Reuniste a los cuatro animales con Maicito!"
)

/** Lección 5: Animales (AnimalsLessonView.swift). */
@Composable
fun AnimalsLessonScreen(
    appState: AppState,
    onExit: () -> Unit,
    onFinished: (LessonReward, Int, Double) -> Unit
) {
    if (!appState.isLessonUnlocked(PathLesson.ANIMALS)) {
        LessonLockedMessage("Completa Colores para comenzar Animales.", onExit)
        return
    }
    val sounds = rememberLessonSounds(appState)
    val progress = remember { LessonProgress() }
    var showExit by remember { mutableStateOf(false) }
    val clock = rememberLessonClock(paused = showExit)
    val active = rememberAppActive()

    var discovered by remember { mutableStateOf(setOf<LessonAnimal>()) }
    var choice by remember { mutableStateOf<LessonAnimal?>(null) }
    var selectedAnimals by remember { mutableStateOf(setOf<LessonAnimal>()) }
    val step = progress.step
    val enabled = !progress.checked && !showExit && active

    fun finish(correct: Boolean) {
        if (!active || !progress.answer(correct)) return
        if (correct) sounds.good() else sounds.bad()
    }

    LessonFrame(
        lessonTitle = "Animales",
        stepTitle = animalsTitles[step],
        step = step,
        total = 5,
        showExitDialog = showExit,
        onShowExitDialog = { showExit = it },
        onExitConfirmed = onExit,
        exitTitle = "¿Salir de Animales?"
    ) {
        if (progress.teaching) {
            PetView(appState = appState, size = 90.dp)
            LessonText("¡Prepara tu mochila de explorador! Descubre cuatro animales y ayuda a ${appState.petName} a encontrarlos.")
            LessonButton("¡Comenzar!") { progress.teaching = false }
            return@LessonFrame
        }
        when (step) {
            0 -> {
                LessonText("Toca los retratos y completa tu álbum de explorador.")
                TwoColumnGrid(LessonAnimal.entries) { animal, modifier ->
                    val known = animal in discovered
                    LessonCard(modifier = modifier, selected = known, enabled = enabled, onClick = {
                        discovered = discovered + animal
                        sounds.select()
                    }) {
                        LessonImage(animal.image, animal.spanish, 120.dp)
                        Text(if (known) animal.word else "Descubrir", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Purple)
                        Text(animal.spanish, fontSize = 14.sp, color = Purple)
                        Icon(if (known) Icons.Filled.Verified else Icons.Filled.Search, contentDescription = null, tint = Purple)
                    }
                }
                Text("${discovered.size} de 4 animales descubiertos", fontSize = 12.sp, color = SubText)
            }
            1 -> {
                LessonText("Reconoce al animal escondido. ¿Cómo se llama en Yuhmu?")
                RabbitSilhouette()
                listOf(LessonAnimal.BEE, LessonAnimal.RABBIT, LessonAnimal.DOG, LessonAnimal.DUCK).forEach { animal ->
                    ChoiceRow(animal.word, choice == animal, enabled = enabled) { choice = animal }
                }
            }
            2 -> {
                LessonText("¡A esta palabra le falta una pieza! Elige cómo termina.")
                LessonImage(LessonAnimal.DUCK.image, LessonAnimal.DUCK.spanish, 180.dp)
                Text("Bà" + (choice?.ending ?: "＿"), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Purple)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LessonAnimal.RABBIT, LessonAnimal.DUCK, LessonAnimal.BEE).forEach { animal ->
                        ChoiceRow(animal.ending, choice == animal, Modifier.weight(1f), enabled) { choice = animal }
                    }
                }
            }
            3 -> {
                LessonText("Encuentra a estos dos amigos", bold = true)
                Text("’Yó  +  Gáne", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Purple)
                LessonText("Toca dos retratos. Toca de nuevo para quitar una selección.", size = 14)
                TwoColumnGrid(listOf(LessonAnimal.DUCK, LessonAnimal.BEE, LessonAnimal.DOG, LessonAnimal.RABBIT)) { animal, modifier ->
                    val isSelected = animal in selectedAnimals
                    LessonCard(modifier = modifier, selected = isSelected, enabled = enabled, onClick = {
                        selectedAnimals = when {
                            isSelected -> selectedAnimals - animal
                            selectedAnimals.size < 2 -> selectedAnimals + animal
                            else -> selectedAnimals
                        }
                    }) {
                        LessonImage(animal.image, animal.spanish, 120.dp)
                        Icon(
                            if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (isSelected) "Seleccionado" else null,
                            tint = LessonTeal,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            else -> AnimalsTrail(appState, enabled, sounds) { mistakes -> finish(mistakes == 0) }
        }

        val canVerify = when (step) {
            0 -> discovered.size == LessonAnimal.entries.size
            1, 2 -> choice != null
            3 -> selectedAnimals.size == 2
            else -> false
        }
        LessonCheckArea(
            progress = progress,
            explanation = animalsExplanations[step],
            canVerify = canVerify,
            showVerify = step < 4,
            onVerify = {
                finish(
                    when (step) {
                        0 -> discovered.size == 4
                        1 -> choice == LessonAnimal.RABBIT
                        2 -> choice == LessonAnimal.DUCK
                        else -> selectedAnimals == setOf(LessonAnimal.DOG, LessonAnimal.BEE)
                    }
                )
            },
            onNext = {
                if (progress.isLastStep) {
                    clock.stop()
                    appState.completeLesson(PathLesson.ANIMALS, progress.score, clock.elapsedSeconds)?.let {
                        onFinished(it, progress.score, clock.elapsedSeconds)
                    }
                } else {
                    progress.next()
                    choice = null
                    selectedAnimals = emptySet()
                }
            }
        )
    }
}

/** Silueta de conejo dibujada (el retrato tiene fondo, así que no sirve como sombra). */
@Composable
private fun RabbitSilhouette() {
    val color = Purple.copy(alpha = 0.8f)
    Canvas(modifier = Modifier.size(160.dp, 190.dp)) {
        val unit = size.width / 160f
        val cx = size.width / 2
        val cy = size.height / 2
        fun u(v: Float) = v * unit
        drawOval(color, Offset(cx - u(45f), cy + u(35f) - u(50f)), Size(u(90f), u(100f)))
        drawCircle(color, u(39f), Offset(cx, cy - u(15f)))
        rotate(-12f, Offset(cx - u(22f), cy - u(65f))) {
            drawRoundRect(color, Offset(cx - u(33f), cy - u(100f)), Size(u(22f), u(70f)), androidx.compose.ui.geometry.CornerRadius(u(11f)))
        }
        rotate(12f, Offset(cx + u(22f), cy - u(65f))) {
            drawRoundRect(color, Offset(cx + u(11f), cy - u(100f)), Size(u(22f), u(70f)), androidx.compose.ui.geometry.CornerRadius(u(11f)))
        }
        drawCircle(color, u(15f), Offset(cx + u(48f), cy + u(52f)))
        drawOval(color, Offset(cx - u(18f) - u(26f), cy + u(80f) - u(11.5f)), Size(u(52f), u(23f)))
    }
}

/** El sendero: llevar a Maicito al animal del letrero (AnimalsTrailView). */
@Composable
private fun AnimalsTrail(appState: AppState, enabled: Boolean, sounds: LessonSounds, onCompleted: (Int) -> Unit) {
    val trail = listOf(LessonAnimal.DUCK, LessonAnimal.BEE, LessonAnimal.RABBIT, LessonAnimal.DOG)
    val candidates = listOf(
        listOf(LessonAnimal.RABBIT, LessonAnimal.DUCK, LessonAnimal.DOG),
        listOf(LessonAnimal.BEE, LessonAnimal.DUCK, LessonAnimal.RABBIT),
        listOf(LessonAnimal.DOG, LessonAnimal.BEE, LessonAnimal.RABBIT),
        listOf(LessonAnimal.DUCK, LessonAnimal.DOG, LessonAnimal.BEE)
    )
    var round by remember { mutableIntStateOf(0) }
    var gathered by remember { mutableStateOf(listOf<LessonAnimal>()) }
    var mistakes by remember { mutableIntStateOf(0) }
    var arrived by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var chosenIndex by remember { mutableStateOf<Int?>(null) }
    var dragging by remember { mutableStateOf<Offset?>(null) }
    var feedback by remember { mutableStateOf("Arrastra a ${appState.petName} hacia el animal que indica el letrero.") }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    fun dp(v: Int) = with(density) { v.dp.toPx() }

    fun start() = Offset(boardSize.width / 2f, dp(270))
    fun destination(index: Int) = Offset(boardSize.width * (index + 0.5f) / 3f, dp(65))

    fun visit(index: Int) {
        if (arrived || finished) return
        val animal = candidates[round][index]
        if (animal == trail[round]) {
            dragging = null
            chosenIndex = index
            arrived = true
            gathered = gathered + animal
            feedback = "¡Encontraste a ${animal.word}! Significa ${animal.spanish.lowercase()}."
            if (gathered.size == 4) {
                finished = true
                onCompleted(mistakes)
            } else {
                sounds.good()
            }
        } else {
            mistakes += 1
            feedback = "Ese es ${animal.word}. El letrero busca ${trail[round].word}, ${trail[round].spanish.lowercase()}."
            sounds.bad()
        }
    }

    val restingPet = if (arrived && chosenIndex != null) destination(chosenIndex!!) + Offset(0f, dp(60)) else start()
    val animatedPet by animateOffsetAsState(restingPet, spring(), label = "pet")
    val petCenter = dragging ?: animatedPet

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("El sendero de los animales", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
        LessonText("Lee el letrero y lleva a ${appState.petName} hasta su nuevo amigo. También puedes tocar el retrato.")
        Text("Encuentra: ${trail[round].word}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Purple)
        Text("${gathered.size} de 4 amigos reunidos", fontWeight = FontWeight.Bold, color = LessonTeal)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(LessonMint.copy(alpha = 0.4f), SuccessGreen.copy(alpha = 0.13f))))
                .onSizeChanged { boardSize = it }
        ) {
            if (boardSize == IntSize.Zero) return@Box
            Canvas(modifier = Modifier.matchParentSize()) {
                repeat(3) { index ->
                    val path = Path().apply {
                        moveTo(start().x, start().y)
                        quadraticTo(boardSize.width / 2f, dp(160), destination(index).x, destination(index).y)
                    }
                    drawPath(path, LessonOrange.copy(alpha = 0.35f), style = Stroke(dp(22), cap = StrokeCap.Round))
                }
            }
            repeat(3) { index ->
                Icon(
                    Icons.Filled.Pets, contentDescription = null, tint = Color(0xFFA2845E).copy(alpha = 0.4f),
                    modifier = Modifier.offset {
                        IntOffset((boardSize.width * (index + 1) / 4f - dp(12)).roundToInt(), (dp(170) - dp(12)).roundToInt())
                    }
                )
            }
            val portrait = 88
            repeat(3) { index ->
                val center = destination(index)
                Box(
                    modifier = Modifier
                        .offset { IntOffset((center.x - dp(portrait) / 2).roundToInt(), (center.y - dp(portrait) / 2).roundToInt()) }
                        .size(portrait.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .then(
                            if (arrived && chosenIndex == index) Modifier.border(4.dp, LessonTeal, RoundedCornerShape(18.dp))
                            else Modifier
                        )
                        .clickable(enabled = enabled && !arrived) { visit(index) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LessonImage(candidates[round][index].image, candidates[round][index].spanish, 80.dp)
                }
            }
            val petSize = 72
            Box(
                modifier = Modifier
                    .offset { IntOffset((petCenter.x - dp(petSize) / 2).roundToInt(), (petCenter.y - dp(petSize) / 2).roundToInt()) }
                    .pointerInput(enabled, arrived, round, boardSize) {
                        if (!enabled || arrived) return@pointerInput
                        var current = start()
                        detectDragGestures(
                            onDragStart = { current = petCenter },
                            onDrag = { change, amount ->
                                change.consume()
                                current = Offset(
                                    (current.x + amount.x).coerceIn(dp(30), boardSize.width - dp(30)),
                                    (current.y + amount.y).coerceIn(dp(35), dp(285))
                                )
                                dragging = current
                            },
                            onDragEnd = {
                                val end = current
                                val index = (0 until 3).firstOrNull {
                                    val p = destination(it)
                                    hypot(end.x - p.x, end.y - p.y) < dp(48)
                                }
                                dragging = null
                                if (index != null) visit(index)
                            },
                            onDragCancel = { dragging = null }
                        )
                    }
            ) {
                PetView(appState = appState, size = petSize.dp)
            }
        }
        LessonText(feedback, bold = true, color = if (arrived) LessonTeal else Purple)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            gathered.forEach { LessonImage(it.image, it.spanish, 52.dp) }
        }
        if (arrived && !finished) {
            LessonButton("Seguir explorando") {
                round += 1
                arrived = false
                chosenIndex = null
                dragging = null
                feedback = "¡Lee el siguiente letrero!"
            }
        }
        LessonText("Sin límite de tiempo. Reúne a los cuatro sin equivocarte para ganar el acierto.", size = 12, color = SubText)
    }
}
