package com.zentemi.app.ui.learning.lessons

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Manual para la escritura de los sonidos del yuhmu (2024), pp. 74, 79, 81 y 84
 * (igual que FamilyLessonSession.swift). Entradas de vocabulario: no se
 * inventan frases posesivas.
 */
enum class FamilyTerm(val yuhmu: String, val meaning: String, @DrawableRes val image: Int) {
    BROTHER("Dó̱t’e", "Hermano", R.drawable.family_leo),
    SISTER_BY_WOMAN("Zíkü", "Hermana · habla una mujer", R.drawable.family_ana),
    SISTER_BY_MAN("Nkhǘ", "Hermana · habla un hombre", R.drawable.family_luz),
    GRANDMOTHER("Tsǘ", "Abuela", R.drawable.family_grandmother),
    GRANDFATHER("Dä̀ta", "Abuelo", R.drawable.family_grandfather),
    UNCLE("’Wě", "Tío", R.drawable.family_uncle),
    GODMOTHER("Měkhä", "Madrina", R.drawable.family_godmother);

    companion object {
        val grandparents = listOf(GRANDMOTHER, GRANDFATHER)
        val review = listOf(BROTHER, GRANDMOTHER, UNCLE, GODMOTHER)
    }
}

private val familyTitles = listOf(
    "Hermanos y hermanas", "Conoce a los abuelos", "Una visita del tío", "La madrina", "La reunión familiar"
)

private val familyTeaching = listOf(
    "Leo es el hermano de Ana y Luz: hermano se escribe Dó̱t’e. Para hermana se usa Zíkü cuando habla una mujer y Nkhǘ cuando habla un hombre.",
    "¡Llegaron los abuelos! Abuela se escribe Tsǘ y abuelo se escribe Dä̀ta. Toca cada tarjeta para descubrir su palabra.",
    "¡El tío viene de visita! Tío se escribe ’Wě. Observa el signo inicial y la marca sobre la e. Después completarás esta palabra.",
    "También nos acompaña la madrina: Měkhä. Mira el orden de sus letras y las marcas de las vocales. Luego formarás la palabra.",
    "Ayuda a Maicito a preparar la reunión. Recoge cada palabra en Yuhmu y llévala a su traducción. Repasarás hermanos, abuelos, tío y madrina."
)

private val familyQuestions = listOf(
    "Leo es el hermano de Ana. ¿Qué palabra significa hermano?",
    "Une cada palabra con su abuelo o abuela.",
    "Completa la palabra para tío: ’W_",
    "Ordena las letras para escribir madrina.",
    "Recoge y entrega las cuatro palabras."
)

private val familyExplanations = listOf(
    "Dó̱t’e significa hermano. Las otras dos palabras significan hermana, según quién habla.",
    "Tsǘ significa abuela; Dä̀ta significa abuelo.",
    "La vocal es ě. La palabra completa es ’Wě: tío.",
    "El orden correcto es M · ě · k · h · ä: Měkhä, madrina.",
    "Dó̱t’e = hermano; Tsǘ = abuela; ’Wě = tío; Měkhä = madrina."
)

private fun teachingTerms(step: Int): List<FamilyTerm> = when (step) {
    0 -> listOf(FamilyTerm.BROTHER, FamilyTerm.SISTER_BY_WOMAN, FamilyTerm.SISTER_BY_MAN)
    1 -> FamilyTerm.grandparents
    2 -> listOf(FamilyTerm.UNCLE)
    3 -> listOf(FamilyTerm.GODMOTHER)
    else -> FamilyTerm.review
}

private val godmotherLetters = listOf("ä", "k", "M", "h", "ě")

