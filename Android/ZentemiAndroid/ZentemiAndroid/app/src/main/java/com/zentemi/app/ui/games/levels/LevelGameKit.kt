package com.zentemi.app.ui.games.levels

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.data.MemoryLevel
import com.zentemi.app.data.WordSearchLevel
import com.zentemi.app.data.gameTimeText
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay

val GameDarkPurple = Color(0xFF5A2BA6)
val GameTeal = Color(0xFF30B0C7)
val GameOrange = Color(0xFFFF9500)
val GamePink = Color(0xFFFF2D55)
val GameDoneGreen = Color(0xFF1A6133)
val GameDoneBackground = Color(0xFFC7F5CC)

/** Fondo lila con círculos suaves (MaicitoGamesBackground). */
@Composable
fun GamesBackground(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        Box(Modifier.offset((-120).dp, (-110).dp).size(230.dp).clip(CircleShape).background(Purple.copy(alpha = 0.06f)))
        Box(Modifier.align(Alignment.CenterEnd).offset(90.dp, (-60).dp).size(190.dp).clip(CircleShape).background(Purple.copy(alpha = 0.05f)))
        Box(Modifier.align(Alignment.BottomStart).offset((-80).dp, 40.dp).size(200.dp).clip(CircleShape).background(Color.Cyan.copy(alpha = 0.06f)))
        Box(Modifier.align(Alignment.BottomEnd).offset(60.dp, 30.dp).size(140.dp).clip(CircleShape).background(Color.Yellow.copy(alpha = 0.08f)))
        content()
    }
}

/** Botón redondo blanco (volver o cerrar). */
@Composable
fun RoundIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Purple)
    }
}

/** Puntos de juego hacia el objetivo de 10,000. */
@Composable
fun GamePointsCard(points: Int, goal: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.96f))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Purple)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Puntos de juego", fontWeight = FontWeight.Bold, color = Purple)
            Spacer(modifier = Modifier.weight(1f))
            Text("%,d / %,d".format(points, goal), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
        }
        LinearProgressIndicator(
            progress = { (points.coerceIn(0, goal)).toFloat() / goal },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = GameTeal,
            trackColor = GameTeal.copy(alpha = 0.18f),
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Text("¡Juega y alcanza tu objetivo!", fontSize = 12.sp, color = SubText)
    }
}

/** Los tres juegos por niveles (MaicitoGamesView.swift). */
enum class LevelGame { MEMORY, MAZE, WORD_SEARCH }

@Composable
fun MaicitoGamesScreen(
    appState: AppState,
    onBack: () -> Unit,
    onGameSelected: (LevelGame) -> Unit
) {
    GamesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Volver", onBack)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Jugar con ${appState.petName}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GameDarkPurple)
            }
            GamePointsCard(appState.gamePoints, appState.gamePointsGoal)
            LevelGameCard(
                title = "Memorama",
                image = R.drawable.memorama_foto,
                color = Purple,
                completed = appState.memoryCompleted(MemoryLevel.THREE),
                modifier = Modifier.fillMaxWidth()
            ) { onGameSelected(LevelGame.MEMORY) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LevelGameCard(
                    title = "Laberinto",
                    image = R.drawable.laberinto_foto,
                    color = GameTeal,
                    completed = appState.mazeCompleted(5),
                    modifier = Modifier.weight(1f)
                ) { onGameSelected(LevelGame.MAZE) }
                LevelGameCard(
                    title = "Sopa de letras",
                    image = R.drawable.sopa_letras_foto,
                    color = GamePink,
                    completed = appState.wordSearchCompleted(WordSearchLevel.THREE),
                    modifier = Modifier.weight(1f)
                ) { onGameSelected(LevelGame.WORD_SEARCH) }
            }
        }
    }
}

@Composable
private fun LevelGameCard(
    title: String,
    @DrawableRes image: Int,
    color: Color,
    completed: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(160), label = "press")
    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (pressed) 0.8f else 1f }
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = color.copy(alpha = 0.10f), spotColor = color.copy(alpha = 0.10f))
            .clip(RoundedCornerShape(24.dp))
            .background(if (completed) GameDoneBackground else Color.White.copy(alpha = 0.96f))
            .border(if (completed) 3.dp else 1.dp, if (completed) SuccessGreen else color.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .heightIn(min = 200.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(if (completed) 110.dp else 130.dp)
        )
        Text(
            title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = if (completed) GameDoneGreen else GameDarkPurple,
            textAlign = TextAlign.Center
        )
        if (completed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = GameDoneGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Completado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GameDoneGreen)
            }
        }
    }
}

