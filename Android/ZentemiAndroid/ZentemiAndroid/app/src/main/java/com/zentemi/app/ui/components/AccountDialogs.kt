package com.zentemi.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.zentemi.app.ui.theme.Purple
import com.zentemi.app.ui.theme.Quicksand
import com.zentemi.app.ui.theme.SubText

/** Sugerencia periódica (cada 7 días, con internet) de guardar el progreso en la nube. Mismos textos que iOS. */
@Composable
fun SuggestAccountDialog(
    petName: String,
    onCreateAccount: () -> Unit,
    onNotNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onNotNow,
        title = {
            Text("¿Quieres guardar tu progreso en la nube?", fontFamily = Quicksand, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Pídele a un adulto que cree una cuenta para no perder tu progreso con $petName si cambias de teléfono.",
                fontFamily = Quicksand
            )
        },
        confirmButton = {
            TextButton(onClick = onCreateAccount) {
                Text("Crear cuenta", fontFamily = Quicksand, fontWeight = FontWeight.Bold, color = Purple)
            }
        },
        dismissButton = {
            TextButton(onClick = onNotNow) {
                Text("Ahora no", fontFamily = Quicksand, color = SubText)
            }
        }
    )
}

/** Al iniciar sesión con progreso de invitado en el teléfono: elegir cuál conservar. */
@Composable
fun ChooseProgressDialog(
    onUsePhone: () -> Unit,
    onUseAccount: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("¿Qué progreso quieres usar?", fontFamily = Quicksand, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Tienes progreso guardado en este teléfono y también en tu cuenta de Zentemi. " +
                    "Elige cuál quieres conservar.",
                fontFamily = Quicksand
            )
        },
        confirmButton = {
            TextButton(onClick = onUsePhone) {
                Text("Usar el progreso de este teléfono", fontFamily = Quicksand, fontWeight = FontWeight.Bold, color = Purple)
            }
        },
        dismissButton = {
            TextButton(onClick = onUseAccount) {
                Text("Usar el progreso de mi cuenta", fontFamily = Quicksand, color = Purple)
            }
        }
    )
}
