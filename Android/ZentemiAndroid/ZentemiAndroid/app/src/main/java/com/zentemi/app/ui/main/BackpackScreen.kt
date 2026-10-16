package com.zentemi.app.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*

private data class BackpackItem(
    val id: String,
    val emoji: String,
    val name: String,
    val isEquipped: Boolean,
    val onToggle: () -> Unit
)

@Composable
fun BackpackScreen(appState: AppState) {

    val items = buildList {
        // Default items (always owned)
        add(BackpackItem("glasses", "👓", "Lentes", appState.glassesEquipped) { appState.toggleGlasses() })
        add(BackpackItem("hair", "💇", "Cabello", appState.hairEquipped) { appState.toggleHair() })
        add(BackpackItem("scarf", "🧣", "Bufanda", appState.scarfEquipped) { appState.toggleScarf() })

        // Purchased items
        if (appState.sunglassesPurchased) {
            add(BackpackItem("sunglasses", "🕶️", "Lentes de sol", appState.sunglassesEquipped) { appState.toggleSunglasses() })
        }
        if (appState.pinkHairPurchased) {
            add(BackpackItem("pinkHair", "💇‍♀️", "Cabello rosa", appState.pinkHairEquipped) { appState.togglePinkHair() })
        }
        if (appState.swordPurchased) {
            add(BackpackItem("sword", "⚔️", "Espada real", appState.swordEquipped) { appState.toggleSword() })
        }
        if (appState.suitPurchased) {
            add(BackpackItem("suit", "🤵", "Traje elegante", appState.suitEquipped) { appState.toggleSuit() })
        }
    }

    val equippedCount = items.count { it.isEquipped }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(StoreHeaderStart, StoreHeaderEnd)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = (-130).dp, y = (-75).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mochila",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${items.size} objetos · $equippedCount equipados",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    Icons.Filled.Backpack,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Pet preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            PetView(appState = appState, size = 100.dp)
        }

        // Items grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                bottom = 100.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                BackpackCard(item = item)
            }
        }
    }
}

@Composable
private fun BackpackCard(item: BackpackItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = if (item.isEquipped) {
            BorderStroke(1.dp, Turquoise.copy(alpha = 0.5f))
        } else null
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.Gray.copy(alpha = 0.055f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = item.onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isEquipped)
                        Turquoise.copy(alpha = 0.12f)
                    else Purple
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                if (item.isEquipped) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Turquoise,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Equipado",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Turquoise
                    )
                } else {
                    Text(
                        text = "Equipar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
