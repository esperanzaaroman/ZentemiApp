package com.zentemi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.zentemi.app.state.AppState

/**
 * Composable mascot with layered accessories.
 *
 * Since the actual image assets (mascota.png, lentes.png, etc.) need to be
 * added to res/drawable, this renders a placeholder emoji mascot.
 * When the real PNGs are added, replace the Text("🌽") with Image() composables
 * using painterResource(R.drawable.mascota), and overlay each accessory
 * using the offset/size ratios from the iOS version.
 *
 * iOS reference offsets (base size 115):
 *   traje1k:      width=115, x=0,  y=-1
 *   cabello:      width=70,  x=0,  y=-31
 *   cabellorosa:  width=70,  x=0,  y=-31
 *   lentes:       width=50,  x=0,  y=-4
 *   lentesdesol:  width=52,  x=0,  y=-4
 *   bufanda:      width=55,  x=-1, y=21
 *   espada:       width=45,  x=37, y=3
 */
@Composable
fun PetView(
    appState: AppState,
    size: Dp = 115.dp,
    modifier: Modifier = Modifier
) {
    val scale = size / 115.dp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Base mascot placeholder
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFE8D5F5).copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🌽",
                fontSize = (size.value * 0.55f).sp
            )
        }

        // Accessory indicators (small colored dots showing equipped items)
        // These will be replaced with actual Image overlays when assets are added

        if (appState.suitEquipped) {
            AccessoryDot(
                color = Color(0xFF4A148C),
                label = "T",
                offset = Pair(0f, (size.value * 0.35f)),
                scale = scale
            )
        }

        if (appState.hairEquipped) {
            AccessoryDot(
                color = Color(0xFF5D4037),
                label = "C",
                offset = Pair(0f, -(size.value * 0.35f)),
                scale = scale
            )
        }

        if (appState.pinkHairEquipped) {
            AccessoryDot(
                color = Color(0xFFE91E63),
                label = "C",
                offset = Pair(0f, -(size.value * 0.35f)),
                scale = scale
            )
        }

        if (appState.glassesEquipped) {
            AccessoryDot(
                color = Color(0xFF212121),
                label = "L",
                offset = Pair(0f, -(size.value * 0.05f)),
                scale = scale
            )
        }

        if (appState.sunglassesEquipped) {
            AccessoryDot(
                color = Color(0xFF1B5E20),
                label = "L",
                offset = Pair(0f, -(size.value * 0.05f)),
                scale = scale
            )
        }

        if (appState.scarfEquipped) {
            AccessoryDot(
                color = Color(0xFFC62828),
                label = "B",
                offset = Pair(-(size.value * 0.01f), (size.value * 0.18f)),
                scale = scale
            )
        }

        if (appState.swordEquipped) {
            AccessoryDot(
                color = Color(0xFF757575),
                label = "E",
                offset = Pair((size.value * 0.32f), (size.value * 0.03f)),
                scale = scale
            )
        }
    }
}

@Composable
private fun AccessoryDot(
    color: Color,
    label: String,
    offset: Pair<Float, Float>,
    scale: Float
) {
    Box(
        modifier = Modifier
            .offset(x = offset.first.dp, y = offset.second.dp)
            .size((18 * scale).dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = (9 * scale).sp,
            color = Color.White
        )
    }
}
