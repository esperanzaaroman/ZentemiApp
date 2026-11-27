package com.zentemi.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private val EaseOutQuint = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Entrada al abrir la pantalla: el bloque sube unos pocos dp y aparece.
 * [order] escalona los bloques (0, 1, 2…) para que se lean de arriba abajo.
 * Respeta la escala de animación del sistema (si el usuario la apaga, no se mueve).
 */
fun Modifier.enterOnce(order: Int, distance: Int = 14): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            1f,
            tween(durationMillis = 420, delayMillis = 50 + order * 70, easing = EaseOutQuint)
        )
    }
    this.graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * distance.dp.toPx()
    }
}
