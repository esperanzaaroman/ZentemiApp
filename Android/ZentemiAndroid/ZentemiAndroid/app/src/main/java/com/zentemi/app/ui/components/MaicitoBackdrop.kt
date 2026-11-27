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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.zentemi.app.data.Backdrop
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Fondo decorativo detrás de Maicito (artículos de la zona "Fondo").
 * Se dibuja en todo su tamaño; quien lo usa decide la forma (círculo, tarjeta…).
 */
@Composable
fun MaicitoBackdrop(backdrop: Backdrop, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        when (backdrop) {
            Backdrop.SUNSET -> drawSunset()
            Backdrop.STARRY_NIGHT -> drawStarryNight()
            Backdrop.PAPEL_PICADO -> drawPapelPicado()
            Backdrop.TALAVERA -> drawTalavera()
            Backdrop.MILPA -> drawMilpa()
            Backdrop.PLAYA -> drawPlaya()
            Backdrop.BOSQUE -> drawBosque()
            Backdrop.PIRAMIDE -> drawPiramide()
            Backdrop.ARCOIRIS -> drawArcoiris()
            Backdrop.DIA_DE_MUERTOS -> drawDiaDeMuertos()
        }
    }
}

private fun DrawScope.drawSunset() {
    drawRect(
        Brush.verticalGradient(
            listOf(Color(0xFFFFC371), Color(0xFFFF8A7A), Color(0xFFC86DD7))
        )
    )
    // Sol
    val r = size.minDimension * 0.22f
    drawCircle(Color(0xFFFFE29A), radius = r * 1.35f, center = Offset(size.width / 2, size.height * 0.78f), alpha = 0.35f)
    drawCircle(Color(0xFFFFD166), radius = r, center = Offset(size.width / 2, size.height * 0.78f))
    // Cerros
    val hills = Path().apply {
        moveTo(0f, size.height * 0.82f)
        cubicTo(size.width * 0.25f, size.height * 0.70f, size.width * 0.45f, size.height * 0.88f, size.width * 0.65f, size.height * 0.80f)
        cubicTo(size.width * 0.80f, size.height * 0.74f, size.width * 0.92f, size.height * 0.80f, size.width, size.height * 0.78f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(hills, Color(0xFF8E4FB8))
}

private fun DrawScope.drawStarryNight() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF141842), Color(0xFF3B2A7A))))
    // Estrellas en posiciones fijas (relativas al tamaño)
    val stars = listOf(
        0.12f to 0.18f, 0.30f to 0.10f, 0.52f to 0.22f, 0.74f to 0.12f, 0.88f to 0.30f,
        0.18f to 0.42f, 0.40f to 0.36f, 0.66f to 0.40f, 0.84f to 0.56f, 0.10f to 0.66f,
        0.28f to 0.74f, 0.58f to 0.62f, 0.76f to 0.80f, 0.46f to 0.86f, 0.92f to 0.88f
    )
    val base = size.minDimension
    stars.forEachIndexed { i, (x, y) ->
        val r = base * (if (i % 3 == 0) 0.018f else 0.011f)
        drawCircle(Color.White, radius = r, center = Offset(size.width * x, size.height * y), alpha = if (i % 2 == 0) 1f else 0.7f)
    }
    // Luna creciente
    val moonCenter = Offset(size.width * 0.78f, size.height * 0.22f)
    val moonR = base * 0.11f
    drawCircle(Color(0xFFFFE9A8), radius = moonR, center = moonCenter)
    drawCircle(Color(0xFF181C48), radius = moonR * 0.85f, center = moonCenter + Offset(moonR * 0.45f, -moonR * 0.25f))
}

private fun DrawScope.drawPapelPicado() {
    drawRect(Color(0xFFFFF4E0))
    val colors = listOf(
        Color(0xFFFF4FA3), Color(0xFF2EC4B6), Color(0xFFFFC93C),
        Color(0xFF7B4ACA), Color(0xFFFF8C42)
    )
    val rows = listOf(0.10f, 0.42f)
    rows.forEachIndexed { row, yFraction ->
        val y = size.height * yFraction
        val flagW = size.width / 5.5f
        val flagH = flagW * 1.05f
        // Cuerda
        drawLine(Color(0xFF8B5A2B), Offset(0f, y), Offset(size.width, y), strokeWidth = size.minDimension * 0.008f)
        var x = -flagW * 0.3f * row
        var i = row
        while (x < size.width) {
            val color = colors[i % colors.size]
            val flag = Path().apply {
                moveTo(x, y)
                lineTo(x + flagW * 0.9f, y)
                lineTo(x + flagW * 0.9f, y + flagH * 0.8f)
                lineTo(x + flagW * 0.45f, y + flagH)
                lineTo(x, y + flagH * 0.8f)
                close()
            }
            drawPath(flag, color)
            // Recortes
            val cx = x + flagW * 0.45f
            drawCircle(Color(0xFFFFF4E0), radius = flagW * 0.09f, center = Offset(cx, y + flagH * 0.35f))
            drawCircle(Color(0xFFFFF4E0), radius = flagW * 0.05f, center = Offset(cx - flagW * 0.2f, y + flagH * 0.6f))
            drawCircle(Color(0xFFFFF4E0), radius = flagW * 0.05f, center = Offset(cx + flagW * 0.2f, y + flagH * 0.6f))
            x += flagW
            i++
        }
    }
}

private fun DrawScope.drawTalavera() {
    drawRect(Color(0xFFF7F3EA))
    val blue = Color(0xFF1E4FA3)
    val yellow = Color(0xFFF2B33D)
    val tiles = 4
    val tile = min(size.width, size.height) / tiles
    val cols = (size.width / tile).toInt() + 1
    val rows = (size.height / tile).toInt() + 1
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val center = Offset(c * tile + tile / 2, r * tile + tile / 2)
            // Pétalos en cruz y puntitos en diagonal (flor de talavera)
            for (k in 0 until 4) {
                rotate(degrees = 45f + k * 90f, pivot = center) {
                    drawCircle(blue, radius = tile * 0.05f, center = Offset(center.x, center.y - tile * 0.36f))
                }
                rotate(degrees = k * 90f, pivot = center) {
                    drawOval(
                        blue,
                        topLeft = Offset(center.x - tile * 0.09f, center.y - tile * 0.42f),
                        size = Size(tile * 0.18f, tile * 0.32f)
                    )
                }
            }
            drawCircle(yellow, radius = tile * 0.10f, center = center)
            drawCircle(blue, radius = tile * 0.10f, center = center, style = Stroke(width = tile * 0.03f))
            // Borde del azulejo
            drawRect(
                blue.copy(alpha = 0.25f),
                topLeft = Offset(c * tile, r * tile),
                size = Size(tile, tile),
                style = Stroke(width = tile * 0.02f)
            )
        }
    }
}
