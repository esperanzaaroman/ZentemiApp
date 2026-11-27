package com.zentemi.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/**
 * Ícono ilustrado de Zentemi (res/drawable-nodpi/ic_*.png).
 * A diferencia de los íconos de Material, ya trae sus colores: no se tiñe.
 *
 * @param muted true para mostrarlo apagado (gris y transparente), por ejemplo
 *   una pestaña sin seleccionar, un logro sin ganar o una estrella vacía.
 */
@Composable
fun ZentemiIcon(
    @DrawableRes icon: Int,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    muted: Boolean = false
) {
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        colorFilter = if (muted) GrayscaleFilter else null,
        modifier = modifier
            .size(size)
            .alpha(if (muted) 0.45f else 1f)
    )
}
