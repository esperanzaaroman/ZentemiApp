package com.zentemi.app.ui.learning.lessons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.data.LessonReward
import com.zentemi.app.data.PathLesson
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*

/** Manual para la escritura de los sonidos del yuhmu: pp. 74, 82 y 85. */
private enum class LessonFood(val word: String, val spanish: String, @DrawableRes val image: Int) {
    WATER("Déhe", "Agua", R.drawable.food_lesson_water),
    AVOCADO("Ts’óni", "Aguacate", R.drawable.food_lesson_avocado),
    POTATO("K’ápuxü", "Papa", R.drawable.food_lesson_potato),
    TORTILLA("Hmé", "Tortilla", R.drawable.food_lesson_tortilla)
}

private val foodTitles = listOf("El puesto del mercado", "Ponle nombre", "Sirve el pedido", "Una comida para llevar", "Las canastas de Maicito")
private val foodExplanations = listOf(
    "Déhe: agua · Ts’óni: aguacate · K’ápuxü: papa · Hmé: tortilla.",
    "La papa se llama K’ápuxü.",
    "Ts’óni significa aguacate.",
    "Hmé significa tortilla y Déhe significa agua.",
    "¡Preparaste las tres canastas siguiendo las listas en Yuhmu!"
)

/** Lección 6: Alimentos (FoodLessonView.swift). */
@Composable
fun FoodLessonScreen(
    appState: AppState,
    onExit: () -> Unit,
    onFinished: (LessonReward, Int, Double) -> Unit
) {
    if (!appState.isLessonUnlocked(PathLesson.FOOD)) {
        LessonLockedMessage("Completa Animales para comenzar Alimentos.", onExit)
        return
    }
    val sounds = rememberLessonSounds(appState)
    val progress = remember { LessonProgress() }
    var showExit by remember { mutableStateOf(false) }
    val clock = rememberLessonClock(paused = showExit)
    val active = rememberAppActive()

    var discovered by remember { mutableStateOf(setOf<LessonFood>()) }
    var choice by remember { mutableStateOf<LessonFood?>(null) }
    var selectedFood by remember { mutableStateOf(setOf<LessonFood>()) }
    val step = progress.step
    val enabled = !progress.checked && !showExit && active

    fun finish(correct: Boolean) {
        if (!active || !progress.answer(correct)) return
        if (correct) sounds.good() else sounds.bad()
    }

    @Composable
    fun FoodGrid() {
        TwoColumnGrid(listOf(LessonFood.AVOCADO, LessonFood.TORTILLA, LessonFood.POTATO, LessonFood.WATER)) { food, modifier ->
            val isSelected = if (step == 2) choice == food else food in selectedFood
            LessonCard(modifier = modifier, selected = isSelected, enabled = enabled, onClick = {
                if (step == 2) choice = food
                else selectedFood = when {
                    food in selectedFood -> selectedFood - food
                    selectedFood.size < 2 -> selectedFood + food
                    else -> selectedFood
                }
                sounds.select()
            }) {
                LessonImage(food.image, food.spanish, 115.dp)
                Icon(
                    if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Seleccionado" else null,
                    tint = LessonTeal,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    LessonFrame(
        lessonTitle = "Alimentos",
        stepTitle = foodTitles[step],
        step = step,
        total = 5,
        showExitDialog = showExit,
        onShowExitDialog = { showExit = it },
        onExitConfirmed = onExit,
        exitTitle = "¿Salir de Alimentos?"
    ) {
        if (progress.teaching) {
            PetView(appState = appState, size = 90.dp)
            LessonText("¡Vamos al mercado con ${appState.petName}! Descubre alimentos y una bebida, practica sus nombres y prepara las canastas.")
            LessonButton("¡Comenzar!") { progress.teaching = false }
            return@LessonFrame
        }
        when (step) {
            0 -> {
                LessonText("Explora el puesto: toca cada producto y descubre su nombre.")
                TwoColumnGrid(LessonFood.entries) { food, modifier ->
                    val known = food in discovered
                    LessonCard(modifier = modifier, selected = known, highlight = LessonOrange, enabled = enabled, onClick = {
                        discovered = discovered + food
                        sounds.select()
                    }) {
                        LessonImage(food.image, food.spanish, 115.dp)
                        Text(if (known) food.word else "Descubrir", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Purple)
                        Text(food.spanish, fontSize = 14.sp, color = Purple)
                        Icon(if (known) Icons.Filled.Verified else Icons.Filled.TouchApp, contentDescription = null, tint = Purple)
                    }
                }
                Text("${discovered.size} de 4 productos descubiertos", fontSize = 12.sp, color = SubText)
            }
            1 -> {
                LessonText("Pon el nombre correcto en la etiqueta de esta papa.")
                LessonImage(LessonFood.POTATO.image, LessonFood.POTATO.spanish, 180.dp)
                listOf(LessonFood.TORTILLA, LessonFood.WATER, LessonFood.POTATO, LessonFood.AVOCADO).forEach { food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(18.dp), ambientColor = Purple.copy(alpha = 0.12f))
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (choice == food) Purple.copy(alpha = 0.15f) else Color.White)
                            .clickable(enabled = enabled) { choice = food }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(food.word, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Purple, modifier = Modifier.weight(1f))
                        Icon(
                            if (choice == food) Icons.Filled.CheckCircle else Icons.Filled.LocalOffer,
                            contentDescription = if (choice == food) "Seleccionado" else null,
                            tint = Purple
                        )
                    }
                }
            }
            2 -> {
                Text("${appState.petName} pide: Ts’óni", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
                LessonText("Toca el producto para colocarlo en el plato.")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White)
                        .border(5.dp, Purple.copy(alpha = 0.2f), RoundedCornerShape(percent = 50)),
                    contentAlignment = Alignment.Center
                ) {
                    val plated = choice
                    if (plated != null) LessonImage(plated.image, plated.spanish, 100.dp)
                    else Icon(Icons.Filled.Restaurant, contentDescription = "Plato vacío", tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
                FoodGrid()
            }
            3 -> {
                LessonText("Prepara una comida para llevar", bold = true)
                Text("Hmé  +  Déhe", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Purple)
                LessonText("Selecciona los dos productos de la lista. Puedes cambiar antes de verificar.")
                FoodGrid()
                Text("${selectedFood.size} de 2 seleccionados", fontSize = 12.sp, color = SubText)
            }
            else -> FoodBaskets(appState, enabled, sounds) { mistakes -> finish(mistakes == 0) }
        }

        val canVerify = when (step) {
            0 -> discovered.size == LessonFood.entries.size
            1, 2 -> choice != null
            3 -> selectedFood.size == 2
            else -> false
        }
        LessonCheckArea(
            progress = progress,
            explanation = foodExplanations[step],
            canVerify = canVerify,
            showVerify = step < 4,
            onVerify = {
                finish(
                    when (step) {
                        0 -> discovered.size == 4
                        1 -> choice == LessonFood.POTATO
                        2 -> choice == LessonFood.AVOCADO
                        else -> selectedFood == setOf(LessonFood.TORTILLA, LessonFood.WATER)
                    }
                )
            },
            onNext = {
                if (progress.isLastStep) {
                    clock.stop()
                    appState.completeLesson(PathLesson.FOOD, progress.score, clock.elapsedSeconds)?.let {
                        onFinished(it, progress.score, clock.elapsedSeconds)
                    }
                } else {
                    progress.next()
                    choice = null
                    selectedFood = emptySet()
                }
            }
        )
    }
}

/** Las canastas: preparar tres pedidos con cantidades exactas (FoodBasketGameView). */
@Composable
private fun FoodBaskets(appState: AppState, enabled: Boolean, sounds: LessonSounds, onCompleted: (Int) -> Unit) {
    val orders = listOf(
        mapOf(LessonFood.TORTILLA to 2, LessonFood.WATER to 1),
        mapOf(LessonFood.AVOCADO to 1, LessonFood.POTATO to 2),
        mapOf(LessonFood.WATER to 1, LessonFood.AVOCADO to 1, LessonFood.POTATO to 1, LessonFood.TORTILLA to 1)
    )
    var round by remember { mutableIntStateOf(0) }
    var basket by remember { mutableStateOf(mapOf<LessonFood, Int>()) }
    var mistakes by remember { mutableIntStateOf(0) }
    var delivered by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("Toca un producto para ponerlo en la canasta.") }

    fun add(food: LessonFood) {
        if (delivered || (basket[food] ?: 0) >= 3) return
        basket = basket + (food to (basket[food] ?: 0) + 1)
        sounds.select()
    }

    fun deliver() {
        if (delivered) return
        if (basket == orders[round]) {
            delivered = true
            feedback = "¡Todo coincide con la lista!"
            if (round == orders.lastIndex) {
                finished = true
                onCompleted(mistakes)
            } else {
                sounds.good()
            }
        } else {
            mistakes += 1
            feedback = "Revisa productos y cantidades. Puedes quitar o agregar antes de intentarlo otra vez."
            sounds.bad()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PetView(appState = appState, size = 75.dp)
            Text("Las canastas de ${appState.petName}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
        }
        Text("Pedido ${round + 1} de 3", fontWeight = FontWeight.Bold, color = DarkText)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFCC00).copy(alpha = 0.15f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = DarkText)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lista de compras", fontWeight = FontWeight.Bold, color = DarkText)
            }
            LessonFood.entries.forEach { food ->
                orders[round][food]?.let { Text("$it × ${food.word}", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = DarkText) }
            }
        }
        LessonText("Cada toque agrega una unidad, aunque el dibujo muestre varias tortillas.", size = 12, color = SubText)
        TwoColumnGrid(LessonFood.entries, spacing = 14.dp) { food, modifier ->
            LessonCard(
                modifier = modifier,
                enabled = enabled && !delivered && (basket[food] ?: 0) < 3,
                onClick = { add(food) }
            ) {
                LessonImage(food.image, "Agregar una unidad de ${food.spanish}", 85.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AddCircle, contentDescription = null, tint = Purple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(if (delivered) LessonMint.copy(alpha = 0.3f) else LessonOrange.copy(alpha = 0.1f))
                .border(2.dp, LessonOrange.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ShoppingBasket, contentDescription = null, tint = LessonOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (delivered) "¡Canasta lista!" else "Tu canasta", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LessonOrange)
            }
            if (basket.isEmpty()) Text("Toca los productos para agregarlos", color = SubText)
            LessonFood.entries.forEach { food ->
                val count = basket[food] ?: 0
                if (count > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        LessonImage(food.image, null, 45.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$count × ${food.spanish}", fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { basket = if (count == 1) basket - food else basket + (food to count - 1) },
                            enabled = enabled && !delivered
                        ) {
                            Icon(Icons.Filled.RemoveCircle, contentDescription = "Quitar una unidad de ${food.spanish}", tint = Purple)
                        }
                    }
                }
            }
        }
        LessonText(feedback, bold = true, color = if (delivered) LessonTeal else Purple)
        if (!delivered) {
            LessonButton("Entregar canasta", enabled = enabled && basket.isNotEmpty()) { deliver() }
        } else if (!finished) {
            LessonButton("Preparar siguiente canasta") {
                round += 1
                basket = emptyMap()
                delivered = false
                feedback = "Lee la nueva lista y prepara sus cantidades."
            }
        }
        LessonText("Sin tiempo límite. Entrega las tres canastas sin errores para ganar el acierto.", size = 12, color = SubText)
    }
}
