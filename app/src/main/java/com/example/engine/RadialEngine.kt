package com.example.engine

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object RadialEngine {

    /**
     * Radial zone calculation for Left Analog Stick.
     * 0 = Neutral / Center (magnitude < deadzone)
     * 1 = North (UP)
     * 2 = North-East (UP-RIGHT)
     * 3 = East (RIGHT)
     * 4 = South-East (DOWN-RIGHT)
     * 5 = South (DOWN)
     * 6 = South-West (DOWN-LEFT)
     * 7 = West (LEFT)
     * 8 = North-West (UP-LEFT)
     *
     * In Android MotionEvent:
     * Up is negative Y (-1.0), Down is positive Y (+1.0)
     * Right is positive X (+1.0), Left is negative X (-1.0)
     *
     * Using atan2(y, x):
     * Right (1, 0) -> 0 deg
     * Down (0, 1) -> 90 deg
     * Left (-1, 0) -> 180 deg
     * Up (0, -1) -> 270 deg (or -90 deg)
     */
    fun getZoneFromStick(x: Float, y: Float, deadzone: Float): Int {
        val magnitude = sqrt(x * x + y * y)
        val effectiveDeadzone = deadzone.coerceIn(0.05f, 0.90f)
        if (magnitude < effectiveDeadzone) {
            return 0 // Neutral / Center
        }

        var degrees = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
        if (degrees < 0) {
            degrees += 360f
        }

        // 8 equal sectors of 45 degrees centered on the primary axes and diagonals
        return when {
            degrees in 247.5f..292.5f -> 1 // UP (around 270 deg)
            degrees in 292.5f..337.5f -> 2 // UP-RIGHT (around 315 deg)
            degrees >= 337.5f || degrees < 22.5f -> 3 // RIGHT (around 0 deg)
            degrees in 22.5f..67.5f -> 4 // DOWN-RIGHT (around 45 deg)
            degrees in 67.5f..112.5f -> 5 // DOWN (around 90 deg)
            degrees in 112.5f..157.5f -> 6 // DOWN-LEFT (around 135 deg)
            degrees in 157.5f..202.5f -> 7 // LEFT (around 180 deg)
            degrees in 202.5f..247.5f -> 8 // UP-LEFT (around 225 deg)
            else -> 0
        }
    }

    /**
     * Radial sector calculation for Right Analog Stick (0..7 matching 3x3 layout perimeter).
     * Inside each zone's 3x3 grid:
     * 0 = Up (North, around 270 deg)
     * 1 = Up-Right (NE, around 315 deg)
     * 2 = Right (East, around 0 deg)
     * 3 = Down-Right (SE, around 45 deg)
     * 4 = Down (South, around 90 deg)
     * 5 = Down-Left (SW, around 135 deg)
     * 6 = Left (West, around 180 deg)
     * 7 = Up-Left (NW, around 225 deg)
     *
     * Returns null if inside deadzone.
     */
    fun getSectorFromStick(x: Float, y: Float, deadzone: Float): Int? {
        val magnitude = sqrt(x * x + y * y)
        val effectiveDeadzone = deadzone.coerceIn(0.05f, 0.90f)
        if (magnitude < effectiveDeadzone) {
            return null
        }

        var degrees = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
        if (degrees < 0) {
            degrees += 360f
        }

        return when {
            degrees in 247.5f..292.5f -> 0 // UP (North)
            degrees in 292.5f..337.5f -> 1 // UP-RIGHT (NE)
            degrees >= 337.5f || degrees < 22.5f -> 2 // RIGHT (East)
            degrees in 22.5f..67.5f -> 3 // DOWN-RIGHT (SE)
            degrees in 67.5f..112.5f -> 4 // DOWN (South)
            degrees in 112.5f..157.5f -> 5 // DOWN-LEFT (SW)
            degrees in 157.5f..202.5f -> 6 // LEFT (West)
            degrees in 202.5f..247.5f -> 7 // UP-LEFT (NW)
            else -> 0
        }
    }

    fun getZoneName(zone: Int): String {
        return when (zone) {
            0 -> "NEUTRAL"
            1 -> "UP"
            2 -> "UP-RIGHT"
            3 -> "RIGHT"
            4 -> "DOWN-RIGHT"
            5 -> "DOWN"
            6 -> "DOWN-LEFT"
            7 -> "LEFT"
            8 -> "UP-LEFT"
            else -> "ZONE $zone"
        }
    }

    fun getSectorDirectionLabel(sector: Int): String {
        return when (sector) {
            0 -> "N"
            1 -> "NE"
            2 -> "E"
            3 -> "SE"
            4 -> "S"
            5 -> "SW"
            6 -> "W"
            7 -> "NW"
            else -> "$sector"
        }
    }
}
