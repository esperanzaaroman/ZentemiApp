package com.zentemi.app.ui.games

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.audio.rememberPhrasePlayer
import com.zentemi.app.data.YuhmuVocabulary
import com.zentemi.app.data.YuhmuWord
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.primerasfrases.ConfettiOverlay
import com.zentemi.app.ui.learning.primerasfrases.ListenButton
import com.zentemi.app.ui.learning.primerasfrases.flashConfetti
import com.zentemi.app.ui.learning.primerasfrases.rememberShakeState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ROUNDS = 5
private const val OPTIONS_PER_ROUND = 3

private data class PideRound(val target: YuhmuWord, val options: List<YuhmuWord>)

/** Rondas al azar sin repetir la misma frase dos veces seguidas. */
private fun buildRounds(words: List<YuhmuWord>): List<PideRound> {
    val rounds = mutableListOf<PideRound>()
    var previous: YuhmuWord? = null
    repeat(ROUNDS) {
        val target = words.filter { it != previous }.ifEmpty { words }.random()
        val others = words.filter { it != target }.shuffled().take(OPTIONS_PER_ROUND - 1)
        rounds += PideRound(target, (others + target).shuffled())
        previous = target
    }
    return rounds
}

/**
 * "Maicito pide": Maicito dice algo en Yuhmu (audio) y hay que elegir qué
 * significa. Cuenta como acierto solo si se elige bien al primer intento.
 */
@Composable
fun MaicitoPideGame(
    appState: AppState,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val phrasePlayer = rememberPhrasePlayer()
    val shake = rememberShakeState()
    val scope = rememberCoroutineScope()
    val words = remember { YuhmuVocabulary.unlocked(appState.pathLessonsCompleted.coerceAtLeast(1)) }

    var gameId by remember { mutableIntStateOf(0) }
    val rounds = remember(gameId) { buildRounds(words) }
    var roundIndex by remember(gameId) { mutableIntStateOf(0) }
    var firstTryCorrect by remember(gameId) { mutableIntStateOf(0) }
    var wrongPicks by remember(gameId, roundIndex) { mutableStateOf(setOf<String>()) }
    var solved by remember(gameId, roundIndex) { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var finished by remember(gameId) { mutableStateOf(false) }

    val round = rounds[roundIndex]

    // Maicito "dice" la frase al empezar cada ronda.
    LaunchedEffect(gameId, roundIndex) {
        delay(450)
        phrasePlayer.play(round.target.audio, appState.soundVolume)
    }

    // Animaciones de Maicito: brinca al acertar y se tambalea al fallar.
    val jump = remember { Animatable(0f) }
    val wobble = remember { Animatable(0f) }

    BackHandler { onExit() }

    fun pick(word: YuhmuWord) {
        if (solved || word.id in wrongPicks) return
        if (word == round.target) {
            solved = true
            if (wrongPicks.isEmpty()) firstTryCorrect += 1
            SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
            scope.flashConfetti { showConfetti = it }
            scope.launch {
                jump.animateTo(-40f, tween(180, easing = FastOutSlowInEasing))
                jump.animateTo(0f, spring(dampingRatio = 0.4f))
            }
            scope.launch {
                delay(1300)
                phrasePlayer.stop()
                if (roundIndex < rounds.lastIndex) roundIndex += 1 else finished = true
            }
        } else {
            wrongPicks = wrongPicks + word.id
            SoundManager.play(context, R.raw.audiomalaseleccion, appState.soundVolume)
            shake.shake(scope)
            scope.launch {
                for (angle in listOf(-12f, 10f, -6f, 0f)) wobble.animateTo(angle, tween(90))
            }
        }
    }

    val breathing = rememberInfiniteTransition(label = "breath")
    val breath by breathing.animateFloat(
        1f, 1.05f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Escena del pueblo, como en la lección y el mercado
        Image(
            painter = painterResource(R.drawable.fondopf_04),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shake.offset.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameTopBar(
                title = "Maicito pide",
                progress = "${roundIndex + 1}/${rounds.size}",
                onClose = onExit,
                titleIcon = R.drawable.ic_maicito_pide
            )

            // Maicito y su globo; Maicito queda parado en el piso de la plaza
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
            // Globo de Maicito
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Purple.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (solved) "¡Eso es! ¡Gracias!" else "¿Sabes qué te estoy diciendo?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    textAlign = TextAlign.Center
                )
                ListenButton(
                    isPlaying = phrasePlayer.isPlaying,
                    onClick = { phrasePlayer.play(round.target.audio, appState.soundVolume) }
                )
            }

            // Espacio para que el globo quede más arriba sin mover a Maicito
            Spacer(modifier = Modifier.height(56.dp))

            // Maicito parado sobre el suelo, con su sombra
            Box(
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = 6.dp)
                        .size(120.dp, 22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.18f))
                )
                PetView(
                    appState = appState,
                    size = 190.dp,
                    modifier = Modifier.graphicsLayer {
                        translationY = jump.value
                        rotationZ = wobble.value
                        scaleY = breath
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                )
            }
            }

            // Opciones en un panel inferior (igual que el mercado)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(BackgroundLight)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Elige lo que significa",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                round.options.forEach { option ->
                    val isWrong = option.id in wrongPicks
                    val isRight = solved && option == round.target
                    val accent = when {
                        isRight -> SuccessGreen
                        isWrong -> ErrorRed
                        else -> LockedNode
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isRight -> SuccessGreen.copy(alpha = 0.08f).compositeOver(Color.White)
                            isWrong -> ErrorRed.copy(alpha = 0.08f).compositeOver(Color.White)
                            else -> Color.White
                        },
                        border = BorderStroke(2.dp, accent),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .clickable(enabled = !solved && !isWrong) { pick(option) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Purple.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ZentemiIcon(option.icon, size = 32.dp)
                            }
                            Spacer(modifier = Modifier.width(13.dp))
                            Text(
                                option.spanish,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                modifier = Modifier.weight(1f)
                            )
                            if (isRight) Icon(Icons.Filled.Check, null, tint = SuccessGreen)
                            if (isWrong) Icon(Icons.Filled.Close, null, tint = ErrorRed)
                        }
                    }
                }
            }
        }

        if (showConfetti) ConfettiOverlay()

        if (finished) {
            GameResultOverlay(
                appState = appState,
                title = "¡Bien jugado!",
                message = "Adivinaste $firstTryCorrect de ${rounds.size} al primer intento.",
                stars = starsFor(firstTryCorrect, rounds.size),
                onPlayAgain = { gameId += 1 },
                onExit = onExit
            )
        }
    }
}
