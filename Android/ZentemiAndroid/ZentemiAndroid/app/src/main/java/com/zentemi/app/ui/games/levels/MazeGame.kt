package com.zentemi.app.ui.games.levels

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.MazeExit
import com.zentemi.app.data.MazeExitSide
import com.zentemi.app.data.MazeLevel
import com.zentemi.app.data.MazeReward
import com.zentemi.app.data.MazeTheme
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.learning.lessons.MaicitoJoystick
import com.zentemi.app.ui.learning.lessons.rememberAppActive
import com.zentemi.app.ui.learning.lessons.rememberLessonSounds
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Laberinto: niveles y partidas (MazeLevelsView + MazeGameView). */
@Composable
fun MazeLevelsFlow(appState: AppState, onBack: () -> Unit) {
    var playing by remember { mutableStateOf<MazeLevel?>(null) }
    val level = playing
    if (level != null) {
        key(level.id) { MazeGame(appState, level, onExit = { playing = null }) }
        return
    }
    BackHandler(onBack = onBack)
    LevelsScreen(
        appState = appState,
        title = "Laberinto",
        subtitle = "Encuentra el camino y la traducción correcta.",
        levels = MazeLevel.ALL.map {
            val index = it.id - 1
            LevelCardInfo(
                number = it.id,
                subtitle = "Dificultad ${it.id} de 5",
                detail = if (it.coins.isEmpty()) "${it.exits.size} salidas" else "${it.exits.size} salidas · ${it.coins.size} monedas por recoger",
                unlocked = appState.mazeUnlocked(it.id),
                completed = appState.mazeCompleted(it.id),
                bestSeconds = appState.mazeBestSeconds(it.id),
                reward = "Premio: +${appState.mazeLevelCoins[index]} monedas · +%,d puntos".format(appState.mazeLevelPoints[index])
            )
        },
        onBack = onBack,
        onLevelSelected = { number -> playing = MazeLevel.ALL[number - 1] },
        footer = "Cada juego tendrá 5 niveles. ¡La dificultad irá aumentando!"
    )
}

/** Movimiento, monedas y salidas de una partida (MazeGameSession.swift). */
@Stable
private class MazeSession(val level: MazeLevel, claimedCoinMask: Int) {
    var position by mutableStateOf(level.start)
        private set
    var remainingCoins by mutableStateOf(level.coins.filter { claimedCoinMask and (1 shl it.id) == 0 })
        private set
    var collectedCoinIds by mutableStateOf(setOf<Int>())
        private set
    var direction = Offset.Zero
    var elapsedSeconds by mutableDoubleStateOf(0.0)
        private set
    var finished by mutableStateOf(false)
        private set
    var wrongExit by mutableStateOf<MazeExit?>(null)
        private set

    /** Devuelve una salida solo en el instante de alcanzarla. */
    fun advance(seconds: Double): MazeExit? {
        if (finished) return null
        elapsedSeconds += max(0.0, seconds)
        if (wrongExit != null) return null

        val length = hypot(direction.x, direction.y)
        if (length <= 0.08f) return null
        val normalization = max(1f, length)
        val distance = seconds.coerceIn(0.0, 0.05).toFloat() * 2.7f
        val dx = direction.x / normalization * distance
        val dy = direction.y / normalization * distance

        // Pasos pequeños y ejes separados: se puede deslizar por una pared, no atravesarla.
        val steps = max(1, ceil(max(abs(dx), abs(dy)) / 0.08f).toInt())
        var point = position
        repeat(steps) {
            val horizontal = Offset(point.x + dx / steps, point.y)
            if (level.canOccupy(horizontal)) point = horizontal
            val vertical = Offset(point.x, point.y + dy / steps)
            if (level.canOccupy(vertical)) point = vertical
        }
        position = point

        remainingCoins.firstOrNull { hypot(point.x - it.position.x, point.y - it.position.y) < 0.42f }?.let { coin ->
            collectedCoinIds = collectedCoinIds + coin.id
            remainingCoins = remainingCoins - coin
        }

        val exit = level.exits.firstOrNull { exit ->
            when (exit.side) {
                MazeExitSide.TOP -> point.y < 0.65f && abs(point.x - exit.center.x) < 0.4f
                MazeExitSide.BOTTOM -> point.y > level.rows - 0.65f && abs(point.x - exit.center.x) < 0.4f
                MazeExitSide.LEFT -> point.x < 0.65f && abs(point.y - exit.center.y) < 0.4f
                MazeExitSide.RIGHT -> point.x > level.columns - 0.65f && abs(point.y - exit.center.y) < 0.4f
            }
        } ?: return null

        direction = Offset.Zero
        if (exit.isCorrect) finished = true else wrongExit = exit
        return exit
    }

