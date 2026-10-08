package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Retro Terminal Amber Palette
// Single canonical shade of amber used everywhere:
val TerminalAmber = Color(0xFFFFB000)          // Standard phosphor Amber
val TerminalAmberBright = Color(0xFFFFC033)    // Slightly brighter amber for highlights if needed
val TerminalAmberDim = Color(0xFF805800)       // Dim amber
val TerminalGreen = Color(0xFF33FF33)          // Phosphor Green (active / enabled / selected state)
val TerminalGreenDim = Color(0xFF1B801B)
val TerminalRed = Color(0xFFFF3333)            // Error / Clear red

// Pure Black CRT backgrounds & Amber outlines
val TerminalBgDark = Color(0xFF000000)         // Pure Black background
val TerminalSurfaceDark = Color(0xFF000000)     // Pure Black surface
val TerminalSurfaceElevated = Color(0xFF000000) // Pure Black component background
val TerminalCardBorder = Color(0xFFFFB000).copy(alpha = 0.55f) // Thin amber outline
val TerminalCardBorderBright = Color(0xFFFFB000) // Full amber outline

// Text colors (all phosphor amber)
val TerminalTextPrimary = TerminalAmber
val TerminalTextSecondary = TerminalAmber.copy(alpha = 0.75f)
val TerminalTextMuted = TerminalAmber.copy(alpha = 0.50f)

// Standard aliases pointing to canonical Retro Terminal Amber and Green
val TerminalOrange = TerminalAmber
val TerminalOrangeBright = TerminalAmber

val FlickCyan = TerminalAmber
val FlickCyanMuted = TerminalAmber.copy(alpha = 0.7f)
val FlickViolet = TerminalAmber
val FlickVioletMuted = TerminalAmber.copy(alpha = 0.7f)
val FlickGreen = TerminalGreen
val FlickAmber = TerminalAmber
val FlickRed = TerminalRed

val FlickBgDark = TerminalBgDark
val FlickSurfaceDark = TerminalSurfaceDark
val FlickSurfaceElevated = TerminalSurfaceElevated
val FlickCardBorder = TerminalCardBorder
val FlickTextPrimary = TerminalTextPrimary
val FlickTextSecondary = TerminalTextSecondary
val FlickTextMuted = TerminalTextMuted

// Overlay HUD Themes (all CRT-based pure black backgrounds)
object OverlayThemes {
    val AMBER_CRT = listOf(TerminalAmber, TerminalAmberBright, TerminalBgDark)
    val GREEN_PHOSPHOR = listOf(TerminalGreen, TerminalGreenDim, TerminalBgDark)
    val SOLAR_AMBER = listOf(TerminalAmber, TerminalAmber, TerminalBgDark)
    val HIGH_CONTRAST = listOf(TerminalAmber, TerminalGreen, TerminalBgDark)

    fun getColorsForTheme(themeName: String): Triple<Color, Color, Color> {
        return when (themeName.uppercase()) {
            "GREEN_PHOSPHOR", "MATRIX_GREEN" -> Triple(TerminalGreen, TerminalGreenDim, TerminalBgDark)
            "HIGH_CONTRAST" -> Triple(TerminalAmber, TerminalGreen, TerminalBgDark)
            else -> Triple(TerminalAmber, TerminalAmber.copy(alpha = 0.7f), TerminalBgDark)
        }
    }
}
