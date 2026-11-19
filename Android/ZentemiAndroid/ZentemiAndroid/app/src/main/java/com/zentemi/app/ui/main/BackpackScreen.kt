package com.zentemi.app.ui.main

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
import com.zentemi.app.audio.SoundManager
import androidx.compose.ui.platform.LocalContext
import com.zentemi.app.data.ShopCatalog
import com.zentemi.app.data.ShopItem
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*

private val TurquoiseDark = Color(0xFF1E9A8A)

@Composable
fun BackpackScreen(appState: AppState) {

    val context = LocalContext.current

    // Sonidos de iOS: "quitar" al desequipar y "equipar" (a la mitad de volumen) al ponerlo.
    fun toggleWithSound(isEquipped: Boolean, toggle: () -> Unit) {
        if (isEquipped) {
            SoundManager.play(context, R.raw.audioquitaritem, appState.soundVolume)
        } else {
            SoundManager.play(context, R.raw.audioequipar, minOf(appState.soundVolume * 0.5f, 1f))
        }
        toggle()
    }

    // Todo lo que tiene Maicito: primero accesorios, luego fondos
    val items = ShopCatalog.items
        .filter { appState.isOwned(it) }
        .sortedBy { if (it.isAccessory) 0 else 1 }

    val equipped = items.filter { appState.isEquipped(it) }

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
                BackpackCard(
                    item = item,
                    isEquipped = appState.isEquipped(item),
                    onToggle = { toggleWithSound(appState.isEquipped(item)) { appState.toggleEquip(item) } }
                )
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
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                appState.equippedBackdrop?.backdrop?.let { backdrop ->
                    MaicitoBackdrop(backdrop = backdrop, modifier = Modifier.fillMaxSize())
                }
                PetView(appState = appState, size = 80.dp, modifier = Modifier.offset(y = 6.dp))
            }

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
private fun BackpackCard(item: ShopItem, isEquipped: Boolean, onToggle: () -> Unit) {
    // Extra top space so the "Equipado" tag can sit on the card's border
    Box(modifier = Modifier.padding(top = 10.dp)) {
        ZentemiCard(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isEquipped) Modifier.border(1.5.dp, Turquoise, RoundedCornerShape(20.dp))
                    else Modifier
                ),
            padding = 12.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                ItemPreview(item = item)

                Text(
                    text = item.slot.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubText.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )

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

                if (isEquipped) {
                    ChunkyButton(
                        text = "Quitar",
                        onClick = onToggle,
                        color = Turquoise,
                        lipColor = TurquoiseDark,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ChunkyButton(text = "Equipar", onClick = onToggle, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (isEquipped) {
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
