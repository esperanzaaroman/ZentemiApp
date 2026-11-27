package com.zentemi.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

// Fondos nuevos de la tienda (ver MaicitoBackdrop). Todo se dibuja relativo
// al tamaño para que se vea igual en la tarjeta chica y detrás de Maicito.

internal fun DrawScope.drawMilpa() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF8FD3FF), Color(0xFFD8F1FF))))
    drawCircle(Color(0xFFFFE08A), radius = size.minDimension * 0.10f, center = Offset(size.width * 0.82f, size.height * 0.16f))
    // Tierra
    drawRect(Color(0xFF8B5A2B), topLeft = Offset(0f, size.height * 0.80f), size = Size(size.width, size.height * 0.20f))
    // Matas de maíz
    val stalks = 7
    for (i in 0 until stalks) {
        val x = size.width * (i + 0.5f) / stalks
        val top = size.height * (if (i % 2 == 0) 0.38f else 0.46f)
        val base = size.height * 0.82f
        drawLine(Color(0xFF3F8A3A), Offset(x, base), Offset(x, top), strokeWidth = size.width * 0.018f)
        val leafW = size.width * 0.07f
        for (k in 0..2) {
            val y = top + (base - top) * (0.25f + k * 0.22f)
            val dir = if ((i + k) % 2 == 0) 1f else -1f
            val leaf = Path().apply {
                moveTo(x, y)
                quadraticTo(x + dir * leafW, y - leafW * 0.9f, x + dir * leafW * 1.4f, y - leafW * 0.2f)
                quadraticTo(x + dir * leafW * 0.6f, y - leafW * 0.2f, x, y + leafW * 0.15f)
                close()
            }
            drawPath(leaf, Color(0xFF6CC24A))
        }
        // Elote
        drawOval(
            Color(0xFFFFC93C),
            topLeft = Offset(x + size.width * 0.008f, top + (base - top) * 0.30f),
            size = Size(size.width * 0.03f, size.height * 0.07f)
        )
        // Espiga
        val tassel = size.width * 0.02f
        drawLine(Color(0xFFE0B84A), Offset(x, top), Offset(x - tassel, top - size.height * 0.05f), strokeWidth = size.width * 0.008f)
        drawLine(Color(0xFFE0B84A), Offset(x, top), Offset(x + tassel, top - size.height * 0.05f), strokeWidth = size.width * 0.008f)
    }
}

internal fun DrawScope.drawPlaya() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF6EC8FF), Color(0xFFBDEBFF))))
    drawCircle(Color(0xFFFFE08A), radius = size.minDimension * 0.12f, center = Offset(size.width * 0.20f, size.height * 0.18f))
    // Mar
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFF2EA8D8), Color(0xFF1E7FB8))),
        topLeft = Offset(0f, size.height * 0.52f),
        size = Size(size.width, size.height * 0.22f)
    )
    // Espuma de las olas
    val wave = Path().apply {
        moveTo(0f, size.height * 0.70f)
        var x = 0f
        val step = size.width / 6f
        while (x < size.width) {
            quadraticTo(x + step / 2, size.height * 0.66f, x + step, size.height * 0.70f)
            x += step
        }
        lineTo(size.width, size.height * 0.74f)
        lineTo(0f, size.height * 0.74f)
        close()
    }
    drawPath(wave, Color.White.copy(alpha = 0.8f))
    // Arena
    drawRect(Color(0xFFF6D9A0), topLeft = Offset(0f, size.height * 0.73f), size = Size(size.width, size.height * 0.27f))
    // Palmera
    val px = size.width * 0.84f
    val crown = Offset(px - size.width * 0.02f, size.height * 0.34f)
    val trunk = Path().apply {
        moveTo(px, size.height * 0.86f)
        quadraticTo(px - size.width * 0.06f, size.height * 0.55f, crown.x, crown.y)
    }
    drawPath(trunk, Color(0xFF9A6A3A), style = Stroke(width = size.width * 0.03f))
    for (k in 0 until 6) {
        val a = Math.toRadians((190 + k * 32).toDouble())
        val end = crown + Offset(
            (cos(a) * size.width * 0.17f).toFloat(),
            (sin(a) * size.width * 0.08f).toFloat() + size.height * 0.05f
        )
        val leaf = Path().apply {
            moveTo(crown.x, crown.y)
            quadraticTo((crown.x + end.x) / 2, (crown.y + end.y) / 2 - size.height * 0.06f, end.x, end.y)
        }
        drawPath(leaf, Color(0xFF3F9A4A), style = Stroke(width = size.width * 0.03f))
    }
}

