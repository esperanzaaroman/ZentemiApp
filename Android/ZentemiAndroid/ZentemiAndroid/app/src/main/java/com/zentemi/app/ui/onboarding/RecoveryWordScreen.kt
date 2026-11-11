package com.zentemi.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.ui.components.*
import com.zentemi.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RecoveryWordScreen(onContinue: () -> Unit) {

    val recoveryWord = "AGUA"

    var secondsRemaining by remember { mutableIntStateOf(20) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    ZentemiBackground {
        AuthLayout {

            HeaderIcon(icon = Icons.Filled.Key)

            AuthTitle(text = "Guarda tu palabra clave", fontSize = 30)

            // White card with the word and the countdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tu palabra clave es:",
                    fontFamily = Quicksand,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SubText
                )

                Text(
                    text = recoveryWord,
                    fontFamily = Quicksand,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple,
                    letterSpacing = 4.sp
                )

                Text(
                    text = "Anota esta palabra en tu libreta o pídele a un tutor que la guarde. La necesitarás si algún día olvidas tu PIN o tu nombre de usuario.",
                    fontFamily = Quicksand,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = SubText,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                HorizontalDivider(color = LockedNode)

                if (secondsRemaining > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Podrás continuar en:",
                            fontFamily = Quicksand,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubText
                        )
                        Text(
                            text = "$secondsRemaining",
                            fontFamily = Quicksand,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple
                        )
                        Text(
                            text = "segundos",
                            fontFamily = Quicksand,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubText
                        )
                    }
                } else {
                    Text(
                        text = "¡Listo! Ya puedes continuar.",
                        fontFamily = Quicksand,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            // Space is reserved so the card doesn't jump when the button appears
            Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                if (secondsRemaining == 0) {
                    AuthPrimaryButton(text = "Entendido", onClick = onContinue)
                }
            }
        }
    }
}
