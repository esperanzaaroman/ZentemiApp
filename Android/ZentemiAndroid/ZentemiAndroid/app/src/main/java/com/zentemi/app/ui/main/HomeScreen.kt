package com.zentemi.app.ui.main

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import com.zentemi.app.R
import androidx.compose.material.icons.automirrored.filled.MenuBook
import com.zentemi.app.ui.components.MaicitoBackdrop
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.LightAuthBackground
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.components.HomeScene
import androidx.compose.ui.text.style.TextOverflow
import com.zentemi.app.ui.theme.*

private val CoinYellow = Color(0xFFF2B705)
private val StreakRed = Color(0xFFF0483E)

@Composable
fun HomeScreen(
    appState: AppState,
    onPlayPressed: () -> Unit,
    onFeedPressed: () -> Unit,
    onPlayWithPressed: () -> Unit
) {
    var showCoinsInfo by remember { mutableStateOf(false) }
    // Celebración de racha: una vez al día (igual que iOS).
    var showStreakCelebration by remember { mutableStateOf(appState.shouldCelebrateStreak) }
    LaunchedEffect(appState.streak) {
        if (appState.shouldCelebrateStreak) showStreakCelebration = true
    }
    var showStreakInfo by remember { mutableStateOf(false) }

    // Mascot "breathing" (stretches up from its feet)
    val anim = rememberInfiniteTransition(label = "home")
    val mascotScaleY by anim.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotScaleY"
    )
    val glowScale by anim.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    LightAuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 72.dp), // space for the tab bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Arriba: saludo (izquierda), monedas y racha (derecha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¡Hola, ${appState.userName}!",
                        fontFamily = Quicksand,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Qué gusto verte hoy",
                        fontFamily = Quicksand,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubText.copy(alpha = 0.8f)
                    )
                }
                StatBadge(
                    icon = R.drawable.ic_moneda,
                    value = "${appState.coins}",
                    tint = Color(0xFFFFF1C7),
                    textColor = CoinGoldDark,
                    onClick = { showCoinsInfo = true }
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatBadge(
                    icon = R.drawable.ic_racha,
                    value = "${appState.streak}",
                    tint = Color(0xFFFFE2DD),
                    textColor = Color(0xFFC2362C),
                    onClick = { showStreakInfo = true }
                )
            }

            // Paisaje con Maicito y su nombre
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                HomeScene(modifier = Modifier.matchParentSize())

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NameBubble(name = appState.petName)
                    Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.size(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                        }
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    SplashGlow.copy(alpha = 0.35f),
                                    Purple.copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Fondo comprado en la tienda (si hay uno puesto)
                appState.equippedBackdrop?.backdrop?.let { backdrop ->
                    MaicitoBackdrop(
                        backdrop = backdrop,
                        modifier = Modifier
                            .size(215.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                    )
                }
                // Sombrita bajo los pies de Maicito
                Box(
                    modifier = Modifier
                        .offset(y = 92.dp)
                        .size(width = 120.dp, height = 16.dp)
                        .drawBehind { drawOval(Purple.copy(alpha = 0.13f)) }
                )
                PetView(
                    appState = appState,
                    size = 190.dp,
                    modifier = Modifier.graphicsLayer {
                        scaleY = mascotScaleY
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                )
            }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Happiness card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Purple.copy(alpha = 0.18f), spotColor = Purple.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.97f))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Felicidad de ${appState.petName}",
                        fontFamily = Quicksand,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${appState.happiness}%",
                        fontFamily = Quicksand,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Turquoise
                    )
                }

                LinearProgressIndicator(
                    progress = { appState.happiness / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Turquoise,
                    trackColor = Turquoise.copy(alpha = 0.15f),
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PetActionButton(
                        text = "Alimentar a ${appState.petName}",
                        onClick = onFeedPressed,
                        modifier = Modifier.weight(1f)
                    )
                    PetActionButton(
                        text = "Jugar con ${appState.petName}",
                        onClick = onPlayWithPressed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ir a las lecciones (el camino de aprendizaje)
            Button(
                onClick = onPlayPressed,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp), ambientColor = Purple.copy(alpha = 0.35f), spotColor = Purple.copy(alpha = 0.35f))
                    .height(56.dp)
            ) {
                ZentemiIcon(R.drawable.ic_comenzar_leccion, size = 30.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Comenzar lección",
                    fontFamily = Quicksand,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Info windows
        InfoOverlay(
            visible = showCoinsInfo,
            title = "Monedas",
            icon = R.drawable.ic_moneda,
            text = "Consigue monedas completando lecciones, retos y actividades.\n\nPuedes utilizarlas para obtener objetos y recompensas dentro de Zentemi.",
            color = CoinYellow,
            onClose = { showCoinsInfo = false }
        )
        InfoOverlay(
            visible = showStreakInfo,
            title = "Racha",
            icon = R.drawable.ic_racha,
            text = "Tu racha representa los días consecutivos que has practicado Yuhmu.\n\nCompleta actividades cada día para aumentarla y mantenerla.",
            color = StreakRed,
            onClose = { showStreakInfo = false }
        )

        StreakCelebration(
            visible = showStreakCelebration,
            appState = appState,
            onContinue = {
                appState.markStreakCelebrated()
                showStreakCelebration = false
            }
        )
    }
}

/** Monedas o racha: fondo de color suave, sin contorno, con el ícono grande. */
@Composable
private fun StatBadge(
    @DrawableRes icon: Int,
    value: String,
    tint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(16.dp), ambientColor = textColor.copy(alpha = 0.25f), spotColor = textColor.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(16.dp))
            .background(tint)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ZentemiIcon(icon, size = 28.dp)
        Text(
            text = value,
            fontFamily = Quicksand,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

/** Nombre de Maicito en una burbuja de diálogo, para que resalte. */
@Composable
private fun NameBubble(name: String) {
    val border = Purple.copy(alpha = 0.30f)
    Box(contentAlignment = Alignment.BottomCenter) {
        // Piquito de la burbuja (queda medio escondido detrás)
        Box(
            modifier = Modifier
                .offset(y = 6.dp)
                .size(14.dp)
                .graphicsLayer { rotationZ = 45f }
                .background(Color.White)
                .border(2.dp, border)
        )
        Text(
            text = name,
            fontFamily = Quicksand,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleDark,
            modifier = Modifier
                .shadow(6.dp, CircleShape, ambientColor = Purple.copy(alpha = 0.2f), spotColor = Purple.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, border, CircleShape)
                .padding(horizontal = 26.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PetActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Purple.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Quicksand,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleDark,
            textAlign = TextAlign.Center
        )
    }
}

/** Centered info card over a dimmed background; tapping outside closes it. */
@Composable
private fun InfoOverlay(
    visible: Boolean,
    title: String,
    @DrawableRes icon: Int,
    text: String,
    color: Color,
    onClose: () -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .padding(horizontal = 20.dp)
                    .animateEnterExit(enter = scaleIn(initialScale = 0.9f))
                    .shadow(15.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .background(color.copy(alpha = 0.12f))
                    .border(2.dp, color.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                    // Swallow taps on the card so they don't close it
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZentemiIcon(icon, size = 34.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontFamily = Quicksand,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Text(
                    text = text,
                    fontFamily = Quicksand,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = SubText,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )
                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.dp, Color.Transparent),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = color.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "Entendido",
                        fontFamily = Quicksand,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
            }
        }
    }
}
