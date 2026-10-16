package com.zentemi.app.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*

private data class StoreItem(
    val id: String,
    @DrawableRes val image: Int,
    val name: String,
    val price: Int,
    val rarity: String,
    val rarityColor: Color
)

private val storeItems = listOf(
    StoreItem("sunglasses", R.drawable.lentesdesol, "Lentes de sol", 120, "NUEVO", RarityNew),
    StoreItem("pinkHair", R.drawable.cabellorosa, "Cabello rosa", 250, "RARO", SplashDotActive),
    StoreItem("sword", R.drawable.espada, "Espada real", 500, "ÉPICO", RarityEpic),
    StoreItem("suit", R.drawable.traje1k, "Traje elegante", 1000, "LEGENDARIO", RarityLegendary)
)

@Composable
fun StoreScreen(appState: AppState) {

    var confirmingItemId by remember { mutableStateOf<String?>(null) }
    var showInsufficientCoins by remember { mutableStateOf(false) }

    fun isPurchased(item: StoreItem) = when (item.id) {
        "sunglasses" -> appState.sunglassesPurchased
        "pinkHair" -> appState.pinkHairPurchased
        "sword" -> appState.swordPurchased
        "suit" -> appState.suitPurchased
        else -> false
    }

    // Items still for sale first, purchased ones at the end
    val sortedItems = storeItems.sortedBy { isPurchased(it) }

    fun purchase(item: StoreItem) {
        if (appState.coins < item.price) {
            confirmingItemId = null
            showInsufficientCoins = true
            return
        }
        appState.coins -= item.price
        when (item.id) {
            "sunglasses" -> appState.sunglassesPurchased = true
            "pinkHair" -> appState.pinkHairPurchased = true
            "sword" -> appState.swordPurchased = true
            "suit" -> appState.suitPurchased = true
        }
        appState.purchasedItemAnimation = item.id
        confirmingItemId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        WaveHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderTitle(title = "Tienda", subtitle = "Viste a ${appState.petName}")
                Spacer(modifier = Modifier.weight(1f))
                CoinPill(coins = appState.coins)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(sortedItems, key = { it.id }) { item ->
                StoreCard(
                    item = item,
                    purchased = isPurchased(item),
                    isConfirming = confirmingItemId == item.id,
                    coins = appState.coins,
                    onBuy = { confirmingItemId = item.id },
                    onConfirm = { purchase(item) },
                    onCancel = { confirmingItemId = null }
                )
            }
        }
    }

    if (showInsufficientCoins) {
        AlertDialog(
            onDismissRequest = { showInsufficientCoins = false },
            containerColor = Color.White,
            title = { Text("Monedas insuficientes") },
            text = { Text("No tienes suficientes monedas para comprar este objeto.") },
            confirmButton = {
                TextButton(onClick = { showInsufficientCoins = false }) {
                    Text("Entendido", color = Purple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun StoreCard(
    item: StoreItem,
    purchased: Boolean,
    isConfirming: Boolean,
    coins: Int,
    onBuy: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ZentemiCard(
        modifier = Modifier
            .animateContentSize()
            .then(
                if (purchased) Modifier.border(1.5.dp, Turquoise.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                else Modifier
            ),
        padding = 12.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                TagChip(text = item.rarity, color = item.rarityColor)
            }

            ItemImage(image = item.image, faded = purchased)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = if (purchased) SubText.copy(alpha = 0.5f) else CoinGold,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "${item.price}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (purchased) SubText.copy(alpha = 0.5f) else DarkText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                purchased -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Turquoise.copy(alpha = 0.12f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Turquoise, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Comprado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Turquoise)
                    }
                }

                isConfirming -> {
                    Text("¿Confirmar compra?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    Text(
                        text = "Te quedarán ${maxOf(0, coins - item.price)} monedas",
                        fontSize = 11.sp,
                        color = SubText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChunkyButton(
                        text = "Confirmar",
                        onClick = onConfirm,
                        color = Turquoise,
                        lipColor = Color(0xFF1E9A8A),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = onCancel) {
                        Text("Regresar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Purple)
                    }
                }

                else -> ChunkyButton(text = "Comprar", onClick = onBuy, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** Item picture on a soft lavender tile. */
@Composable
fun ItemImage(@DrawableRes image: Int, faded: Boolean = false) {
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .size(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            modifier = Modifier
                .size(66.dp)
                .alpha(if (faded) 0.55f else 1f)
        )
    }
}
