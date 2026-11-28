package com.zentemi.app.ui.games.levels

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.WordSearchLevel
import com.zentemi.app.data.WordSearchWord
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.learning.lessons.rememberAppActive
import com.zentemi.app.ui.learning.lessons.rememberLessonSounds
import com.zentemi.app.ui.theme.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

/** Sopa de letras: niveles y partidas (WordSearchLevelsView + WordSearchGameView). */
@Composable
fun WordSearchLevelsFlow(appState: AppState, onBack: () -> Unit) {
    var playing by remember { mutableStateOf<WordSearchLevel?>(null) }
    val level = playing
    if (level != null) {
        key(level) { WordSearchGame(appState, level, onExit = { playing = null }) }
        return
    }
    BackHandler(onBack = onBack)
    LevelsScreen(
        appState = appState,
        title = "Sopa de letras",
        subtitle = "Encuentra las palabras en Yuhmu.",
        levels = WordSearchLevel.entries.map {
            LevelCardInfo(
                number = it.number,
                subtitle = "${it.words.size} palabras · tablero de ${it.side} × ${it.side}",
                detail = it.instructions,
                unlocked = appState.wordSearchUnlocked(it),
                completed = appState.wordSearchCompleted(it),
                bestSeconds = appState.wordSearchBestSeconds(it),
                reward = "Premio: +${it.coins} monedas · +%,d puntos".format(it.points)
            )
        },
        onBack = onBack,
        onLevelSelected = { number -> playing = WordSearchLevel.entries[number - 1] }
    )
}

/** Tablero y reglas de una partida (WordSearchSession.swift). */
@Stable
private class WordSearchBoard(val level: WordSearchLevel) {
    val words: List<WordSearchWord> = level.words
    val side = level.side
    val cells: List<String>

    init {
        val fillers = "AEIOUMNLPST".map { it.toString() }
        val letters = MutableList(side * side) { fillers.random() }
        // Recorridos fijos por nivel; relleno nuevo en cada partida.
        words.zip(level.paths).forEach { (word, path) ->
            word.letters.zip(path).forEach { (letter, index) -> letters[index] = letter }
        }
        cells = letters
    }

    var found by mutableStateOf(setOf<String>())
        private set
    var marked by mutableStateOf(setOf<Int>())
        private set
    var selection by mutableStateOf(listOf<Int>())
        private set
    var feedback by mutableStateOf("Desliza desde la primera hasta la última letra.")
        private set
    val finished: Boolean get() = found.size == words.size

    fun select(start: Int, end: Int) {
        if (finished || start !in cells.indices || end !in cells.indices) return
        val rowDelta = end / side - start / side
        val columnDelta = end % side - start % side
        if (!(rowDelta == 0 || columnDelta == 0 ||
                (level == WordSearchLevel.THREE && abs(rowDelta) == abs(columnDelta)))
        ) {
            selection = emptyList()
            return
        }
        val distance = max(abs(rowDelta), abs(columnDelta))
        val step = rowDelta.sign * side + columnDelta.sign
        selection = (0..distance).map { start + it * step }
    }

    fun cancel() {
        selection = emptyList()
    }

    enum class Answer { IGNORED, CORRECT, INCORRECT }

    fun submit(): Answer {
        val current = selection
        selection = emptyList()
        if (finished || current.size <= 1) return Answer.IGNORED
        val letters = current.map { cells[it] }
        val word = words.firstOrNull { it.letters == letters || it.letters == letters.reversed() }
        if (word == null) {
            feedback = "Busca una palabra completa en línea recta. ¡Inténtalo otra vez!"
            return Answer.INCORRECT
        }
        if (word.id in found) {
            feedback = "Ya encontraste esa palabra. ¡Busca las demás!"
            return Answer.IGNORED
        }
        found = found + word.id
        marked = marked + current
        feedback = if (finished) "¡Encontraste las ${words.size} palabras!" else "¡Muy bien! ${word.yuhmu} · ${word.spanish}"
        return Answer.CORRECT
    }
}

private enum class WordSearchPhase { PLAYING, CELEBRATION, RESULTS }

