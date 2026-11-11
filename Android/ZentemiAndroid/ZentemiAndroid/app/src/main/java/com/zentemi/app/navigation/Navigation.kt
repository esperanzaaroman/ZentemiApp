package com.zentemi.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.learning.GreetingExerciseScreen
import com.zentemi.app.ui.learning.LearningPathScreen
import com.zentemi.app.ui.learning.LessonCompleteScreen
import com.zentemi.app.ui.learning.LessonIntroScreen
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

    fun lessonIntro(lessonNumber: Int) = "lesson_intro/$lessonNumber"
    fun lessonComplete(correct: Int, total: Int, elapsed: Int) =
        "lesson_complete/$correct/$total/$elapsed"
}

@Composable
fun ZentemiNavigation(appState: AppState = viewModel()) {

    val navController = rememberNavController()

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
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
