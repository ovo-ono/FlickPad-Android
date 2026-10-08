package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class ProfileRepository(private val profileDao: ProfileDao) {

    val allProfiles: Flow<List<ProfileEntity>> = profileDao.getAllProfiles()

    suspend fun getProfileById(id: Long): ProfileEntity? = profileDao.getProfileById(id)

    suspend fun getDefaultProfile(): ProfileEntity? = profileDao.getDefaultProfile()

    suspend fun insertProfile(profile: ProfileEntity): Long = profileDao.insert(profile)

    suspend fun updateProfile(profile: ProfileEntity) = profileDao.update(profile)

    suspend fun deleteProfile(profile: ProfileEntity) = profileDao.delete(profile)

    suspend fun setDefaultProfile(id: Long) {
        profileDao.clearDefaultFlags()
        profileDao.setDefaultProfile(id)
    }

    suspend fun ensureDefaultProfilesExist() {
        val existing = profileDao.getDefaultProfile()
        if (existing == null) {
            val list = DefaultProfiles.createInitialProfiles()
            list.forEach { profileDao.insert(it) }
        }
    }

    // Export format compatible with Windows & Linux ports
    fun exportProfileToJson(profile: ProfileEntity): String {
        val root = JSONObject()
        root.put("format", "flickpad_profile")
        root.put("version", 2)
        root.put("name", profile.name)
        root.put("description", profile.description)
        root.put("mode", profile.mode)
        
        val settings = JSONObject()
        val leftStick = JSONObject()
        leftStick.put("deadzone", profile.leftDeadzone.toDouble())
        leftStick.put("sensitivity", profile.leftSensitivity.toDouble())
        settings.put("left_stick", leftStick)
        
        val rightStick = JSONObject()
        rightStick.put("deadzone", profile.rightDeadzone.toDouble())
        rightStick.put("sensitivity", profile.rightSensitivity.toDouble())
        settings.put("right_stick", rightStick)
        
        val cursor = JSONObject()
        cursor.put("mouse_sensitivity", profile.mouseSensitivity.toDouble())
        cursor.put("acceleration", profile.mouseAccelerationCurve)
        cursor.put("left_stick_move_cursor", profile.leftStickMoveCursor)
        cursor.put("right_stick_move_cursor", profile.rightStickMoveCursor)
        cursor.put("left_stick_radial_actions", org.json.JSONArray(profile.leftStickRadialActionsJson))
        cursor.put("right_stick_radial_actions", org.json.JSONArray(profile.rightStickRadialActionsJson))
        cursor.put("cursor_mappings", JSONObject(profile.cursorMappingsJson))
        settings.put("cursor", cursor)
        
        val overlay = JSONObject()
        overlay.put("scale", profile.overlayScale.toDouble())
        overlay.put("opacity", profile.overlayOpacity.toDouble())
        overlay.put("pos_x", profile.overlayPosX.toDouble())
        overlay.put("pos_y", profile.overlayPosY.toDouble())
        overlay.put("auto_hide_seconds", profile.overlayAutoHideSeconds.toDouble())
        overlay.put("theme", profile.overlayTheme)
        settings.put("overlay", overlay)
        
        val shortcuts = JSONObject()
        shortcuts.put("text_input", profile.shortcutTextInput)
        shortcuts.put("overlay", profile.shortcutOverlay)
        shortcuts.put("cursor", profile.shortcutCursor)
        settings.put("shortcuts", shortcuts)
        
        root.put("settings", settings)
        
        // Layouts & Zones & Mappings
        root.put("active_layout_id", profile.activeLayoutId)
        root.put("layouts", org.json.JSONArray(profile.layoutsJson))
        root.put("button_combinations", org.json.JSONArray(profile.buttonCombinationsJson))
        root.put("cursor_combinations", org.json.JSONArray(profile.cursorCombinationsJson))
        root.put("zones", org.json.JSONArray(profile.zonesJson))
        root.put("mappings", JSONObject(profile.mappingsJson))
        
        return root.toString(2)
    }

    // Import format parser
    fun importProfileFromJson(jsonString: String): ProfileEntity {
        val root = JSONObject(jsonString)
        val name = root.optString("name", "Imported Profile")
        val desc = root.optString("description", "Imported from FlickPad configuration JSON")
        val mode = root.optString("mode", "TEXT_INPUT")
        
        val settings = root.optJSONObject("settings")
        val leftStick = settings?.optJSONObject("left_stick")
        val leftDeadzone = leftStick?.optDouble("deadzone", 0.22)?.toFloat() ?: 0.22f
        val leftSens = leftStick?.optDouble("sensitivity", 1.0)?.toFloat() ?: 1.0f
        
        val rightStick = settings?.optJSONObject("right_stick")
        val rightDeadzone = rightStick?.optDouble("deadzone", 0.22)?.toFloat() ?: 0.22f
        val rightSens = rightStick?.optDouble("sensitivity", 1.0)?.toFloat() ?: 1.0f
        
        val cursor = settings?.optJSONObject("cursor")
        val mouseSens = cursor?.optDouble("mouse_sensitivity", 1.5)?.toFloat() ?: 1.5f
        val mouseCurve = cursor?.optString("acceleration", "EXPONENTIAL") ?: "EXPONENTIAL"
        val leftMoveCursor = cursor?.optBoolean("left_stick_move_cursor", true) ?: true
        val rightMoveCursor = cursor?.optBoolean("right_stick_move_cursor", false) ?: false
        val leftRadialActions = cursor?.optJSONArray("left_stick_radial_actions")?.toString()
            ?: DefaultProfiles.radialActionsToJson(DefaultProfiles.getDefaultRadialStickActions())
        val rightRadialActions = cursor?.optJSONArray("right_stick_radial_actions")?.toString()
            ?: DefaultProfiles.radialActionsToJson(DefaultProfiles.getDefaultRadialStickActions())
        val cursorMappings = cursor?.optJSONObject("cursor_mappings")?.toString()
            ?: DefaultProfiles.getDefaultCursorMappingsJson()
        
        val overlay = settings?.optJSONObject("overlay")
        val overlayScale = overlay?.optDouble("scale", 1.0)?.toFloat() ?: 1.0f
        val overlayOpacity = overlay?.optDouble("opacity", 0.90)?.toFloat() ?: 0.90f
        val overlayPosX = overlay?.optDouble("pos_x", 0.5)?.toFloat() ?: 0.5f
        val overlayPosY = overlay?.optDouble("pos_y", 0.65)?.toFloat() ?: 0.65f
        val autoHide = overlay?.optDouble("auto_hide_seconds", 0.0)?.toFloat() ?: 0f
        val overlayTheme = overlay?.optString("theme", "CYBERPUNK") ?: "CYBERPUNK"

        val shortcuts = settings?.optJSONObject("shortcuts")
        val shortcutText = shortcuts?.optString("text_input", "L3 + R3") ?: "L3 + R3"
        val shortcutOvl = shortcuts?.optString("overlay", "BACK + START") ?: "BACK + START"
        val shortcutCur = shortcuts?.optString("cursor", "L1 + R1") ?: "L1 + R1"
        
        val activeLayout = root.optString("active_layout_id", "K1")
        val layoutsJson = if (root.has("layouts")) {
            root.getJSONArray("layouts").toString()
        } else {
            KeyboardLayout.layoutsToJson(KeyboardLayout.createDefaultLayouts())
        }
        val buttonCombosJson = if (root.has("button_combinations")) {
            root.getJSONArray("button_combinations").toString()
        } else {
            ButtonCombination.combinationsToJson(ButtonCombination.getDefaultCombinations())
        }
        val cursorCombosJson = if (root.has("cursor_combinations")) {
            root.getJSONArray("cursor_combinations").toString()
        } else {
            ButtonCombination.combinationsToJson(ButtonCombination.getDefaultCombinations())
        }

        val zonesJson = if (root.has("zones")) {
            root.getJSONArray("zones").toString()
        } else {
            DefaultProfiles.getDefaultZonesJson()
        }
        
        val mappingsJson = if (root.has("mappings")) {
            root.getJSONObject("mappings").toString()
        } else {
            DefaultProfiles.getDefaultMappingsJson()
        }
        
        return ProfileEntity(
            name = name,
            description = desc,
            isDefault = false,
            mode = mode,
            leftDeadzone = leftDeadzone,
            leftSensitivity = leftSens,
            rightDeadzone = rightDeadzone,
            rightSensitivity = rightSens,
            mouseSensitivity = mouseSens,
            mouseAccelerationCurve = mouseCurve,
            leftStickMoveCursor = leftMoveCursor,
            rightStickMoveCursor = rightMoveCursor,
            leftStickRadialActionsJson = leftRadialActions,
            rightStickRadialActionsJson = rightRadialActions,
            cursorMappingsJson = cursorMappings,
            cursorCombinationsJson = cursorCombosJson,
            overlayScale = overlayScale,
            overlayOpacity = overlayOpacity,
            overlayPosX = overlayPosX,
            overlayPosY = overlayPosY,
            overlayAutoHideSeconds = autoHide,
            overlayTheme = overlayTheme,
            shortcutTextInput = shortcutText,
            shortcutOverlay = shortcutOvl,
            shortcutCursor = shortcutCur,
            activeLayoutId = activeLayout,
            layoutsJson = layoutsJson,
            buttonCombinationsJson = buttonCombosJson,
            zonesJson = zonesJson,
            mappingsJson = mappingsJson
        )
    }
}
