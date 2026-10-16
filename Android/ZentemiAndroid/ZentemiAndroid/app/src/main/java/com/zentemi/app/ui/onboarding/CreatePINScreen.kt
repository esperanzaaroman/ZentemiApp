package com.zentemi.app.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import com.zentemi.app.ui.components.*

@Composable
fun CreatePINScreen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    val isValidPIN = pin.length == 4 && confirmPin.length == 4 && pin == confirmPin

    ZentemiBackground {
        AuthLayout(onBack = onBack) {

            HeaderIcon(icon = Icons.Filled.Lock)

            AuthTitle(text = "Crea tu PIN")

            AuthSubtitle(text = "Usarás este PIN para entrar a tu cuenta.")

            GlassCard {
                AuthTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    placeholder = "PIN de 4 dígitos",
                    pin = true
                )
                AuthTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    placeholder = "Confirma tu PIN",
                    pin = true
                )
                if (confirmPin.isNotEmpty()) {
                    PinMatchRow(matches = isValidPIN)
                }
            }

            AuthPrimaryButton(
                text = "Continuar",
                onClick = onNext,
                enabled = isValidPIN
            )
        }
    }
}
