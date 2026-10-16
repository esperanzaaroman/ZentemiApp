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
import com.zentemi.app.ui.theme.*

private data class LessonInfo(
    val number: Int,
    val name: String,
    val emoji: String,
    val xOffset: Int
)

@Composable
fun LearningPathScreen(
    appState: AppState,
    onBack: () -> Unit,
    onLessonSelected: (Int) -> Unit
) {
    val lessons = listOf(
        LessonInfo(1, "Saludos", "✅", -85),
        LessonInfo(2, "Familia", "👨‍👩‍👧‍👦", 75),
        LessonInfo(3, "Números", "#️⃣", -70),
        LessonInfo(4, "Colores", "🎨", 80),
        LessonInfo(5, "Animales", "🐾", -75),
        LessonInfo(6, "Alimentos", "🍽️", 75),
        LessonInfo(7, "Frases básicas", "💬", -65)
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
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLight.copy(alpha = 0.96f))
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .shadow(5.dp, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Purple,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Tu camino",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${appState.pathLessonsCompleted} / 7",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Turquoise
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { appState.pathLessonsCompleted / 7f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Turquoise,
                    trackColor = Color.Gray.copy(alpha = 0.1f)
                )
            }

            // Lesson nodes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 24.dp, bottom = 70.dp),
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
                            // Circle
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .shadow(
                                        elevation = if (isUnlocked) 8.dp else 0.dp,
                                        shape = CircleShape,
                                        ambientColor = if (isCompleted) Turquoise.copy(alpha = 0.25f)
                                        else Purple.copy(alpha = 0.25f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> Turquoise
                                            isUnlocked -> Purple
                                            else -> LockedNode
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    Text(
                                        text = lesson.emoji,
                                        fontSize = 20.sp
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = Purple.copy(alpha = 0.28f),
                                        modifier = Modifier.size(20.dp)
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
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SubText,
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
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(PathDot)
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

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Text(
                        text = "Entendido",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
