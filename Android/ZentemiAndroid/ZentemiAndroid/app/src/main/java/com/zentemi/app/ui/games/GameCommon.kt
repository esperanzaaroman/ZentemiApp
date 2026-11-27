package com.zentemi.app.ui.games

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.border
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay

/** Felicidad que da cada partida con recompensa. */
const val GAME_HAPPINESS_REWARD = 5

/** Monedas por estrella ganada. */
const val GAME_COINS_PER_STAR = 5

/** Estrellas (0-3) según la proporción de aciertos, igual que en las lecciones. */
fun starsFor(correct: Int, total: Int): Int {
    if (total <= 0) return 0
    val ratio = correct.toFloat() / total
    return when {
        ratio >= 0.8f -> 3
        ratio >= 0.5f -> 2
        correct > 0 -> 1
        else -> 0
    }
}

/** Barra superior de los juegos: cerrar, nombre del juego y progreso. */
@Composable
fun GameTopBar(
    title: String,
    progress: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    /** Si se da, el título se dibuja como una etiqueta festiva con este ícono. */
    @DrawableRes titleIcon: Int? = null,
    /** Colores del degradado de la etiqueta festiva. */
    titleColors: List<Color> = listOf(Color(0xFFFF9F1C), Color(0xFFFF4F98))
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Purple)
        }

        Spacer(modifier = Modifier.weight(1f))

        if (titleIcon != null) {
            FunGameTitle(title = title, icon = titleIcon, colors = titleColors)
        } else {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Purple,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = progress,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Turquoise,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        )
    }
}

/**
 * Ventana final de un juego: Maicito, estrellas, recompensa y botones.
 * Entrega la recompensa una sola vez al aparecer.
 */
@Composable
fun GameResultOverlay(
    appState: AppState,
    title: String,
    message: String,
    stars: Int,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val coinsEarned = stars * GAME_COINS_PER_STAR
    var rewardGiven by remember { mutableStateOf<Boolean?>(null) }
    var visibleStars by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        rewardGiven = appState.claimGameReward(coinsEarned, GAME_HAPPINESS_REWARD)
        SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
        repeat(3) {
            delay(220)
            visibleStars = it + 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(25.dp))
                .clip(RoundedCornerShape(25.dp))
                .background(Color.White)
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PetView(appState = appState, size = 110.dp)

            Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Purple)
            Text(message, fontSize = 16.sp, color = DarkText, textAlign = TextAlign.Center)

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.height(44.dp)) {
                repeat(3) { index ->
                    AnimatedVisibility(
                        visible = index < visibleStars,
                        enter = scaleIn(
                            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                        ) + fadeIn()
                    ) {
                        ZentemiIcon(R.drawable.ic_estrella, size = 46.dp, muted = index >= stars)
                    }
                }
            }

            when (rewardGiven) {
                true -> Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (coinsEarned > 0) {
                        RewardChip(R.drawable.ic_moneda, "+$coinsEarned", Color(0xFFFF9500))
                    }
                    RewardChip(R.drawable.ic_felicidad, "+$GAME_HAPPINESS_REWARD%", Color(0xFFFF2D55))
                }
                false -> Text(
                    "Ya usaste las ${appState.maxGameRewardsPerDay} recompensas de hoy. ¡Puedes seguir practicando!",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                null -> Unit
            }

            ChunkyButton(
                text = "Jugar otra vez",
                onClick = onPlayAgain,
                height = 52.dp,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = onExit) {
                Text("Salir", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun RewardChip(@DrawableRes icon: Int, text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ZentemiIcon(icon, size = 26.dp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

/**
 * Título festivo de un juego: etiqueta con degradado, borde blanco, ícono
 * en un circulito, un poco inclinada y con un "pop" al aparecer.
 */
@Composable
private fun FunGameTitle(title: String, @DrawableRes icon: Int, colors: List<Color>) {
    val pop = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
    }
    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .graphicsLayer {
                rotationZ = -3f
                scaleX = pop.value
                scaleY = pop.value
            }
            .shadow(8.dp, shape, spotColor = colors.last().copy(alpha = 0.6f))
            .clip(shape)
            .background(Brush.horizontalGradient(colors))
            .border(2.5.dp, Color.White, shape)
            .padding(start = 5.dp, end = 16.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            ZentemiIcon(icon, size = 24.dp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontFamily = Quicksand,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(Color.Black.copy(alpha = 0.25f), Offset(0f, 2f), blurRadius = 3f)
            )
        )
    }
}
