package com.zentemi.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zentemi.app.ui.theme.*

/**
 * Layout for the account screens: content centered vertically, scrollable and
 * pushed up by the keyboard, with an optional back button pinned at the top-left.
 */
@Composable
fun AuthLayout(
    onBack: (() -> Unit)? = null,
    lightBackground: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .padding(top = 64.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                content = content
            )
        }

        if (onBack != null) {
            BackCircleButton(
                onClick = onBack,
                lightBackground = lightBackground,
                modifier = Modifier.padding(start = 20.dp, top = 10.dp)
            )
        }
    }
}

/** Round back button: white with purple chevron on light screens, translucent on purple ones. */
@Composable
fun BackCircleButton(
    onClick: () -> Unit,
    lightBackground: Boolean,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .then(if (lightBackground) Modifier.shadow(3.dp, CircleShape) else Modifier)
            .clip(CircleShape)
            .background(if (lightBackground) Color.White else Color.White.copy(alpha = 0.15f))
    ) {
        Icon(
            imageVector = Icons.Filled.ChevronLeft,
            contentDescription = "Regresar",
            tint = if (lightBackground) Purple else Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

/** Large icon over a soft glow, used as the header of the account screens. */
@Composable
fun HeaderIcon(icon: ImageVector, tint: Color = Color.White, size: Dp = 56.dp) {
    val anim = rememberInfiniteTransition(label = "headerIcon")
    val glow by anim.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size * 1.9f * glow)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(SplashGlow.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(size))
    }
}

@Composable
fun AuthTitle(text: String, color: Color = Color.White, fontSize: Int = 32) {
    Text(
        text = text,
        fontFamily = Quicksand,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        lineHeight = (fontSize + 4).sp,
        modifier = Modifier.padding(horizontal = 30.dp)
    )
}

@Composable
fun AuthSubtitle(text: String, color: Color = Color.White.copy(alpha = 0.82f)) {
    Text(
        text = text,
        fontFamily = Quicksand,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

/** Translucent rounded card that groups the inputs on purple screens. */
@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/** White rounded input field. With [pin] it only accepts 4 digits and hides them. */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    pin: Boolean = false,
    bordered: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (pin) onValueChange(it.filter { c -> c.isDigit() }.take(4)) else onValueChange(it) },
        placeholder = {
            Text(placeholder, fontFamily = Quicksand, fontWeight = FontWeight.Medium, color = SubText.copy(alpha = 0.55f))
        },
        textStyle = TextStyle(fontFamily = Quicksand, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkText),
        visualTransformation = if (pin) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (pin) KeyboardOptions(keyboardType = KeyboardType.NumberPassword) else KeyboardOptions.Default,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = if (bordered) Purple else Color.Transparent,
            unfocusedBorderColor = if (bordered) Purple.copy(alpha = 0.18f) else Color.Transparent,
            cursorColor = Purple
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Check / cross row telling whether both PINs match. */
@Composable
fun PinMatchRow(matches: Boolean, textColor: Color = Color.White.copy(alpha = 0.88f)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (matches) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (matches) SuccessGreen else ErrorRed,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (matches) "Los PIN coinciden" else "Los PIN no coinciden",
            fontFamily = Quicksand,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

/** Main action button. On purple screens it's gold; on light screens it's purple. */
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    onLightBackground: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = if (onLightBackground) {
            ButtonDefaults.buttonColors(
                containerColor = Purple,
                disabledContainerColor = Color.Gray.copy(alpha = 0.35f),
                disabledContentColor = Color.White
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = SplashDotActive,
                disabledContainerColor = Color.White.copy(alpha = 0.35f),
                disabledContentColor = PurpleDark.copy(alpha = 0.6f)
            )
        },
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
            .height(56.dp)
    ) {
        Text(
            text = text,
            fontFamily = Quicksand,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (onLightBackground) Color.White else PurpleDark.copy(alpha = if (enabled) 1f else 0.6f)
        )
    }
}

/** Light lavender background with soft colored circles (light account screens). */
@Composable
fun LightAuthBackground(content: @Composable BoxScope.() -> Unit) {
    val anim = rememberInfiniteTransition(label = "lightBg")
    val drift by anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        val w = maxWidth
        val h = maxHeight
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = w - 130.dp + (drift * 30).dp, y = (-70).dp - (drift * 30).dp)
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (-60).dp - (drift * 30).dp, y = h * 0.38f - (drift * 30).dp)
                .clip(CircleShape)
                .background(Turquoise.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = w - 90.dp + (drift * 25).dp, y = h * 0.8f + (drift * 25).dp)
                .clip(CircleShape)
                .background(SplashDotActive.copy(alpha = 0.14f))
        )
        content()
    }
}
