package com.zentemi.app.ui.components

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
import com.zentemi.app.ui.theme.*

/** Rectangle whose bottom edge is a soft double wave. */
class WaveBottomShape(private val waveHeight: Dp = 22.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val wave = with(density) { waveHeight.toPx() }
        val w = size.width
        val h = size.height
        val base = h - wave
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, base + wave * 0.35f)
            // right crest -> trough -> left crest
            cubicTo(w * 0.85f, base - wave * 0.55f, w * 0.68f, base - wave * 0.4f, w * 0.52f, base + wave * 0.45f)
            cubicTo(w * 0.38f, h + wave * 0.15f, w * 0.16f, h, 0f, base + wave * 0.1f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Purple gradient header with a wavy bottom edge and decorative circles.
 * A lighter wave sits behind it for depth. Content is laid out inside the
 * status bar padding.
 */
@Composable
fun WaveHeader(
    modifier: Modifier = Modifier,
    waveHeight: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val anim = rememberInfiniteTransition(label = "waveHeader")
    val drift by anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = EaseInOut), RepeatMode.Reverse),
        label = "drift"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Back wave (lighter), slightly lower than the main one
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 10.dp)
                .offset(y = 8.dp)
                .clip(WaveBottomShape(waveHeight))
                .background(SplashBottom.copy(alpha = 0.35f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WaveBottomShape(waveHeight))
                .background(Brush.linearGradient(listOf(SplashTop, Purple, SplashBottom)))
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp + (drift * 10).dp, y = (-50).dp + (drift * 8).dp)
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp - (drift * 8).dp, y = 30.dp)
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 18.dp + waveHeight),
                content = content
            )
        }
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
        Icon(Icons.Filled.MonetizationOn, contentDescription = "Monedas", tint = CoinGoldDark, modifier = Modifier.size(18.dp))
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
    Column {
        Text(text = title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.82f)
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
    leading: (@Composable () -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val lip = 4.dp
    val press by animateDpAsState(if (pressed && enabled) lip else 0.dp, label = "press")
    val shape = RoundedCornerShape(14.dp)
    val face = if (enabled) color else Color(0xFFCFCBD8)
    val under = if (enabled) lipColor else Color(0xFFB4AFC0)

    Box(
        modifier = modifier
            .height(height + lip)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
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
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

private fun Color.lighten(amount: Float) = lerp(this, Color.White, amount)

/** White rounded card with a soft purple shadow. */
@Composable
fun ZentemiCard(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Purple.copy(alpha = 0.18f), spotColor = Purple.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(padding),
        content = content
    )
}

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
