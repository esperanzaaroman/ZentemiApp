package com.zentemi.app.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.zentemi.app.ui.components.MascotGlow
import com.zentemi.app.ui.components.ZentemiBackground
import com.zentemi.app.ui.theme.*

data class OnboardingPage(
    val title: String,
    val subtitle: String
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {

    val pages = listOf(
        OnboardingPage("Zentemi", "Aprende Yuhmu jugando"),
        OnboardingPage("Aprende paso a paso", "Explora lecciones sobre saludos, familia, números y más."),
        OnboardingPage("Conecta con tu cultura", "Descubre la lengua Yuhmu de San Juan Ixtenco, Tlaxcala.")
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    ZentemiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // The whole upper area (mascot + text) is swipeable
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(0.3f))

                    MascotGlow()

                    Text(
                        text = pages[page].title,
                        fontFamily = Quicksand,
                        fontSize = if (page == 0) 44.sp else 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = if (page == 0) 48.sp else 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pages[page].subtitle,
                        fontFamily = Quicksand,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp
                    )

                    Spacer(modifier = Modifier.weight(0.15f))
                }
            }

            // Page indicators
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) SplashDotActive else Color.White.copy(alpha = 0.45f),
                        label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Button only on the last page; space is reserved so the layout doesn't jump
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                if (pagerState.currentPage == pages.size - 1) {
                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SplashDotActive
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Siguiente",
                            fontFamily = Quicksand,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
