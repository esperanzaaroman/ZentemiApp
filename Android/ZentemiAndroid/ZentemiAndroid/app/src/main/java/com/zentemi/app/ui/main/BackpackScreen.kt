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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val TurquoiseDark = Color(0xFF1E9A8A)

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

    val equipped = items.filter { it.isEquipped }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        WaveHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderTitle(
                    title = "Mochila",
                    subtitle = "${items.size} objetos · ${equipped.size} equipados"
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutfitPreviewCard(appState = appState, equippedNames = equipped.map { it.name })
            }

            items(items, key = { it.id }) { item ->
                BackpackCard(item = item)
            }
        }
    }
}

/** Card with the mascot wearing the current outfit and the list of equipped items. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OutfitPreviewCard(appState: AppState, equippedNames: List<String>) {
    ZentemiCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        TagChip(text = "Preview", color = Turquoise)

        Row(verticalAlignment = Alignment.CenterVertically) {
            PetView(appState = appState, size = 96.dp)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vestimenta actual",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (equippedNames.isEmpty()) {
                    Text(
                        text = "Sin accesorios",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubText.copy(alpha = 0.6f)
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        equippedNames.forEach { name ->
                            Text(
                                text = name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TurquoiseDark,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Turquoise.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Toca un objeto para equiparlo.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SubText
                )
            }
        }
    }
}

@Composable
private fun BackpackCard(item: BackpackItem) {
    // Extra top space so the "Equipado" tag can sit on the card's border
    Box(modifier = Modifier.padding(top = 10.dp)) {
        ZentemiCard(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (item.isEquipped) Modifier.border(1.5.dp, Turquoise, RoundedCornerShape(20.dp))
                    else Modifier
                ),
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
                        text = "Quitar",
                        onClick = item.onToggle,
                        color = Turquoise,
                        lipColor = TurquoiseDark,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ChunkyButton(text = "Equipar", onClick = item.onToggle, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (item.isEquipped) {
            Text(
                text = "Equipado",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TurquoiseDark,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6F7F4))
                    .border(1.dp, Turquoise, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            )
        }
    }
}
