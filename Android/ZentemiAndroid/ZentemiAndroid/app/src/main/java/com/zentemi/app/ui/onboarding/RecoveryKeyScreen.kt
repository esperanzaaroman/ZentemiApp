package com.zentemi.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*

@Composable
fun RecoveryKeyScreen(
    recoveryType: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var word by remember { mutableStateOf("") }
    val canContinue = word.trim().isNotEmpty()

    val title = if (recoveryType == "pin") "Recuperar PIN" else "Recuperar nombre de usuario"

    LightAuthBackground {
        AuthLayout {

            HeaderIcon(icon = Icons.Filled.Key, tint = Purple)

            AuthTitle(text = title, color = DarkText, fontSize = 28)

            AuthSubtitle(
                text = "Escribe la palabra clave que recibiste cuando creaste tu cuenta.",
                color = SubText
            )

            Box(modifier = Modifier.padding(horizontal = 30.dp)) {
                AuthTextField(
                    value = word,
                    onValueChange = { word = it },
                    placeholder = "Palabra clave",
                    bordered = true
                )
            }

            AuthPrimaryButton(
                text = "Continuar",
                onClick = onNext,
                enabled = canContinue,
                onLightBackground = true
            )

            TextButton(onClick = onBack) {
                Text(
                    text = "Cancelar",
                    fontFamily = Quicksand,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubText
                )
            }
        }
    }
}