    fun continueAfterWrongExit() {
        val exit = wrongExit ?: return
        position = exit.returnPosition
        direction = Offset.Zero
        wrongExit = null
    }
}

private enum class MazePhase { PLAYING, CELEBRATION, RESULTS }

@Composable
internal fun MazeGame(appState: AppState, level: MazeLevel, onExit: () -> Unit) {
    val sounds = rememberLessonSounds(appState)
    val active = rememberAppActive()
    val session = remember(level.id) { MazeSession(level, appState.mazeClaimedCoinMask(level.id)) }
    var reward by remember { mutableStateOf(MazeReward.NONE) }
    var phase by remember { mutableStateOf(MazePhase.PLAYING) }
    var joystickKey by remember { mutableIntStateOf(0) }

    BackHandler(onBack = onExit)

    LaunchedEffect(active, phase) {
        if (!active || phase != MazePhase.PLAYING) {
            session.direction = Offset.Zero
            joystickKey++
            return@LaunchedEffect
        }
        var last = System.nanoTime()
        while (!session.finished) {
            delay(16)
            val now = System.nanoTime()
            val exit = session.advance((now - last) / 1_000_000_000.0)
            last = now
            if (exit != null) {
                joystickKey++
                if (exit.isCorrect) {
                    sounds.good()
                    reward = appState.completeMaze(level.id, session.elapsedSeconds, session.collectedCoinIds, level.coins.size)
                } else {
                    sounds.bad()
                }
            }
        }
    }
    LaunchedEffect(session.collectedCoinIds.size) {
        if (session.collectedCoinIds.isNotEmpty()) sounds.select()
    }

    when (phase) {
        MazePhase.CELEBRATION -> {
            GameCompletionCelebration(appState, "el Laberinto") { phase = MazePhase.RESULTS }
            return
        }
        MazePhase.RESULTS -> {
            LevelResultsScreen(
                appState = appState,
                level = level.id,
                elapsedSeconds = session.elapsedSeconds,
                bestSeconds = appState.mazeBestSeconds(level.id),
                earnedCoins = reward.totalCoins,
                earnedPoints = reward.points,
                extraNote = if (reward.collectedCoins > 0) "Incluye ${reward.collectedCoins} monedas recogidas en el camino." else null,
                onContinue = onExit
            )
            return
        }
        MazePhase.PLAYING -> Unit
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(level.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                LevelGameHeader("Laberinto · Nivel ${level.id}", session.elapsedSeconds, onExit) {
                    Text(level.phrase, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GameDarkPurple)
                    Text("Busca su traducción y llega a esa salida.", fontSize = 12.sp, color = GameDarkPurple)
                    if (level.coins.isNotEmpty()) {
                        Text(
                            "+${session.collectedCoinIds.size * appState.mazeCoinValue} monedas al completar",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GameOrange
                        )
                    }
                }
            }
            MazeBoard(appState, session, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            Row(
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                key(joystickKey) {
                    MaicitoJoystick(
                        onDirection = { session.direction = it },
                        enabled = !session.finished && session.wrongExit == null && active
                    )
                }
                Text(
                    when {
                        level.startsInside -> "Empiezas en el centro. Explora arriba y abajo: hay ${level.exits.size} salidas."
                        level.followsPlayerVertically -> "Explora hacia arriba. La cámara sigue a tu mascota por el laberinto."
                        else -> "Mueve a tu mascota por los caminos hasta la salida correcta."
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3B5726),
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.93f))
                        .padding(14.dp)
                )
            }
        }

        if (session.finished) {
            MazeExitFeedback(
                appState = appState,
                isCorrect = true,
                phrase = level.phrase,
                translation = level.exits.firstOrNull { it.isCorrect }?.translation ?: ""
            ) {
                phase = if (level.id == 5 && reward.baseCoins > 0) MazePhase.CELEBRATION else MazePhase.RESULTS
            }
        } else if (session.wrongExit != null) {
            MazeExitFeedback(appState, isCorrect = false, phrase = level.phrase, translation = "") {
                session.continueAfterWrongExit()
                joystickKey++
            }
        }
    }
}

