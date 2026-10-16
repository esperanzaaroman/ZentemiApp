package com.zentemi.app.ui.learning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LessonCompleteScreen(
    appState: AppState,
    correctAnswers: Int,
    totalAnswers: Int,
    elapsedSeconds: Int,
    onContinue: () -> Unit
) {
    val rewardCoins = 700
    val rewardXP = 140

    val starsEarned = when {
        totalAnswers == 0 -> 1
        correctAnswers.toFloat() / totalAnswers >= 1.0f -> 4
        correctAnswers.toFloat() / totalAnswers >= 0.75f -> 3
        correctAnswers.toFloat() / totalAnswers >= 0.50f -> 2
        else -> 1
    }

    val formattedTime = "${elapsedSeconds / 60}:${String.format("%02d", elapsedSeconds % 60)}"

    var showMascot by remember { mutableStateOf(false) }
    var visibleStars by remember { mutableIntStateOf(0) }
    var showCorrectCard by remember { mutableStateOf(false) }
    var showTimeCard by remember { mutableStateOf(false) }
    var showCoinsCard by remember { mutableStateOf(false) }
    var showXPCard by remember { mutableStateOf(false) }
    var showContinueButton by remember { mutableStateOf(false) }

    var displayedXP by remember { mutableIntStateOf(appState.totalXP) }
    var displayedLevel by remember { mutableIntStateOf(appState.currentLevel) }
    var displayedXPRemaining by remember { mutableIntStateOf(appState.xpRemaining) }
    var xpBarProgress by remember { mutableFloatStateOf(appState.levelProgress) }

    var rewardApplied by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Run the staggered animation sequence
    LaunchedEffect(Unit) {
        val startingXP = appState.totalXP

        // Show mascot
        showMascot = true

        // Show stars one by one
        delay(450)
        for (i in 1..starsEarned) {
            delay(180)
            visibleStars = i
        }

        // Show result cards
        delay(400)
        showCorrectCard = true
        delay(250)
        showTimeCard = true
        delay(250)
        showCoinsCard = true

        // Show XP card
        delay(300)
        showXPCard = true

        // Animate XP gain
        delay(500)
        if (!rewardApplied) {
            rewardApplied = true
            val wasAlreadyClaimed = appState.greetingsRewardClaimed
            appState.completeGreetingsLesson()

            if (!wasAlreadyClaimed) {
                // Animate XP counting up
                val steps = 70
                for (step in 0..steps) {
                    val animatedAmount = (rewardXP.toFloat() * step / steps).toInt()
                    val total = startingXP + animatedAmount
                    displayedXP = total

                    val calculatedLevel = minOf(
                        (total / appState.xpRequiredPerLevel) + 1,
                        appState.maxLevel
                    )
                    displayedLevel = calculatedLevel

                    if (calculatedLevel >= appState.maxLevel) {
                        displayedXPRemaining = 0
                        xpBarProgress = 1f
                    } else {
                        val levelXP = total % appState.xpRequiredPerLevel
                        displayedXPRemaining = appState.xpRequiredPerLevel - levelXP
                        xpBarProgress = levelXP.toFloat() / appState.xpRequiredPerLevel
                    }

                    delay(20)
                }

                // Final sync
                delay(100)
                displayedXP = appState.totalXP
                displayedLevel = appState.currentLevel
                displayedXPRemaining = appState.xpRemaining
                xpBarProgress = appState.levelProgress
            } else {
                displayedXP = appState.totalXP
                displayedLevel = appState.currentLevel
                displayedXPRemaining = appState.xpRemaining
                xpBarProgress = appState.levelProgress
            }
        }

        // Show continue button
        delay(700)
        showContinueButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(45.dp))

            // Mascot
            AnimatedVisibility(
                visible = showMascot,
                enter = scaleIn() + fadeIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PetView(appState = appState, size = 105.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "¡Excelente!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stars
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.height(45.dp)
            ) {
                repeat(starsEarned) { index ->
                    AnimatedVisibility(
                        visible = index < visibleStars,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn()
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFEB3B),
                            modifier = Modifier.size(31.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Result cards
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultCard(
                    visible = showCorrectCard,
                    icon = Icons.Filled.Check,
                    value = "$correctAnswers/$totalAnswers",
                    title = "Aciertos",
                    color = Turquoise,
                    modifier = Modifier.weight(1f)
                )

                ResultCard(
                    visible = showTimeCard,
                    icon = Icons.Filled.Schedule,
                    value = formattedTime,
                    title = "Tiempo",
                    color = Purple,
                    modifier = Modifier.weight(1f)
                )

                ResultCard(
                    visible = showCoinsCard,
                    icon = Icons.Filled.MonetizationOn,
                    value = "+$rewardCoins",
                    title = "Monedas",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // XP card
            AnimatedVisibility(
                visible = showXPCard,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Experiencia",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Text(
                                text = "Nivel $displayedLevel · $displayedXP XP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Turquoise
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // XP progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.Gray.copy(alpha = 0.10f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(xpBarProgress)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Turquoise)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (displayedLevel >= appState.maxLevel)
                                "Nivel máximo alcanzado"
                            else
                                "$displayedXPRemaining XP para el Nivel ${displayedLevel + 1}",
                            fontSize = 11.sp,
                            color = Color.Gray.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Continue button
        AnimatedVisibility(
            visible = showContinueButton,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    visible: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.7f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(15.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.height(88.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = SubText
                )
            }
        }
    }
}
