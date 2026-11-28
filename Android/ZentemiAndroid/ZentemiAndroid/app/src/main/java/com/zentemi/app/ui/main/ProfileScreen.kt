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
import androidx.compose.ui.draw.shadow
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
import androidx.compose.material.icons.automirrored.filled.Logout
import com.zentemi.app.data.AuthService
import com.zentemi.app.ui.components.MaicitoBackdrop
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.components.WaveHeader
import com.zentemi.app.ui.components.ScreenTitle
import com.zentemi.app.state.PET_NAME_MAX
import com.zentemi.app.ui.components.avatarBackgroundColor
import com.zentemi.app.ui.components.profileHeaderColors
import com.zentemi.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appState: AppState,
    onSettingsPressed: () -> Unit,
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onVerifyEmail: () -> Unit,
    onLogout: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showLevelList by remember { mutableStateOf(false) }



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
                    ScreenTitle(
                        title = "Perfil",
                        subtitle = "Tu avance en Yuhmu",
                        onColor = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSettingsPressed,
                        // Círculo blanco sólido: el ícono morado ya no se pierde en el encabezado
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(6.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.25f))
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        ZentemiIcon(R.drawable.ic_ajustes, size = 30.dp, contentDescription = "Ajustes")
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

        // Cuenta: invitar a guardar el progreso, o mostrar la cuenta con sesión
        val email = appState.accountEmail
        if (appState.isGuest || email == null) {
            SaveProgressCard(onCreateAccount = onCreateAccount, onSignIn = onSignIn)
        } else {
            AccountCard(
                email = email,
                onVerifyEmail = onVerifyEmail,
                onLogout = { showLogoutConfirmation = true }
            )
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

        // Logros (iguales a iOS): juegos, lecciones, comidas, monedas ganadas y rachas
        ProfileAchievementsSection(appState)

        // Espacio para la barra de pestañas (y la barra de gestos del sistema) + aire
        Spacer(
            modifier = Modifier
                .navigationBarsPadding()
                .height(108.dp)
        )
    }

    // Editor bottom sheet
    if (showEditor) {
        AvatarEditorDialog(
            appState = appState,
            onDismiss = { showEditor = false }
        )
    }

    if (showLogoutConfirmation) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutConfirmation = false
                onLogout()
            },
            onDismiss = { showLogoutConfirmation = false }
        )
    }
}

/** Modo invitado: invita a guardar el progreso en la nube (opcional). */
@Composable
private fun SaveProgressCard(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CloudUpload,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Guardar mi progreso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ahora tu progreso está guardado solo en este teléfono. " +
                    "Pídele a un adulto que cree una cuenta para guardarlo en la nube.",
                fontSize = 13.sp,
                color = SubText,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onCreateAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Crear cuenta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onSignIn,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Iniciar sesión", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Purple)
                }
            }
        }
    }
}

/** Con sesión: correo del tutor, aviso de verificación y cerrar sesión. */
@Composable
private fun AccountCard(
    email: String,
    onVerifyEmail: () -> Unit,
    onLogout: () -> Unit
) {
    val isEmailVerified by AuthService.isEmailVerified.collectAsState()

    // Si confirmó el enlace con la app cerrada, se actualiza al ver el perfil.
    LaunchedEffect(Unit) {
        if (!AuthService.isEmailVerified.value) {
            runCatching { AuthService.reloadAndCheckVerification() }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CloudDone,
                    contentDescription = null,
                    tint = Turquoise,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progreso guardado en la nube",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = email,
                        fontSize = 13.sp,
                        color = SubText,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isEmailVerified) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Correo verificado", fontSize = 13.sp, color = SubText)
                }
            } else {
                // Aviso, no bloqueo: la app se puede usar sin verificar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CoinGold.copy(alpha = 0.15f))
                        .clickable(onClick = onVerifyEmail)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.MarkEmailUnread,
                        contentDescription = null,
                        tint = CoinGoldDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Falta confirmar el correo del adulto. Toca aquí para verificarlo.",
                        fontSize = 13.sp,
                        color = DarkText,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ErrorRed)
            }
        }
    }
}

/** Misma confirmación en Perfil y Ajustes: el progreso no se borra del teléfono. */
@Composable
fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cerrar sesión?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Tu progreso se queda en este teléfono y puedes seguir jugando. " +
                    "Para volver a guardarlo en la nube, inicia sesión otra vez."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cerrar sesión", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
                    onValueChange = { petNameInput = it.take(PET_NAME_MAX) },
                    label = { Text("Nombre de mascota") },
                    supportingText = { Text("${petNameInput.length} / $PET_NAME_MAX") },
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
                appState.petName = petNameInput.trim().take(PET_NAME_MAX).ifEmpty { "Maicito" }
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
