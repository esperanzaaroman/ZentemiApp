package com.zentemi.app.ui.main

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import com.zentemi.app.ui.components.pressable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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

                // Píldora de la pestaña activa, icono y texto se animan juntos
                val pillAlpha by animateFloatAsState(if (isSelected) 1f else 0f, tween(220), label = "pill")
                val iconSize by animateDpAsState(
                    if (isSelected) 30.dp else 26.dp,
                    spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                    label = "iconSize"
                )
                val labelColor by animateColorAsState(
                    if (isSelected) PurpleDark else MutedText,
                    tween(220),
                    label = "labelColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .pressable(pressedScale = 0.9f, role = Role.Tab) { selectedTab = index }
                        .semantics { selected = isSelected }
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(width = 60.dp, height = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer { alpha = pillAlpha }
                                .clip(CircleShape)
                                .background(PurpleTint)
                        )
                        ZentemiIcon(
                            icon = tab.icon,
                            contentDescription = tab.label,
                            size = iconSize,
                            muted = !isSelected,
                            modifier = if (index == 0) Modifier.scale(backpackScale.value) else Modifier
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        fontFamily = Quicksand,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = labelColor
                    )
                }
            }
        }
    }
}
