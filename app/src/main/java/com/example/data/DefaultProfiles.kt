package com.example.data

import org.json.JSONArray
import org.json.JSONObject

data class ZoneData(
    val index: Int,
    val name: String,
    val symbols: List<String> // 8 symbols: NW, N, NE, W, E, SW, S, SE
)

object DefaultProfiles {

    val DIRECTION_NAMES = listOf("NW", "N", "NE", "W", "E", "SW", "S", "SE")
    val DIRECTION_LABELS = listOf(
        "Up-Left (NW)",
        "Up (N)",
        "Up-Right (NE)",
        "Left (W)",
        "Right (E)",
        "Down-Left (SW)",
        "Down (S)",
        "Down-Right (SE)"
    )

    fun getDefaultRadialStickActions(): List<String> {
        return listOf(
            "Zooming In",     // NW
            "Scrolling Up",   // N
            "Zooming Out",    // NE
            "Scrolling Left", // W
            "Scrolling Right",// E
            "None",           // SW
            "Scrolling Down", // S
            "None"            // SE
        )
    }

    fun radialActionsToJson(actions: List<String>): String {
        val arr = JSONArray()
        actions.forEach { arr.put(it) }
        return arr.toString()
    }

    fun jsonToRadialActions(jsonStr: String): List<String> {
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            while (list.size < 8) list.add("None")
            list.take(8)
        } catch (_: Exception) {
            getDefaultRadialStickActions()
        }
    }

    // Default layout matching the reference image (MessagEase / ThumbKey 3x3 layout)
    fun getDefaultZonesJson(): String {
        val zones = listOf(
            // Zone 0: NEUTRAL (Center)
            ZoneData(0, "NEUTRAL", listOf("i", "e", "s", "a", "t", "r", "o", "n")),
            // Zone 1: UP
            ZoneData(1, "UP", listOf("(", "u", ")", "l", "h", "\"", "d", "'")),
            // Zone 2: UP-RIGHT
            ZoneData(2, "UP-RIGHT", listOf("7", "$", "8", "%", "q", "6", "z", "5")),
            // Zone 3: RIGHT
            ZoneData(3, "RIGHT", listOf("/", "y", "\\", "f", "c", "-", "m", "_")),
            // Zone 4: DOWN-RIGHT
            ZoneData(4, "DOWN-RIGHT", listOf("£", "§", "°", "€", "^", "~", "`", "|")),
            // Zone 5: DOWN
            ZoneData(5, "DOWN", listOf("@", "b", "&", "p", "w", ":", "g", ";")),
            // Zone 6: DOWN-LEFT
            ZoneData(6, "DOWN-LEFT", listOf("]", "{", "}", "[", "9", ">", "<", "0")),
            // Zone 7: LEFT
            ZoneData(7, "LEFT", listOf("#", "!", "=", "?", ".", "+", ",", "*")),
            // Zone 8: UP-LEFT
            ZoneData(8, "UP-LEFT", listOf("3", "x", "4", "j", "v", "2", "k", "1"))
        )
        return zonesToJson(zones)
    }

    fun getGamingZonesJson(): String {
        val zones = listOf(
            ZoneData(0, "NEUTRAL", listOf("gg", "glhf", "yes", "no", "thanks", "sorry", "nice!", "ok")),
            ZoneData(1, "UP", listOf("cover me", "rush A", "defuse", "fall back", "need ammo", "covering", "plant!", "help")),
            ZoneData(2, "UP-RIGHT", listOf("mid", "long", "short", "spawn", "site A", "site B", "flank", "behind")),
            ZoneData(3, "RIGHT", listOf("!", "?", ".", ",", "XD", ":D", ";-)", "<3")),
            ZoneData(4, "DOWN-RIGHT", listOf("1", "2", "3", "4", "5", "6", "7", "8")),
            ZoneData(5, "DOWN", listOf("enemy mid", "rush B", "on my way", "wait", "push", "hold", "clear", "retreat")),
            ZoneData(6, "DOWN-LEFT", listOf("w", "s", "a", "d", "c", "v", "f", "x")),
            ZoneData(7, "LEFT", listOf("🔥", "👍", "💀", "👑", "🎯", "⚡", "🎮", "🛡️")),
            ZoneData(8, "UP-LEFT", listOf("ggwp", "nt", "wp", "afk", "brb", "lmao", "ez", "clutch"))
        )
        return zonesToJson(zones)
    }

    fun getDefaultMappingsJson(): String {
        val json = JSONObject()
        json.put("KEYCODE_BUTTON_A", "SPACE")
        json.put("KEYCODE_BUTTON_B", "BACKSPACE")
        json.put("KEYCODE_BUTTON_X", "SHIFT_TOGGLE")
        json.put("KEYCODE_BUTTON_Y", "ENTER")
        json.put("KEYCODE_BUTTON_L1", "TAB")
        json.put("KEYCODE_BUTTON_R1", "CLEAR_ALL")
        json.put("KEYCODE_BUTTON_L2", "MOUSE_LEFT_CLICK")
        json.put("KEYCODE_BUTTON_R2", "MOUSE_RIGHT_CLICK")
        json.put("KEYCODE_BUTTON_THUMBL", "TOGGLE_OVERLAY")
        json.put("KEYCODE_BUTTON_THUMBR", "CYCLE_MODE")
        json.put("KEYCODE_BUTTON_SELECT", "CLEAR_ALL")
        json.put("KEYCODE_BUTTON_START", "CYCLE_MODE")
        json.put("KEYCODE_DPAD_UP", "MEDIA_VOLUME_UP")
        json.put("KEYCODE_DPAD_DOWN", "MEDIA_VOLUME_DOWN")
        json.put("KEYCODE_DPAD_LEFT", "COPY_CLIPBOARD")
        json.put("KEYCODE_DPAD_RIGHT", "PASTE_CLIPBOARD")
        return json.toString()
    }

    fun getDefaultCursorMappingsJson(): String {
        val json = JSONObject()
        json.put("KEYCODE_BUTTON_A", "MOUSE_LEFT_CLICK")
        json.put("KEYCODE_BUTTON_B", "MOUSE_RIGHT_CLICK")
        json.put("KEYCODE_BUTTON_X", "MOUSE_MIDDLE_CLICK")
        json.put("KEYCODE_BUTTON_Y", "MOUSE_DOUBLE_CLICK")
        json.put("KEYCODE_BUTTON_L1", "BROWSER_BACK")
        json.put("KEYCODE_BUTTON_R1", "BROWSER_FORWARD")
        json.put("KEYCODE_BUTTON_L2", "MOUSE_DRAG_LOCK")
        json.put("KEYCODE_BUTTON_R2", "MOUSE_LEFT_CLICK")
        json.put("KEYCODE_BUTTON_THUMBL", "TOGGLE_OVERLAY")
        json.put("KEYCODE_BUTTON_THUMBR", "CYCLE_MODE")
        json.put("KEYCODE_BUTTON_SELECT", "MOUSE_SCROLL_CLICK")
        json.put("KEYCODE_BUTTON_START", "CYCLE_MODE")
        json.put("KEYCODE_DPAD_UP", "SCROLL_UP_ACTION")
        json.put("KEYCODE_DPAD_DOWN", "SCROLL_DOWN_ACTION")
        json.put("KEYCODE_DPAD_LEFT", "ZOOM_OUT_ACTION")
        json.put("KEYCODE_DPAD_RIGHT", "ZOOM_IN_ACTION")
        return json.toString()
    }

    fun zonesToJson(zones: List<ZoneData>): String {
        val array = JSONArray()
        zones.forEach { zone ->
            val obj = JSONObject()
            obj.put("index", zone.index)
            obj.put("name", zone.name)
            val symArray = JSONArray()
            zone.symbols.forEach { symArray.put(it) }
            obj.put("symbols", symArray)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToZones(jsonStr: String): List<ZoneData> {
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ZoneData>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val idx = obj.optInt("index", i)
                val name = obj.optString("name", "Zone $idx")
                val symArray = obj.getJSONArray("symbols")
                val symbols = mutableListOf<String>()
                for (j in 0 until symArray.length()) {
                    symbols.add(symArray.getString(j))
                }
                while (symbols.size < 8) {
                    symbols.add(" ")
                }
                list.add(ZoneData(idx, name, symbols.take(8)))
            }
            if (list.size < 9) {
                val defaults = jsonToZones(getDefaultZonesJson())
                for (k in list.size until 9) {
                    list.add(defaults.getOrNull(k) ?: ZoneData(k, "Zone $k", List(8) { " " }))
                }
            }
            list
        } catch (_: Exception) {
            jsonToZones(getDefaultZonesJson())
        }
    }

    fun jsonToMappings(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getString(key)
            }
        } catch (_: Exception) {}
        return map
    }

    fun mappingsToJson(map: Map<String, String>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    fun createInitialProfiles(): List<ProfileEntity> {
        val defaultRadialActions = radialActionsToJson(getDefaultRadialStickActions())
        val defaultLayoutsJson = KeyboardLayout.layoutsToJson(KeyboardLayout.createDefaultLayouts())
        val defaultCombosJson = ButtonCombination.combinationsToJson(ButtonCombination.getDefaultCombinations())

        return listOf(
            ProfileEntity(
                name = "Standard 9-Zone MessagEase",
                description = "ThumbKey / FlickBoard inspired layout with 9 zones & 8 radial characters.",
                isDefault = true,
                mode = "TEXT_INPUT",
                leftDeadzone = 0.22f,
                leftSensitivity = 1.0f,
                rightDeadzone = 0.22f,
                rightSensitivity = 1.0f,
                mouseSensitivity = 1.5f,
                mouseAccelerationCurve = "EXPONENTIAL",
                leftStickMoveCursor = true,
                rightStickMoveCursor = false,
                leftStickRadialActionsJson = defaultRadialActions,
                rightStickRadialActionsJson = defaultRadialActions,
                cursorMappingsJson = getDefaultCursorMappingsJson(),
                cursorCombinationsJson = defaultCombosJson,
                overlayScale = 1.0f,
                overlayOpacity = 0.90f,
                overlayPosX = 0.5f,
                overlayPosY = 0.65f,
                overlayAutoHideSeconds = 0f,
                overlayTheme = "CYBERPUNK",
                shortcutTextInput = "L3 + R3",
                shortcutOverlay = "BACK + START",
                shortcutCursor = "L1 + R1",
                activeLayoutId = "K1",
                layoutsJson = defaultLayoutsJson,
                buttonCombinationsJson = defaultCombosJson,
                zonesJson = getDefaultZonesJson(),
                mappingsJson = getDefaultMappingsJson()
            ),
            ProfileEntity(
                name = "Tactical Gaming Chat",
                description = "Optimized for in-game squad communication, tactical callouts, and emotes.",
                isDefault = false,
                mode = "TEXT_INPUT",
                leftDeadzone = 0.20f,
                leftSensitivity = 1.1f,
                rightDeadzone = 0.20f,
                rightSensitivity = 1.1f,
                mouseSensitivity = 1.8f,
                mouseAccelerationCurve = "SMOOTH",
                leftStickMoveCursor = true,
                rightStickMoveCursor = false,
                leftStickRadialActionsJson = defaultRadialActions,
                rightStickRadialActionsJson = defaultRadialActions,
                cursorMappingsJson = getDefaultCursorMappingsJson(),
                cursorCombinationsJson = defaultCombosJson,
                overlayScale = 1.15f,
                overlayOpacity = 0.95f,
                overlayPosX = 0.5f,
                overlayPosY = 0.70f,
                overlayAutoHideSeconds = 5.0f,
                overlayTheme = "MATRIX_GREEN",
                shortcutTextInput = "L3 + R3",
                shortcutOverlay = "BACK + START",
                shortcutCursor = "L1 + R1",
                activeLayoutId = "K2",
                layoutsJson = defaultLayoutsJson,
                buttonCombinationsJson = defaultCombosJson,
                zonesJson = getGamingZonesJson(),
                mappingsJson = getDefaultMappingsJson()
            ),
            ProfileEntity(
                name = "Precision Desktop Cursor",
                description = "Analog mouse control with scrolling, zoom actions, and productivity shortcuts.",
                isDefault = false,
                mode = "CURSOR_CONTROL",
                leftDeadzone = 0.18f,
                leftSensitivity = 1.2f,
                rightDeadzone = 0.18f,
                rightSensitivity = 1.0f,
                mouseSensitivity = 2.0f,
                mouseAccelerationCurve = "SMOOTH",
                leftStickMoveCursor = true,
                rightStickMoveCursor = false,
                leftStickRadialActionsJson = defaultRadialActions,
                rightStickRadialActionsJson = defaultRadialActions,
                cursorMappingsJson = getDefaultCursorMappingsJson(),
                cursorCombinationsJson = defaultCombosJson,
                overlayScale = 0.85f,
                overlayOpacity = 0.75f,
                overlayPosX = 0.8f,
                overlayPosY = 0.2f,
                overlayAutoHideSeconds = 0f,
                overlayTheme = "DEEP_OLED",
                shortcutTextInput = "L3 + R3",
                shortcutOverlay = "BACK + START",
                shortcutCursor = "L1 + R1",
                activeLayoutId = "K1",
                layoutsJson = defaultLayoutsJson,
                buttonCombinationsJson = defaultCombosJson,
                zonesJson = getDefaultZonesJson(),
                mappingsJson = getDefaultMappingsJson()
            )
        )
    }
}