@Composable
internal fun WordSearchGame(appState: AppState, level: WordSearchLevel, onExit: () -> Unit) {
    val sounds = rememberLessonSounds(appState)
    val active = rememberAppActive()
    val board = remember(level) { WordSearchBoard(level) }
    var phase by remember { mutableStateOf(WordSearchPhase.PLAYING) }
    var rewardGiven by remember { mutableStateOf(false) }
    val clock = rememberGameClock(running = !board.finished)

    BackHandler(onBack = onExit)
    LaunchedEffect(active) { if (!active) board.cancel() }

    when (phase) {
        WordSearchPhase.CELEBRATION -> {
            GameCompletionCelebration(appState, "la Sopa de letras") { phase = WordSearchPhase.RESULTS }
            return
        }
        WordSearchPhase.RESULTS -> {
            LevelResultsScreen(
                appState = appState,
                level = level.number,
                elapsedSeconds = clock.doubleValue,
                bestSeconds = appState.wordSearchBestSeconds(level),
                earnedCoins = if (rewardGiven) level.coins else 0,
                earnedPoints = if (rewardGiven) level.points else 0,
                onContinue = onExit
            )
            return
        }
        WordSearchPhase.PLAYING -> Unit
    }

    fun submit() {
        when (board.submit()) {
            WordSearchBoard.Answer.CORRECT -> {
                if (board.finished) rewardGiven = appState.completeWordSearch(level, clock.doubleValue)
                sounds.good()
            }
            WordSearchBoard.Answer.INCORRECT -> sounds.bad()
            WordSearchBoard.Answer.IGNORED -> Unit
        }
    }

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
            LevelGameHeader("Sopa de letras · Nivel ${level.number}", clock.doubleValue, onExit) {
                Text("${board.found.size} de ${board.words.size}", fontWeight = FontWeight.Bold, color = GameTeal)
                Text(level.instructions, fontSize = 12.sp, color = SubText, textAlign = TextAlign.Center)
            }

            WordSearchGrid(board, enabled = active && !board.finished, onSubmit = { submit() })

            Text(board.feedback, fontWeight = FontWeight.Bold, color = Purple, textAlign = TextAlign.Center, fontSize = 15.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                board.words.forEach { word ->
                    val done = word.id in board.found
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (done) "Encontrada" else null,
                            tint = if (done) SuccessGreen else SubText.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (done) "${word.yuhmu} · ${word.spanish}" else word.spanish,
                            fontWeight = FontWeight.SemiBold,
                            color = if (done) GameDoneGreen else DarkText
                        )
                    }
                }
            }

            if (board.finished) {
                ChunkyButton(
                    text = "Siguiente",
                    onClick = {
                        phase = if (level == WordSearchLevel.THREE && rewardGiven) WordSearchPhase.CELEBRATION
                        else WordSearchPhase.RESULTS
                    },
                    height = 52.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WordSearchGrid(board: WordSearchBoard, enabled: Boolean, onSubmit: () -> Unit) {
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val side = board.side

    fun cellAt(point: Offset): Int? {
        if (gridSize.width == 0) return null
        val cell = gridSize.width / side.toFloat()
        val column = (point.x / cell).toInt()
        val row = (point.y / cell).toInt()
        if (column !in 0 until side || row !in 0 until side) return null
        return row * side + column
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .onSizeChanged { gridSize = it }
            .pointerInput(enabled, side) {
                if (!enabled) return@pointerInput
                var start: Int? = null
                detectDragGestures(
                    onDragStart = { point ->
                        start = cellAt(point)
                        start?.let { board.select(it, it) }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val from = start ?: return@detectDragGestures
                        cellAt(change.position)?.let { board.select(from, it) }
                    },
                    onDragEnd = {
                        start = null
                        onSubmit()
                    },
                    onDragCancel = {
                        start = null
                        board.cancel()
                    }
                )
            }
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until side) {
                Row(modifier = Modifier.weight(1f)) {
                    for (column in 0 until side) {
                        val index = row * side + column
                        val inSelection = index in board.selection
                        val isMarked = index in board.marked
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(1.5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        inSelection -> Purple.copy(alpha = 0.35f)
                                        isMarked -> SuccessGreen.copy(alpha = 0.35f)
                                        else -> Purple.copy(alpha = 0.05f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                board.cells[index],
                                fontSize = if (side >= 10) 15.sp else if (side >= 8) 18.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GameDarkPurple
                            )
                        }
                    }
                }
            }
        }
    }
}
