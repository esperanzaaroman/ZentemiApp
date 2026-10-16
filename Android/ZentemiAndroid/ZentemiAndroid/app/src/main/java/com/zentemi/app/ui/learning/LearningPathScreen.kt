package com.zentemi.app.ui.learning

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import androidx.compose.foundation.border
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import com.zentemi.app.ui.components.BackCircleButton
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.HeaderTitle
import com.zentemi.app.ui.components.WaveHeader
import com.zentemi.app.ui.theme.*

private data class LessonInfo(
    val number: Int,
    val name: String,
    val icon: ImageVector,
    val xOffset: Int
)

@Composable
fun LearningPathScreen(
    appState: AppState,
    onBack: () -> Unit,
    onLessonSelected: (Int) -> Unit
) {
    val lessons = listOf(
        LessonInfo(1, "Saludos", Icons.Filled.WavingHand, -85),
        LessonInfo(2, "Familia", Icons.Filled.FamilyRestroom, 75),
        LessonInfo(3, "Números", Icons.Filled.Numbers, -70),
        LessonInfo(4, "Colores", Icons.Filled.Palette, 80),
        LessonInfo(5, "Animales", Icons.Filled.Pets, -75),
        LessonInfo(6, "Alimentos", Icons.Filled.Restaurant, 75),
        LessonInfo(7, "Frases básicas", Icons.Filled.Forum, -65)
    )

    var lockedLessonName by remember { mutableStateOf<String?>(null) }

    val bgAnim = rememberInfiniteTransition(label = "bg")
    val circleOffset by bgAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circleOffset"
    )

    val ringPulse by bgAnim.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Animated background circles
        Box(
            modifier = Modifier
                .size(230.dp)
                .offset(
                    x = (-145 - circleOffset * 40).dp,
                    y = (-270 - circleOffset * 50).dp
                )
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.045f))
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(
                    x = (155 + circleOffset * 40).dp,
                    y = (80 + circleOffset * 50).dp
                )
                .clip(CircleShape)
                .background(Turquoise.copy(alpha = 0.055f))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header with waves
            WaveHeader {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BackCircleButton(onClick = onBack, lightBackground = false)
                        Spacer(modifier = Modifier.width(14.dp))
                        HeaderTitle(title = "Tu camino", subtitle = "Aprende Yuhmu paso a paso")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${appState.pathLessonsCompleted} / 7",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoinGoldDark,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SplashDotActive)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { appState.pathLessonsCompleted / 7f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = SplashDotActive,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }

            // Lesson nodes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                lessons.forEachIndexed { index, lesson ->
                    val isUnlocked = lesson.number <= appState.unlockedPathLesson
                    val isCompleted = lesson.number <= appState.pathLessonsCompleted

                    // Lesson node
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = lesson.xOffset.dp)
                            .clickable {
                                if (isUnlocked && (lesson.number == 1 || lesson.number == 2)) {
                                    onLessonSelected(lesson.number)
                                } else if (!isUnlocked) {
                                    lockedLessonName = lesson.name
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Circle (3D: darker lip underneath)
                            val isCurrent = isUnlocked && !isCompleted
                            val face = when {
                                isCompleted -> Turquoise
                                isUnlocked -> Purple
                                else -> LockedNode
                            }
                            val lip = when {
                                isCompleted -> Color(0xFF1E9A8A)
                                isUnlocked -> PurpleDark
                                else -> Color(0xFFC9C2DE)
                            }
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .then(
                                        if (isCurrent) Modifier
                                            .scale(ringPulse)
                                            .border(4.dp, SplashDotActive, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(66.dp)
                                        .offset(y = 3.dp)
                                        .clip(CircleShape)
                                        .background(lip)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(66.dp)
                                        .offset(y = (-2).dp)
                                        .clip(CircleShape)
                                        .background(Brush.verticalGradient(listOf(lerp(face, Color.White, 0.15f), face))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isCompleted -> Icons.Filled.Check
                                            isUnlocked -> lesson.icon
                                            else -> Icons.Filled.Lock
                                        },
                                        contentDescription = null,
                                        tint = if (isUnlocked) Color.White else Purple.copy(alpha = 0.3f),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            // Label
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(
                                    alpha = if (isUnlocked) 0.95f else 0.5f
                                )
                            ) {
                                Text(
                                    text = lesson.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) DarkText else SubText.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Path dots between nodes
                    if (index < lessons.size - 1) {
                        val goesRight = index % 2 == 0
                        PathDots(goesRight = goesRight)
                    }
                }
            }
        }

        // Locked overlay
        lockedLessonName?.let { name ->
            LockedOverlay(
                lessonName = name,
                lessons = lessons.map { it.name },
                onDismiss = { lockedLessonName = null }
            )
        }
    }
}

@Composable
private fun PathDots(goesRight: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        val startX = if (goesRight) -70f else 70f
        val endX = if (goesRight) 70f else -70f

        repeat(6) { index ->
            val progress = index / 5f
            Box(
                modifier = Modifier
                    .offset(
                        x = (startX + (endX - startX) * progress).dp,
                        y = (index * 12 - 30).dp
                    )
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.22f))
            )
        }
    }
}

@Composable
private fun LockedOverlay(
    lessonName: String,
    lessons: List<String>,
    onDismiss: () -> Unit
) {
    val index = lessons.indexOf(lessonName)
    val message = if (index > 0) {
        "Completa ${lessons[index - 1]} para desbloquear esta lección."
    } else {
        "Completa la lección anterior para continuar."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 15.dp,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(27.dp)
                )

                Spacer(modifier = Modifier.height(13.dp))

                Text(
                    text = "Lección bloqueada",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = SubText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                ChunkyButton(
                    text = "Entendido",
                    onClick = onDismiss,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
    }
}
