package com.zentemi.app.ui.learning

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun GreetingExerciseScreen(
    appState: AppState,
    exerciseNumber: Int,
    totalExercises: Int,
    onBack: () -> Unit,
    onLessonCompleted: (correctAnswers: Int, totalAnswers: Int, elapsedSeconds: Int) -> Unit
) {
    val correctAnswer = 2 // "Hä nzengwa" is correct (index 1, but we use 1-based)
    val answers = listOf("Nzengwa", "Hä nzengwa", "Ra hyadi", "Nänä")

    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var verifiedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var errorShakeOffset by remember { mutableStateOf(0f) }

    val exerciseStartTime = remember { System.currentTimeMillis() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .offset(x = errorShakeOffset.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Gray.copy(alpha = 0.65f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.10f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(exerciseNumber.toFloat() / totalExercises.toFloat())
                            .clip(RoundedCornerShape(4.dp))
                            .background(Turquoise)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "$exerciseNumber / $totalExercises",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Turquoise
                )
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 120.dp)
            ) {
                // Question
                Text(
                    text = "¿Cómo se dice «hola»?",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Audio button
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (isAudioPlaying) Purple else Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        if (!isAudioPlaying) {
                            isAudioPlaying = true
                            scope.launch {
                                delay(2000)
                                isAudioPlaying = false
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 6.dp,
                            end = 14.dp,
                            top = 6.dp,
                            bottom = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isAudioPlaying) Color.White.copy(alpha = 0.18f)
                                    else Purple
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription = "Audio",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = if (isAudioPlaying) "Reproduciendo..." else "Escuchar audio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAudioPlaying) Color.White else Purple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Answer cards
                answers.forEachIndexed { index, answer ->
                    val number = index + 1
                    val isSelected = selectedAnswer == number
                    val wasVerified = verifiedAnswer != null
                    val isCorrect = number == correctAnswer
                    val isWrongSelection = wasVerified && isSelected && !isCorrect

                    Surface(
                        shape = RoundedCornerShape(13.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = if (isWrongSelection) {
                            BorderStroke(2.dp, Color.Red.copy(alpha = 0.70f))
                        } else if (isSelected) {
                            BorderStroke(2.dp, Purple)
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                            .clickable(enabled = verifiedAnswer == null) {
                                selectedAnswer = number
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Letter badge
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Purple
                                        else Color(0xFFF2F0F8)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (number) {
                                        1 -> "A"; 2 -> "B"; 3 -> "C"; 4 -> "D"; else -> ""
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White
                                    else Color.Gray.copy(alpha = 0.55f)
                                )
                            }

                            Spacer(modifier = Modifier.width(13.dp))

                            Text(
                                text = answer,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Purple else DarkText
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (wasVerified && isCorrect) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Turquoise,
                                    modifier = Modifier.size(15.dp)
                                )
                            } else if (isWrongSelection) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (index < answers.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Verify / Continue button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Button(
                onClick = {
                    val sel = selectedAnswer ?: return@Button

                    if (verifiedAnswer == null) {
                        verifiedAnswer = sel

                        if (sel == correctAnswer) {
                            showConfetti = true
                            scope.launch {
                                delay(1600)
                                showConfetti = false
                            }
                        } else {
                            // Error shake animation
                            scope.launch {
                                errorShakeOffset = -7f; delay(60)
                                errorShakeOffset = 7f; delay(60)
                                errorShakeOffset = -5f; delay(60)
                                errorShakeOffset = 5f; delay(60)
                                errorShakeOffset = 0f
                            }
                        }
                    } else {
                        if (exerciseNumber >= totalExercises) {
                            val elapsed = maxOf(
                                1,
                                ((System.currentTimeMillis() - exerciseStartTime) / 1000).toInt()
                            )
                            val correct = if (verifiedAnswer == correctAnswer) 1 else 0
                            onLessonCompleted(correct, 1, elapsed)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedAnswer == null) Purple.copy(alpha = 0.35f) else Purple
                ),
                shape = RoundedCornerShape(14.dp),
                enabled = selectedAnswer != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (verifiedAnswer == null) "Verificar" else "Continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Confetti
        if (showConfetti) {
            ConfettiOverlay()
        }
    }
}

@Composable
private fun ConfettiOverlay() {
    val confettiColors = listOf(
        Color(0xFF9C27B0), // purple
        Color(0xFFFFEB3B), // yellow
        Color(0xFF4CAF50), // green
        Color(0xFFE91E63), // pink
        Color(0xFF00BCD4), // cyan
        Color(0xFFFF9800)  // orange
    )

    val shapes = listOf("●", "■", "▲", "★")

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(30) { index ->
            val xPos = remember { Random.nextFloat() }
            val animatable = remember { Animatable(1f) }

            LaunchedEffect(Unit) {
                animatable.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 800 + (index % 5) * 120,
                        delayMillis = (index % 8) * 25,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            Text(
                text = shapes[index % shapes.size],
                fontSize = (7 + (index % 6)).sp,
                color = confettiColors[index % confettiColors.size],
                modifier = Modifier
                    .offset(
                        x = (xPos * 340 + 20).dp,
                        y = (animatable.value * 800).dp
                    )
            )
        }
    }
}
