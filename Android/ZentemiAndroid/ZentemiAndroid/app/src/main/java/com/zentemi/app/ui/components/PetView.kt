package com.zentemi.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zentemi.app.R
import com.zentemi.app.state.AppState
import kotlin.math.cos
import kotlin.math.sin

/**
 * The mascot with its equipped accessories layered on top.
 *
 * Accessory width and offsets are given for a base size of 115 (same values as
 * the iOS PetView) and scaled to [size].
 */
@Composable
fun PetView(
    appState: AppState,
    size: Dp = 115.dp,
    modifier: Modifier = Modifier
) {
    val scale = size / 115.dp

    val hueFilter = remember(appState.mascotHue) {
        if (appState.mascotHue == 0f) null
        else ColorFilter.colorMatrix(hueRotationMatrix(appState.mascotHue * 360f))
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mascota),
            contentDescription = appState.petName,
            colorFilter = hueFilter,
            modifier = Modifier.size(size)
        )

        if (appState.suitEquipped) Accessory(R.drawable.traje1k, 135f, 1f, 1f, scale)
        if (appState.hairEquipped) Accessory(R.drawable.cabello, 100f, 0f, -30f, scale)
        if (appState.pinkHairEquipped) Accessory(R.drawable.cabellorosa, 105f, 0f, -25f, scale)
        if (appState.glassesEquipped) Accessory(R.drawable.lentes, 70f, 1f, 0f, scale)
        if (appState.sunglassesEquipped) Accessory(R.drawable.lentesdesol, 70f, 0f, 0f, scale)
        if (appState.scarfEquipped) Accessory(R.drawable.bufanda, 110f, -2f, 40f, scale)
        if (appState.swordEquipped) Accessory(R.drawable.espada, 70f, 45f, 9f, scale)
    }
}

@Composable
private fun Accessory(
    @DrawableRes image: Int,
    width: Float,
    x: Float,
    y: Float,
    scale: Float
) {
    val painter = painterResource(id = image)
    val intrinsic = painter.intrinsicSize
    val w = (width * scale).dp
    val h = w * (intrinsic.height / intrinsic.width)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .offset(x = (x * scale).dp, y = (y * scale).dp)
            .requiredSize(w, h) // may be larger than the mascot (e.g. the suit)
    )
}

/** Hue rotation matrix (same math as CSS hue-rotate / SwiftUI hueRotation). */
private fun hueRotationMatrix(degrees: Float): ColorMatrix {
    val rad = Math.toRadians(degrees.toDouble())
    val c = cos(rad).toFloat()
    val s = sin(rad).toFloat()
    return ColorMatrix(
        floatArrayOf(
            0.213f + c * 0.787f - s * 0.213f, 0.715f - c * 0.715f - s * 0.715f, 0.072f - c * 0.072f + s * 0.928f, 0f, 0f,
            0.213f - c * 0.213f + s * 0.143f, 0.715f + c * 0.285f + s * 0.140f, 0.072f - c * 0.072f - s * 0.283f, 0f, 0f,
            0.213f - c * 0.213f - s * 0.787f, 0.715f - c * 0.715f + s * 0.715f, 0.072f + c * 0.928f + s * 0.072f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
}
