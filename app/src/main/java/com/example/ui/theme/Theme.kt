package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    secondary = MintSecondary,
    tertiary = GoldTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceCard,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    primaryContainer = LightSurfaceCard,
    secondary = MintSecondary,
    tertiary = GoldTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceCard,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun WalkEarnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
