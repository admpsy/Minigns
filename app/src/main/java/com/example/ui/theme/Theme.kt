package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CryptsColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1A1202),
    primaryContainer = GoldSecondary,
    onPrimaryContainer = GoldLight,
    secondary = ArcaneCyan,
    onSecondary = Color(0xFF001B2E),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = ArcanePurple,
    onTertiary = Color(0xFF2E004F),
    background = DarkBg,
    onBackground = Color(0xFFEDEDED),
    surface = DarkSurface,
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = BloodCrimson,
    onError = Color.White,
    outline = DarkBorder,
    outlineVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our rich dark fantasy palette for immersion
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CryptsColorScheme,
        typography = Typography,
        content = content
    )
}
