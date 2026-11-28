package com.zentemi.app.ui.games

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.audio.rememberPhrasePlayer
import com.zentemi.app.data.YuhmuTheme
import com.zentemi.app.data.YuhmuVocabulary
import com.zentemi.app.data.YuhmuWord
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.primerasfrases.MaicitoTipCard
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Máximo de parejas en el tablero (4 parejas = 8 cartas). */
private const val MAX_PAIRS = 4

/** Una carta: el audio en Yuhmu o su significado en español. */
private data class MemoCard(val key: Int, val word: YuhmuWord, val isAudio: Boolean)

private fun buildDeck(words: List<YuhmuWord>): List<MemoCard> {
    val chosen = words.shuffled().take(MAX_PAIRS)
    return chosen.flatMap { listOf(MemoCard(0, it, true), MemoCard(0, it, false)) }
        .shuffled()
        .mapIndexed { index, card -> card.copy(key = index) }
}

/**
 * Memorama: hay que juntar cada carta de audio en Yuhmu con la carta de su
 * significado. Al voltear una carta de audio se escucha la frase.
 * Las estrellas dependen de cuántos intentos se necesitaron.
 */
@Composable
fun MemoramaGame(
    appState: AppState,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val phrasePlayer = rememberPhrasePlayer()
    val scope = rememberCoroutineScope()
    // Las frases de las lecciones terminadas son un tema más, junto con el
    // vocabulario del socio formador (animales, comida, números…).
    val themes = remember {
        val lessonWords = YuhmuVocabulary.unlocked(appState.pathLessonsCompleted.coerceAtLeast(1))
        listOf(YuhmuTheme("frases", "Primeras frases", R.drawable.ic_frases_basicas, lessonWords)) +
            YuhmuVocabulary.themes
    }

    var gameId by remember { mutableIntStateOf(0) }
    var previousThemeId by remember { mutableStateOf<String?>(null) }
    // Cada partida es de un tema distinto al de la anterior.
    val theme = remember(gameId) {
        themes.filter { it.id != previousThemeId && it.words.size >= 2 }.random()
            .also { previousThemeId = it.id }
    }
    val deck = remember(gameId) { buildDeck(theme.words) }
    var faceUp by remember(gameId) { mutableStateOf(listOf<Int>()) }
    var matched by remember(gameId) { mutableStateOf(setOf<String>()) }
    var attempts by remember(gameId) { mutableIntStateOf(0) }
    var busy by remember(gameId) { mutableStateOf(false) }
    var finished by remember(gameId) { mutableStateOf(false) }
    val pairs = deck.size / 2

    BackHandler { onExit() }

    fun flip(card: MemoCard) {
        if (busy || card.key in faceUp || card.word.id in matched) return
        if (card.isAudio) phrasePlayer.play(card.word.audio, appState.soundVolume)
        else SoundManager.play(context, R.raw.audioseleccion, appState.soundVolume)

        val newFaceUp = faceUp + card.key
        faceUp = newFaceUp
        if (newFaceUp.size < 2) return

        attempts += 1
        val (first, second) = newFaceUp.map { key -> deck.first { it.key == key } }
        busy = true
        scope.launch {
            if (first.word == second.word) {
                delay(450)
                matched = matched + first.word.id
                SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
                faceUp = emptyList()
                busy = false
                if (matched.size == pairs) {
                    delay(700)
                    finished = true
                }
            } else {
                delay(1100)
                faceUp = emptyList()
                busy = false
            }
        }
    }

    // Menos intentos = más estrellas (lo mínimo posible es una por pareja).
    val stars = when {
        attempts <= pairs + 2 -> 3
        attempts <= pairs * 2 + 1 -> 2
        else -> 1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            GameTopBar(
                title = "Memorama",
                progress = "${matched.size}/$pairs",
                onClose = onExit
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 6.dp)
            ) {
                ZentemiIcon(theme.icon, size = 20.dp)
                Text(
                    text = "${theme.title} · Intentos: $attempts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Purple
                )
            }

            // Tablero 2 x 4
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                deck.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { card ->
                            MemoCardView(
                                card = card,
                                isFaceUp = card.key in faceUp || card.word.id in matched,
                                isMatched = card.word.id in matched,
                                onClick = { flip(card) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                MaicitoTipCard(
                    appState = appState,
                    text = "Las cartas con bocina suenan en Yuhmu. ¡Júntalas con su significado!"
                )
            }
        }

        if (finished) {
            GameResultOverlay(
                appState = appState,
                title = "¡Memorama completo!",
                message = "Encontraste las $pairs parejas en $attempts intentos.",
                stars = stars,
                onPlayAgain = { gameId += 1 },
                onExit = onExit
            )
        }
    }
}

@Composable
private fun MemoCardView(
    card: MemoCard,
    isFaceUp: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = tween(350),
        label = "flip"
    )
    val density = LocalDensity.current.density
    val showFront = rotation > 90f
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .shadow(4.dp, shape)
            .clip(shape)
            .clickable(enabled = !isFaceUp, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!showFront) {
            // Reverso: morado con Maicito
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(PurpleGradientStart, PurpleGradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.mascota),
                    contentDescription = null,
                    modifier = Modifier
                        .size(54.dp)
                        .graphicsLayer { alpha = 0.9f }
                )
            }
        } else {
            // Frente (se voltea para que no se vea al revés)
            val background = when {
                isMatched -> SuccessGreen.copy(alpha = 0.12f)
                card.isAudio -> Purple.copy(alpha = 0.10f)
                else -> Color.White
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(Color.White)
                    .background(background)
                    .border(2.dp, if (isMatched) SuccessGreen else Color.Transparent, shape)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
            ) {
                if (card.isAudio) {
                    ZentemiIcon(R.drawable.ic_escuchar_audio, size = 40.dp)
                    Text(
                        text = card.word.yuhmu ?: "Yuhmu",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple,
                        textAlign = TextAlign.Center
                    )
                } else {
                    ZentemiIcon(card.word.icon, size = 40.dp)
                    Text(
                        text = card.word.spanish,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        textAlign = TextAlign.Center
                    )
                }
                if (isMatched) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
