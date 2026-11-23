package com.zentemi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.zentemi.app.ui.theme.BackgroundLight
import com.zentemi.app.ui.theme.Purple
import com.zentemi.app.ui.theme.SplashGlow
import com.zentemi.app.ui.theme.Turquoise

/**
 * Paisaje suave detrás de Maicito en Inicio: sol, nubes, cerritos y matas de maíz
 * en colores pastel, para que la pantalla no se vea vacía sin distraer.
 * La parte de abajo se desvanece hacia el fondo de la pantalla.
 */
@Composable
fun HomeScene(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sol con resplandor
        val sun = Offset(w * 0.80f, h * 0.20f)
        drawCircle(
            Brush.radialGradient(listOf(SplashGlow.copy(alpha = 0.45f), Color.Transparent), center = sun, radius = w * 0.26f),
            radius = w * 0.26f, center = sun
        )
        drawCircle(Color(0xFFFFE3A0), radius = w * 0.07f, center = sun)

        // Nubes
        cloud(Offset(w * 0.18f, h * 0.16f), w * 0.05f)
        cloud(Offset(w * 0.62f, h * 0.08f), w * 0.035f)

        // Cerro de atrás (lila)
        val back = Path().apply {
            moveTo(0f, h * 0.62f)
            cubicTo(w * 0.22f, h * 0.48f, w * 0.40f, h * 0.56f, w * 0.55f, h * 0.52f)
            cubicTo(w * 0.72f, h * 0.47f, w * 0.88f, h * 0.55f, w, h * 0.50f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(back, Purple.copy(alpha = 0.10f))

        // Matas de maíz a los lados, sobre el cerro de atrás
        cornPlant(Offset(w * 0.08f, h * 0.70f), h * 0.22f)
        cornPlant(Offset(w * 0.17f, h * 0.72f), h * 0.16f)
        cornPlant(Offset(w * 0.90f, h * 0.68f), h * 0.20f)

        // Cerro de enfrente (menta)
        val front = Path().apply {
            moveTo(0f, h * 0.76f)
            cubicTo(w * 0.25f, h * 0.68f, w * 0.45f, h * 0.74f, w * 0.62f, h * 0.71f)
            cubicTo(w * 0.80f, h * 0.68f, w * 0.92f, h * 0.73f, w, h * 0.70f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(front, Turquoise.copy(alpha = 0.13f))

        // Florecitas sueltas
        listOf(0.26f to 0.80f, 0.74f to 0.78f, 0.86f to 0.84f).forEach { (fx, fy) ->
            flower(Offset(w * fx, h * fy), w * 0.012f)
        }

        // Se desvanece hacia el fondo de la pantalla
        drawRect(
            Brush.verticalGradient(
                0.80f to Color.Transparent,
                1f to BackgroundLight,
                startY = 0f, endY = h
            )
        )
    }
}

private fun DrawScope.cloud(c: Offset, s: Float) {
    val color = Color.White.copy(alpha = 0.85f)
    drawCircle(color, s, c)
    drawCircle(color, s * 0.75f, c + Offset(-s, s * 0.25f))
    drawCircle(color, s * 0.75f, c + Offset(s, s * 0.25f))
    drawRect(color, topLeft = c + Offset(-s * 1.6f, s * 0.1f), size = Size(s * 3.2f, s * 0.9f))
}

private fun DrawScope.cornPlant(base: Offset, height: Float) {
    val green = Color(0xFF7BC67E).copy(alpha = 0.55f)
    val top = base + Offset(0f, -height)
    drawLine(green, base, top, strokeWidth = height * 0.06f)
    for (k in 0..2) {
        val y = base.y - height * (0.25f + k * 0.22f)
        val dir = if (k % 2 == 0) 1f else -1f
        val leaf = Path().apply {
            moveTo(base.x, y)
            quadraticTo(base.x + dir * height * 0.22f, y - height * 0.20f, base.x + dir * height * 0.30f, y - height * 0.04f)
            quadraticTo(base.x + dir * height * 0.12f, y - height * 0.04f, base.x, y + height * 0.03f)
            close()
        }
        drawPath(leaf, green)
    }
    // Elotito
    drawOval(
        Color(0xFFFFD36B).copy(alpha = 0.75f),
        topLeft = base + Offset(height * 0.02f, -height * 0.62f),
        size = Size(height * 0.09f, height * 0.20f)
    )
}

private fun DrawScope.flower(c: Offset, r: Float) {
    val petal = Color(0xFFFFB3CF).copy(alpha = 0.8f)
    drawCircle(petal, r, c + Offset(-r, 0f))
    drawCircle(petal, r, c + Offset(r, 0f))
    drawCircle(petal, r, c + Offset(0f, -r))
    drawCircle(petal, r, c + Offset(0f, r))
    drawCircle(Color(0xFFFFD36B), r * 0.8f, c)
}
