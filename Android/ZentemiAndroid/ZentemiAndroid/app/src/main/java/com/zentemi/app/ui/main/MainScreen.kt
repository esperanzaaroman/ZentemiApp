package com.zentemi.app.ui.main

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import com.zentemi.app.R
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
    @DrawableRes val icon: Int,
    val label: String
)

@Composable
fun MainScreen(
    appState: AppState,
    onPlayPressed: () -> Unit,
    onFeedPressed: () -> Unit,
    onPlayWithPressed: () -> Unit,
    onSettingsPressed: () -> Unit,
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onVerifyEmail: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Inicio es la pestaña por defecto

    val tabs = listOf(
        TabItem(R.drawable.ic_tienda, "Tienda"),
        TabItem(R.drawable.ic_inicio, "Inicio"),
        TabItem(R.drawable.ic_perfil, "Perfil")
    )

    // Al comprar, el ícono de la tienda rebota
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
            1 -> HomeScreen(
                appState = appState,
                onPlayPressed = onPlayPressed,
                onFeedPressed = onFeedPressed,
                onPlayWithPressed = onPlayWithPressed
            )
            2 -> ProfileScreen(
                appState = appState,
                onSettingsPressed = onSettingsPressed,
                onCreateAccount = onCreateAccount,
                onSignIn = onSignIn,
                onVerifyEmail = onVerifyEmail,
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
                    ZentemiIcon(
                        icon = tab.icon,
                        contentDescription = tab.label,
                        // La pestaña activa a color y un poco más grande; las demás apagadas
                        size = if (isSelected) 32.dp else 28.dp,
                        muted = !isSelected,
                        modifier = Modifier
                            .then(
                                if (index == 0) Modifier.scale(backpackScale.value)
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