@Composable
private fun MazeBoard(appState: AppState, session: MazeSession, modifier: Modifier) {
    val level = session.level
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val viewportWidth = maxWidth
        val viewportHeight = maxHeight
        val sidePadding = if (level.exits.any { it.side == MazeExitSide.LEFT || it.side == MazeExitSide.RIGHT }) 30.dp else 0.dp
        val topPadding = 48.dp
        val bottomPadding = if (level.exits.any { it.side == MazeExitSide.BOTTOM }) 48.dp else 24.dp
        val widthTile = (viewportWidth - sidePadding * 2) / level.columns
        val tile: Dp = if (level.followsPlayerVertically) widthTile
        else min(widthTile.value, ((viewportHeight - 72.dp) / level.rows).value).dp
        val boardWidth = tile * level.columns
        val boardHeight = tile * level.rows
        val contentWidth = boardWidth + sidePadding * 2
        val contentHeight = boardHeight + topPadding + bottomPadding

        // La cámara mueve todo el mundo junto; no cambia las coordenadas de colisión.
        val cameraOffset: Dp = if (level.followsPlayerVertically && contentHeight > viewportHeight) {
            val desired = topPadding + tile * session.position.y - viewportHeight / 2
            desired.coerceIn(0.dp, contentHeight - viewportHeight)
        } else {
            -(viewportHeight - contentHeight) / 2
        }
        val left = (viewportWidth - contentWidth) / 2

        fun screen(point: Offset): DpOffsetPx = DpOffsetPx(
            left + sidePadding + tile * point.x,
            topPadding + tile * point.y - cameraOffset
        )

        // Paredes y caminos
        Canvas(
            modifier = Modifier
                .offset(left + sidePadding, topPadding - cameraOffset)
                .size(boardWidth, boardHeight)
        ) {
            drawMaze(level, size.width / level.columns)
        }

        // Letreros de las salidas
        level.exits.forEach { exit ->
            val horizontalEdge = exit.side == MazeExitSide.TOP || exit.side == MazeExitSide.BOTTOM
            val signWidth = if (horizontalEdge) tile * 2.9f else 100.dp
            val signHeight = if (horizontalEdge) 44.dp else 28.dp
            val center = when (exit.side) {
                MazeExitSide.TOP -> DpOffsetPx(left + sidePadding + tile * exit.center.x, 22.dp - cameraOffset)
                MazeExitSide.BOTTOM -> DpOffsetPx(left + sidePadding + tile * exit.center.x, topPadding + boardHeight + 22.dp - cameraOffset)
                MazeExitSide.LEFT -> DpOffsetPx(left + sidePadding / 2, topPadding + tile * exit.center.y - cameraOffset)
                MazeExitSide.RIGHT -> DpOffsetPx(left + sidePadding + boardWidth + sidePadding / 2, topPadding + tile * exit.center.y - cameraOffset)
            }
            Column(
                modifier = Modifier
                    .offset(center.x - signWidth / 2, center.y - signHeight / 2)
                    .size(signWidth, signHeight)
                    .rotate(if (horizontalEdge) 0f else if (exit.side == MazeExitSide.LEFT) -90f else 90f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (exit.side == MazeExitSide.BOTTOM) Icon(Icons.Filled.ArrowDropUp, null, tint = GameDarkPurple, modifier = Modifier.size(14.dp))
                Text(
                    exit.translation,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameDarkPurple,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                )
                if (exit.side != MazeExitSide.BOTTOM) Icon(Icons.Filled.ArrowDropDown, null, tint = GameDarkPurple, modifier = Modifier.size(14.dp))
            }
        }

        // Monedas
        val bob = rememberInfiniteTransition(label = "coin")
        val coinFloat by bob.animateFloat(-1f, 1f, infiniteRepeatable(tween(700, easing = EaseInOut), RepeatMode.Reverse), label = "float")
        val coinSize = tile * 0.65f
        session.remainingCoins.forEach { coin ->
            val p = screen(coin.position)
            Box(
                modifier = Modifier
                    .offset(p.x - coinSize / 2, p.y - coinSize / 2 + coinSize * 0.14f * coinFloat)
                    .size(coinSize)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFE066), Color(0xFFFFC107))))
                    .padding(coinSize * 0.08f),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Star, contentDescription = "Moneda", tint = Color(0xFFBF6308), modifier = Modifier.size(coinSize * 0.5f))
            }
        }

        if (level.startsInside) {
            val p = screen(level.start)
            Icon(
                Icons.Filled.MyLocation,
                contentDescription = "Punto de inicio",
                tint = Color.Blue.copy(alpha = 0.55f),
                modifier = Modifier.offset(p.x - tile * 0.42f, p.y - tile * 0.42f).size(tile * 0.85f)
            )
        } else {
            Text(
                "ENTRADA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .offset(left + sidePadding + tile * level.start.x - 34.dp, topPadding + boardHeight + 4.dp - cameraOffset)
                    .clip(CircleShape)
                    .background(Color(0xFF42631F))
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            )
        }

        // Mascota
        val petSize = tile * MazeLevel.PET_RADIUS * 2.4f
        val p = screen(session.position)
        Box(modifier = Modifier.offset(p.x - petSize / 2, p.y - petSize / 2)) {
            PetView(appState = appState, size = petSize)
        }
    }
}

