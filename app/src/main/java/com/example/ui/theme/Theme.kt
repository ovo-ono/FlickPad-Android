package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TerminalColorScheme = darkColorScheme(
    primary = TerminalAmber,
    onPrimary = Color.Black,
    primaryContainer = Color.Black,
    onPrimaryContainer = TerminalAmber,
    secondary = TerminalGreen,
    onSecondary = Color.Black,
    secondaryContainer = Color.Black,
    onSecondaryContainer = TerminalGreen,
    tertiary = TerminalGreen,
    onTertiary = Color.Black,
    tertiaryContainer = Color.Black,
    onTertiaryContainer = TerminalGreen,
    background = Color.Black,
    onBackground = TerminalAmber,
    surface = Color.Black,
    onSurface = TerminalAmber,
    surfaceVariant = Color.Black,
    onSurfaceVariant = TerminalAmber.copy(alpha = 0.75f),
    outline = TerminalCardBorder,
    outlineVariant = TerminalAmber.copy(alpha = 0.35f),
    error = TerminalRed,
    onError = Color.Black
)

@Composable
fun FlickPadTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TerminalColorScheme,
        typography = Typography,
        content = content
    )
}
