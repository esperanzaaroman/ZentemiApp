package com.zentemi.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*

@Composable
fun NameScreen(
    appState: AppState,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val isValidName = name.trim().length >= 3

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
                    .size(100.dp)
                    .graphicsLayer {
                        scaleY = mascotScaleY
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthTitle(text = "¿Cómo te llamas?", color = DarkText, fontSize = 30)
                Spacer(modifier = Modifier.height(8.dp))
                AuthSubtitle(
                    text = "Maicito quiere saber tu nombre para\nempezar la aventura.",
                    color = SubText
                )
            }

            // Name card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = Purple.copy(alpha = 0.2f), spotColor = Purple.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.97f))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Tu nombre",
                    fontFamily = Quicksand,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubText
                )

                AuthTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Escribe tu nombre",
                    bordered = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isValidName) SuccessGreen.copy(alpha = 0.15f)
                                else Color.Gray.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isValidName) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isValidName) "Se ve muy bien" else "Escribe al menos 3 caracteres",
                        fontFamily = Quicksand,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubText
                    )
                }
            }

            Text(
                text = "Podrás cambiarlo después en tu perfil",
                fontFamily = Quicksand,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SubText.copy(alpha = 0.6f)
            )

            AuthPrimaryButton(
                text = "¡Empezar!",
                onClick = {
                    appState.userName = name.trim()
                    onNext()
                },
                enabled = isValidName,
                onLightBackground = true
            )
        }
    }
}