/** Lección 2: Familia (FamilyLessonView.swift). */
@Composable
fun FamilyLessonScreen(
    appState: AppState,
    onExit: () -> Unit,
    onFinished: (LessonReward, Int, Double) -> Unit
) {
    if (!appState.isLessonUnlocked(PathLesson.FAMILY)) {
        LessonLockedMessage("Completa Primeras frases para comenzar Familia.", onExit)
        return
    }
    val sounds = rememberLessonSounds(appState)
    val progress = remember { LessonProgress() }
    var showExit by remember { mutableStateOf(false) }
    val clock = rememberLessonClock(paused = showExit)
    val active = rememberAppActive()

    // Respuestas del ejercicio actual (se reinician en cada paso)
    var choice by remember { mutableStateOf<FamilyTerm?>(null) }
    var selectedLabel by remember { mutableStateOf<FamilyTerm?>(null) }
    var assignments by remember { mutableStateOf<Map<FamilyTerm, FamilyTerm>>(emptyMap()) }
    var missingLetter by remember { mutableStateOf<String?>(null) }
    var orderedLetters by remember { mutableStateOf<List<String>>(emptyList()) }

    val step = progress.step
    val canVerify = !progress.teaching && !progress.checked && when (step) {
        0 -> choice != null
        1 -> assignments.size == 2
        2 -> missingLetter != null
        3 -> orderedLetters.size == godmotherLetters.size
        else -> false
    }

    LessonFrame(
        lessonTitle = "Familia",
        stepTitle = familyTitles[step],
        step = step,
        total = 5,
        showExitDialog = showExit,
        onShowExitDialog = { showExit = it },
        onExitConfirmed = onExit,
        background = listOf(Color(0xFFF8F3FC), LessonTeal.copy(alpha = 0.09f))
    ) {
        if (progress.teaching) {
            key(step) {
                FamilyTeachingCard(appState, step, sounds) { progress.teaching = false }
            }
        } else {
            LessonText(familyQuestions[step], size = 19, bold = true)
            if (step == 4) {
                FamilyReviewGame(appState, paused = showExit || !active, sounds = sounds) { mistakes ->
                    progress.answer(mistakes == 0)
                }
            } else {
                val enabled = !progress.checked && active && !showExit
                when (step) {
                    0 -> {
                        FamilyPortrait("Leo · hermano de Ana", FamilyTerm.BROTHER.image, 180)
                        listOf(FamilyTerm.SISTER_BY_MAN, FamilyTerm.BROTHER, FamilyTerm.SISTER_BY_WOMAN).forEach { term ->
                            ChoiceRow(term.yuhmu, choice == term, enabled = enabled) { choice = term }
                        }
                    }
                    1 -> FamilyMatchingBoard(
                        selectedLabel = selectedLabel,
                        assignments = assignments,
                        enabled = enabled,
                        onSelectLabel = { selectedLabel = it },
                        onAssign = { target ->
                            val label = selectedLabel ?: return@FamilyMatchingBoard
                            assignments = assignments.filterValues { it != label } + (target to label)
                            selectedLabel = null
                            sounds.select()
                        }
                    )
                    2 -> {
                        FamilyPortrait("Una visita del tío", FamilyTerm.UNCLE.image, 170)
                        BigWordBox("’W" + (missingLetter ?: "＿"))
                        LessonText("Mira la marca sobre la vocal y toca la que falta.", size = 15)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("é", "ě", "è").forEach { letter ->
                                ChoiceRow(letter, missingLetter == letter, Modifier.weight(1f), enabled) {
                                    missingLetter = letter
                                }
                            }
                        }
                    }
                    3 -> {
                        FamilyPortrait("La madrina", FamilyTerm.GODMOTHER.image, 170)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(godmotherLetters.size) { index ->
                                Text(
                                    orderedLetters.getOrNull(index) ?: "＿",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .padding(vertical = 14.dp)
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            godmotherLetters.forEach { letter ->
                                val used = letter in orderedLetters
                                Text(
                                    letter,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple.copy(alpha = if (used) 0.25f else 1f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(if (used) 0.dp else 3.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Purple.copy(alpha = if (used) 0.04f else 0.12f))
                                        .clickable(enabled = enabled && !used) {
                                            orderedLetters = orderedLetters + letter
                                            sounds.select()
                                        }
                                        .padding(vertical = 14.dp)
                                )
                            }
                        }
                        TextButton(
                            onClick = { orderedLetters = orderedLetters.dropLast(1) },
                            enabled = enabled && orderedLetters.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = Purple)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quitar última letra", color = Purple, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            LessonCheckArea(
                progress = progress,
                explanation = familyExplanations[step],
                canVerify = canVerify,
                showVerify = step != 4,
                wrongTitle = "Aprendamos de esta respuesta",
                onVerify = {
                    if (!active) return@LessonCheckArea
                    val correct = when (step) {
                        0 -> choice == FamilyTerm.BROTHER
                        1 -> FamilyTerm.grandparents.all { assignments[it] == it }
                        2 -> missingLetter == "ě"
                        else -> orderedLetters.joinToString("") == FamilyTerm.GODMOTHER.yuhmu
                    }
                    if (progress.answer(correct)) if (correct) sounds.good() else sounds.bad()
                },
                onNext = {
                    if (progress.isLastStep) {
                        clock.stop()
                        appState.completeLesson(PathLesson.FAMILY, progress.score, clock.elapsedSeconds)?.let {
                            onFinished(it, progress.score, clock.elapsedSeconds)
                        }
                    } else {
                        progress.next(teachEachStep = true)
                        choice = null
                        selectedLabel = null
                        assignments = emptyMap()
                        missingLetter = null
                        orderedLetters = emptyList()
                    }
                }
            )
        }
    }
}

@Composable
private fun FamilyPortrait(name: String, @DrawableRes image: Int, height: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LessonImage(image, null, height.dp)
        Text(name, fontWeight = FontWeight.Bold, color = Purple, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BigWordBox(text: String) {
    Text(
        text,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = Purple,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(24.dp)
    )
}

@Composable
private fun FamilyTeachingCard(appState: AppState, step: Int, sounds: LessonSounds, onContinue: () -> Unit) {
    val terms = teachingTerms(step)
    var revealed by remember { mutableStateOf(setOf<FamilyTerm>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PetView(appState = appState, size = 75.dp)
            Text(familyTeaching[step], fontSize = 16.sp, color = DarkText, modifier = Modifier.weight(1f))
        }
        Text(
            "Toca a cada personaje para descubrir su palabra.",
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SubText, textAlign = TextAlign.Center
        )
        TwoColumnGrid(terms) { term, modifier ->
            val isRevealed = term in revealed
            LessonCard(
                modifier = modifier.border(2.dp, LessonTeal.copy(alpha = 0.3f), RoundedCornerShape(22.dp)),
                selected = isRevealed,
                onClick = {
                    revealed = revealed + term
                    sounds.select()
                }
            ) {
                FamilyPortrait(term.meaning, term.image, 125)
                Text(
                    if (isRevealed) term.yuhmu else "Toca para descubrir",
                    fontSize = if (isRevealed) 22.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.heightIn(min = 38.dp)
                )
                Icon(
                    if (isRevealed) Icons.Filled.CheckCircle else Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = LessonTeal
                )
            }
        }
        Text("${revealed.size} de ${terms.size} palabras descubiertas", fontSize = 12.sp, color = SubText)
        LessonButton(
            if (step == 4) "¡Vamos a jugar!" else "¡Vamos a practicar!",
            enabled = revealed.size == terms.size,
            onClick = onContinue
        )
    }
}

@Composable
private fun FamilyMatchingBoard(
    selectedLabel: FamilyTerm?,
    assignments: Map<FamilyTerm, FamilyTerm>,
    enabled: Boolean,
    onSelectLabel: (FamilyTerm) -> Unit,
    onAssign: (FamilyTerm) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        LessonText(
            "Toca una palabra y después su personaje. Puedes cambiar las parejas antes de verificar.",
            size = 15, color = SubText
        )
        listOf(FamilyTerm.GRANDFATHER, FamilyTerm.GRANDMOTHER).forEach { label ->
            ChoiceRow(label.yuhmu, selectedLabel == label, enabled = enabled) { onSelectLabel(label) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FamilyTerm.grandparents.forEach { target ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = LessonTeal.copy(alpha = 0.15f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .drawBehind {
                            drawRoundRect(
                                color = LessonTeal.copy(alpha = 0.6f),
                                cornerRadius = CornerRadius(20.dp.toPx()),
                                style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f)))
                            )
                        }
                        .clickable(enabled = enabled && selectedLabel != null) { onAssign(target) }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FamilyPortrait(target.meaning, target.image, 120)
                    Text(
                        assignments[target]?.yuhmu ?: "Coloca aquí",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Purple
                    )
                }
            }
        }
    }
}

// ── Juego de repaso: la reunión familiar (FamilyReviewGame.swift) ──

private enum class ReviewEvent { NONE, PICKUP, CORRECT, WRONG, FINISHED }

@Stable
private class FamilyReviewState {
    val words = FamilyTerm.review
    val targets = listOf(FamilyTerm.UNCLE, FamilyTerm.BROTHER, FamilyTerm.GODMOTHER, FamilyTerm.GRANDMOTHER)
    var position by mutableStateOf(Offset(0.49f, 0.5f))
        private set
    var carried by mutableStateOf<FamilyTerm?>(null)
        private set
    var delivered by mutableStateOf(setOf<FamilyTerm>())
        private set
    var mistakes by mutableIntStateOf(0)
        private set
    var feedback by mutableStateOf("Acércate a una palabra para recogerla.")
        private set
    private var lastTarget: FamilyTerm? = null
    val finished: Boolean get() = delivered.size == words.size

    fun wordPosition(term: FamilyTerm) = Offset(0.19f, 0.14f + words.indexOf(term) * 0.24f)
    fun targetPosition(term: FamilyTerm) = Offset(0.80f, 0.14f + targets.indexOf(term) * 0.24f)

    fun move(direction: Offset, delta: Double): ReviewEvent {
        if (finished) return ReviewEvent.NONE
        val dt = delta.coerceIn(0.0, 0.06).toFloat()
        position = Offset(
            (position.x + direction.x * dt * 0.42f).coerceIn(0.06f, 0.94f),
            (position.y + direction.y * dt * 0.32f).coerceIn(0.06f, 0.94f)
        )
        return interact()
    }

    private fun near(point: Offset) = hypot(position.x - point.x, position.y - point.y) < 0.105f

    private fun interact(): ReviewEvent {
        val holding = carried
        if (holding == null) {
            lastTarget = null
            val term = words.firstOrNull { it !in delivered && near(wordPosition(it)) } ?: return ReviewEvent.NONE
            carried = term
            feedback = "Llevas ${term.yuhmu}. Busca su traducción."
            return ReviewEvent.PICKUP
        }
        val target = targets.firstOrNull { near(targetPosition(it)) }
        if (target == null) {
            lastTarget = null
            return ReviewEvent.NONE
        }
        if (lastTarget == target) return ReviewEvent.NONE
        lastTarget = target
        if (target == holding) {
            delivered = delivered + holding
            carried = null
            feedback = if (finished) "¡La reunión está lista! Entregaste las cuatro palabras."
            else "¡Correcto! ${holding.yuhmu} · ${holding.meaning}"
            return if (finished) ReviewEvent.FINISHED else ReviewEvent.CORRECT
        }
        mistakes += 1
        feedback = "Aquí dice ${target.meaning}. Sigue buscando dónde llevar ${holding.yuhmu}."
        return ReviewEvent.WRONG
    }
}

@Composable
internal fun FamilyReviewGame(
    appState: AppState,
    paused: Boolean,
    sounds: LessonSounds,
    onCompleted: (Int) -> Unit
) {
    val game = remember { FamilyReviewState() }
    var direction by remember { mutableStateOf(Offset.Zero) }
    var instructions by remember { mutableStateOf(true) }
    var successPulse by remember { mutableStateOf(false) }
    var errorPulse by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    fun handle(event: ReviewEvent) {
        when (event) {
            ReviewEvent.NONE -> Unit
            ReviewEvent.PICKUP -> sounds.select()
            ReviewEvent.CORRECT, ReviewEvent.FINISHED -> {
                successPulse = true
                errorPulse = false
                sounds.good()
                if (game.finished && !reported) {
                    reported = true
                    direction = Offset.Zero
                    onCompleted(game.mistakes)
                }
            }
            ReviewEvent.WRONG -> {
                errorPulse = true
                successPulse = false
                sounds.bad(0.65f)
            }
        }
    }

    LaunchedEffect(instructions, paused) {
        if (instructions || paused) {
            direction = Offset.Zero
            return@LaunchedEffect
        }
        var previous = System.nanoTime()
        while (!game.finished) {
            delay(20)
            val now = System.nanoTime()
            handle(game.move(direction, (now - previous) / 1_000_000_000.0))
            previous = now
        }
    }
    LaunchedEffect(game.delivered.size, game.mistakes) {
        if (!successPulse && !errorPulse) return@LaunchedEffect
        delay(850)
        successPulse = false
        errorPulse = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = LessonTeal)
            Spacer(modifier = Modifier.width(6.dp))
            Text("${game.delivered.size} / 4 entregas", fontWeight = FontWeight.Bold, color = LessonTeal)
            Spacer(modifier = Modifier.weight(1f))
            game.carried?.let { Text("Llevas: ${it.yuhmu}", fontWeight = FontWeight.Bold, color = Purple) }
        }

        if (instructions) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PetView(appState = appState, size = 85.dp)
                LessonText("¡Vamos a reunir a la familia!", size = 17, bold = true)
                LessonText("Usa la palanca para mover a ${appState.petName}. Recoge una palabra de la izquierda y llévala al personaje con su traducción, a la derecha.")
                LessonText(
                    "Completa las cuatro entregas. El ejercicio cuenta como acierto si no haces entregas equivocadas.",
                    size = 12, color = SubText
                )
                LessonButton("¡A jugar!") { instructions = false }
            }
        } else {
            FamilyReviewArena(appState, game, successPulse, errorPulse)
            LessonText(
                game.feedback, size = 14, bold = true,
                color = if (errorPulse) LessonOrange else Purple,
                modifier = Modifier.heightIn(min = 48.dp)
            )
            if (!game.finished) {
                MaicitoJoystick(onDirection = { direction = it }, enabled = !paused)
                Text("Recoge → lleva → entrega", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SubText)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = LessonTeal)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("¡Cuatro palabras aprendidas!", fontWeight = FontWeight.Bold, color = LessonTeal)
                }
            }
        }
    }
}