/** Datos de una tarjeta de nivel. */
data class LevelCardInfo(
    val number: Int,
    val subtitle: String,
    val detail: String,
    val unlocked: Boolean,
    val completed: Boolean,
    val bestSeconds: Double,
    val reward: String
)

/** Lista de niveles de un juego (MemoryLevelsView, MazeLevelsView, WordSearchLevelsView). */
@Composable
fun LevelsScreen(
    appState: AppState,
    title: String,
    subtitle: String,
    levels: List<LevelCardInfo>,
    onBack: () -> Unit,
    onLevelSelected: (Int) -> Unit,
    footer: String? = null
) {
    GamesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Volver a los juegos", onBack)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GameDarkPurple)
                    Text(subtitle, fontSize = 14.sp, color = SubText)
                }
            }
            GamePointsCard(appState.gamePoints, appState.gamePointsGoal)
            levels.forEach { level ->
                LevelCard(level) { if (level.unlocked) onLevelSelected(level.number) }
            }
            footer?.let { Text(it, fontSize = 13.sp, color = SubText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
private fun LevelCard(level: LevelCardInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Purple.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(22.dp))
            .background(
                when {
                    level.completed -> GameDoneBackground
                    level.unlocked -> Color.White
                    else -> LockedNode
                }
            )
            .clickable(enabled = level.unlocked, onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Nivel ${level.number}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (level.unlocked) GameDarkPurple else SubText.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.weight(1f))
            when {
                level.completed -> Icon(Icons.Filled.Verified, contentDescription = "Completado", tint = GameDoneGreen)
                !level.unlocked -> Icon(Icons.Filled.Lock, contentDescription = "Bloqueado", tint = SubText.copy(alpha = 0.6f))
            }
        }
        Text(level.subtitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
        Text(level.detail, fontSize = 13.sp, color = SubText)
        if (!level.unlocked) {
            Text("Completa el nivel ${level.number - 1} para desbloquearlo.", fontSize = 13.sp, color = SubText)
        } else if (level.bestSeconds > 0) {
            Text("Mejor tiempo: ${gameTimeText(level.bestSeconds)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GameTeal)
        }
        if (level.unlocked && !level.completed) {
            Text(level.reward, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GameOrange)
        }
    }
}

/** Encabezado de una partida: cerrar, título, cronómetro. */
@Composable
fun LevelGameHeader(
    title: String,
    elapsedSeconds: Double,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RoundIconButton(Icons.Filled.Close, "Salir del juego", onClose)
            Spacer(modifier = Modifier.weight(1f))
            Text(title, fontWeight = FontWeight.Bold, color = GameDarkPurple, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Timer, contentDescription = null, tint = GameDarkPurple, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(gameTimeText(elapsedSeconds), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GameDarkPurple)
        }
        content()
    }
}

/**
 * Resultados de un nivel: tiempo, monedas y puntos con animación
 * (MemoryResultsView, MazeResultsView, WordSearchResultsView).
 */
