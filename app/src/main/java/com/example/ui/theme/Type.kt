package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Monospace retro terminal typography
val TerminalMonospace = FontFamily.Monospace

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.5.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.5.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Normal,
        fontSize = 16.5.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.3.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 16.5.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.5.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TerminalMonospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        letterSpacing = 0.5.sp
    )
)
