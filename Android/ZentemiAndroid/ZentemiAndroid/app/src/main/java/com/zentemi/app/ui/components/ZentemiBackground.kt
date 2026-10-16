package com.zentemi.app.ui.components

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.zentemi.app.ui.theme.*

// Small twinkling dots scattered over the background (x/y as fraction of screen)
private data class Sparkle(val x: Float, val y: Float, val size: Dp, val color: Color, val delay: Int)

private val sparkles = listOf(
    Sparkle(0.16f, 0.21f, 5.dp, SplashGlow, 0),
    Sparkle(0.82f, 0.29f, 4.dp, Color.White, 600),
    Sparkle(0.86f, 0.68f, 6.dp, Color.White, 1200),
    Sparkle(0.24f, 0.76f, 4.dp, SplashGlow, 1800),
    Sparkle(0.70f, 0.12f, 3.dp, Color.White, 900)
)

private var activeBackgrounds = 0
private var restoreLightBars: Boolean? = null

/**
 * Purple gradient background shared by the splash and login screens:
 * drifting translucent circles, twinkling sparkles and light system bar icons.
 */
@Composable
fun ZentemiBackground(content: @Composable BoxScope.() -> Unit) {

    // Light status/navigation bar icons over the purple background. Counted so that
    // navigating between two purple screens doesn't restore dark icons mid-transition.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (activeBackgrounds++ == 0) {
            restoreLightBars = controller?.isAppearanceLightStatusBars
        }
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            val restore = restoreLightBars
            if (--activeBackgrounds == 0 && controller != null && restore != null) {
                controller.isAppearanceLightStatusBars = restore
                controller.isAppearanceLightNavigationBars = restore
            }
        }
    }

    val anim = rememberInfiniteTransition(label = "background")

    // Slow drift for the large background circles
    val circleDrift by anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circleDrift"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SplashTop, SplashBottom)
                )
            )
    ) {
        val w = maxWidth
        val h = maxHeight

        // Large translucent circles (top-right and bottom-left)
        Box(
            modifier = Modifier
                .size(w * 0.62f)
                .offset(
                    x = w * 0.58f + (circleDrift * 10).dp,
                    y = -(w * 0.18f) + (circleDrift * 8).dp
                )
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(w * 0.6f)
                .offset(
                    x = -(w * 0.2f) - (circleDrift * 8).dp,
                    y = h * 0.7f - (circleDrift * 10).dp
                )
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )

        // Sparkles
        sparkles.forEach { s ->
            val twinkle by anim.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1600, delayMillis = s.delay, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sparkle"
            )
            Box(
                modifier = Modifier
                    .offset(x = w * s.x, y = h * s.y)
                    .size(s.size)
                    .alpha(twinkle)
                    .clip(CircleShape)
                    .background(s.color)
            )
        }

        content()
    }
}
