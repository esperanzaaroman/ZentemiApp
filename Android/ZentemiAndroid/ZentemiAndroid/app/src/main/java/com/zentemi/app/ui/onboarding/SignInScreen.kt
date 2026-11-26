package com.zentemi.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.AuthService
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Inicio de sesión del tutor con correo y contraseña (equivalente a SingInView.swift).
 * Incluye el aviso de bloqueo por intentos fallidos y la recuperación de contraseña.
 *
 * Al autenticarse, la puerta de autenticación de la navegación lleva a la app.
 */
@Composable
fun SignInScreen(
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPassword by remember { mutableStateOf(false) }

    val isLockedOut by AuthService.isLockedOut.collectAsState()
    val lockoutRemainingSeconds by AuthService.lockoutRemainingSeconds.collectAsState()

    val scope = rememberCoroutineScope()

    val canLogin = email.trim().isNotEmpty() && password.length >= 6 && !isLockedOut && !isLoading

    fun executeLogin() {
        if (!canLogin) return
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                AuthService.signInParent(email = email, password = password)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    ZentemiBackground {
        AuthLayout(onBack = onBack) {

            HeaderIcon(icon = Icons.Filled.AccountCircle, size = 72.dp)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthTitle(text = "Iniciar sesión")
                Spacer(modifier = Modifier.height(8.dp))
                AuthSubtitle(text = "Cuenta de tutor / adulto responsable")
            }

            if (isLockedOut) {
                val minutes = maxOf(1, ceil(lockoutRemainingSeconds / 60.0).toInt())
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 25.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Acceso bloqueado por seguridad",
                            fontFamily = Quicksand,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Podrás reintentar en $minutes minuto(s).",
                            fontFamily = Quicksand,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            GlassCard {
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Correo del tutor",
                    email = true
                )
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Contraseña",
                    password = true
                )
            }

            errorMessage?.let { AuthMessage(text = it) }

            TextButton(onClick = { showForgotPassword = true }) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontFamily = Quicksand,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            }

            AuthPrimaryButton(
                text = if (isLoading) "Entrando..." else "Iniciar sesión",
                onClick = { executeLogin() },
                enabled = canLogin
            )
        }
    }

    if (showForgotPassword) {
        ForgotPasswordDialog(
            initialEmail = email,
            onDismiss = { showForgotPassword = false }
        )
    }
}

@Composable
private fun ForgotPasswordDialog(initialEmail: String, onDismiss: () -> Unit) {
    var resetEmail by remember { mutableStateOf(initialEmail) }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var resetSuccess by remember { mutableStateOf(false) }
    var isResetting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Ingresa el correo electrónico del tutor registrado. " +
                        "Te enviaremos un enlace seguro para restablecerla."
                )
                AuthTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    placeholder = "Correo electrónico",
                    bordered = true,
                    email = true
                )
                resetMessage?.let {
                    Text(it, color = if (resetSuccess) SuccessGreen else ErrorRed, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isResetting && resetEmail.isNotBlank(),
                onClick = {
                    isResetting = true
                    resetMessage = null
                    scope.launch {
                        try {
                            AuthService.sendPasswordReset(resetEmail)
                            resetSuccess = true
                            resetMessage = "¡Enlace enviado con éxito! Revisa tu bandeja de entrada."
                        } catch (e: Exception) {
                            resetSuccess = false
                            resetMessage = "Error: ${e.message}"
                        } finally {
                            isResetting = false
                        }
                    }
                }
            ) {
                Text(if (isResetting) "Enviando..." else "Enviar enlace", color = Purple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
