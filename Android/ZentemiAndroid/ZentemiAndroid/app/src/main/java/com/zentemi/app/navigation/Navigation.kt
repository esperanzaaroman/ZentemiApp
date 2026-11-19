package com.zentemi.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zentemi.app.audio.MusicManager
import com.zentemi.app.audio.MusicTrack
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.primerasfrases.PrimerasFrasesFlow
import com.zentemi.app.ui.learning.LearningPathScreen
import com.zentemi.app.ui.learning.LessonCompleteScreen
import com.zentemi.app.ui.learning.LessonIntroScreen
import com.zentemi.app.ui.games.GamesHubScreen
import com.zentemi.app.ui.games.MaicitoGame
import com.zentemi.app.ui.games.MaicitoPideGame
import com.zentemi.app.ui.games.MemoramaGame
import com.zentemi.app.ui.games.MercadoGame
import com.zentemi.app.ui.main.FeedMaicitoScreen
import com.zentemi.app.ui.main.MainScreen
import com.zentemi.app.ui.onboarding.CreatePINScreen
import com.zentemi.app.ui.onboarding.LoginScreen
import com.zentemi.app.ui.onboarding.NameScreen
import com.zentemi.app.ui.onboarding.OnboardingScreen
import com.zentemi.app.ui.onboarding.RecoveryWordScreen
import com.zentemi.app.ui.onboarding.SignInScreen
import com.zentemi.app.ui.onboarding.RecoveryKeyScreen
import com.zentemi.app.ui.onboarding.NewPINScreen
import com.zentemi.app.ui.settings.SettingsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val NAME = "name"
    const val CREATE_PIN = "create_pin"
    const val RECOVERY_WORD = "recovery_word"
    const val SIGN_IN = "sign_in"
    const val RECOVERY_KEY = "recovery_key"
    const val NEW_PIN = "new_pin"
    const val MAIN = "main"
    const val LEARNING_PATH = "learning_path"
    const val LESSON_INTRO = "lesson_intro/{lessonNumber}"
    const val GREETING_EXERCISE = "greeting_exercise"
    const val LESSON_COMPLETE = "lesson_complete/{correct}/{total}/{elapsed}"
    const val SETTINGS = "settings"
    const val FEED_MAICITO = "feed_maicito"
    const val GAMES = "games"
    const val GAME_MAICITO_PIDE = "game_maicito_pide"
    const val GAME_MEMORAMA = "game_memorama"
    const val GAME_MERCADO = "game_mercado"

    fun lessonIntro(lessonNumber: Int) = "lesson_intro/$lessonNumber"
    fun lessonComplete(correct: Int, total: Int, elapsed: Int) =
        "lesson_complete/$correct/$total/$elapsed"
}