internal fun DrawScope.drawBosque() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFFBFE8C8), Color(0xFFE9F7E4))))

    fun tree(x: Float, base: Float, h: Float, color: Color) {
        val w = h * 0.55f
        drawLine(Color(0xFF7A4A2A), Offset(x, base), Offset(x, base - h * 0.2f), strokeWidth = w * 0.16f)
        for (k in 0..2) {
            val bottom = base - h * (0.12f + k * 0.25f)
            val top = base - h * (0.55f + k * 0.25f)
            val half = w * (0.5f - k * 0.1f)
            val tri = Path().apply {
                moveTo(x, top)
                lineTo(x + half, bottom)
                lineTo(x - half, bottom)
                close()
            }
            drawPath(tri, color)
        }
    }

    val far = Color(0xFF7CC08A)
    val near = Color(0xFF3F8A4A)
    listOf(0.08f, 0.30f, 0.55f, 0.78f, 0.96f).forEach { tree(size.width * it, size.height * 0.72f, size.height * 0.45f, far) }
    drawRect(Color(0xFF8BCB6A), topLeft = Offset(0f, size.height * 0.78f), size = Size(size.width, size.height * 0.22f))
    listOf(0.0f, 0.18f, 0.88f).forEach { tree(size.width * it, size.height * 0.90f, size.height * 0.58f, near) }
    // Hongos
    listOf(0.40f to 0.90f, 0.66f to 0.93f).forEach { (fx, fy) ->
        val c = Offset(size.width * fx, size.height * fy)
        val r = size.minDimension * 0.04f
        drawRect(Color(0xFFFFF6E5), topLeft = Offset(c.x - r * 0.3f, c.y - r * 0.3f), size = Size(r * 0.6f, r))
        drawArc(Color(0xFFE63946), 180f, 180f, true, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 1.4f))
        drawCircle(Color.White, r * 0.16f, Offset(c.x - r * 0.4f, c.y - r * 0.55f))
        drawCircle(Color.White, r * 0.12f, Offset(c.x + r * 0.35f, c.y - r * 0.6f))
    }
}

internal fun DrawScope.drawPiramide() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFFFFB86B), Color(0xFFFFE2A8))))
    drawCircle(Color(0xFFFFF1C2), radius = size.minDimension * 0.10f, center = Offset(size.width * 0.80f, size.height * 0.20f))
    drawRect(Color(0xFFE6B877), topLeft = Offset(0f, size.height * 0.78f), size = Size(size.width, size.height * 0.22f))

    val cx = size.width * 0.5f
    val base = size.height * 0.80f
    val levels = 5
    val maxW = size.width * 0.86f
    val levelH = size.height * 0.08f
    for (i in 0 until levels) {
        val w = maxW * (1f - i * 0.16f)
        val y = base - (i + 1) * levelH
        drawRect(
            if (i % 2 == 0) Color(0xFFB9794A) else Color(0xFFA8693E),
            topLeft = Offset(cx - w / 2, y),
            size = Size(w, levelH)
        )
    }
    // Templo de arriba
    val tw = maxW * 0.22f
    drawRect(Color(0xFF8E5530), topLeft = Offset(cx - tw / 2, base - (levels + 1) * levelH), size = Size(tw, levelH))
    // Escalera
    drawRect(
        Color(0xFFD9A46E),
        topLeft = Offset(cx - size.width * 0.06f, base - levels * levelH),
        size = Size(size.width * 0.12f, levels * levelH)
    )
    // Nopales
    listOf(0.08f, 0.92f).forEach { fx ->
        val x = size.width * fx
        drawOval(Color(0xFF4CAF50), topLeft = Offset(x - size.width * 0.035f, size.height * 0.70f), size = Size(size.width * 0.07f, size.height * 0.11f))
        drawOval(Color(0xFF5CBF60), topLeft = Offset(x - size.width * 0.01f, size.height * 0.64f), size = Size(size.width * 0.05f, size.height * 0.08f))
    }
}

