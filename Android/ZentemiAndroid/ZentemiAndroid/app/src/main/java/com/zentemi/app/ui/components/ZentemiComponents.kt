package com.zentemi.app.ui.components

import com.zentemi.app.R
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.border
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import com.zentemi.app.ui.theme.*

/** Rectangle whose bottom edge is a gentle wave (low on the left, rising to the right). */
class WaveBottomShape(private val waveHeight: Dp = 14.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val wave = with(density) { waveHeight.toPx() }
        val w = size.width
        val h = size.height
        val top = h - wave
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, top)
            // right edge high -> soft dip in the middle -> small rise on the left
            cubicTo(w * 0.72f, top - wave * 0.2f, w * 0.55f, h, w * 0.32f, h - wave * 0.15f)
            cubicTo(w * 0.16f, h - wave * 0.3f, w * 0.06f, top + wave * 0.25f, 0f, top + wave * 0.4f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Encabezado morado con degradado y un círculo decorativo. El borde de abajo
 * es recto (antes era ondulado; [waveHeight] se conserva solo para no romper
 * a quien lo pasa, y ya no se usa). El contenido va debajo de la barra de estado.
 */
@Composable
fun WaveHeader(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") waveHeight: Dp = 14.dp,
    /** Colores del degradado (el perfil usa el color que eligió el usuario). */
    colors: List<Color> = listOf(HeaderTop, HeaderBottom),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(Brush.linearGradient(colors))
    ) {
        // Decorative circle (top-right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-40).dp)
                .size(190.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp),
            content = content
        )
    }
}

/** Gold pill with the coin icon, used in headers. */
@Composable
fun CoinPill(coins: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(4.dp, CircleShape, ambientColor = CoinGoldDark, spotColor = CoinGoldDark)
            .clip(CircleShape)
            .background(SplashDotActive)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ZentemiIcon(R.drawable.ic_moneda, size = 22.dp, contentDescription = "Monedas")
        Text(
            text = "%,d".format(coins),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = CoinGoldDark
        )
    }
}

/** Header title + subtitle in white. */
@Composable
fun HeaderTitle(title: String, subtitle: String? = null) {
    // Mismas medidas que ScreenTitle; con nombres largos baja a un segundo renglón sin encimarse
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            fontSize = ScreenHeaderDefaults.titleSize,
            lineHeight = 31.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = ScreenHeaderDefaults.subtitleSize,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Game-like 3D button: a darker "lip" underneath that the face presses into.
 */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Purple,
    lipColor: Color = PurpleDark,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 44.dp,
    textSize: TextUnit = 15.sp,
    leading: (@Composable () -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val lip = 4.dp
    val press by animateDpAsState(if (pressed && enabled) lip else 0.dp, label = "press")
    val shape = RoundedCornerShape(Radius.md)
    val haptics = LocalHapticFeedback.current
    val face = if (enabled) color else Color(0xFFCFCBD8)
    val under = if (enabled) lipColor else Color(0xFFB4AFC0)

    Box(
        modifier = modifier
            .height(height + lip)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = lip)
                .clip(shape)
                .background(under)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = press)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(face.lighten(0.12f), face))),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = text, fontSize = textSize, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

private fun Color.lighten(amount: Float) = lerp(this, Color.White, amount)

/** White rounded card with a soft purple shadow. */
@Composable
fun ZentemiCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .zentemiCardSurface()
            .background(Color.White)
            .padding(padding),
        content = content
    )
}

/**
 * La superficie de tarjeta única de la app: blanca, esquina [Radius.lg], sombra
 * morada suave y un filo fino para que no se pierda sobre fondos claros.
 */
fun Modifier.zentemiCardSurface(shape: Shape = RoundedCornerShape(Radius.lg)): Modifier = this
    .shadow(6.dp, shape, ambientColor = Purple.copy(alpha = 0.14f), spotColor = Purple.copy(alpha = 0.16f))
    .clip(shape)
    .background(Color.White)
    .border(1.dp, Hairline.copy(alpha = 0.7f), shape)

/** Small colored tag such as NUEVO / RARO. */
@Composable
fun TagChip(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color.darkenForText(),
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

private fun Color.darkenForText() = lerp(this, Color.Black, 0.25f)

// ── Colores personalizables del perfil (mismas fórmulas que ProfileView.swift) ──

/** Degradado del encabezado del perfil para un tono de 0 a 1. */
fun profileHeaderColors(hue: Float): List<Color> = listOf(
    Color.hsv(hue.coerceIn(0f, 1f) * 360f, 0.72f, 0.72f),
    Color.hsv(hue.coerceIn(0f, 1f) * 360f, 0.64f, 0.84f)
)

/** Fondo pastel detrás de Maicito en el avatar, para un tono de 0 a 1. */
fun avatarBackgroundColor(hue: Float): Color =
    Color.hsv(hue.coerceIn(0f, 1f) * 360f, 0.22f, 1f)
