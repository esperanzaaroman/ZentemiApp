package com.zentemi.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zentemi.app.audio.MusicManager
import com.zentemi.app.audio.MusicTrack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zentemi.app.data.AuthService
import com.zentemi.app.data.NetworkMonitor
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChooseProgressDialog
import com.zentemi.app.ui.components.SuggestAccountDialog
import com.zentemi.app.ui.learning.primerasfrases.PrimerasFrasesFlow
import com.zentemi.app.data.PathLesson
import com.zentemi.app.ui.learning.lessons.PathLessonFlow
import com.zentemi.app.ui.learning.LearningPathScreen
import com.zentemi.app.ui.learning.LessonCompleteScreen
import com.zentemi.app.ui.learning.LessonIntroScreen
import com.zentemi.app.ui.games.MaicitoPideGame
import com.zentemi.app.ui.games.MercadoGame
import com.zentemi.app.ui.games.levels.LevelGame
import com.zentemi.app.ui.games.levels.MaicitoGamesScreen
import com.zentemi.app.ui.games.levels.MazeLevelsFlow
import com.zentemi.app.ui.games.levels.MemoryLevelsFlow
import com.zentemi.app.ui.games.levels.WordSearchLevelsFlow
import com.zentemi.app.ui.main.FeedMaicitoScreen
import com.zentemi.app.ui.main.MainScreen
import com.zentemi.app.ui.onboarding.CreateAccountScreen
import com.zentemi.app.ui.onboarding.EmailVerificationScreen
import com.zentemi.app.ui.onboarding.NameScreen
import com.zentemi.app.ui.onboarding.OnboardingScreen
import com.zentemi.app.ui.onboarding.SignInScreen
import com.zentemi.app.ui.settings.SettingsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val NAME = "name"
    const val CREATE_ACCOUNT = "create_account"
    const val SIGN_IN = "sign_in"
    const val EMAIL_VERIFICATION = "email_verification"
    const val MAIN = "main"
    const val LEARNING_PATH = "learning_path"
    const val LESSON_INTRO = "lesson_intro/{lessonNumber}"
    const val GREETING_EXERCISE = "greeting_exercise"
    const val PATH_LESSON = "path_lesson/{lessonNumber}"
    const val LESSON_COMPLETE = "lesson_complete/{correct}/{total}/{elapsed}"
    const val SETTINGS = "settings"
    const val FEED_MAICITO = "feed_maicito"
    const val GAMES = "games"
    const val GAME_MAICITO_PIDE = "game_maicito_pide"
    const val GAME_MERCADO = "game_mercado"
    const val GAME_MEMORY_LEVELS = "game_memory_levels"
    const val GAME_MAZE_LEVELS = "game_maze_levels"
    const val GAME_WORD_SEARCH_LEVELS = "game_word_search_levels"

    /** Pantallas para entrar a una cuenta; al iniciar sesión se regresa solo a la app. */
    val accountRoutes = setOf(CREATE_ACCOUNT, SIGN_IN)

    fun lessonIntro(lessonNumber: Int) = "lesson_intro/$lessonNumber"
    fun pathLesson(lessonNumber: Int) = "path_lesson/$lessonNumber"
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

    // Mientras la app está abierta, la felicidad de Maicito baja con el
    // tiempo; se revisa al volver y cada 30 segundos (como iOS).
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                appState.updateHappinessDecay()
                kotlinx.coroutines.delay(30_000)
            }
        }
    }

    // ── Modo invitado ──
    // La cuenta es opcional: Onboarding → nombre → app. Crear cuenta e
    // iniciar sesión se abren desde Perfil (o desde la sugerencia), y al
    // tener sesión se regresa sola a donde estaba.
    val startDestination = remember {
        if (appState.hasCompletedOnboarding) Routes.MAIN else Routes.ONBOARDING
    }

    val isAuthenticated by AuthService.isAuthenticated.collectAsState()
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated && currentRoute in Routes.accountRoutes) {
            navController.popBackStack(Routes.MAIN, inclusive = false)
        }
    }

    // Sugerir cuenta: invitado + internet + 7 días desde el último "Ahora no".
    val isOnline by NetworkMonitor.isOnline.collectAsState()
    if (currentRoute == Routes.MAIN && isOnline && appState.isAccountSuggestionDue) {
        SuggestAccountDialog(
            petName = appState.petName,
            onCreateAccount = {
                // También cuenta como respuesta: si regresa sin crearla,
                // no vuelve a salir enseguida.
                appState.markAccountSuggestionAnswered()
                navController.navigate(Routes.CREATE_ACCOUNT)
            },
            onNotNow = { appState.markAccountSuggestionAnswered() }
        )
    }

    // Al iniciar sesión con progreso de invitado en el teléfono: elegir cuál usar.
    if (appState.pendingProgressChoice != null) {
        ChooseProgressDialog(
            onUsePhone = { appState.keepPhoneProgress() },
            onUseAccount = { appState.useAccountProgress() }
        )
    }

    val goToCreateAccount = { navController.navigate(Routes.CREATE_ACCOUNT) }
    val goToSignIn = { navController.navigate(Routes.SIGN_IN) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.NAME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // Solo el nombre; el progreso se guarda en el teléfono desde aquí.
        composable(Routes.NAME) {
            NameScreen(
                appState = appState,
                onBack = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.NAME) { inclusive = true }
                    }
                },
                onNext = {
                    appState.hasCompletedOnboarding = true
                    navController.navigate(Routes.MAIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // Cuenta del tutor (Firebase Auth) para guardar el progreso en la nube.
        composable(Routes.CREATE_ACCOUNT) {
            CreateAccountScreen(
                childName = appState.userName,
                onBack = { navController.popBackStack() }
            )
        }

        // Inicio de sesión del tutor con correo y contraseña.
        composable(Routes.SIGN_IN) {
            SignInScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EMAIL_VERIFICATION) {
            EmailVerificationScreen(
                onBack = { navController.popBackStack() }
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
                onCreateAccount = goToCreateAccount,
                onSignIn = goToSignIn,
                onVerifyEmail = {
                    navController.navigate(Routes.EMAIL_VERIFICATION)
                },
                onLogout = { AuthService.signOut() }
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
                    navController.navigate(
                        if (lessonNumber == 1) Routes.GREETING_EXERCISE else Routes.pathLesson(lessonNumber)
                    )
                }
            )
        }

        // Lecciones 2 a 6 (Familia, Números, Colores, Animales y Alimentos) con sus resultados
        composable(Routes.PATH_LESSON) { backStackEntry ->
            val lesson = backStackEntry.arguments?.getString("lessonNumber")?.toIntOrNull()
                ?.let { PathLesson.fromNumber(it) }
            if (lesson == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                PathLessonFlow(
                    appState = appState,
                    lesson = lesson,
                    onExit = { navController.popBackStack() },
                    onContinue = { navController.popBackStack(Routes.LEARNING_PATH, inclusive = false) }
                )
            }
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

        // Catálogo "Jugar con Maicito": Memorama, Laberinto y Sopa de letras por
        // niveles (MaicitoGamesView.swift), más Maicito pide y El mercado.
        composable(Routes.GAMES) {
            MaicitoGamesScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onGameSelected = { game ->
                    navController.navigate(
                        when (game) {
                            LevelGame.MEMORY -> Routes.GAME_MEMORY_LEVELS
                            LevelGame.MAZE -> Routes.GAME_MAZE_LEVELS
                            LevelGame.WORD_SEARCH -> Routes.GAME_WORD_SEARCH_LEVELS
                            LevelGame.MAICITO_PIDE -> Routes.GAME_MAICITO_PIDE
                            LevelGame.MERCADO -> Routes.GAME_MERCADO
                        }
                    )
                }
            )
        }

        composable(Routes.GAME_MEMORY_LEVELS) {
            MemoryLevelsFlow(appState = appState, onBack = { navController.popBackStack() })
        }

        composable(Routes.GAME_MAZE_LEVELS) {
            MazeLevelsFlow(appState = appState, onBack = { navController.popBackStack() })
        }

        composable(Routes.GAME_WORD_SEARCH_LEVELS) {
            WordSearchLevelsFlow(appState = appState, onBack = { navController.popBackStack() })
        }

        composable(Routes.GAME_MAICITO_PIDE) {
            MaicitoPideGame(appState = appState, onExit = { navController.popBackStack() })
        }

        composable(Routes.GAME_MERCADO) {
            MercadoGame(appState = appState, onExit = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onLogout = { AuthService.signOut() }
            )
        }
    }
}

private fun musicFor(route: String): MusicTrack? = when {
    route == Routes.MAIN || route == Routes.SETTINGS || route == Routes.FEED_MAICITO ||
        route == Routes.GAMES ||
        route == Routes.GAME_MEMORY_LEVELS || route == Routes.GAME_MAZE_LEVELS ||
        route == Routes.GAME_WORD_SEARCH_LEVELS -> MusicTrack.LOBBY
    // En los juegos se escuchan frases en Yuhmu, así que la música se pausa.
    route == Routes.GAME_MAICITO_PIDE || route == Routes.GAME_MERCADO -> null
    route == Routes.LEARNING_PATH || route == Routes.LESSON_INTRO -> MusicTrack.CAMINO
    route == Routes.GREETING_EXERCISE || route == Routes.LESSON_COMPLETE ||
        route == Routes.PATH_LESSON -> null
    else -> MusicTrack.INICIO
}
