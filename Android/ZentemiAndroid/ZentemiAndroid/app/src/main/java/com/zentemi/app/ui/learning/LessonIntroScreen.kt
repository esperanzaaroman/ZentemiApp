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
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*

private enum class LessonInfoType {
    EXERCISES, TIME, COINS
}

@Composable
fun LessonIntroScreen(
    appState: AppState,
    lessonNumber: Int,
    onBack: () -> Unit,
    onStartExercise: () -> Unit
) {
    val title = when (lessonNumber) {
        1 -> "Saludos básicos"
        2 -> "Mi familia"
        else -> "Lección $lessonNumber"
    }

    val description = when (lessonNumber) {
        1 -> "Aprende a saludar y despedirte en Yuhmu como lo hacen en San Juan Ixtenco."
        2 -> "Conoce las palabras para referirte a los miembros de tu familia en Yuhmu."
        else -> "Aprende nuevas palabras y frases en Yuhmu."
    }

    val exercises = when (lessonNumber) {
        1 -> appState.greetingsExerciseCount
        else -> 0
    }

    val estimatedMinutes = when (lessonNumber) {
        1 -> 3
        2 -> 5
        else -> 5
    }

    val reward = 700

    var selectedInfo by remember { mutableStateOf<LessonInfoType?>(null) }

    val breathe = rememberInfiniteTransition(label = "breathe")
    val mascotScaleY by breathe.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotScaleY"
    )

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
                .size(220.dp)
                .offset(
                    x = (150 + circleOffset * 35).dp,
                    y = (-100 + circleOffset * 45).dp
                )
                .clip(CircleShape)
                .background(Turquoise.copy(alpha = 0.055f))
        )
        Box(
            modifier = Modifier
                .size(170.dp)
                .offset(
                    x = (145 + circleOffset * 30).dp,
                    y = (350 + circleOffset * 40).dp
                )
                .clip(CircleShape)
                .background(Color.Yellow.copy(alpha = 0.055f))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 15.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Lección $lessonNumber de 7",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = SubText
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lesson card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(18.dp))

                        // Badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Purple.copy(alpha = 0.09f)
                        ) {
                            Text(
                                text = "NUEVA SECCIÓN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        // Mascot
                        Box(
                            modifier = Modifier.height(125.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            PetView(appState = appState, size = 125.dp)
                        }

                        Spacer(modifier = Modifier.height(13.dp))

                        // Title
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = SubText,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 25.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                        // Info rows
                        Column(
                            modifier = Modifier.padding(horizontal = 17.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            InfoRow(
                                icon = Icons.Filled.CheckCircle,
                                text = "$exercises ejercicios",
                                color = Turquoise,
                                onClick = { selectedInfo = LessonInfoType.EXERCISES }
                            )

                            InfoRow(
                                icon = Icons.Filled.Schedule,
                                text = "~$estimatedMinutes min",
                                color = Purple,
                                onClick = { selectedInfo = LessonInfoType.TIME }
                            )

                            InfoRow(
                                icon = Icons.Filled.MonetizationOn,
                                text = "+$reward monedas",
                                color = CoinGold,
                                onClick = { selectedInfo = LessonInfoType.COINS }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Start button
                Button(
                    onClick = { if (exercises > 0) onStartExercise() },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    shape = RoundedCornerShape(14.dp),
                    enabled = exercises > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Comenzar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Info overlay
        selectedInfo?.let { info ->
            LessonInfoOverlay(
                infoType = info,
                exercises = exercises,
                estimatedMinutes = estimatedMinutes,
                reward = reward,
                onDismiss = { selectedInfo = null }
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(11.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.45f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun LessonInfoOverlay(
    infoType: LessonInfoType,
    exercises: Int,
    estimatedMinutes: Int,
    reward: Int,
    onDismiss: () -> Unit
) {
    val (icon, title, message) = when (infoType) {
        LessonInfoType.EXERCISES -> Triple(
            Icons.Filled.CheckCircle,
            "Ejercicios",
            "Esta sección contiene $exercises ejercicios para practicar lo aprendido."
        )
        LessonInfoType.TIME -> Triple(
            Icons.Filled.Schedule,
            "Tiempo estimado",
            "Los ~$estimatedMinutes minutos son solo un tiempo estimado. Puedes avanzar a tu propio ritmo y tomarte el tiempo que necesites."
        )
        LessonInfoType.COINS -> Triple(
            Icons.Filled.MonetizationOn,
            "Recompensa",
            "Al completar esta sección recibirás $reward monedas para utilizar dentro de Zentemi."
        )
    }

    val iconColor = when (infoType) {
        LessonInfoType.EXERCISES -> Turquoise
        LessonInfoType.TIME -> Purple
        LessonInfoType.COINS -> CoinGold
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 15.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(27.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = SubText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
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
