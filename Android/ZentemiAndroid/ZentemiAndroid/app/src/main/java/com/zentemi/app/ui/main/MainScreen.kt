package com.zentemi.app.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.theme.*

data class TabItem(
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainScreen(
    appState: AppState,
    onPlayPressed: () -> Unit,
    onSettingsPressed: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(2) } // HomeView is default

    val tabs = listOf(
        TabItem(Icons.Filled.Storefront, "Tienda"),
        TabItem(Icons.Filled.Backpack, "Mochila"),
        TabItem(Icons.Filled.PlayCircle, "Jugar"),
        TabItem(Icons.Filled.Person, "Perfil")
    )

    // Backpack scale animation on purchase
    val backpackScale = remember { Animatable(1f) }

    LaunchedEffect(appState.purchasedItemAnimation) {
        if (appState.purchasedItemAnimation != null) {
            backpackScale.animateTo(
                1.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            backpackScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            appState.purchasedItemAnimation = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Content
        when (selectedTab) {
            0 -> StoreScreen(appState = appState)
            1 -> BackpackScreen(appState = appState)
            2 -> HomeScreen(
                appState = appState,
                onPlayPressed = onPlayPressed
            )
            3 -> ProfileScreen(
                appState = appState,
                onSettingsPressed = onSettingsPressed,
                onLogout = onLogout
            )
        }

        // Bottom tab bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), ambientColor = Purple.copy(alpha = 0.25f), spotColor = Purple.copy(alpha = 0.25f))
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedTab = index
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) Purple else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(24.dp)
                            .then(
                                if (index == 1) Modifier.scale(backpackScale.value)
                                else Modifier
                            )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        fontFamily = Quicksand,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Purple else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
