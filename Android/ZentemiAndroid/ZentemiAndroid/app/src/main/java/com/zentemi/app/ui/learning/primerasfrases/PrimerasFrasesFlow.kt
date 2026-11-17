package com.zentemi.app.ui.learning.primerasfrases

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.zentemi.app.R
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.GreetingExerciseScreen

/** Número de ejercicios de la lección (las escenas no cuentan). */
const val PRIMERAS_FRASES_EXERCISES = 5

private val scenes = listOf(
    PhraseScene(
        background = R.drawable.fondopf_01,
        dialogues = listOf(
            "¡Hola!",
            "¡Hola!",
            "¿Sabes cómo se dice buenos días en Yuhmu?",
            "¡Sí! Se dice «Té xki hats’i».",
            "¡Muchas gracias!"
        ),
        audio = R.raw.buenosdias
    ),
    PhraseScene(
        background = R.drawable.fondopf_02,
        dialogues = listOf(
            "¡Hola! Quiero conocerte mejor.",
            "¡Claro!",
            "¿Cómo puedo preguntarte tu nombre en Yuhmu?",
            "¡Se dice «¿Cómo te llamas?» en Yuhmu!",
            "¡Gracias! Ahora voy a practicar."
        ),
        audio = R.raw.comotellamas
    ),
    PhraseScene(
        background = R.drawable.fondopf_03,
        dialogues = listOf(
            "¡Ya aprendimos a saludarnos y preguntar nuestros nombres!",
            "¡Así es! Ahora podemos conocernos un poco más.",
            "¿Cómo se pregunta «¿Cuántos años tienes?» en Yuhmu?",
            "¡Escucha cómo se dice en Yuhmu!",
            "¡Gracias! Ahora intentaré pronunciarlo."
        ),
        audio = R.raw.cuantosaniostienes
    ),
    PhraseScene(
        background = R.drawable.fondopf_04,
        dialogues = listOf(
            "¡Ya sabemos saludar, preguntar nombres y edades!",
            "¡Sí! Pero a veces también necesitamos pedir ayuda.",
            "¿Cómo se dice «Ayúdame por favor» en Yuhmu?",
            "¡Escucha cómo se dice en Yuhmu!",
            "¡Muchas gracias! Ahora sabré cómo pedir ayuda."
        ),
        audio = R.raw.ayudameporfavor
    )
)

/**
 * Orden de la lección: escena 1 → ejercicio 1 → … → escena 4 → ejercicio 4
 * → ejercicio 5 (repaso final, sin escena).
 */
private sealed interface LessonStep {
    data class Scene(val index: Int) : LessonStep
    data class Exercise(val number: Int) : LessonStep
}

private val lessonSteps: List<LessonStep> = listOf(
    LessonStep.Scene(0), LessonStep.Exercise(1),
    LessonStep.Scene(1), LessonStep.Exercise(2),
    LessonStep.Scene(2), LessonStep.Exercise(3),
    LessonStep.Scene(3), LessonStep.Exercise(4),
    LessonStep.Exercise(5)
)

/**
 * Lección 1 "Primeras frases" completa en una sola ruta. En iOS cada paso
 * abre un fullScreenCover encima del anterior; aquí un solo estado avanza
 * los pasos, así que la X (o el botón atrás) sale de toda la lección.
 */
@Composable
fun PrimerasFrasesFlow(
    appState: AppState,
    onExit: () -> Unit,
    onLessonCompleted: (correctAnswers: Int, totalAnswers: Int, elapsedSeconds: Int) -> Unit
) {
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var correctAnswers by rememberSaveable { mutableIntStateOf(0) }
    val lessonStart = rememberSaveable { System.currentTimeMillis() }

    BackHandler { onExit() }

    fun next(answeredCorrectly: Boolean? = null) {
        if (answeredCorrectly == true) correctAnswers += 1
        if (stepIndex < lessonSteps.lastIndex) {
            stepIndex += 1
        } else {
            val elapsed = maxOf(1, ((System.currentTimeMillis() - lessonStart) / 1000).toInt())
            onLessonCompleted(correctAnswers, PRIMERAS_FRASES_EXERCISES, elapsed)
        }
    }

    AnimatedContent(
        targetState = stepIndex,
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
        label = "lessonStep"
    ) { index ->
        when (val step = lessonSteps[index]) {
            is LessonStep.Scene -> PhraseSceneScreen(
                appState = appState,
                scene = scenes[step.index],
                onClose = onExit,
                onSceneCompleted = { next() }
            )

            is LessonStep.Exercise -> when (step.number) {
                1 -> GreetingExerciseScreen(
                    appState = appState,
                    exerciseNumber = 1,
                    totalExercises = PRIMERAS_FRASES_EXERCISES,
                    onBack = onExit,
                    onContinue = { next(it) }
                )
                2 -> WriteTranslationExerciseScreen(
                    appState = appState,
                    exerciseNumber = 2,
                    totalExercises = PRIMERAS_FRASES_EXERCISES,
                    onClose = onExit,
                    onContinue = { next(it) }
                )
                3 -> PronunciationExerciseScreen(
                    appState = appState,
                    exerciseNumber = 3,
                    totalExercises = PRIMERAS_FRASES_EXERCISES,
                    onClose = onExit,
                    onContinue = { next(it) }
                )
                4 -> MeaningChoiceExerciseScreen(
                    appState = appState,
                    exerciseNumber = 4,
                    totalExercises = PRIMERAS_FRASES_EXERCISES,
                    onClose = onExit,
                    onContinue = { next(it) }
                )
                else -> PairMatchingGameScreen(
                    appState = appState,
                    exerciseNumber = 5,
                    totalExercises = PRIMERAS_FRASES_EXERCISES,
                    onClose = onExit,
                    onContinue = { next(it) }
                )
            }
        }
    }
}
