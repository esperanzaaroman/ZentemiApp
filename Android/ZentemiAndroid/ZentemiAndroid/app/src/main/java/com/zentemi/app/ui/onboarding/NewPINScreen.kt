package com.zentemi.app.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.runtime.*
import com.zentemi.app.ui.components.*

@Composable
fun NewPINScreen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var newPIN by remember { mutableStateOf("") }
    var confirmPIN by remember { mutableStateOf("") }

    val isValidPIN = newPIN.length == 4 && confirmPIN.length == 4 && newPIN == confirmPIN

    ZentemiBackground {
        AuthLayout(onBack = onBack) {

            HeaderIcon(icon = Icons.Filled.LockReset)

            AuthTitle(text = "Crea un nuevo PIN", fontSize = 30)

            AuthSubtitle(text = "Elige un nuevo PIN de 4 dígitos.")

            GlassCard {
                AuthTextField(
                    value = newPIN,
                    onValueChange = { newPIN = it },
                    placeholder = "Nuevo PIN",
                    pin = true
                )
                AuthTextField(
                    value = confirmPIN,
                    onValueChange = { confirmPIN = it },
                    placeholder = "Confirma tu nuevo PIN",
                    pin = true
                )
                if (confirmPIN.isNotEmpty()) {
                    PinMatchRow(matches = isValidPIN)
                }
            }

            AuthPrimaryButton(
                text = "Guardar nuevo PIN",
                onClick = onNext,
                enabled = isValidPIN
            )
        }
    }
}
