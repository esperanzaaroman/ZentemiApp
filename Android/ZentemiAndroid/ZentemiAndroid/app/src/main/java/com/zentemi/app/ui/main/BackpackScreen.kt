package com.zentemi.app.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*

private data class BackpackItem(
    val id: String,
    @DrawableRes val image: Int,
    val name: String,
    val isEquipped: Boolean,
    val onToggle: () -> Unit
)

@Composable
fun BackpackScreen(appState: AppState) {

    val items = buildList {
        // Default items (always owned)
        add(BackpackItem("glasses", R.drawable.lentes, "Lentes", appState.glassesEquipped) { appState.toggleGlasses() })
        add(BackpackItem("hair", R.drawable.cabello, "Cabello", appState.hairEquipped) { appState.toggleHair() })
        add(BackpackItem("scarf", R.drawable.bufanda, "Bufanda", appState.scarfEquipped) { appState.toggleScarf() })

        // Purchased items
        if (appState.sunglassesPurchased) {
            add(BackpackItem("sunglasses", R.drawable.lentesdesol, "Lentes de sol", appState.sunglassesEquipped) { appState.toggleSunglasses() })
        }
        if (appState.pinkHairPurchased) {
            add(BackpackItem("pinkHair", R.drawable.cabellorosa, "Cabello rosa", appState.pinkHairEquipped) { appState.togglePinkHair() })
        }
        if (appState.swordPurchased) {
            add(BackpackItem("sword", R.drawable.espada, "Espada real", appState.swordEquipped) { appState.toggleSword() })
        }
        if (appState.suitPurchased) {
            add(BackpackItem("suit", R.drawable.traje1k, "Traje elegante", appState.suitEquipped) { appState.toggleSuit() })
        }
    }

    val equippedCount = items.count { it.isEquipped }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        WaveHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderTitle(
                    title = "Mochila",
                    subtitle = "${items.size} objetos · $equippedCount equipados"
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Backpack, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pet preview
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(SplashGlow.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                    )
                    PetView(appState = appState, size = 130.dp)
                }
            }

            items(items, key = { it.id }) { item ->
                BackpackCard(item = item)
            }
        }
    }
}

@Composable
private fun BackpackCard(item: BackpackItem) {
    ZentemiCard(
        modifier = if (item.isEquipped) {
            Modifier.border(1.5.dp, Turquoise.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
        } else Modifier,
        padding = 12.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            ItemImage(image = item.image)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (item.isEquipped) {
                ChunkyButton(
                    text = "Equipado",
                    onClick = item.onToggle,
                    color = Turquoise.copy(alpha = 0.16f).compositeOverWhite(),
                    lipColor = Turquoise.copy(alpha = 0.35f).compositeOverWhite(),
                    textColor = Color(0xFF14917F),
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF14917F), modifier = Modifier.size(16.dp))
                    }
                )
            } else {
                ChunkyButton(text = "Equipar", onClick = item.onToggle, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun Color.compositeOverWhite(): Color =
    Color(
        red = red * alpha + (1 - alpha),
        green = green * alpha + (1 - alpha),
        blue = blue * alpha + (1 - alpha)
    )
