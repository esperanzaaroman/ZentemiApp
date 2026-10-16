package com.zentemi.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*

@Composable
fun SignInScreen(
    appState: AppState,
    onBack: () -> Unit,
    onSignedIn: () -> Unit,
    onForgotPIN: () -> Unit,
    onForgotUsername: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    val canLogin = userName.trim().isNotEmpty() && pin.length == 4

    ZentemiBackground {
        AuthLayout(onBack = onBack) {

            HeaderIcon(icon = Icons.Filled.AccountCircle, size = 72.dp)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthTitle(text = "Iniciar sesión")
                Spacer(modifier = Modifier.height(8.dp))
                AuthSubtitle(text = "Ingresa a tu aventura en Zentemi")
            }

            GlassCard {
                AuthTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    placeholder = "Nombre de usuario"
                )
                AuthTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    placeholder = "PIN de 4 dígitos",
                    pin = true
                )
            }

            // Recovery links
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = onForgotPIN) {
                    Text(
                        text = "Olvidé mi PIN",
                        fontFamily = Quicksand,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                TextButton(onClick = onForgotUsername) {
                    Text(
                        text = "Olvidé mi nombre de usuario",
                        fontFamily = Quicksand,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            AuthPrimaryButton(
                text = "Iniciar sesión",
                onClick = {
                    appState.userName = userName.trim()
                    onSignedIn()
                },
                enabled = canLogin
            )
        }
    }
}
