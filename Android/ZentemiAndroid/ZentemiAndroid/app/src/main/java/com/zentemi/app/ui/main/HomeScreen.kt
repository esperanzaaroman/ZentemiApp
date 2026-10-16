package com.zentemi.app.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*

@Composable
fun HomeScreen(
    appState: AppState,
    onPlayPressed: () -> Unit
) {
    var showCoinsInfo by remember { mutableStateOf(false) }
    var showStreakInfo by remember { mutableStateOf(false) }

    val breathe = rememberInfiniteTransition(label = "breathe")
    val mascotScale by breathe.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotScale"
    )

    val bgAnim = rememberInfiniteTransition(label = "bg")
    val circleOffset by bgAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circleOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Animated background circles
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(
                    x = (-100 + circleOffset * 30).dp,
                    y = (-50 + circleOffset * 40).dp
                )
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(
                    x = (200 - circleOffset * 25).dp,
                    y = (300 + circleOffset * 35).dp
                )
                .clip(CircleShape)
                .background(Turquoise.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 80.dp), // space for tab bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top stats bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF9C4),
                    modifier = Modifier.clickable { showCoinsInfo = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Filled.MonetizationOn,
                            contentDescription = "Monedas",
                            tint = CoinGoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${appState.coins}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoinGoldDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Streak badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.clickable { showStreakInfo = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocalFireDepartment,
                            contentDescription = "Racha",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${appState.streak}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Pet name
            Text(
                text = appState.petName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mascot with glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .blur(30.dp)
                        .clip(CircleShape)
                        .background(Purple.copy(alpha = 0.08f))
                )
                Box(modifier = Modifier.scale(mascotScale)) {
                    PetView(
                        appState = appState,
                        size = 130.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Happiness bar
            Column(
                modifier = Modifier.padding(horizontal = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Felicidad",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubText
                    )
                    Text(
                        text = "${appState.happiness}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Turquoise
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { appState.happiness / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Turquoise,
                    trackColor = Color.Gray.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Feed pet */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Turquoise.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "🍎 Alimentar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Turquoise
                    )
                }

                Button(
                    onClick = { /* Play with pet */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple.copy(alpha = 0.10f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "🎮 Jugar con",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Purple
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Play button
            Button(
                onClick = onPlayPressed,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(
                    Icons.Filled.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jugar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Info dialogs
    if (showCoinsInfo) {
        AlertDialog(
            onDismissRequest = { showCoinsInfo = false },
            title = { Text("Monedas") },
            text = { Text("Las monedas se ganan al completar lecciones. Úsalas para comprar objetos en la tienda.") },
            confirmButton = {
                TextButton(onClick = { showCoinsInfo = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showStreakInfo) {
        AlertDialog(
            onDismissRequest = { showStreakInfo = false },
            title = { Text("Racha") },
            text = { Text("Tu racha aumenta cada día que completas al menos una lección. ¡No la pierdas!") },
            confirmButton = {
                TextButton(onClick = { showStreakInfo = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}