@Composable
fun ZentemiNavigation(appState: AppState = viewModel()) {

    val navController = rememberNavController()

    // Música según la pantalla, igual que iOS: inicio en onboarding/login,
    // lobby en las pantallas principales, camino en el camino y la intro,
    // y silencio durante la lección y sus resultados.
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        if (currentRoute != null) MusicManager.play(musicFor(currentRoute))
    }

    // Al volver a la app se revisa la racha diaria.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        appState.updateDailyStreak()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onCreateAccount = {
                    navController.navigate(Routes.NAME)
                },
                onSignIn = {
                    navController.navigate(Routes.SIGN_IN)
                }
            )
        }

        composable(Routes.NAME) {
            NameScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate(Routes.CREATE_PIN)
                }
            )
        }

        composable(Routes.CREATE_PIN) {
            CreatePINScreen(
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate(Routes.RECOVERY_WORD)
                }
            )
        }

        composable(Routes.RECOVERY_WORD) {
            RecoveryWordScreen(
                onContinue = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SIGN_IN) {
            SignInScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onSignedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPIN = {
                    navController.navigate(Routes.RECOVERY_KEY + "?type=pin")
                },
                onForgotUsername = {
                    navController.navigate(Routes.RECOVERY_KEY + "?type=username")
                }
            )
        }

        composable(Routes.RECOVERY_KEY + "?type={type}") {
            val type = it.arguments?.getString("type") ?: "pin"
            RecoveryKeyScreen(
                recoveryType = type,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate(Routes.NEW_PIN)
                }
            )
        }

        composable(Routes.NEW_PIN) {
            NewPINScreen(
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate(Routes.RECOVERY_WORD)
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                appState = appState,
                onPlayPressed = {
                    navController.navigate(Routes.LEARNING_PATH)
                },
                onFeedPressed = {
                    navController.navigate(Routes.FEED_MAICITO)
                },
                onPlayWithPressed = {
                    navController.navigate(Routes.GAMES)
                },
                onSettingsPressed = {
                    navController.navigate(Routes.SETTINGS)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LEARNING_PATH) {
            LearningPathScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onLessonSelected = { lessonNumber ->
                    navController.navigate(Routes.lessonIntro(lessonNumber))
                }
            )
        }

        composable(Routes.LESSON_INTRO) { backStackEntry ->
            val lessonNumber = backStackEntry.arguments
                ?.getString("lessonNumber")?.toIntOrNull() ?: 1

            LessonIntroScreen(
                appState = appState,
                lessonNumber = lessonNumber,
                onBack = { navController.popBackStack() },
                onStartExercise = {
                    navController.navigate(Routes.GREETING_EXERCISE)
                }
            )
        }

        composable(Routes.GREETING_EXERCISE) {
            PrimerasFrasesFlow(
                appState = appState,
                onExit = { navController.popBackStack() },
                onLessonCompleted = { correct, total, elapsed ->
                    navController.navigate(Routes.lessonComplete(correct, total, elapsed)) {
                        popUpTo(Routes.LEARNING_PATH)
                    }
                }
            )
        }

        composable(Routes.LESSON_COMPLETE) { backStackEntry ->
            val correct = backStackEntry.arguments
                ?.getString("correct")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments
                ?.getString("total")?.toIntOrNull() ?: 1
            val elapsed = backStackEntry.arguments
                ?.getString("elapsed")?.toIntOrNull() ?: 0

            LessonCompleteScreen(
                appState = appState,
                correctAnswers = correct,
                totalAnswers = total,
                elapsedSeconds = elapsed,
                onContinue = {
                    navController.popBackStack(
                        Routes.LEARNING_PATH,
                        inclusive = false
                    )
                }
            )
        }

        composable(Routes.FEED_MAICITO) {
            FeedMaicitoScreen(
                appState = appState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GAMES) {
            GamesHubScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onGameSelected = { game ->
                    navController.navigate(
                        when (game) {
                            MaicitoGame.MAICITO_PIDE -> Routes.GAME_MAICITO_PIDE
                            MaicitoGame.MEMORAMA -> Routes.GAME_MEMORAMA
                            MaicitoGame.MERCADO -> Routes.GAME_MERCADO
                        }
                    )
                }
            )
        }

        composable(Routes.GAME_MAICITO_PIDE) {
            MaicitoPideGame(appState = appState, onExit = { navController.popBackStack() })
        }

        composable(Routes.GAME_MEMORAMA) {
            MemoramaGame(appState = appState, onExit = { navController.popBackStack() })
        }

        composable(Routes.GAME_MERCADO) {
            MercadoGame(appState = appState, onExit = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun musicFor(route: String): MusicTrack? = when {
    route == Routes.MAIN || route == Routes.SETTINGS || route == Routes.FEED_MAICITO ||
        route == Routes.GAMES -> MusicTrack.LOBBY
    // En los juegos se escuchan frases en Yuhmu, así que la música se pausa.
    route == Routes.GAME_MAICITO_PIDE || route == Routes.GAME_MEMORAMA ||
        route == Routes.GAME_MERCADO -> null
    route == Routes.LEARNING_PATH || route == Routes.LESSON_INTRO -> MusicTrack.CAMINO
    route == Routes.GREETING_EXERCISE || route == Routes.LESSON_COMPLETE -> null
    else -> MusicTrack.INICIO
}
