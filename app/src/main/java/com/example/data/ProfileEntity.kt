package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val isDefault: Boolean = false,
    val mode: String = "TEXT_INPUT", // "OFF", "TEXT_INPUT", "CURSOR_CONTROL"
    
    // Sensitivity and Deadzone
    val leftDeadzone: Float = 0.22f,
    val leftSensitivity: Float = 1.0f,
    val rightDeadzone: Float = 0.22f,
    val rightSensitivity: Float = 1.0f,
    
    // Cursor control settings
    val mouseSensitivity: Float = 1.5f,
    val mouseAccelerationCurve: String = "EXPONENTIAL", // "LINEAR", "EXPONENTIAL", "SMOOTH"
    val leftStickMoveCursor: Boolean = true,
    val rightStickMoveCursor: Boolean = false,
    val leftStickRadialActionsJson: String = "[]",
    val rightStickRadialActionsJson: String = "[]",
    val cursorMappingsJson: String = "{}",
    val cursorCombinationsJson: String = "[]",
    
    // Overlay settings
    val overlayScale: Float = 1.0f,
    val overlayOpacity: Float = 0.90f,
    val overlayPosX: Float = 0.5f,
    val overlayPosY: Float = 0.65f,
    val overlayAutoHideSeconds: Float = 0f, // 0 means always on, 0.1 to 999
    val overlayTheme: String = "CYBERPUNK", // "CYBERPUNK", "DEEP_OLED", "MATRIX_GREEN", "SOLAR_AMBER", "FROST_BLUE"
    
    // Hotkey / Shortcuts for toggling modes (1 to 6 keys)
    val shortcutTextInput: String = "L3 + R3",
    val shortcutOverlay: String = "BACK + START",
    val shortcutCursor: String = "L1 + R1",
    
    // Raw JSON data for radial zones & button mappings & layouts K1..K16
    val activeLayoutId: String = "K1",
    val layoutsJson: String = "[]",
    val buttonCombinationsJson: String = "[]",
    val zonesJson: String,
    val mappingsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
