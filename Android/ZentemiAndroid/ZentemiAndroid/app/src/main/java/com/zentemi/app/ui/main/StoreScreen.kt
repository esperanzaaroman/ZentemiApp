package com.zentemi.app.ui.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.theme.*

private data class StoreItem(
    val id: String,
    val emoji: String,
    val name: String,
    val price: Int,
    val rarity: String,
    val rarityColor: Color
)

@Composable
fun StoreScreen(appState: AppState) {

    var confirmingItemId by remember { mutableStateOf<String?>(null) }
    var showInsufficientCoins by remember { mutableStateOf(false) }

    val unpurchasedItems = buildList {
        if (!appState.sunglassesPurchased) add(
            StoreItem("sunglasses", "🕶️", "Lentes de sol", 120, "NUEVO", Turquoise)
        )
        if (!appState.pinkHairPurchased) add(
            StoreItem("pinkHair", "💇‍♀️", "Cabello rosa", 250, "RARO", Color(0xFFFFEB3B))
        )
        if (!appState.swordPurchased) add(
            StoreItem("sword", "⚔️", "Espada real", 500, "ÉPICO", Color(0xFF9C27B0))
        )
        if (!appState.suitPurchased) add(
            StoreItem("suit", "🤵", "Traje elegante", 1000, "LEGENDARIO", Color(0xFFFF9800))
        )
    }

    val purchasedItems = buildList {
        if (appState.sunglassesPurchased) add(
            StoreItem("sunglasses", "🕶️", "Lentes de sol", 120, "NUEVO", Turquoise)
        )
        if (appState.pinkHairPurchased) add(
            StoreItem("pinkHair", "💇‍♀️", "Cabello rosa", 250, "RARO", Color(0xFFFFEB3B))
        )
        if (appState.swordPurchased) add(
            StoreItem("sword", "⚔️", "Espada real", 500, "ÉPICO", Color(0xFF9C27B0))
        )
        if (appState.suitPurchased) add(
            StoreItem("suit", "🤵", "Traje elegante", 1000, "LEGENDARIO", Color(0xFFFF9800))
        )
    }

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
        appState.purchasedItemAnimation = item.emoji
        confirmingItemId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundStore)
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
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = (-130).dp, y = (-75).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
            )
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .offset(x = 150.dp, y = 80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.035f))
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
                        text = "Tienda",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Viste a ${appState.petName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Coin badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Yellow
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = CoinGoldDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${appState.coins}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoinGoldDark
                        )
                    }
                }
            }
        }

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 14.dp,
                bottom = 100.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(unpurchasedItems, key = { it.id }) { item ->
                StoreCard(
                    item = item,
                    isConfirming = confirmingItemId == item.id,
                    coins = appState.coins,
                    onBuy = { confirmingItemId = item.id },
                    onConfirm = { purchase(item) },
                    onCancel = { confirmingItemId = null }
                )
            }

            items(purchasedItems, key = { "purchased_${it.id}" }) { item ->
                PurchasedCard(item = item)
            }
        }
    }

    if (showInsufficientCoins) {
        AlertDialog(
            onDismissRequest = { showInsufficientCoins = false },
            title = { Text("Monedas insuficientes") },
            text = { Text("No tienes suficientes monedas para comprar este objeto.") },
            confirmButton = {
                TextButton(onClick = { showInsufficientCoins = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun StoreCard(
    item: StoreItem,
    isConfirming: Boolean,
    coins: Int,
    onBuy: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rarity badge
            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = item.rarityColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.rarity,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.rarityColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.Gray.copy(alpha = 0.055f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = CoinGold,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "${item.price}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoinGold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isConfirming) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¿Confirmar compra?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Te quedarán ${maxOf(0, coins - item.price)} monedas",
                    fontSize = 10.sp,
                    color = SubText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Text("Confirmar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onCancel) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Purple
                        )
                        Text(
                            text = "Regresar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Purple
                        )
                    }
                }

            } else {
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Text("Comprar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PurchasedCard(item: StoreItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Turquoise.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = item.rarityColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.rarity,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.rarityColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.Gray.copy(alpha = 0.055f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = SubText,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "${item.price}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Turquoise.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Turquoise,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Comprado",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Turquoise
                    )
                }
            }
        }
    }
}
