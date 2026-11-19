package com.zentemi.app.ui.games

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
    modifier: Modifier = Modifier
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
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < stars) SplashDotActive else Color.Gray.copy(alpha = 0.30f),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            when (rewardGiven) {
                true -> Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (coinsEarned > 0) {
                        RewardChip(Icons.Filled.MonetizationOn, "+$coinsEarned", Color(0xFFFF9500))
                    }
                    RewardChip(Icons.Filled.Favorite, "+$GAME_HAPPINESS_REWARD%", Color(0xFFFF2D55))
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
private fun RewardChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
