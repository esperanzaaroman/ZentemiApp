package com.zentemi.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.res.painterResource
import com.zentemi.app.R
import com.zentemi.app.ui.components.BackCircleButton
import com.zentemi.app.ui.components.ChunkyButton
import com.zentemi.app.ui.components.HeaderTitle
import com.zentemi.app.ui.components.WaveHeader
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with waves
            WaveHeader {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BackCircleButton(onClick = onBack, lightBackground = false)
                    Spacer(modifier = Modifier.width(14.dp))
                    HeaderTitle(title = "Ajustes", subtitle = "Personaliza tu experiencia")
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp)
            ) {
                // Main settings card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
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

                        HorizontalDivider(modifier = Modifier.padding(start = 61.dp), color = LockedNode.copy(alpha = 0.6f))

                        // Sound volume
                        VolumeRow(
                            icon = Icons.Filled.VolumeUp,
                            iconColor = Purple,
                            title = "Sonido",
                            volume = appState.soundVolume,
                            onVolumeChange = { appState.soundVolume = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 61.dp), color = LockedNode.copy(alpha = 0.6f))

                        // Notifications
                        ToggleRow(
                            icon = Icons.Filled.Notifications,
                            iconColor = Color.Red.copy(alpha = 0.65f),
                            iconBackground = Color.Red.copy(alpha = 0.09f),
                            title = "Notificaciones",
                            isOn = appState.notificationsEnabled,
                            onToggle = { appState.notificationsEnabled = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 61.dp), color = LockedNode.copy(alpha = 0.6f))

                        // Language
                        NavigationRow(
                            icon = Icons.Filled.Language,
                            iconColor = Turquoise,
                            iconBackground = Turquoise.copy(alpha = 0.10f),
                            title = "Idioma de la app",
                            trailingText = appState.selectedLanguage,
                            onClick = { showLanguageSelector = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 61.dp), color = LockedNode.copy(alpha = 0.6f))

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
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SplashDotActive.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.mascota),
                                contentDescription = null,
                                modifier = Modifier.size(46.dp)
                            )
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
                ChunkyButton(
                    text = "Cerrar sesión",
                    onClick = { showLogoutConfirmation = true },
                    color = ErrorRed,
                    lipColor = Color(0xFFD94848),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp),
                    leading = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "Versión 1.0",
                    fontSize = 12.sp,
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
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
            modifier = Modifier.padding(start = 48.dp),
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
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
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AboutZentemiDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.mascota),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp)
                )

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
