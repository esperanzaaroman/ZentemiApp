package com.zentemi.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zentemi.app.R
import com.zentemi.app.ui.theme.SplashGlow

/** The Zentemi mascot floating over a pulsing golden glow. */
@Composable
fun MascotGlow(
    modifier: Modifier = Modifier,
    glowSize: Dp = 300.dp,
    mascotSize: Dp = 170.dp
) {
    val anim = rememberInfiniteTransition(label = "mascot")

    // Gentle floating motion for the mascot
    val mascotFloat by anim.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotFloat"
    )

    // Pulsing golden glow behind the mascot
    val glowScale by anim.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = modifier.size(glowSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(glowScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SplashGlow.copy(alpha = 0.55f),
                            SplashGlow.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
        Image(
            painter = painterResource(id = R.drawable.mascota),
            contentDescription = "Mascota de Zentemi",
            modifier = Modifier
                .size(mascotSize)
                .graphicsLayer { translationY = mascotFloat.dp.toPx() }
        )
    }
}
