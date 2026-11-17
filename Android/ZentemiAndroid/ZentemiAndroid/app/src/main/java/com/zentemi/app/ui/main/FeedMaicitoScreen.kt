package com.zentemi.app.ui.main

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.audio.SoundManager
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.Purple
import com.zentemi.app.ui.theme.Turquoise
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

private data class Food(
    val id: String,
    @param:DrawableRes val image: Int,
    val name: String,
    val price: Int,
    val happiness: Int
)

private val foods = listOf(
    Food("manzana", R.drawable.manzana, "Manzana", price = 20, happiness = 5),
    Food("tortillas", R.drawable.tortillas, "Tortillas", price = 30, happiness = 8),
    Food("tamal", R.drawable.tamal, "Tamal", price = 50, happiness = 15)
)

private val CoinOrange = Color(0xFFFF9500)

/** Distancia (en dp) a Maicito para que la comida cuente como entregada. */
private const val FEED_DISTANCE_DP = 105f

/**
 * Alimentar a Maicito: se elige una comida, aparece en el plato y se
 * arrastra hacia Maicito. Cuesta monedas, sube la felicidad y hay máximo
 * 3 comidas cada 4 horas. Equivalente a FeedMaicitoView.swift.
 */
@Composable
fun FeedMaicitoScreen(
    appState: AppState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var foodDragOffset by remember { mutableStateOf(Offset.Zero) }
    val returnAnimation = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var isFeeding by remember { mutableStateOf(false) }
    var showHearts by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    var tutorialShownThisSession by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageJob by remember { mutableStateOf<Job?>(null) }

    val happiness = appState.happiness.coerceIn(0, 100)
    val availableMeals = appState.availableMeals
    val isFull = happiness >= 100
    val canChooseFood = !isFull && availableMeals > 0 && !isFeeding

    BackHandler { onBack() }

    fun showMessage(text: String) {
        messageJob?.cancel()
        message = text
        messageJob = scope.launch {
            delay(2000)
            message = null
        }
    }

    fun selectFood(food: Food) {
        if (!canChooseFood) return
        if (appState.coins < food.price) {
            showMessage("No tienes suficientes monedas.")
            return
        }
        selectedFood = food
        foodDragOffset = Offset.Zero
        SoundManager.play(context, R.raw.audioseleccion, appState.soundVolume)
        if (!tutorialShownThisSession) {
            showTutorial = true
            tutorialShownThisSession = true
        }
    }

    fun snapFoodBack() {
        val from = foodDragOffset
        scope.launch {
            returnAnimation.snapTo(from)
            returnAnimation.animateTo(Offset.Zero, spring(dampingRatio = 0.65f, stiffness = 300f)) {
                foodDragOffset = value
            }
        }
    }

    fun feedMaicito(food: Food) {
        if (isFeeding || happiness >= 100 || availableMeals <= 0 || appState.coins < food.price) {
            snapFoodBack()
            return
        }
        isFeeding = true
        appState.coins -= food.price
        appState.happiness = minOf(100, appState.happiness + food.happiness)
        appState.registerMeal()

        selectedFood = null
        foodDragOffset = Offset.Zero
        showTutorial = false
        showHearts = true
        SoundManager.play(context, R.raw.audiobien, appState.soundVolume)
        showMessage("¡${appState.petName} ganó +${food.happiness}% de felicidad!")

        scope.launch {
            delay(1500)
            showHearts = false
            isFeeding = false
        }
    }

    val breathing = rememberInfiniteTransition(label = "breathing")
    val mascotScaleY by breathing.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "mascotScaleY"
    )
    val tutorialHand by breathing.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tutorialHand"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.fondococina),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Oscurecimiento ligero para legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.08f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Encabezado ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Purple)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = 0.96f))
                        .padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row {
                        Text(
                            "Felicidad de ${appState.petName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text("$happiness%", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Purple)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Gray.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(happiness / 100f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Turquoise)
                        )
                    }
                }
            }

            // ── Área de alimentación ──
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                val areaWidth = maxWidth
                val areaHeight = maxHeight
                val mascotCenterY = areaHeight * 0.32f
                val plateCenterY = areaHeight * 0.73f

                // Maicito
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = mascotCenterY - 82.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    PetView(
                        appState = appState,
                        size = 165.dp,
                        modifier = Modifier.graphicsLayer {
                            scaleY = mascotScaleY
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                    )
                }

                // Corazones
                if (showHearts) {
                    HeartBurst(
                        modifier = Modifier
                            .size(180.dp, 150.dp)
                            .offset(x = areaWidth / 2 - 90.dp, y = mascotCenterY - 85.dp - 75.dp)
                    )
                }

                // Plato
                Image(
                    painter = painterResource(R.drawable.plato),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(190.dp, 130.dp)
                        .offset(x = areaWidth / 2 - 95.dp, y = plateCenterY - 65.dp)
                )

                // Comida seleccionada (se arrastra hacia Maicito)
                selectedFood?.let { food ->
                    Image(
                        painter = painterResource(food.image),
                        contentDescription = food.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .offset(x = areaWidth / 2 - 50.dp, y = plateCenterY - 12.dp - 50.dp)
                            .offset { IntOffset(foodDragOffset.x.roundToInt(), foodDragOffset.y.roundToInt()) }
                            .size(100.dp)
                            .pointerInput(food) {
                                detectDragGestures(
                                    onDragStart = {
                                        if (showTutorial) showTutorial = false
                                    },
                                    onDrag = { change, amount ->
                                        if (!isFeeding) {
                                            change.consume()
                                            foodDragOffset += amount
                                        }
                                    },
                                    onDragEnd = {
                                        if (isFeeding) return@detectDragGestures
                                        // Distancia entre la comida y el centro de Maicito.
                                        val dyToMascot = with(density) {
                                            ((plateCenterY - 12.dp) - mascotCenterY).toPx()
                                        }
                                        val distance = hypot(
                                            foodDragOffset.x,
                                            foodDragOffset.y + dyToMascot
                                        )
                                        val limit = with(density) { FEED_DISTANCE_DP.dp.toPx() }
                                        if (distance < limit) feedMaicito(food) else snapFoodBack()
                                    },
                                    onDragCancel = { snapFoodBack() }
                                )
                            }
                    )
                }

                // Tutorial
                if (showTutorial && selectedFood != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = plateCenterY - 115.dp - 35.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "¡Desliza la comida hacia ${appState.petName}!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Purple)
                                .padding(horizontal = 15.dp, vertical = 10.dp)
                        )
                        Icon(
                            Icons.Filled.TouchApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                                .offset(y = tutorialHand.dp)
                        )
                    }
                }
            }

            // ── Carrusel de comida ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.96f))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alimentos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Purple)
                Spacer(modifier = Modifier.weight(1f))
                CoinLabel(appState.coins, 14)
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(foods, key = { it.id }) { food ->
                    val canAfford = appState.coins >= food.price
                    val enabled = canChooseFood && canAfford
                    Column(
                        modifier = Modifier
                            .size(120.dp, 157.dp)
                            .graphicsLayer { alpha = if (enabled) 1f else 0.55f }
                            .clip(RoundedCornerShape(17.dp))
                            .background(Color.White.copy(alpha = 0.96f))
                            .border(
                                3.dp,
                                if (selectedFood?.id == food.id) Purple else Color.Transparent,
                                RoundedCornerShape(17.dp)
                            )
                            // Sin monedas se puede tocar para ver el aviso "No tienes suficientes monedas".
                            .clickable(enabled = canChooseFood) { selectFood(food) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
                    ) {
                        Image(
                            painter = painterResource(food.image),
                            contentDescription = null,
                            modifier = Modifier.size(75.dp)
                        )
                        Text(food.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Purple)
                        Text(
                            "+${food.happiness}% felicidad",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Turquoise
                        )
                        CoinLabel(food.price, 12)
                    }
                }
            }

            // ── Estado ──
            Text(
                text = when {
                    isFull -> "¡${appState.petName} está muy feliz!"
                    availableMeals == 0 -> "${appState.petName} está lleno. Vuelve más tarde."
                    else -> "Comidas disponibles: $availableMeals/${appState.maxMeals}"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFull) Color(0xFF1E9E47) else Purple,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            )
        }

        // ── Mensajes ──
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 185.dp, start = 20.dp, end = 20.dp)
        ) {
            Text(
                text = message.orEmpty(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .shadow(5.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(Purple.copy(alpha = 0.95f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun CoinLabel(amount: Int, fontSize: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.MonetizationOn,
            contentDescription = null,
            tint = CoinOrange,
            modifier = Modifier.size((fontSize + 4).dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("$amount", fontSize = fontSize.sp, fontWeight = FontWeight.Bold, color = CoinOrange)
    }
}

/** 8 corazones que suben y se desvanecen (HeartAnimationView de iOS). */
@Composable
private fun HeartBurst(modifier: Modifier = Modifier) {
    val progress = remember { List(8) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        progress.forEachIndexed { index, anim ->
            launch {
                delay(index * 60L)
                anim.animateTo(1f, tween(900 + (index % 3) * 150, easing = LinearOutSlowInEasing))
            }
        }
    }
    BoxWithConstraints(modifier = modifier) {
        val h = maxHeight
        progress.forEachIndexed { index, anim ->
            val p = anim.value
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = if (index % 2 == 0) Color(0xFFFF2D55) else Color(0xFFFF3B30),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(
                        x = ((index % 4) * 25 - 37).dp,
                        y = (h / 2) - (h / 2 + 25.dp) * p
                    )
                    .size((17 + (index % 4) * 5).dp)
                    .graphicsLayer {
                        alpha = 1f - p
                        val scale = 0.6f + 0.8f * p
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}