internal fun DrawScope.drawArcoiris() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFFB9E6FF), Color(0xFFFDE7FF))))
    val colors = listOf(
        Color(0xFFFF6B6B), Color(0xFFFFA94D), Color(0xFFFFD43B),
        Color(0xFF69DB7C), Color(0xFF4DABF7), Color(0xFF9775FA)
    )
    val center = Offset(size.width / 2, size.height * 0.80f)
    val outer = size.width * 0.46f
    val band = outer * 0.10f
    colors.forEachIndexed { i, c ->
        val r = outer - i * band - band / 2
        drawArc(
            c, 180f, 180f, false,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = band)
        )
    }

    fun cloud(x: Float, y: Float, s: Float) {
        drawCircle(Color.White, s, Offset(x, y))
        drawCircle(Color.White, s * 0.8f, Offset(x - s, y + s * 0.2f))
        drawCircle(Color.White, s * 0.8f, Offset(x + s, y + s * 0.2f))
        drawRect(Color.White, topLeft = Offset(x - s * 1.6f, y + s * 0.1f), size = Size(s * 3.2f, s * 0.9f))
    }
    val s = size.minDimension * 0.08f
    cloud(size.width * 0.12f, size.height * 0.78f, s)
    cloud(size.width * 0.88f, size.height * 0.78f, s)
    cloud(size.width * 0.70f, size.height * 0.16f, s * 0.7f)
}

internal fun DrawScope.drawDiaDeMuertos() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF2A1450), Color(0xFF5B2A86))))
    // Papel picado arriba
    val colors = listOf(Color(0xFFFF4FA3), Color(0xFFFF8C42), Color(0xFF2EC4B6), Color(0xFFFFC93C))
    val flagW = size.width / 6f
    val y0 = size.height * 0.06f
    drawLine(Color(0xFFFFF4E0), Offset(0f, y0), Offset(size.width, y0), strokeWidth = size.minDimension * 0.006f)
    for (i in 0 until 6) {
        val x = i * flagW + flagW * 0.06f
        val flag = Path().apply {
            moveTo(x, y0)
            lineTo(x + flagW * 0.88f, y0)
            lineTo(x + flagW * 0.88f, y0 + flagW * 0.8f)
            lineTo(x + flagW * 0.44f, y0 + flagW)
            lineTo(x, y0 + flagW * 0.8f)
            close()
        }
        drawPath(flag, colors[i % colors.size])
        drawCircle(Color(0xFF2A1450), flagW * 0.09f, Offset(x + flagW * 0.44f, y0 + flagW * 0.4f))
    }
    // Altar
    drawRect(Color(0xFF8E4FB8), topLeft = Offset(0f, size.height * 0.80f), size = Size(size.width, size.height * 0.20f))
    drawRect(Color(0xFFB073D6), topLeft = Offset(0f, size.height * 0.80f), size = Size(size.width, size.height * 0.03f))
    // Velas
    listOf(0.10f, 0.26f, 0.74f, 0.90f).forEachIndexed { i, fx ->
        val x = size.width * fx
        val h = size.height * (if (i % 2 == 0) 0.12f else 0.09f)
        val w = size.width * 0.045f
        val top = size.height * 0.80f - h
        drawCircle(Color(0xFFFFC93C).copy(alpha = 0.25f), w * 1.6f, Offset(x, top - w * 0.6f))
        drawRect(Color(0xFFFFF4E0), topLeft = Offset(x - w / 2, top), size = Size(w, h))
        drawOval(Color(0xFFFFB020), topLeft = Offset(x - w * 0.25f, top - w * 1.1f), size = Size(w * 0.5f, w * 0.9f))
    }
    // Flores de cempasúchil
    listOf(0.18f, 0.40f, 0.60f, 0.82f).forEach { fx ->
        val c = Offset(size.width * fx, size.height * 0.88f)
        val r = size.minDimension * 0.045f
        for (k in 0 until 8) {
            val a = Math.toRadians((k * 45).toDouble())
            drawCircle(Color(0xFFFF8C1A), r * 0.55f, c + Offset((cos(a) * r * 0.6f).toFloat(), (sin(a) * r * 0.6f).toFloat()))
        }
        drawCircle(Color(0xFFFFB020), r * 0.5f, c)
    }
}
