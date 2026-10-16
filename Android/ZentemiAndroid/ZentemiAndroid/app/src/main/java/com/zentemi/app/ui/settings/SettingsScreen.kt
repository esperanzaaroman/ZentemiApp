package com.zentemi.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.theme.*

@Composable
fun SettingsScreen(
    appState: AppState,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var showLanguageSelector by remember { mutableStateOf(false) }
    var showAboutZentemi by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Decorative circle
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 150.dp, y = 260.dp)
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.025f))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Purple,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Ajustes",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp)
            ) {
                // Main settings card
                Surface(
                    shape = RoundedCornerShape(17.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        // Music volume
                        VolumeRow(
                            icon = Icons.Filled.MusicNote,
                            iconColor = Purple,
                            title = "Música",
                            volume = appState.musicVolume,
                            onVolumeChange = { appState.musicVolume = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 55.dp))

                        // Sound volume
                        VolumeRow(
                            icon = Icons.Filled.VolumeUp,
                            iconColor = Purple,
                            title = "Sonido",
                            volume = appState.soundVolume,
                            onVolumeChange = { appState.soundVolume = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 55.dp))

                        // Notifications
                        ToggleRow(
                            icon = Icons.Filled.Notifications,
                            iconColor = Color.Red.copy(alpha = 0.65f),
                            iconBackground = Color.Red.copy(alpha = 0.09f),
                            title = "Notificaciones",
                            isOn = appState.notificationsEnabled,
                            onToggle = { appState.notificationsEnabled = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 55.dp))

                        // Language
                        NavigationRow(
                            icon = Icons.Filled.Language,
                            iconColor = Turquoise,
                            iconBackground = Turquoise.copy(alpha = 0.10f),
                            title = "Idioma de la app",
                            trailingText = appState.selectedLanguage,
                            onClick = { showLanguageSelector = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 55.dp))

                        // About
                        NavigationRow(
                            icon = Icons.Filled.Info,
                            iconColor = Color(0xFFFF9800),
                            iconBackground = Color.Yellow.copy(alpha = 0.14f),
                            title = "Sobre Zentemi",
                            trailingText = null,
                            onClick = { showAboutZentemi = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Yuhmu de Ixtenco card
                Surface(
                    shape = RoundedCornerShape(17.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Yellow.copy(alpha = 0.13f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🌽", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.width(15.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Yuhmu de Ixtenco",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = "Hecho con la comunidad de San Juan Ixtenco, Tlaxcala.",
                                fontSize = 11.sp,
                                color = SubText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Logout and version
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { showLogoutConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Cerrar sesión",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "Versión 1.0",
                    fontSize = 10.sp,
                    color = SubText.copy(alpha = 0.55f)
                )
            }
        }
    }

    // Language selector dialog
    if (showLanguageSelector) {
        AlertDialog(
            onDismissRequest = { showLanguageSelector = false },
            title = { Text("Idioma de la app", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Español", "English", "Yuhmu").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    appState.selectedLanguage = lang
                                    showLanguageSelector = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appState.selectedLanguage == lang,
                                onClick = {
                                    appState.selectedLanguage = lang
                                    showLanguageSelector = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Purple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageSelector = false }) {
                    Text("Cancelar", color = Purple)
                }
            }
        )
    }

    // About Zentemi dialog
    if (showAboutZentemi) {
        AboutZentemiDialog(onDismiss = { showAboutZentemi = false })
    }

    // Logout confirmation
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("¿Cerrar sesión?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tendrás que ingresar nuevamente tu nombre de usuario y PIN para entrar a tu cuenta.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation = false
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun VolumeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DarkText
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${(volume * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubText
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.padding(start = 42.dp),
            colors = SliderDefaults.colors(
                thumbColor = Purple,
                activeTrackColor = Purple
            )
        )
    }
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(13.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DarkText
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = isOn,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Purple
            )
        )
    }
}

@Composable
private fun NavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    trailingText: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(13.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DarkText
        )

        Spacer(modifier = Modifier.weight(1f))

        trailingText?.let {
            Text(
                text = it,
                fontSize = 11.sp,
                color = SubText
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = SubText.copy(alpha = 0.45f),
            modifier = Modifier.size(11.dp)
        )
    }
}

@Composable
private fun AboutZentemiDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🌽", fontSize = 60.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Zentemi",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Aprende y descubre la lengua Yuhmu de San Juan Ixtenco.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Purple,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "¿Qué es Zentemi?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Zentemi es una aplicación educativa creada para apoyar el aprendizaje y la preservación de la lengua Yuhmu de San Juan Ixtenco, Tlaxcala.",
                    fontSize = 14.sp,
                    color = DarkText.copy(alpha = 0.78f),
                    lineHeight = 20.sp
                )
                Text(
                    text = "A través de lecciones, actividades y juegos, Zentemi busca acercar la lengua a nuevas generaciones de una forma sencilla, visual e interactiva.",
                    fontSize = 14.sp,
                    color = DarkText.copy(alpha = 0.78f),
                    lineHeight = 20.sp
                )
                Text(
                    text = "El proyecto reconoce la importancia de la comunidad de San Juan Ixtenco y de las personas que mantienen viva su lengua, conocimientos y cultura.",
                    fontSize = 14.sp,
                    color = DarkText.copy(alpha = 0.78f),
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Purple, fontWeight = FontWeight.Bold)
            }
        }
    )
}