/** Punto en pantalla expresado en dp. */
private data class DpOffsetPx(val x: Dp, val y: Dp)

private fun DrawScope.drawMaze(level: MazeLevel, tile: Float) {
    for (row in 0 until level.rows) {
        for (column in 0 until level.columns) {
            val rect = Rect(Offset(column * tile, row * tile), Size(tile, tile))
            if (!level.isWall(column, row)) {
                val floor = when (level.theme) {
                    MazeTheme.ICE -> Color(0xFFF0FAFF)
                    MazeTheme.LAVA -> Color(0xFFC7B8A6)
                    else -> Color(0xFFF0E0A8)
                }
                drawRect(floor, rect.topLeft, rect.size)
                drawOval(
                    if (level.theme == MazeTheme.ICE) Color.Cyan.copy(alpha = 0.22f) else Color(0xFF8B5A2B).copy(alpha = 0.18f),
                    Offset(rect.center.x + tile * 0.18f, rect.center.y + tile * 0.19f),
                    Size(tile * 0.06f, tile * 0.04f)
                )
                continue
            }
            when (level.theme) {
                MazeTheme.GRASS -> {
                    drawRect(Color(0xFF266326), rect.topLeft, rect.size)
                    val crown = rect.deflate(tile * 0.035f)
                    drawRoundRect(Color(0xFF3B8C30), crown.topLeft, crown.size, CornerRadius(tile * 0.22f))
                    drawOval(Color(0xFF66AD3D), Offset(rect.left + tile * 0.10f, rect.top + tile * 0.09f), Size(tile * 0.53f, tile * 0.48f))
                    drawOval(Color(0xFF4D9C33), Offset(rect.left + tile * 0.42f, rect.top + tile * 0.32f), Size(tile * 0.45f, tile * 0.47f))
                }
                MazeTheme.STONE -> {
                    drawRect(Color(0xFF525759), rect.topLeft, rect.size)
                    val shade = ((column * 3 + row) % 3) * 0.025f
                    val stone = rect.deflate(tile * 0.04f)
                    drawRoundRect(Color(0.57f + shade, 0.59f + shade, 0.60f + shade), stone.topLeft, stone.size, CornerRadius(tile * 0.17f))
                    drawRect(Color.White.copy(alpha = 0.19f), Offset(rect.left + tile * 0.15f, rect.top + tile * 0.15f), Size(tile * 0.5f, tile * 0.2f))
                }
                MazeTheme.BRICK -> {
                    drawRect(Color(0xFFC7AD8A), rect.topLeft, rect.size)
                    val mortar = max(1f, tile * 0.055f)
                    val half = tile / 2
                    for (course in 0 until 2) {
                        val y = rect.top + course * half
                        val split = if ((row + column + course) % 2 == 0) tile * 0.35f else tile * 0.65f
                        val color = Color(0.65f, 0.29f + course * 0.04f, 0.19f)
                        drawRoundRect(color, Offset(rect.left + mortar / 2, y + mortar / 2), Size(split - mortar, half - mortar), CornerRadius(tile * 0.035f))
                        drawRoundRect(color, Offset(rect.left + split + mortar / 2, y + mortar / 2), Size(tile - split - mortar, half - mortar), CornerRadius(tile * 0.035f))
                    }
                }
                MazeTheme.ICE -> {
                    drawRect(Color(0xFF479EC7), rect.topLeft, rect.size)
                    val block = rect.deflate(tile * 0.04f)
                    drawRoundRect(Color(0xFF99D9F0), block.topLeft, block.size, CornerRadius(tile * 0.12f))
                    drawRoundRect(Color.White.copy(alpha = 0.9f), Offset(rect.left + tile * 0.08f, rect.top + tile * 0.04f), Size(tile * 0.84f, tile * 0.10f), CornerRadius(tile * 0.04f))
                }
                MazeTheme.LAVA -> {
                    drawRect(Color(0xFFC7300A), rect.topLeft, rect.size)
                    val block = rect.deflate(tile * 0.055f)
                    drawRoundRect(Color(0xFF3B262E), block.topLeft, block.size, CornerRadius(tile * 0.09f))
                    drawRoundRect(Color(0xFF663B38), Offset(block.left + tile * 0.04f, block.top + tile * 0.03f), Size(block.width - tile * 0.08f, tile * 0.20f), CornerRadius(tile * 0.04f))
                    val shift = ((column + row) % 3) * tile * 0.08f
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(rect.center.x + shift, rect.top + tile * 0.08f)
                        lineTo(rect.center.x - tile * 0.16f, rect.center.y)
                        lineTo(rect.center.x + tile * 0.15f, rect.center.y + tile * 0.17f)
                        lineTo(rect.center.x - shift, rect.bottom - tile * 0.08f)
                    }
                    drawPath(path, Color(0xFFFF9500), style = Stroke(max(1.5f, tile * 0.10f)))
                    drawPath(path, Color(0xFFFFCC00), style = Stroke(max(0.7f, tile * 0.035f)))
                }
            }
        }
    }
}

@Composable
private fun MazeExitFeedback(
    appState: AppState,
    isCorrect: Boolean,
    phrase: String,
    translation: String,
    onContinue: () -> Unit
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val iconScale by animateFloatAsState(if (appeared) 1f else 0.3f, spring(dampingRatio = 0.5f), label = "icon")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(22.dp)
                .widthIn(max = 380.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                if (isCorrect) Icons.Filled.Verified else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (isCorrect) GameTeal else GameOrange,
                modifier = Modifier.size(74.dp).scale(iconScale)
            )
            PetView(appState = appState, size = 100.dp)
            Text(
                if (isCorrect) "¡Esa es la salida correcta!" else "Esta no es la salida correcta",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText, textAlign = TextAlign.Center
            )
            Text(
                if (isCorrect) "$phrase · $translation" else "¡Sigue buscando! Puedes regresar por el camino y probar otra salida.",
                fontSize = 16.sp, color = SubText, textAlign = TextAlign.Center
            )
            ChunkyButton(
                text = if (isCorrect) "Siguiente" else "Seguir buscando",
                onClick = onContinue,
                height = 52.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
