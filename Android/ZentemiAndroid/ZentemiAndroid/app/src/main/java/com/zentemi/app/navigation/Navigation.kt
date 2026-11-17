package com.zentemi.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zentemi.app.data.AuthService
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.GreetingExerciseScreen
import com.zentemi.app.ui.learning.LearningPathScreen
import com.zentemi.app.ui.learning.LessonCompleteScreen
import com.zentemi.app.ui.learning.LessonIntroScreen
import com.zentemi.app.ui.main.MainScreen
import com.zentemi.app.ui.onboarding.EmailVerificationScreen
import com.zentemi.app.ui.onboarding.LoginScreen
import com.zentemi.app.ui.onboarding.NameScreen
import com.zentemi.app.ui.onboarding.OnboardingScreen
import com.zentemi.app.ui.onboarding.SignInScreen
import com.zentemi.app.ui.settings.SettingsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val NAME = "name"
    const val SIGN_IN = "sign_in"
    const val EMAIL_VERIFICATION = "email_verification"
    const val MAIN = "main"
    const val LEARNING_PATH = "learning_path"
    const val LESSON_INTRO = "lesson_intro/{lessonNumber}"
    const val GREETING_EXERCISE = "greeting_exercise"
    const val LESSON_COMPLETE = "lesson_complete/{correct}/{total}/{elapsed}"
    const val SETTINGS = "settings"

    /** Pantallas accesibles sin sesión iniciada. */
    val publicRoutes = setOf(ONBOARDING, LOGIN, NAME, SIGN_IN)

    fun lessonIntro(lessonNumber: Int) = "lesson_intro/$lessonNumber"
    fun lessonComplete(correct: Int, total: Int, elapsed: Int) =
        "lesson_complete/$correct/$total/$elapsed"
}

@Composable
fun ZentemiNavigation(appState: AppState = viewModel()) {

    val navController = rememberNavController()

    // ── Puerta de autenticación (igual que ContentView en iOS) ──
    // Sin sesión → pantallas públicas; sesión sin verificar → verificación
    // de correo; sesión verificada → app principal.
    val isAuthenticated by AuthService.isAuthenticated.collectAsState()
    val isEmailVerified by AuthService.isEmailVerified.collectAsState()

    val startDestination = remember {
        when {
            !isAuthenticated -> Routes.ONBOARDING
            !isEmailVerified -> Routes.EMAIL_VERIFICATION
            else -> Routes.MAIN
        }
    }

    LaunchedEffect(isAuthenticated, isEmailVerified) {
        val current = navController.currentDestination?.route ?: return@LaunchedEffect
        val target = when {
            !isAuthenticated ->
                if (current in Routes.publicRoutes) null else Routes.LOGIN
            !isEmailVerified ->
                if (current == Routes.EMAIL_VERIFICATION) null else Routes.EMAIL_VERIFICATION
            else ->
                if (current in Routes.publicRoutes || current == Routes.EMAIL_VERIFICATION) Routes.MAIN
                else null
        }
        if (target != null) {
            navController.navigate(target) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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

        // Registro familiar: nombre del menor + cuenta del tutor (Firebase Auth).
        // Al crearse la sesión, la puerta de autenticación lleva a la verificación.
        composable(Routes.NAME) {
            NameScreen(
                appState = appState,
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
            EmailVerificationScreen()
        }

        composable(Routes.MAIN) {
            MainScreen(
                appState = appState,
                onPlayPressed = {
                    navController.navigate(Routes.LEARNING_PATH)
                },
                onSettingsPressed = {
                    navController.navigate(Routes.SETTINGS)
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
                    navController.navigate(Routes.GREETING_EXERCISE)
                }
            )
        }

        composable(Routes.GREETING_EXERCISE) {
            GreetingExerciseScreen(
                appState = appState,
                exerciseNumber = 1,
                totalExercises = 1,
                onBack = { navController.popBackStack() },
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

        composable(Routes.SETTINGS) {
            SettingsScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onLogout = { AuthService.signOut() }
            )
        }
    }
}
