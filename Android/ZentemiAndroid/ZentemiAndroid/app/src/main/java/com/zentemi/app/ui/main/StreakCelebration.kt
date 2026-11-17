package com.zentemi.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.Purple

/** Ventana "¡Llevas N días de racha!", igual que streakCelebrationOverlay en HomeView.swift. */
@Composable
fun StreakCelebration(
    visible: Boolean,
    appState: AppState,
    onContinue: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.75f)),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.85f)
    ) {
        val bounce = rememberInfiniteTransition(label = "flame")
        val flameScale by bounce.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "flameScale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                // Bloquea toques a la pantalla de atrás.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .widthIn(max = 340.dp)
                    .shadow(20.dp, RoundedCornerShape(25.dp))
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color.White)
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF9500),
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer {
                            scaleX = flameScale
                            scaleY = flameScale
                        }
                )

                Text(
                    text = "¡Felicidades!",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Purple
                )

                Text(
                    text = "¡Llevas ${appState.streak} ${if (appState.streak == 1) "día" else "días"} de racha!",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Destellos alrededor de Maicito (3 filas de 4, como iOS).
                    repeat(12) { index ->
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = if (index % 2 == 0) Color(0xFFFFCC00) else Color(0xFFFF9500),
                            modifier = Modifier
                                .size(18.dp)
                                .offset(
                                    x = ((index % 4) * 65 - 95).dp,
                                    y = ((index / 4) * 70 - 70).dp
                                )
                        )
                    }
                    PetView(appState = appState, size = 175.dp)
                }

                Text(
                    text = "¡Sigue así! ${appState.petName} está orgulloso de ti.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                ChunkyButton(
                    text = "¡Continuar!",
                    onClick = onContinue,
                    height = 52.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