@Composable
private fun FamilyReviewArena(appState: AppState, game: FamilyReviewState, successPulse: Boolean, errorPulse: Boolean) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val floating = rememberInfiniteTransition(label = "float")
    val floatY by floating.animateFloat(
        0f, -4f, infiniteRepeatable(tween(1100, easing = EaseInOut), RepeatMode.Reverse), label = "floatY"
    )

    fun Modifier.at(point: Offset, widthPx: Int, heightPx: Int) = this.offset {
        IntOffset(
            (point.x * size.width - widthPx / 2f).roundToInt(),
            (point.y * size.height - heightPx / 2f).roundToInt()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Brush.verticalGradient(listOf(LessonMint.copy(alpha = 0.25f), SuccessGreen.copy(alpha = 0.16f))))
            .clipToBounds()
            .onSizeChanged { size = it }
    ) {
        if (size == IntSize.Zero) return@Box
        // Camino central
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .width(with(density) { (size.width * 0.36f).toDp() })
                .clip(RoundedCornerShape(40.dp))
                .background(Color.Yellow.copy(alpha = 0.18f))
        )

        game.words.forEach { term ->
            if (term !in game.delivered && game.carried != term) {
                var labelSize by remember { mutableStateOf(IntSize.Zero) }
                Text(
                    term.yuhmu,
                    fontWeight = FontWeight.Bold,
                    color = Purple,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .onSizeChanged { labelSize = it }
                        .at(game.wordPosition(term), labelSize.width, labelSize.height)
                        .offset(y = floatY.dp)
                        .shadow(3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(10.dp)
                )
            }
        }

        val targetWidth = with(density) { (size.width * 0.29f).toDp() }
        game.targets.forEach { term ->
            val done = term in game.delivered
            var cardSize by remember { mutableStateOf(IntSize.Zero) }
            Column(
                modifier = Modifier
                    .onSizeChanged { cardSize = it }
                    .at(game.targetPosition(term), cardSize.width, cardSize.height)
                    .width(targetWidth)
                    .scale(if (done) 1.04f else 1f)
                    .clip(RoundedCornerShape(15.dp))
                    .background(if (done) LessonMint.copy(alpha = 0.7f) else Color.White)
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                LessonImage(term.image, null, 56.dp)
                Text(term.meaning.substringBefore(" ·"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkText)
                if (done) Icon(Icons.Filled.CheckCircle, contentDescription = "Completado", tint = LessonTeal, modifier = Modifier.size(16.dp))
            }
        }

        var petSize by remember { mutableStateOf(IntSize.Zero) }
        Column(
            modifier = Modifier
                .onSizeChanged { petSize = it }
                .at(game.position, petSize.width, petSize.height),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            game.carried?.let {
                Text(
                    it.yuhmu, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Purple,
                    modifier = Modifier.clip(CircleShape).background(Color.White).padding(5.dp)
                )
            }
            PetView(
                appState = appState,
                size = 60.dp,
                modifier = Modifier
                    .scale(if (successPulse) 1.15f else 1f)
                    .rotate(if (errorPulse) -8f else 0f)
            )
        }
    }
}
