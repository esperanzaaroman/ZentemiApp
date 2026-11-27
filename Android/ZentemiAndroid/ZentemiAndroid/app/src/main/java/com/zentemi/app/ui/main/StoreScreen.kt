package com.zentemi.app.ui.main

import com.zentemi.app.ui.components.pressable
import com.zentemi.app.ui.components.CoinBadge
import com.zentemi.app.ui.components.ScreenHeaderDefaults
import com.zentemi.app.ui.components.ScreenTitle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.data.Backdrop
import com.zentemi.app.data.ItemCategory
import com.zentemi.app.data.ItemSlot
import com.zentemi.app.data.ShopCatalog
import com.zentemi.app.data.ShopItem
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.MaicitoBackdrop
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.components.TagChip
import com.zentemi.app.ui.components.ZentemiIcon
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay

// Colores de la tienda: claros, como Inicio, con tarjetas de borde lila
private val StoreBgTop = BackgroundLight
private val StoreBgBottom = Color(0xFFF1EAFB)
private val TabBarColor = Color.White
private val CardTop = Color.White
private val CardBottom = Color(0xFFF1E9FC)
private val CardBorder = Color(0xFFD7C8F2)
private val TurquoiseDark = Color(0xFF1E9A8A)

/**
 * Tienda y mochila en una sola pantalla: arriba Maicito con lo que trae puesto
 * (o lo que se está probando), en medio las pestañas y abajo la cuadrícula.
 * Lo que ya es tuyo no tiene precio: tocarlo lo pone o lo quita.
 */
@Composable
fun StoreScreen(appState: AppState) {
    val context = LocalContext.current

    var category by rememberSaveable { mutableStateOf(ItemCategory.CLOTHES) }
    // Artículo que no es tuyo y Maicito se está probando
    var previewId by remember { mutableStateOf<String?>(null) }
    var justBoughtId by remember { mutableStateOf<String?>(null) }

    val preview = previewId?.let { ShopCatalog.byId(it) }
    val items = ShopCatalog.items.filter { category.contains(it) }
    val anyEquippedHere = items.any { appState.isEquipped(it) }

    LaunchedEffect(justBoughtId) {
        if (justBoughtId != null) {
            delay(1800)
            justBoughtId = null
        }
    }

    fun onItemTap(item: ShopItem) {
        if (appState.isOwned(item)) {
            previewId = null
            if (appState.isEquipped(item)) {
                SoundManager.play(context, R.raw.audioquitaritem, appState.soundVolume)
            } else {
                SoundManager.play(context, R.raw.audioequipar, minOf(appState.soundVolume * 0.5f, 1f))
            }
            appState.toggleEquip(item)
        } else {
            previewId = if (previewId == item.id) null else item.id
        }
    }

    fun buy(item: ShopItem) {
        if (!appState.purchase(item)) return
        SoundManager.play(context, R.raw.audiocomprado, appState.soundVolume)
        if (!appState.isEquipped(item)) appState.toggleEquip(item)
        appState.purchasedItemAnimation = item.id
        previewId = null
        justBoughtId = item.id
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(StoreBgTop, StoreBgBottom)))
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        StoreHero(
            appState = appState,
            preview = preview,
            justBought = justBoughtId?.let { ShopCatalog.byId(it) },
            showUnequip = anyEquippedHere && preview == null,
            onUnequip = {
                SoundManager.play(context, R.raw.audioquitaritem, appState.soundVolume)
                appState.unequipSlots(category.slots)
            }
        )

        StoreTabs(
            selected = category,
            onSelect = {
                category = it
                previewId = null
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            // Abajo deja lugar para la barra de pestañas (y para la de compra si está abierta)
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 14.dp, bottom = if (preview != null) 200.dp else 110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items, key = { it.id }) { item ->
                StoreItemCard(
                    item = item,
                    owned = appState.isOwned(item),
                    equipped = appState.isEquipped(item),
                    previewing = previewId == item.id,
                    justBought = justBoughtId == item.id,
                    tall = category == ItemCategory.BACKGROUNDS,
                    onClick = { onItemTap(item) }
                )
            }
        }
    }

        // Barra para comprar lo que se está probando: flota sobre la cuadrícula,
        // justo arriba del menú, para que las tarjetas no se muevan.
        AnimatedVisibility(
            visible = preview != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 88.dp)
        ) {
            // Se recuerda el último para que el contenido no desaparezca mientras se va
            var shown by remember { mutableStateOf(preview) }
            if (preview != null) shown = preview
            shown?.let { item ->
                BuyBanner(
                    item = item,
                    coins = appState.coins,
                    onBuy = { buy(item) },
                    onClose = { previewId = null }
                )
            }
        }
    }
}

