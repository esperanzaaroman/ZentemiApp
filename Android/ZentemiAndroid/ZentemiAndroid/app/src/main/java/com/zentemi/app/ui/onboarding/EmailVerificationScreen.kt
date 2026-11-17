package com.zentemi.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.AuthService
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Verificación del correo del tutor (equivalente a EmailVerificationView.swift).
 * Cuando el correo queda verificado, la navegación entra sola a la app.
 */
@Composable
fun EmailVerificationScreen() {
    var isChecking by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val currentUser by AuthService.currentUser.collectAsState()

    fun checkVerificationStatus() {
        isChecking = true
        statusMessage = null
        scope.launch {
            try {
                val verified = AuthService.reloadAndCheckVerification()
                isSuccessMessage = verified
                statusMessage = if (verified) {
                    "¡Correo verificado con éxito! Entrando..."
                } else {
                    "Aún no se ha verificado. Haz clic en el enlace que enviamos a tu correo."
                }
            } catch (e: Exception) {
                isSuccessMessage = false
                statusMessage = "Error al comprobar: ${e.message}"
            } finally {
                isChecking = false
            }
        }
    }

    fun resendEmail() {
        isResending = true
        statusMessage = null
        scope.launch {
            try {
                AuthService.sendEmailVerification()
                isSuccessMessage = true
                statusMessage = "¡Nuevo enlace enviado! Revisa tu bandeja de entrada o spam."
            } catch (e: Exception) {
                isSuccessMessage = false
                statusMessage = "Error al reenviar: ${e.message}"
            } finally {
                isResending = false
            }
        }
    }

    // Comprobación pasiva al entrar a la pantalla
    LaunchedEffect(Unit) { checkVerificationStatus() }

    ZentemiBackground {
        AuthLayout {

            HeaderIcon(icon = Icons.Filled.MarkEmailUnread, size = 64.dp)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthTitle(text = "Confirma tu correo")
                Spacer(modifier = Modifier.height(8.dp))
                AuthSubtitle(text = "Validación del Tutor Legal")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthSubtitle(text = "Enviamos un enlace de verificación a:")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentUser?.email ?: "",
                    fontFamily = Quicksand,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            GlassCard {
                Text(
                    text = "Cumplimiento LFPDPPP y LGDNNA",
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Por protección de datos de los menores, el padre o tutor debe validar " +
                        "su identidad antes de que el alumno comience sus lecciones de Yuhmu.",
                    fontFamily = Quicksand,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            statusMessage?.let { AuthMessage(text = it, success = isSuccessMessage) }

            AuthPrimaryButton(
                text = if (isChecking) "Comprobando..." else "Ya confirmé mi correo",
                onClick = { checkVerificationStatus() },
                enabled = !isChecking
            )

            TextButton(onClick = { resendEmail() }, enabled = !isResending) {
                Text(
                    text = if (isResending) "Reenviando..." else "Reenviar enlace de confirmación",
                    fontFamily = Quicksand,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            TextButton(onClick = { AuthService.signOut() }) {
                Text(
                    text = "Iniciar con otra cuenta",
                    fontFamily = Quicksand,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