@Composable
fun LevelResultsScreen(
    appState: AppState,
    level: Int,
    elapsedSeconds: Double,
    bestSeconds: Double,
    earnedCoins: Int,
    earnedPoints: Int,
    extraNote: String? = null,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    var visibleCards by remember { mutableIntStateOf(0) }
    var showMascot by remember { mutableStateOf(false) }
    var showContinue by remember { mutableStateOf(false) }
    val startPoints = remember { appState.gamePoints - earnedPoints }
    var displayedPoints by remember { mutableIntStateOf(startPoints) }

    LaunchedEffect(Unit) {
        showMascot = true
        for (i in 1..3) {
            delay(350)
            visibleCards = i
            SoundManager.play(context, R.raw.audioseleccion, appState.soundVolume)
        }
        if (earnedPoints > 0) {
            val steps = 40
            for (step in 1..steps) {
                displayedPoints = startPoints + earnedPoints * step / steps
                delay(25)
            }
        }
        displayedPoints = appState.gamePoints
        SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
        delay(300)
        showContinue = true
    }

    GamesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text("¡Nivel $level completado!", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Purple, textAlign = TextAlign.Center)
            val mascotScale by animateFloatAsState(if (showMascot) 1f else 0.6f, spring(dampingRatio = 0.6f), label = "mascot")
            PetView(appState = appState, size = 140.dp, modifier = Modifier.scale(mascotScale))
            ResultRow(visibleCards >= 1, Icons.Filled.Timer, "Tiempo", gameTimeText(elapsedSeconds),
                "Mejor tiempo: ${gameTimeText(bestSeconds)}", GameTeal)
            ResultRow(visibleCards >= 2, Icons.Filled.MonetizationOn, "Monedas ganadas", "+$earnedCoins",
                "Monedas totales: %,d".format(appState.coins), GameOrange)
            AnimatedVisibility(visible = visibleCards >= 3, enter = scaleIn(initialScale = 0.75f) + fadeIn()) {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(bottom = 16.dp)
                ) {
                    ResultRowContent(Icons.Filled.Star, "Puntos ganados", "+$earnedPoints",
                        "%,d / %,d puntos totales".format(displayedPoints, appState.gamePointsGoal), Purple)
                    LinearProgressIndicator(
                        progress = { displayedPoints.coerceIn(0, appState.gamePointsGoal).toFloat() / appState.gamePointsGoal },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Purple,
                        trackColor = Purple.copy(alpha = 0.15f),
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
            extraNote?.let { Text(it, fontSize = 14.sp, color = SubText, textAlign = TextAlign.Center) }
            if (earnedCoins == 0 && earnedPoints == 0) {
                Text(
                    "Ya recibiste el premio de este nivel. Puedes seguir jugando para mejorar tu tiempo.",
                    fontSize = 14.sp, color = SubText, textAlign = TextAlign.Center
                )
            }
            ChunkyButton(
                text = "Volver a niveles",
                onClick = onContinue,
                enabled = showContinue,
                height = 52.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ResultRow(visible: Boolean, icon: ImageVector, title: String, value: String, detail: String, color: Color) {
    AnimatedVisibility(visible = visible, enter = scaleIn(initialScale = 0.75f) + fadeIn()) {
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White)) {
            ResultRowContent(icon, title, value, detail, color)
        }
    }
}

@Composable
private fun ResultRowContent(icon: ImageVector, title: String, value: String, detail: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = color) }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = SubText)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(detail, fontSize = 12.sp, color = SubText)
        }
    }
}

/** Celebración al terminar el último nivel de un juego por primera vez. */
@Composable
fun GameCompletionCelebration(appState: AppState, gameName: String, onContinue: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { SoundManager.play(context, R.raw.audiobien, appState.soundVolume) }
    val pulse = rememberInfiniteTransition(label = "celebrate")
    val scale by pulse.animateFloat(0.95f, 1.08f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "scale")
    GamesBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically)
        ) {
            Icon(Icons.Filled.Celebration, contentDescription = null, tint = GameOrange, modifier = Modifier.size(80.dp).scale(scale))
            PetView(appState = appState, size = 150.dp)
            Text("¡Completaste $gameName!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Purple, textAlign = TextAlign.Center)
            Text(
                "Superaste todos los niveles. ¡${appState.petName} está muy orgulloso de ti!",
                fontSize = 16.sp, color = DarkText, textAlign = TextAlign.Center
            )
            ChunkyButton(text = "Ver resultados", onClick = onContinue, height = 52.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Cronómetro de partida: avanza solo con la app abierta y sin pausa. */
@Composable
fun rememberGameClock(running: Boolean): MutableDoubleState {
    val elapsed = remember { mutableDoubleStateOf(0.0) }
    val active = com.zentemi.app.ui.learning.lessons.rememberAppActive()
    LaunchedEffect(running, active) {
        if (!running || !active) return@LaunchedEffect
        var last = System.nanoTime()
        while (true) {
            delay(200)
            val now = System.nanoTime()
            elapsed.doubleValue += (now - last) / 1_000_000_000.0
            last = now
        }
    }
    return elapsed
}
