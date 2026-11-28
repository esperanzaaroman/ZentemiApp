package com.zentemi.app.ui.games.levels

import androidx.activity.compose.BackHandler
import com.zentemi.app.audio.rememberPhrasePlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.MemoryLevel
import com.zentemi.app.data.MemoryWord
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.lessons.rememberAppActive
import com.zentemi.app.ui.learning.lessons.rememberLessonSounds
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay

private data class MemoryCard(val word: MemoryWord, val isPicture: Boolean) {
    val id: String get() = word.id + if (isPicture) "-image" else "-text"
}

/** Memorama: niveles y partidas (MemoryLevelsView + MemoryGameView). */
@Composable
fun MemoryLevelsFlow(appState: AppState, onBack: () -> Unit) {
    var playing by remember { mutableStateOf<MemoryLevel?>(null) }
    val level = playing
    if (level != null) {
        key(level) { MemoryGame(appState, level, onExit = { playing = null }) }
        return
    }
    BackHandler(onBack = onBack)
    LevelsScreen(
        appState = appState,
        title = "Memorama",
        subtitle = "Une cada imagen con su palabra en Yuhmu.",
        levels = MemoryLevel.entries.map {
            LevelCardInfo(
                number = it.number,
                subtitle = "${it.words.size} pares · ${it.theme}",
                detail = "Sin límite de tiempo. ¡Aprende a tu ritmo!",
                unlocked = appState.memoryUnlocked(it),
                completed = appState.memoryCompleted(it),
                bestSeconds = appState.memoryBestSeconds(it),
                reward = "Premio: +${it.coins} monedas · +%,d puntos".format(it.points)
            )
        },
        onBack = onBack,
        onLevelSelected = { number -> playing = MemoryLevel.entries[number - 1] }
    )
}

private enum class MemoryPhase { PLAYING, CELEBRATION, RESULTS }

@Composable
internal fun MemoryGame(appState: AppState, level: MemoryLevel, onExit: () -> Unit) {
    val sounds = rememberLessonSounds(appState)
    val phrasePlayer = rememberPhrasePlayer()
    // Última pareja encontrada: se escucha cómo se dice en Yuhmu.
    var lastMatch by remember { mutableStateOf<MemoryWord?>(null) }
    val active = rememberAppActive()
    val cards = remember(level) {
        level.words.flatMap { listOf(MemoryCard(it, true), MemoryCard(it, false)) }.shuffled()
    }
    var selected by remember { mutableStateOf(listOf<String>()) }
    var matched by remember { mutableStateOf(setOf<String>()) }
    var resolving by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("Encuentra cada imagen y su palabra.") }
    var phase by remember { mutableStateOf(MemoryPhase.PLAYING) }
    var rewardGiven by remember { mutableStateOf(false) }
    val finished = matched.size == level.words.size
    val clock = rememberGameClock(running = !finished)

    BackHandler {
        if (phase == MemoryPhase.PLAYING) onExit() else onExit()
    }

    LaunchedEffect(lastMatch) {
        val audio = lastMatch?.audio ?: return@LaunchedEffect
        delay(500)
        phrasePlayer.play(audio, appState.soundVolume)
    }

    LaunchedEffect(resolving, active) {
        if (!resolving || !active) return@LaunchedEffect
        delay(1500)
        selected = emptyList()
        resolving = false
    }

    fun select(card: MemoryCard) {
        if (finished || resolving || card.word.id in matched || card.id in selected) return
        val newSelection = selected + card.id
        selected = newSelection
        if (newSelection.size < 2) {
            feedback = "Ahora busca su pareja."
            sounds.select()
            return
        }
        val first = cards.first { it.id == newSelection[0] }
        if (first.word.id == card.word.id) {
            matched = matched + card.word.id
            selected = emptyList()
            lastMatch = card.word
            if (matched.size == level.words.size) {
                feedback = "¡Encontraste los ${level.words.size} pares!"
                rewardGiven = appState.completeMemory(level, clock.doubleValue)
            } else {
                feedback = "¡Muy bien! ${card.word.yuhmu} · ${card.word.spanish}"
            }
            sounds.good()
        } else {
            resolving = true
            feedback = "Observa las tarjetas e inténtalo otra vez."
            sounds.bad()
        }
    }

    when (phase) {
        MemoryPhase.CELEBRATION -> {
            GameCompletionCelebration(appState, "el Memorama") { phase = MemoryPhase.RESULTS }
            return
        }
        MemoryPhase.RESULTS -> {
            LevelResultsScreen(
                appState = appState,
                level = level.number,
                elapsedSeconds = clock.doubleValue,
                bestSeconds = appState.memoryBestSeconds(level),
                earnedCoins = if (rewardGiven) level.coins else 0,
                earnedPoints = if (rewardGiven) level.points else 0,
                onContinue = onExit
            )
            return
        }
        MemoryPhase.PLAYING -> Unit
    }

    val columns = if (cards.size <= 10) 3 else 4
    GamesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LevelGameHeader("Memorama · Nivel ${level.number}", clock.doubleValue, onExit) {
                Text("${matched.size}/${level.words.size} pares", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GameTeal)
            }
            Text("Une la imagen con su palabra", fontWeight = FontWeight.Bold, color = GameDarkPurple, fontSize = 17.sp)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                cards.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { card ->
                            val faceUp = card.word.id in matched || card.id in selected
                            MemoryCardView(
                                card = card,
                                faceUp = faceUp,
                                matched = card.word.id in matched,
                                modifier = Modifier.weight(1f),
                                onClick = { if (active) select(card) }
                            )
                        }
                        repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
            Text(feedback, fontWeight = FontWeight.Bold, color = Purple, textAlign = TextAlign.Center, fontSize = 15.sp)
            if (finished) {
                com.zentemi.app.ui.components.ChunkyButton(
                    text = "Siguiente",
                    onClick = {
                        phase = if (level == MemoryLevel.THREE && rewardGiven) MemoryPhase.CELEBRATION else MemoryPhase.RESULTS
                    },
                    height = 52.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MemoryCardView(
    card: MemoryCard,
    faceUp: Boolean,
    matched: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(if (faceUp) 0f else 180f, tween(300), label = "flip")
    Box(
        modifier = modifier
            .aspectRatio(0.78f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (rotation > 90f) Brush.linearGradient(listOf(Purple, PurpleDark))
                else Brush.linearGradient(listOf(Color.White, Color.White))
            )
            .border(3.dp, if (matched) SuccessGreen else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(enabled = !faceUp, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (rotation > 90f) {
            // El reverso está girado 180°; se vuelve a girar para que el signo no salga al revés.
            Icon(
                Icons.Filled.QuestionMark,
                contentDescription = "Tarjeta boca abajo",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(30.dp).graphicsLayer { rotationY = 180f }
            )
        } else if (card.isPicture) {
            Image(
                painter = painterResource(card.word.image),
                contentDescription = card.word.spanish,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(6.dp)
            )
        } else {
            Text(
                card.word.yuhmu,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = GameDarkPurple,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}
