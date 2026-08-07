package com.example.musicsiren.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SirenDarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color.Black,
    primaryContainer = AccentCyan.copy(alpha = 0.15f),
    onPrimaryContainer = AccentCyan,
    secondary = AccentTeal,
    background = Background,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = PlayerBarColor,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    outlineVariant = HairlineWhite,
    error = Color(0xFF9D1919),
)

/** 始终强制暗黑（网站本身是暗色主题）。 */
@Composable
fun SirenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SirenDarkColorScheme,
        shapes = SirenShapes,
        content = content,
    )
}
