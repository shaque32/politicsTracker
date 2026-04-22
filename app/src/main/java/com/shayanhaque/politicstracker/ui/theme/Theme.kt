package com.shayanhaque.politicstracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = IndigoPrimary,
    secondary = IndigoSecondary,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onSurface = OnSurface,
    background = Surface,
)

private val DarkColors = darkColorScheme(
    primary = IndigoPrimaryContainer,
    onPrimary = Color(0xFF17171B),
    primaryContainer = Color(0xFF242A60),
    onPrimaryContainer = IndigoPrimaryContainer,
    secondary = Color(0xFFB5C2D1),
    surface = Color(0xFF121316),
    surfaceVariant = Color(0xFF1C1E22),
    onSurface = Color(0xFFE6E7EA),
    background = Color(0xFF0E0F12),
)

@Composable
fun PoliticsTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
