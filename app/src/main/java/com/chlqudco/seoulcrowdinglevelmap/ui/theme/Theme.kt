package com.chlqudco.seoulcrowdinglevelmap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RadarGreen,
    onPrimary = Color.White,
    primaryContainer = RadarMintSoft,
    onPrimaryContainer = RadarGreenDark,
    secondary = RadarMint,
    onSecondary = RadarGreenDark,
    background = RadarBackground,
    onBackground = RadarInk,
    surface = RadarSurface,
    onSurface = RadarInk,
    surfaceVariant = Color(0xFFEDF2F0),
    onSurfaceVariant = RadarMuted,
    outline = RadarLine,
    error = CrowdedRed
)

private val DarkColorScheme = darkColorScheme(
    primary = RadarMint,
    onPrimary = RadarGreenDark,
    primaryContainer = Color(0xFF174E3E),
    onPrimaryContainer = Color(0xFFC5F3E1),
    secondary = RadarMint,
    onSecondary = RadarGreenDark,
    background = DarkBackground,
    onBackground = Color(0xFFE5ECE9),
    surface = DarkSurface,
    onSurface = Color(0xFFE5ECE9),
    surfaceVariant = Color(0xFF23302B),
    onSurfaceVariant = Color(0xFFB6C2BD),
    outline = DarkLine,
    error = Color(0xFFFFB4AB)
)

@Composable
fun SeoulCrowdingLevelMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
