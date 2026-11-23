package com.zentemi.app.ui.games

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.learning.primerasfrases.ConfettiOverlay
import com.zentemi.app.ui.learning.primerasfrases.flashConfetti
import com.zentemi.app.ui.learning.primerasfrases.rememberShakeState
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Un momento de la historia.
 * - Si [speakWithAudio] es true, las opciones son audios en Yuhmu y hay que
 *   elegir el que dice [answer] (practica decir la frase).
 * - Si es false, el vendedor dice [answer] en Yuhmu y hay que elegir qué
 *   significa en español (practica entender).
 */
private data class MarketStep(
    val vendorLine: String,
    val prompt: String,
    val answer: YuhmuWord,
    val speakWithAudio: Boolean,
    val successLine: String
)

private fun buildStory(userName: String): List<MarketStep> {
    val v = YuhmuVocabulary
    return listOf(
        MarketStep(
            vendorLine = "¡Bienvenidos al mercado de Ixtenco!",
            prompt = "Saluda al vendedor. ¿Cuál audio dice «Buenos días»?",
            answer = v.byId("buenos_dias"),
            speakWithAudio = true,
            successLine = "¡Té xki hats’i! ¡Qué gusto verlos por aquí!"
        ),
        MarketStep(
            vendorLine = "El vendedor te pregunta algo en Yuhmu…",
            prompt = "¿Qué te preguntó?",
            answer = v.byId("como_te_llamas"),
            speakWithAudio = false,
            successLine = "¡Mucho gusto, $userName!"
        ),
        MarketStep(
            vendorLine = "Ahora te pregunta otra cosa…",
            prompt = "¿Qué te preguntó ahora?",
            answer = v.byId("cuantos_anios"),
            speakWithAudio = false,
            successLine = "¡Ya eres todo un experto del mercado!"
        ),
        MarketStep(
            vendorLine = "¡Oh no! Se te cayó la canasta con las tortillas.",
            prompt = "Pide ayuda al vendedor. ¿Cuál audio dice «Ayúdame por favor»?",
            answer = v.byId("ayudame"),
            speakWithAudio = true,
            successLine = "¡Claro que te ayudo! Y por ser tan amable, aquí tienes un tamal para Maicito."
        )
    )
}

/**
 * "El mercado de Ixtenco": Maicito visita el tianguis y platica con el
 * vendedor (el ajolote). Cada paso es entender o decir una frase en Yuhmu.
 */
