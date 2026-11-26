package com.zentemi.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.data.AuthService
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Registro familiar (equivalente a NameView.swift en iOS):
 * nombre del menor + correo y contraseña del tutor + consentimiento de privacidad.
 *
 * Al crearse la cuenta, la puerta de autenticación de la navegación
 * lleva automáticamente a la verificación de correo.
 */
@Composable
fun NameScreen(
    appState: AppState,
    onBack: () -> Unit
) {
    var childName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedPrivacyConsent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val isValidChildName = childName.trim().length >= 3
    val cleanEmail = email.trim()
    val isValidEmail = cleanEmail.contains("@") && cleanEmail.contains(".") && cleanEmail.length >= 6
    val isValidPassword = password.length >= 6 && password == confirmPassword
    val canCreateAccount = isValidChildName && isValidEmail && isValidPassword &&
        acceptedPrivacyConsent && !isLoading

    fun executeRegister() {
        if (!canCreateAccount) return
        isLoading = true
        errorMessage = null
        // El nombre se fija antes para que el perfil del menor se cree con él
        appState.userName = childName.trim()
        scope.launch {
            try {
                AuthService.registerParent(
                    email = email,
                    password = password,
                    acceptedPrivacyConsent = acceptedPrivacyConsent
                )
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Mascot "breathing" (stretches up from its feet)
    val anim = rememberInfiniteTransition(label = "mascot")
    val mascotScaleY by anim.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotScaleY"
    )

    LightAuthBackground {
        AuthLayout(onBack = onBack, lightBackground = true) {

            Image(
                painter = painterResource(id = R.drawable.mascota),
                contentDescription = "Maicito",
                modifier = Modifier
                    .size(90.dp)
                    .graphicsLayer {
                        scaleY = mascotScaleY
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthTitle(text = "Registro Familiar", color = DarkText, fontSize = 30)
                Spacer(modifier = Modifier.height(8.dp))
                AuthSubtitle(
                    text = "Supervisado por un adulto responsable",
                    color = SubText
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = Purple.copy(alpha = 0.2f), spotColor = Purple.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.97f))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FieldLabel("Nombre del menor / aprendiz")
                AuthTextField(
                    value = childName,
                    onValueChange = { childName = it },
                    placeholder = "¿Cómo te llamas?",
                    bordered = true
                )

                FieldLabel("Correo del tutor legal")
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "tutor@ejemplo.com",
                    bordered = true,
                    email = true
                )

                FieldLabel("Contraseña del tutor (mínimo 6 caracteres)")
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Contraseña",
                    bordered = true,
                    password = true
                )

                FieldLabel("Confirmar contraseña")
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Repite la contraseña",
                    bordered = true,
                    password = true
                )

                if (confirmPassword.isNotEmpty()) {
                    PinMatchRow(
                        matches = password == confirmPassword,
                        textColor = SubText,
                        matchText = "Las contraseñas coinciden",
                        mismatchText = "Las contraseñas no coinciden"
                    )
                }

                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = acceptedPrivacyConsent,
                        onCheckedChange = { acceptedPrivacyConsent = it },
                        colors = CheckboxDefaults.colors(checkedColor = Purple)
                    )
                    Text(
                        text = "Soy mayor de edad y autorizo como tutor el tratamiento de datos " +
                            "para el aprendizaje del menor en Zentemi (cumplimiento LFPDPPP y LGDNNA).",
                        fontFamily = Quicksand,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubText,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            errorMessage?.let { AuthMessage(text = it) }

            if (isLoading) {
                CircularProgressIndicator(color = Purple)
            }

            AuthPrimaryButton(
                text = if (isLoading) "Creando cuenta..." else "Crear cuenta",
                onClick = { executeRegister() },
                enabled = canCreateAccount,
                onLightBackground = true
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = Quicksand,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = SubText
    )
}
