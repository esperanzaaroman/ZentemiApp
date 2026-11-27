package com.zentemi.app.ui.games

import androidx.annotation.DrawableRes
import com.zentemi.app.ui.components.ZentemiIcon
import com.zentemi.app.R
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.components.BackCircleButton
import com.zentemi.app.ui.components.HeaderTitle
import com.zentemi.app.ui.components.PetView
import com.zentemi.app.ui.components.TagChip
import com.zentemi.app.ui.components.WaveHeader
import com.zentemi.app.ui.theme.*

/** Lila del globo de diálogo de Maicito. */
private val BubbleColor = Color(0xFFEDE4FA)

/** Juegos disponibles en "Jugar con Maicito". */
enum class MaicitoGame(
    val title: String,
    val description: String,
    val tag: String,
    @param:DrawableRes val icon: Int,
    val color: Color
) {
    MAICITO_PIDE(
        "Maicito pide",
        "Escucha lo que dice Maicito en Yuhmu y elige qué significa.",
        "Escuchar",
        R.drawable.ic_maicito_pide,
        Purple
    ),
    MEMORAMA(
        "Memorama",
        "Voltea las cartas y junta cada audio en Yuhmu con su significado.",
        "Memoria",
        R.drawable.ic_memorama,
        Turquoise
    ),
    MERCADO(
        "El mercado de Ixtenco",
        "Visita el tianguis con Maicito y platica con el vendedor en Yuhmu.",
        "Historia",
        R.drawable.ic_mercado,
        Color(0xFFFF9500)
    )
}

/** Pantalla "Jugar con Maicito" con la lista de juegos. */
@Composable
fun GamesHubScreen(
    appState: AppState,
    onBack: () -> Unit,
    onGameSelected: (MaicitoGame) -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        WaveHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackCircleButton(onClick = onBack, lightBackground = false)
                Spacer(modifier = Modifier.width(14.dp))
                HeaderTitle(title = "Jugar con ${appState.petName}", subtitle = "Repasa Yuhmu jugando")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Maicito "platicando": globo de diálogo, no es una tarjeta tocable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PetView(appState = appState, size = 78.dp)

                // Colita del globo apuntando a Maicito
                Canvas(modifier = Modifier.size(10.dp, 18.dp)) {
                    val tail = Path().apply {
                        moveTo(size.width, 0f)
                        lineTo(0f, size.height / 2)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(tail, BubbleColor)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(BubbleColor)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (appState.gamesUnlocked) "¡Juguemos y practiquemos!"
                        else "¡Primero aprendamos juntos!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleDark
                    )
                    if (appState.gamesUnlocked) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ZentemiIcon(R.drawable.ic_regalo, size = 20.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Recompensas de hoy: ${appState.gameRewardsLeft}/${appState.maxGameRewardsPerDay}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        }
                        Text(
                            "Cada partida sube la felicidad de ${appState.petName}.",
                            fontSize = 12.sp,
                            color = SubText
                        )
                    } else {
                        Text(
                            "Termina la lección «Primeras frases» para desbloquear los juegos.",
                            fontSize = 13.sp,
                            color = SubText
                        )
                    }
                }
            }

            MaicitoGame.entries.forEach { game ->
                GameCard(
                    game = game,
                    unlocked = appState.gamesUnlocked,
                    onClick = { onGameSelected(game) }
                )
            }
        }
    }
}

@Composable
private fun GameCard(game: MaicitoGame, unlocked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (unlocked) game.color.copy(alpha = 0.14f) else LockedNode),
            contentAlignment = Alignment.Center
        ) {
            ZentemiIcon(
                if (unlocked) game.icon else R.drawable.ic_leccion_bloqueada,
                size = 44.dp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            TagChip(text = game.tag.uppercase(), color = game.color)
            Text(
                game.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) DarkText else DarkText.copy(alpha = 0.5f)
            )
            Text(
                game.description,
                fontSize = 13.sp,
                color = if (unlocked) SubText else SubText.copy(alpha = 0.5f)
            )
        }

        if (unlocked) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Purple.copy(alpha = 0.6f)
            )
        }
    }
}