@Composable
fun MercadoGame(
    appState: AppState,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val phrasePlayer = rememberPhrasePlayer()
    val shake = rememberShakeState()
    val scope = rememberCoroutineScope()
    val vocabulary = remember { YuhmuVocabulary.all }

    var gameId by remember { mutableIntStateOf(0) }
    val story = remember(gameId) { buildStory(appState.userName) }
    // Opciones: la respuesta y dos frases más, en orden al azar.
    val options = remember(gameId) {
        story.map { step ->
            (vocabulary.filter { it != step.answer }.shuffled().take(2) + step.answer).shuffled()
        }
    }
    var stepIndex by remember(gameId) { mutableIntStateOf(0) }
    var firstTryCorrect by remember(gameId) { mutableIntStateOf(0) }
    var selected by remember(gameId, stepIndex) { mutableStateOf<YuhmuWord?>(null) }
    var wrongPicks by remember(gameId, stepIndex) { mutableStateOf(setOf<String>()) }
    var solved by remember(gameId, stepIndex) { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var finished by remember(gameId) { mutableStateOf(false) }

    val step = story[stepIndex]
    val stepOptions = options[stepIndex]

    // Cuando el vendedor habla en Yuhmu, se reproduce su frase.
    LaunchedEffect(gameId, stepIndex) {
        if (!step.speakWithAudio) {
            delay(500)
            phrasePlayer.play(step.answer.audio, appState.soundVolume)
        }
    }

    BackHandler { onExit() }

    fun answer(choice: YuhmuWord) {
        if (solved || choice.id in wrongPicks) return
        if (choice == step.answer) {
            solved = true
            if (wrongPicks.isEmpty()) firstTryCorrect += 1
            SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
            scope.flashConfetti { showConfetti = it }
        } else {
            wrongPicks = wrongPicks + choice.id
            selected = null
            SoundManager.play(context, R.raw.audiomalaseleccion, appState.soundVolume)
            shake.shake(scope)
        }
    }

    fun next() {
        phrasePlayer.stop()
        if (stepIndex < story.lastIndex) stepIndex += 1 else finished = true
    }

    val breathing = rememberInfiniteTransition(label = "breath")
    val breath by breathing.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.fondopf_02),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shake.offset.dp)
                .navigationBarsPadding()
        ) {
            GameTopBar(
                title = "El mercado de Ixtenco",
                progress = "${stepIndex + 1}/${story.size}",
                onClose = onExit
            )

            // Globo del vendedor
            AnimatedContent(
                targetState = Triple(gameId, stepIndex, solved),
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.92f))
                        .togetherWith(fadeOut(tween(150)))
                },
                label = "vendorLine"
            ) { (_, index, isSolved) ->
                val current = story[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp, end = 20.dp, top = 6.dp)
                        .shadow(6.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.96f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSolved) current.successLine else current.vendorLine,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier.weight(1f)
                    )
                    if (!current.speakWithAudio && !isSolved) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Purple.copy(alpha = 0.10f))
                                .clickable { phrasePlayer.play(current.answer.audio, appState.soundVolume) },
                            contentAlignment = Alignment.Center
                        ) {
                            ZentemiIcon(R.drawable.ic_escuchar_audio, size = 34.dp, contentDescription = "Escuchar al vendedor")
                        }
                    }
                }
            }

            // Maicito y el vendedor
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                    PetView(
                        appState = appState,
                        size = 130.dp,
                        modifier = Modifier.graphicsLayer {
                            scaleY = 0.985f + 0.06f * breath
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                    )
                }
                Image(
                    painter = painterResource(R.drawable.personaje00),
                    contentDescription = "Vendedor",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 170.dp)
                        .graphicsLayer {
                            scaleY = 1.045f - 0.06f * breath
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                )
            }

            // Panel de respuesta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(BackgroundLight)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(step.prompt, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkText)

                stepOptions.forEachIndexed { index, option ->
                    val isWrong = option.id in wrongPicks
                    val isRight = solved && option == step.answer
                    val isSelected = selected == option
                    val accent = when {
                        isRight -> SuccessGreen
                        isWrong -> ErrorRed
                        isSelected -> Purple
                        else -> LockedNode
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isRight -> SuccessGreen.copy(alpha = 0.08f).compositeOver(Color.White)
                            isWrong -> ErrorRed.copy(alpha = 0.08f).compositeOver(Color.White)
                            isSelected -> Purple.copy(alpha = 0.06f).compositeOver(Color.White)
                            else -> Color.White
                        },
                        border = BorderStroke(2.dp, accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable(enabled = !solved && !isWrong) {
                                if (step.speakWithAudio) {
                                    // Audio: tocar lo escucha y lo selecciona; luego "Responder".
                                    selected = option
                                    phrasePlayer.play(option.audio, appState.soundVolume)
                                } else {
                                    answer(option)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Purple.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (step.speakWithAudio) {
                                    ZentemiIcon(R.drawable.ic_escuchar_audio, size = 26.dp)
                                } else {
                                    ZentemiIcon(option.icon, size = 26.dp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (step.speakWithAudio) {
                                    // Antes de responder no se revela qué dice cada audio.
                                    if (isRight) "«${option.yuhmu ?: option.spanish}»" else "Audio ${'A' + index}"
                                } else option.spanish,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                modifier = Modifier.weight(1f)
                            )
                            if (isRight) Icon(Icons.Filled.Check, null, tint = SuccessGreen)
                            if (isWrong) Icon(Icons.Filled.Close, null, tint = ErrorRed)
                        }
                    }
                }

                when {
                    solved -> ChunkyButton(
                        text = if (stepIndex < story.lastIndex) "Continuar" else "Terminar",
                        onClick = { next() },
                        color = SuccessGreen,
                        lipColor = Color(0xFF2E9E4D),
                        height = 52.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    step.speakWithAudio -> ChunkyButton(
                        text = "Responder",
                        onClick = { selected?.let { answer(it) } },
                        enabled = selected != null,
                        height = 52.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> Text(
                        "Escucha al vendedor y toca lo que significa.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showConfetti) ConfettiOverlay()

        if (finished) {
            GameResultOverlay(
                appState = appState,
                title = "¡Visita completada!",
                message = "Platicaste con el vendedor y respondiste $firstTryCorrect de ${story.size} al primer intento.",
                stars = starsFor(firstTryCorrect, story.size),
                onPlayAgain = { gameId += 1 },
                onExit = onExit
            )
        }
    }
}
