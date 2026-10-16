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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
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
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Purple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Purple.copy(alpha = 0.10f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(exerciseNumber.toFloat() / totalExercises.toFloat())
                            .clip(RoundedCornerShape(7.dp))
                            .background(Brush.horizontalGradient(listOf(Turquoise, SuccessGreen)))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PetView(appState = appState, size = 86.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "¿Cómo se dice «hola»?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        lineHeight = 27.sp,
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Purple.copy(alpha = 0.2f), spotColor = Purple.copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }

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
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = if (isAudioPlaying) "Reproduciendo..." else "Escuchar audio",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
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

                    val accent = when {
                        wasVerified && isCorrect -> SuccessGreen
                        isWrongSelection -> ErrorRed
                        isSelected -> Purple
                        else -> LockedNode
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            wasVerified && isCorrect -> SuccessGreen.copy(alpha = 0.08f).compositeOver(Color.White)
                            isWrongSelection -> ErrorRed.copy(alpha = 0.08f).compositeOver(Color.White)
                            isSelected -> Purple.copy(alpha = 0.06f).compositeOver(Color.White)
                            else -> Color.White
                        },
                        border = BorderStroke(2.dp, accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(74.dp)
                            // 3D lip under the card
                            .drawBehind {
                                drawRoundRect(
                                    color = accent,
                                    topLeft = androidx.compose.ui.geometry.Offset(0f, 4.dp.toPx()),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                                )
                            }
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
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected || (wasVerified && isCorrect)) accent
                                        else Color(0xFFF2F0F8)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (number) {
                                        1 -> "A"; 2 -> "B"; 3 -> "C"; 4 -> "D"; else -> ""
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected || (wasVerified && isCorrect)) Color.White
                                    else Color.Gray.copy(alpha = 0.55f)
                                )
                            }

                            Spacer(modifier = Modifier.width(13.dp))

                            Text(
                                text = answer,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) accent else DarkText
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (wasVerified && isCorrect) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else if (isWrongSelection) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    if (index < answers.size - 1) {
                        Spacer(modifier = Modifier.height(14.dp))
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
            ChunkyButton(
                text = if (verifiedAnswer == null) "Verificar" else "Continuar",
                onClick = {
                    val sel = selectedAnswer ?: return@ChunkyButton

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
                color = when {
                    verifiedAnswer == null -> Purple
                    verifiedAnswer == correctAnswer -> SuccessGreen
                    else -> ErrorRed
                },
                lipColor = when {
                    verifiedAnswer == null -> PurpleDark
                    verifiedAnswer == correctAnswer -> Color(0xFF2E9E4D)
                    else -> Color(0xFFD94848)
                },
                enabled = selectedAnswer != null,
                height = 54.dp,
                modifier = Modifier.fillMaxWidth()
            )
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
