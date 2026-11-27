package com.zentemi.app.ui.learning.lessons

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.zentemi.app.data.LessonReward
import com.zentemi.app.data.PathLesson
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.LessonCompleteScreen

/**
 * Una lección del camino (2 a 6) y, al terminarla, sus resultados.
 * El premio se entrega al terminar el último ejercicio; los resultados solo lo animan.
 */
@Composable
fun PathLessonFlow(
    appState: AppState,
    lesson: PathLesson,
    onExit: () -> Unit,
    onContinue: () -> Unit
) {
    var result by remember { mutableStateOf<Triple<LessonReward, Int, Double>?>(null) }
    val finished = result

    if (finished != null) {
        BackHandler(onBack = onContinue)
        LessonCompleteScreen(
            appState = appState,
            correctAnswers = finished.second,
            totalAnswers = lesson.exerciseCount,
            elapsedSeconds = finished.third.toInt(),
            onContinue = onContinue,
            lessonReward = finished.first
        )
        return
    }

    val onFinished: (LessonReward, Int, Double) -> Unit = { reward, correct, seconds ->
        result = Triple(reward, correct, seconds)
    }
    when (lesson) {
        PathLesson.FAMILY -> FamilyLessonScreen(appState, onExit, onFinished)
        PathLesson.NUMBERS -> NumbersLessonScreen(appState, onExit, onFinished)
        PathLesson.COLORS -> ColorsLessonScreen(appState, onExit, onFinished)
        PathLesson.ANIMALS -> AnimalsLessonScreen(appState, onExit, onFinished)
        PathLesson.FOOD -> FoodLessonScreen(appState, onExit, onFinished)
    }
}
