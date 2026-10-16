package com.zentemi.app.ui.main

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appState: AppState,
    onSettingsPressed: () -> Unit,
    onLogout: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var showLevelList by remember { mutableStateOf(false) }

    val achievements = listOf(
        Triple("🎓", "Primera lección", appState.pathLessonsCompleted >= 1),
        Triple("🔥", "Racha de 3 días", appState.streak >= 3),
        Triple("💰", "Ahorrador: 500 monedas", appState.coins >= 500),
        Triple("⭐", "Nivel 5", appState.currentLevel >= 5),
        Triple("🏆", "Maestro Zentemi", appState.currentLevel >= 10),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PurpleGradientStart, PurpleGradientEnd)
                    )
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(x = (-80).dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = 200.dp, y = 100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Settings button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(1.dp))
                    IconButton(
                        onClick = onSettingsPressed,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Ajustes",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { showEditor = true },
                    contentAlignment = Alignment.Center
                ) {
                    PetView(appState = appState, size = 65.dp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = appState.userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Level badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LevelColors.getOrElse(appState.currentLevel - 1) { Purple },
                    modifier = Modifier.clickable { showLevelList = !showLevelList }
                ) {
                    Text(
                        text = "Nv. ${appState.currentLevel} — ${appState.levelName(appState.currentLevel)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Expandable level list
        if (showLevelList) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Niveles",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    appState.levelNames.forEachIndexed { index, name ->
                        val level = index + 1
                        val isReached = appState.currentLevel >= level
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isReached) LevelColors.getOrElse(index) { Purple }
                                        else Color.Gray.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$level",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReached) Color.White else SubText
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = name,
                                fontSize = 13.sp,
                                fontWeight = if (appState.currentLevel == level) FontWeight.Bold else FontWeight.Normal,
                                color = if (isReached) DarkText else SubText
                            )
                            if (appState.currentLevel == level) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "← Tú",
                                    fontSize = 11.sp,
                                    color = Purple,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Filled.LocalFireDepartment,
                value = "${appState.streak}",
                label = "Racha",
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.School,
                value = "${appState.lessonsCompleted}",
                label = "Lecciones",
                color = Turquoise,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.MonetizationOn,
                value = "${appState.coins}",
                label = "Monedas",
                color = CoinGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Progreso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nv. ${appState.currentLevel}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    Text(
                        text = "${appState.currentLevelXP} / ${appState.xpRequiredPerLevel} XP",
                        fontSize = 12.sp,
                        color = SubText
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { appState.levelProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Purple,
                    trackColor = Purple.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Faltan ${appState.xpRemaining} XP para el siguiente nivel",
                    fontSize = 11.sp,
                    color = SubText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Achievements section
        Text(
            text = "Logros",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(achievements) { _, (emoji, title, unlocked) ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.width(120.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 30.sp,
                            modifier = Modifier.then(
                                if (!unlocked) Modifier else Modifier
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (unlocked) DarkText else SubText,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                        if (unlocked) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Turquoise,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // bottom tab bar space
    }

    // Editor bottom sheet
    if (showEditor) {
        AvatarEditorDialog(
            appState = appState,
            onDismiss = { showEditor = false }
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = SubText
            )
        }
    }
}

@Composable
private fun AvatarEditorDialog(
    appState: AppState,
    onDismiss: () -> Unit
) {
    var petNameInput by remember { mutableStateOf(appState.petName) }
    var userNameInput by remember { mutableStateOf(appState.userName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Personalizar",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Pet name
                OutlinedTextField(
                    value = petNameInput,
                    onValueChange = { petNameInput = it },
                    label = { Text("Nombre de mascota") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // User name
                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = { userNameInput = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = appState.canChangeUserName
                )

                if (!appState.canChangeUserName) {
                    Text(
                        text = "Puedes cambiar tu nombre cada 7 días",
                        fontSize = 11.sp,
                        color = SubText
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                appState.petName = petNameInput.trim().ifEmpty { "Maicito" }
                if (appState.canChangeUserName && userNameInput.trim().isNotEmpty()) {
                    appState.userName = userNameInput.trim()
                    appState.lastUserNameChange = System.currentTimeMillis()
                }
                onDismiss()
            }) {
                Text("Guardar", color = Purple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
