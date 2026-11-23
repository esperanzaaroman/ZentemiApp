package com.zentemi.app.ui.learning.primerasfrases

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import com.zentemi.app.R
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Encabezado de ejercicio: cerrar, barra de progreso y "n / total". */
@Composable
internal fun ExerciseHeader(
    exerciseNumber: Int,
    totalExercises: Int,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier
                .size(42.dp)
                .clickable { onClose() }
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
                    .fillMaxWidth(exerciseNumber.toFloat() / totalExercises.coerceAtLeast(1))
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
}

/** Botón "Escuchar audio" estilo cápsula morada. */
@Composable
internal fun ListenButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Purple)
            .clickable { onClick() }
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            ZentemiIcon(R.drawable.ic_escuchar_audio, size = 26.dp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isPlaying) "Reproduciendo..." else "Escuchar audio",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/** Tarjeta blanca con Maicito dando un consejo. */
@Composable
internal fun MaicitoTipCard(appState: AppState, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PetView(appState = appState, size = 75.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SubText
        )
    }
}

/** Mensaje de resultado con ícono de palomita o tache. */
@Composable
internal fun ResultLabel(success: Boolean, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (success) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (success) Turquoise else ErrorRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (success) Turquoise else ErrorRed,
            textAlign = TextAlign.Center
        )
    }
}

/** Sacudida horizontal de la pantalla al equivocarse. */
@Stable
internal class ShakeState {
    var offset by mutableFloatStateOf(0f)
        private set

    fun shake(scope: CoroutineScope) {
        scope.launch {
            for (value in listOf(-7f, 7f, -5f, 5f, 0f)) {
                offset = value
                delay(60)
            }
        }
    }
}

@Composable
internal fun rememberShakeState() = remember { ShakeState() }

/** Confeti que sube desde abajo, igual que en iOS. */
@Composable
internal fun ConfettiOverlay() {
    val confettiColors = listOf(
        Color(0xFF9C27B0),
        Color(0xFFFFEB3B),
        Color(0xFF4CAF50),
        Color(0xFFE91E63),
        Color(0xFF00BCD4),
        Color(0xFFFF9800)
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
                modifier = Modifier.offset(
                    x = (xPos * 340 + 20).dp,
                    y = (animatable.value * 800).dp
                )
            )
        }
    }
}

/** Muestra el confeti 1.6 s. */
internal fun CoroutineScope.flashConfetti(set: (Boolean) -> Unit) {
    launch {
        set(true)
        delay(1600)
        set(false)
    }
}
