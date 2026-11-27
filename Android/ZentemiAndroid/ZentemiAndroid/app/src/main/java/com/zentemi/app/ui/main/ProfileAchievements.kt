package com.zentemi.app.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.data.ProfileAchievement
import com.zentemi.app.state.AppState
import com.zentemi.app.ui.theme.*

/** Logros del perfil, 3 por fila; al tocar uno se ven sus detalles (ProfileAchievementsSection.swift). */
@Composable
fun ProfileAchievementsSection(appState: AppState, modifier: Modifier = Modifier) {
    val achievements = appState.profileAchievements
    var selected by remember { mutableStateOf<ProfileAchievement?>(null) }

    Column(modifier = modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Logros", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${achievements.count { it.isCompleted }} de ${achievements.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Purple,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        achievements.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                row.forEach { achievement ->
                    AchievementTile(achievement, Modifier.weight(1f).fillMaxHeight()) { selected = achievement }
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }

    selected?.let { achievement ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                TextButton(onClick = { selected = null }) {
                    Text("Entendido", color = Purple, fontWeight = FontWeight.Bold)
                }
            },
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(achievement.image),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(110.dp).clip(RoundedCornerShape(24.dp))
                    )
                    Text(achievement.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText, textAlign = TextAlign.Center)
                    Text(achievement.detail, fontSize = 15.sp, color = SubText, textAlign = TextAlign.Center)
                    Text(
                        if (achievement.isCompleted) "¡Logro completado!" else "Sigue avanzando",
                        fontWeight = FontWeight.Bold,
                        color = if (achievement.isCompleted) Turquoise else Purple
                    )
                    Text("%,d de %,d".format(achievement.displayedProgress, achievement.target), fontSize = 14.sp, color = DarkText)
                }
            }
        )
    }
}

private val grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
private fun AchievementTile(achievement: ProfileAchievement, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(15.dp), ambientColor = Purple.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .border(1.5.dp, if (achievement.isCompleted) Turquoise.copy(alpha = 0.45f) else Color.Transparent, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            Image(
                painter = painterResource(achievement.image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = if (achievement.isCompleted) null else grayscale,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .alpha(if (achievement.isCompleted) 1f else 0.55f)
            )
            Icon(
                if (achievement.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = if (achievement.isCompleted) "Completado" else "Bloqueado",
                tint = if (achievement.isCompleted) Turquoise else Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
            )
        }
        Text(
            achievement.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            modifier = Modifier.heightIn(min = 30.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        LinearProgressIndicator(
            progress = { achievement.displayedProgress.toFloat() / achievement.target },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = if (achievement.isCompleted) Turquoise else Purple,
            trackColor = Purple.copy(alpha = 0.12f),
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Text("${achievement.displayedProgress}/${achievement.target}", fontSize = 11.sp, color = SubText)
    }
}