// ─────────────────────────── Parte de arriba ───────────────────────────

@Composable
private fun StoreHero(
    appState: AppState,
    preview: ShopItem?,
    justBought: ShopItem?,
    showUnequip: Boolean,
    onUnequip: () -> Unit
) {
    // Si se está probando un fondo, se ve ese; si no, el que trae puesto
    val backdrop: Backdrop? = preview?.backdrop ?: appState.equippedBackdrop?.backdrop

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFEEE6FC), Color(0xFFD9C9F6))))
    ) {
        if (backdrop != null) {
            MaicitoBackdrop(backdrop = backdrop, modifier = Modifier.matchParentSize())
        } else {
            // Círculos suaves para que el fondo vacío no se vea plano
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-50).dp, y = 30.dp)
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = Spacing.screen, end = Spacing.screen, top = ScreenHeaderDefaults.top, bottom = 14.dp)
        ) {
            // Arriba: título a la izquierda y monedas a la derecha (igual que en Inicio)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ScreenTitle(
                    title = "Tienda",
                    subtitle = "Viste a ${appState.petName}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CoinBadge(coins = appState.coins)
            }

            // Maicito, con el botón de quitar a la izquierda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            ) {
                PetView(
                    appState = appState,
                    size = 150.dp,
                    preview = preview,
                    modifier = Modifier.align(Alignment.Center)
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = showUnequip,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, Purple.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onUnequip),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Block, contentDescription = "Quitar", tint = Purple, modifier = Modifier.size(30.dp))
                    }
                }

                // Aviso de compra
                androidx.compose.animation.AnimatedVisibility(
                    visible = justBought != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.6f),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Text(
                        text = "¡${justBought?.name ?: ""} es tuyo!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Turquoise)
                            .border(2.dp, Color.White, CircleShape)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BuyBanner(item: ShopItem, coins: Int, onBuy: () -> Unit, onClose: () -> Unit) {
    val missing = item.price - coins
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(3.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            TagChip(text = item.rarity.label, color = item.rarity.color)
            Text(
                text = item.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                maxLines = 1
            )
            Text(
                text = if (missing > 0) "Te faltan $missing monedas" else "Te quedarán ${coins - item.price} monedas",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (missing > 0) ErrorRed else SubText
            )
        }
        ChunkyButton(
            text = "${item.price}",
            onClick = onBuy,
            enabled = missing <= 0,
            color = Turquoise,
            lipColor = TurquoiseDark,
            leading = { ZentemiIcon(R.drawable.ic_moneda, size = 20.dp) },
            modifier = Modifier.width(112.dp)
        )
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = SubText, modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────── Pestañas ───────────────────────────

private val GrayscaleTab = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
private fun StoreTabs(selected: ItemCategory, onSelect: (ItemCategory) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Purple.copy(alpha = 0.2f), spotColor = Purple.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(20.dp))
            .background(TabBarColor)
            .padding(vertical = 8.dp)
    ) {
        ItemCategory.entries.forEach { category ->
            val isSelected = category == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(category) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .scale(if (isSelected) 1.1f else 1f)
                        .alpha(if (isSelected) 1f else 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    if (category == ItemCategory.BACKGROUNDS) {
                        MaicitoBackdrop(
                            backdrop = Backdrop.SUNSET,
                            modifier = Modifier
                                .size(width = 22.dp, height = 30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(2.dp, if (isSelected) Purple else Color.Gray, RoundedCornerShape(6.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(tabIcon(category)),
                            contentDescription = null,
                            colorFilter = if (isSelected) null else GrayscaleTab,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = category.label.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = if (isSelected) Purple else Color.Gray.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Purple else Color.Transparent)
                )
            }
        }
    }
}

private fun tabIcon(category: ItemCategory): Int = when (category) {
    ItemCategory.CLOTHES -> R.drawable.bufanda
    ItemCategory.HEAD -> R.drawable.acc_corona
    ItemCategory.FACE -> R.drawable.lentesdesol
    ItemCategory.OBJECTS -> R.drawable.espada
    ItemCategory.BACKGROUNDS -> R.drawable.ic_tienda
}

// ─────────────────────────── Tarjetas ───────────────────────────

@Composable
private fun StoreItemCard(
    item: ShopItem,
    owned: Boolean,
    equipped: Boolean,
    previewing: Boolean,
    justBought: Boolean,
    tall: Boolean,
    onClick: () -> Unit
) {
    // Rebote cuando se acaba de comprar
    val bounce = remember { Animatable(1f) }
    LaunchedEffect(justBought) {
        if (justBought) {
            bounce.animateTo(1.12f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    val shape = RoundedCornerShape(18.dp)
    val borderColor = when {
        previewing -> SplashDotActive
        equipped -> Turquoise
        else -> CardBorder
    }

    Box(
        modifier = Modifier
            .scale(bounce.value)
            .pressable(pressedScale = 0.95f, onClick = onClick)
            .fillMaxWidth()
            .aspectRatio(if (tall) 0.66f else 1f)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(CardTop, CardBottom)))
            .border(if (previewing || equipped) 3.5.dp else 2.dp, borderColor, shape)
    ) {
        val image = item.image
        val backdrop = item.backdrop
        when {
            backdrop != null -> MaicitoBackdrop(
                backdrop = backdrop,
                modifier = Modifier
                    .matchParentSize()
                    .padding(3.dp)
                    .clip(RoundedCornerShape(15.dp))
            )
            image != null -> Image(
                painter = painterResource(id = image),
                contentDescription = item.name,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = if (owned) 12.dp else 26.dp)
                    .then(cardZoom(item))
            )
        }

        // Precio (solo lo que todavía no es tuyo)
        if (!owned) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Purple.copy(alpha = 0.14f))))
                    .padding(bottom = 6.dp, top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.price}",
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurpleDark
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                ZentemiIcon(R.drawable.ic_moneda, size = 18.dp)
            }
        }

        // Palomita en lo que trae puesto
        if (equipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Turquoise)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Puesto", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/**
 * Los vestidos y cabellos nuevos se dibujan del tamaño de Maicito (con mucho
 * espacio vacío alrededor); en la tarjeta se agrandan y se centran.
 */
private fun cardZoom(item: ShopItem): Modifier {
    val layout = item.layout ?: return Modifier
    if (layout.width < 115f) return Modifier
    return when (item.slot) {
        // La ropa ocupa la mitad de abajo del lienzo
        ItemSlot.BODY -> Modifier.graphicsLayer {
            scaleX = 1.6f; scaleY = 1.6f
            translationY = -size.height * 0.20f * 1.6f
        }
        // Los cabellos ocupan la mitad de arriba
        ItemSlot.HAIR -> Modifier.graphicsLayer {
            scaleX = 1.3f; scaleY = 1.3f
            translationY = size.height * 0.12f * 1.3f
        }
        else -> Modifier
    }
}
