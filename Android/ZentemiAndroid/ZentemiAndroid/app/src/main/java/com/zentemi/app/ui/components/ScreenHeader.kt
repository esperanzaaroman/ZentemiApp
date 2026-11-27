package com.zentemi.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.ui.theme.*

/** Medidas del título de cada pestaña (Inicio, Tienda, Perfil): iguales en todas. */
object ScreenHeaderDefaults {
    val titleSize = 26.sp
    val subtitleSize = 16.sp
    /** Separación de arriba, debajo de la barra de estado. */
    val top = 12.dp
}

/**
 * Título de pantalla arriba a la izquierda. En fondo claro va en morado oscuro;
 * sobre un encabezado de color, en blanco ([onColor] = true).
 */
@Composable
fun ScreenTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onColor: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontFamily = Quicksand,
            fontSize = ScreenHeaderDefaults.titleSize,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (onColor) Color.White else PurpleDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontFamily = Quicksand,
                fontSize = ScreenHeaderDefaults.subtitleSize,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = if (onColor) Color.White.copy(alpha = 0.85f) else MutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Insignia de monedas o racha: fondo suave, ícono grande y número.
 * Es la misma en Inicio y en la Tienda. [onClick] nulo = no se puede tocar.
 */
@Composable
fun StatBadge(
    @DrawableRes icon: Int,
    value: String,
    tint: Color,
    textColor: Color,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .then(if (onClick != null) Modifier.pressable(pressedScale = 0.92f, onClick = onClick) else Modifier)
            .shadow(3.dp, RoundedCornerShape(Radius.md), ambientColor = textColor.copy(alpha = 0.25f), spotColor = textColor.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(Radius.md))
            .background(tint)
            .heightIn(min = 44.dp)
            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ZentemiIcon(icon, size = 28.dp, contentDescription = contentDescription)
        Text(
            text = value,
            fontFamily = Quicksand,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

/** Monedas con los colores de siempre. */
@Composable
fun CoinBadge(coins: Int, onClick: (() -> Unit)? = null) = StatBadge(
    icon = R.drawable.ic_moneda,
    value = "%,d".format(coins),
    tint = CoinBadgeTint,
    textColor = CoinGoldDark,
    contentDescription = "Monedas",
    onClick = onClick
)

val CoinBadgeTint = Color(0xFFFFF1C7)
