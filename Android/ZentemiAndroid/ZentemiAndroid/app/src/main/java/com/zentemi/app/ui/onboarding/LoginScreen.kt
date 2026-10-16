package com.zentemi.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.ui.components.MascotGlow
import com.zentemi.app.ui.components.ZentemiBackground
import com.zentemi.app.ui.theme.*

@Composable
fun LoginScreen(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit
) {
    ZentemiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            MascotGlow(glowSize = 280.dp, mascotSize = 180.dp)

            Text(
                text = "¡Bienvenido!",
                fontFamily = Quicksand,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Comencemos tu aventura aprendiendo Yuhmu",
                fontFamily = Quicksand,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                lineHeight = 23.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.weight(0.4f))

            // Crear cuenta
            Button(
                onClick = onCreateAccount,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SplashDotActive
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Crear cuenta",
                    fontFamily = Quicksand,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Iniciar sesión
            OutlinedButton(
                onClick = onSignIn,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Ya tengo una cuenta",
                    fontFamily = Quicksand,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Tu progreso estará asociado a tu cuenta",
                fontFamily = Quicksand,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
