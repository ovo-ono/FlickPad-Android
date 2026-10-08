package com.example.engine

import kotlin.math.pow
import kotlin.math.sqrt

enum class CursorAccelerationCurve {
    LINEAR,
    EXPONENTIAL,
    SMOOTH
}

enum class StickRole {
    MOUSE_MOVEMENT,
    SCROLLING,
    ZOOMING,
    NONE
}

object CursorEngine {

    /**
     * Compute delta movement for cursor based on input stick (x, y), deadzone, sensitivity, and acceleration curve.
     */
    fun computeMovementDelta(
        x: Float,
        y: Float,
        deadzone: Float,
        sensitivity: Float,
        curve: CursorAccelerationCurve
    ): Pair<Float, Float> {
        val magnitude = sqrt(x * x + y * y)
        if (magnitude < deadzone) return Pair(0f, 0f)

        // Normalize range from deadzone to 1.0
        val normalizedMag = ((magnitude - deadzone) / (1.0f - deadzone)).coerceIn(0f, 1f)
        val dirX = x / magnitude
        val dirY = y / magnitude

        val acceleratedFactor = when (curve) {
            CursorAccelerationCurve.LINEAR -> normalizedMag
            CursorAccelerationCurve.EXPONENTIAL -> normalizedMag.pow(2.2f)
            CursorAccelerationCurve.SMOOTH -> {
                // S-curve: 3x^2 - 2x^3
                3f * (normalizedMag.pow(2)) - 2f * (normalizedMag.pow(3))
            }
        }

        val baseSpeed = 24.0f * sensitivity
        val deltaX = dirX * acceleratedFactor * baseSpeed
        val deltaY = dirY * acceleratedFactor * baseSpeed

        return Pair(deltaX, deltaY)
    }

    /**
     * Compute scroll delta based on input stick
     */
    fun computeScrollDelta(
        x: Float,
        y: Float,
        deadzone: Float,
        sensitivity: Float
    ): Pair<Float, Float> {
        val magnitude = sqrt(x * x + y * y)
        if (magnitude < deadzone) return Pair(0f, 0f)

        val normalizedMag = ((magnitude - deadzone) / (1.0f - deadzone)).coerceIn(0f, 1f)
        val dirX = x / magnitude
        val dirY = y / magnitude

        val scrollSpeed = 16.0f * sensitivity
        return Pair(dirX * normalizedMag * scrollSpeed, dirY * normalizedMag * scrollSpeed)
    }
}
