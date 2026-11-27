package com.zentemi.app.ui.onboarding

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.R
import com.zentemi.app.data.AuthService
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Cuenta del tutor para guardar el progreso en la nube (opcional, modo invitado).
 * El nombre del menor ya se escribió en el onboarding; aquí solo van el correo,
 * la contraseña y el consentimiento. Al crearse, la navegación regresa sola a la app
 * y AppState sube el progreso del teléfono al perfil nuevo.
 */
@Composable
fun CreateAccountScreen(
    childName: String,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedPrivacyConsent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val cleanEmail = email.trim()
    val isValidEmail = cleanEmail.contains("@") && cleanEmail.contains(".") && cleanEmail.length >= 6
    val isValidPassword = password.length >= 6 && password == confirmPassword
    val canCreateAccount = isValidEmail && isValidPassword && acceptedPrivacyConsent && !isLoading

    fun executeRegister() {
        if (!canCreateAccount) return
        isLoading = true
        errorMessage = null
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

    LightAuthBackground {
        AuthLayout(onBack = onBack, lightBackground = true) {

            Image(
                painter = painterResource(id = R.drawable.mascota),
                contentDescription = "Maicito",
                modifier = Modifier.size(90.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthTitle(text = "Guardar mi progreso", color = DarkText, fontSize = 28)
                Spacer(modifier = Modifier.height(8.dp))
                AuthSubtitle(
                    text = "Cuenta de un adulto para el progreso de $childName",
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
