package com.zentemi.app.ui.main

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import com.zentemi.app.R
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.zentemi.app.data.ShopCatalog
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.zentemi.app.ui.components.MaicitoBackdrop
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.components.WaveHeader
import com.zentemi.app.ui.components.avatarBackgroundColor
import com.zentemi.app.ui.components.profileHeaderColors
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

    // Artículos de la tienda comprados (sin contar los que vienen de regalo)
    val boughtItems = ShopCatalog.items.count { it.price > 0 && appState.isOwned(it) }

    val achievements = listOf(
        Triple(R.drawable.ic_primera_leccion, "Primera lección", appState.pathLessonsCompleted >= 1),
        Triple(R.drawable.ic_logro_racha, "Racha de 3 días", appState.streak >= 3),
        Triple(R.drawable.ic_ahorrador, "Ahorrador: 500 monedas", appState.coins >= 500),
        Triple(R.drawable.ic_nivel_5, "Nivel 5", appState.currentLevel >= 5),
        Triple(R.drawable.ic_maestro_zentemi, "Maestro Zentemi", appState.currentLevel >= 10),
        Triple(R.drawable.ic_racha, "Racha de 7 días", appState.streak >= 7),
        Triple(R.drawable.ic_comenzar_leccion, "10 lecciones", appState.lessonsCompleted >= 10),
        Triple(R.drawable.ic_tienda, "Primera compra", boughtItems >= 1),
        Triple(R.drawable.ic_mochila, "Coleccionista: 10 objetos", boughtItems >= 10),
        Triple(R.drawable.ic_felicidad, "${appState.petName} muy feliz", appState.happiness >= 100),
        Triple(R.drawable.ic_regalo, "Ahorrador: 2000 monedas", appState.coins >= 2000),
        Triple(R.drawable.ic_estrella, "Nivel 3", appState.currentLevel >= 3),
    )
    val unlockedCount = achievements.count { it.third }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado con el color que eligió el usuario
        WaveHeader(colors = profileHeaderColors(appState.profileBackgroundHue)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title + settings button
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Perfil",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onSettingsPressed,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                    ) {
                        ZentemiIcon(R.drawable.ic_ajustes, size = 28.dp, contentDescription = "Ajustes")
                    }
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(avatarBackgroundColor(appState.avatarBackgroundHue))
                        .border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        .clickable { showEditor = true },
                    contentAlignment = Alignment.Center
                ) {
                    appState.equippedBackdrop?.backdrop?.let { backdrop ->
                        MaicitoBackdrop(backdrop = backdrop, modifier = Modifier.fillMaxSize())
                    }
                    PetView(appState = appState, size = 80.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = appState.userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Level badge
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SplashDotActive)
                        .clickable { showLevelList = !showLevelList }
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZentemiIcon(R.drawable.ic_estrella, size = 18.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Nv. ${appState.currentLevel} · ${appState.levelName(appState.currentLevel)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoinGoldDark
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
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp
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
                                // Marca grande de "aquí vas"
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Purple)
                                        .padding(start = 6.dp, end = 12.dp, top = 3.dp, bottom = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tú",
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
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
                icon = R.drawable.ic_racha,
                value = "${appState.streak}",
                label = "Racha",
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = R.drawable.ic_primera_leccion,
                value = "${appState.lessonsCompleted}",
                label = "Lecciones",
                color = Turquoise,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = R.drawable.ic_moneda,
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
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 3.dp
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
                    trackColor = Purple.copy(alpha = 0.1f),
                    gapSize = 0.dp,
                    drawStopIndicator = {}
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

        // Logros: galería hacia abajo (3 por fila)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logros",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$unlockedCount de ${achievements.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Purple,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            achievements.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (icon, title, unlocked) ->
                        AchievementCard(icon, title, unlocked, Modifier.weight(1f))
                    }
                    // Rellena la última fila para que las tarjetas no se estiren
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
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
private fun AchievementCard(
    @DrawableRes icon: Int,
    title: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = modifier.height(132.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) SplashDotActive.copy(alpha = 0.2f) else LockedNode.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                ZentemiIcon(icon, size = 40.dp, muted = !unlocked)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (unlocked) DarkText else SubText,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (unlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = if (unlocked) "Desbloqueado" else "Bloqueado",
                tint = if (unlocked) Turquoise else Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun StatCard(
    @DrawableRes icon: Int,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZentemiIcon(icon, size = 30.dp)
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

    // Los colores se aplican en vivo (como en iOS); "Cancelar" los regresa.
    val originalProfileHue = remember { appState.profileBackgroundHue }
    val originalAvatarHue = remember { appState.avatarBackgroundHue }
    val originalMascotHue = remember { appState.mascotHue }

    fun cancel() {
        appState.profileBackgroundHue = originalProfileHue
        appState.avatarBackgroundHue = originalAvatarHue
        appState.mascotHue = originalMascotHue
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { cancel() },
        containerColor = Color.White,
        title = {
            Text(
                "Personalizar",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Vista previa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(profileHeaderColors(appState.profileBackgroundHue))),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(avatarBackgroundColor(appState.avatarBackgroundHue))
                            .border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        appState.equippedBackdrop?.backdrop?.let { backdrop ->
                            MaicitoBackdrop(backdrop = backdrop, modifier = Modifier.fillMaxSize())
                        }
                        PetView(appState = appState, size = 74.dp)
                    }
                }

                HueSlider(
                    label = "Color del fondo del perfil",
                    value = appState.profileBackgroundHue,
                    swatch = profileHeaderColors(appState.profileBackgroundHue).first(),
                    onChange = { appState.profileBackgroundHue = it }
                )
                HueSlider(
                    label = "Color del fondo de la mascota",
                    value = appState.avatarBackgroundHue,
                    swatch = avatarBackgroundColor(appState.avatarBackgroundHue),
                    onChange = { appState.avatarBackgroundHue = it }
                )
                HueSlider(
                    label = "Color de ${appState.petName}",
                    value = appState.mascotHue,
                    // Maicito es amarillo (tono ~45°); el control gira ese tono
                    swatch = Color.hsv((45f + appState.mascotHue * 360f) % 360f, 0.75f, 0.98f),
                    onChange = { appState.mascotHue = it }
                )
                if (appState.equippedBackdrop != null) {
                    Text(
                        text = "Tienes un fondo de la tienda puesto: se ve encima del color de fondo de la mascota.",
                        fontSize = 11.sp,
                        color = SubText
                    )
                }

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
                val newName = userNameInput.trim()
                if (appState.canChangeUserName && newName.isNotEmpty() && newName != appState.userName) {
                    appState.userName = newName
                    appState.lastUserNameChange = System.currentTimeMillis()
                }
                onDismiss()
            }) {
                Text("Guardar", color = Purple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { cancel() }) {
                Text("Cancelar")
            }
        }
    )
}

/** Control de tono (0 a 1) con una muestra del color actual, como los Slider de iOS. */
@Composable
private fun HueSlider(
    label: String,
    value: Float,
    swatch: Color,
    onChange: (Float) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(swatch)
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = swatch,
                activeTrackColor = Purple,
                inactiveTrackColor = Purple.copy(alpha = 0.15f)
            )
        )
    }
}
