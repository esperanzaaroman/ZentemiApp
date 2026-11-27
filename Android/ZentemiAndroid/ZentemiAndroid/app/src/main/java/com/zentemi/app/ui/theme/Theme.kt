package com.zentemi.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleTint,
    onPrimaryContainer = PurpleDark,
    secondary = Turquoise,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = DarkText,
    surface = Color.White,
    onSurface = DarkText,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = SubText,
    outline = Hairline,
    outlineVariant = Hairline,
)

/** Una sola escala de esquinas para toda la app. */
private val ZentemiShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl),
)

/** Esquinas y márgenes compartidos, para que las pantallas queden alineadas. */
object Radius {
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 28.dp
}

object Spacing {
    /** Margen izquierdo y derecho de cada pantalla. */
    val screen = 20.dp
    val gap = 12.dp
    val section = 18.dp
}

@Composable
fun ZentemiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = ZentemiShapes,
        content = content
    )
}
