package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ButtonCombination(
    val id: String = UUID.randomUUID().toString(),
    val buttons: List<String>, // list of 2 to 4 keyCodes
    val actionCode: String
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        val arr = JSONArray()
        buttons.forEach { arr.put(it) }
        obj.put("buttons", arr)
        obj.put("action", actionCode)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): ButtonCombination {
            val id = obj.optString("id", UUID.randomUUID().toString())
            val bArr = obj.getJSONArray("buttons")
            val list = mutableListOf<String>()
            for (i in 0 until bArr.length()) {
                list.add(bArr.getString(i))
            }
            val act = obj.optString("action", "NONE")
            return ButtonCombination(id, list, act)
        }

        fun getDefaultCombinations(): List<ButtonCombination> {
            return listOf(
                ButtonCombination(
                    buttons = listOf("KEYCODE_BUTTON_START", "KEYCODE_BUTTON_Y"),
                    actionCode = "MEDIA_VOLUME_UP"
                ),
                ButtonCombination(
                    buttons = listOf("KEYCODE_BUTTON_START", "KEYCODE_BUTTON_A"),
                    actionCode = "MEDIA_VOLUME_DOWN"
                ),
                ButtonCombination(
                    buttons = listOf("KEYCODE_BUTTON_L1", "KEYCODE_BUTTON_Y"),
                    actionCode = "BRIGHTNESS_UP"
                ),
                ButtonCombination(
                    buttons = listOf("KEYCODE_BUTTON_L1", "KEYCODE_BUTTON_A"),
                    actionCode = "BRIGHTNESS_DOWN"
                ),
                ButtonCombination(
                    buttons = listOf("KEYCODE_BUTTON_START", "KEYCODE_BUTTON_B"),
                    actionCode = "MEDIA_MUTE"
                ),
                ButtonCombination(
                    buttons = listOf("KEYCODE_BUTTON_START", "KEYCODE_BUTTON_X"),
                    actionCode = "MEDIA_PLAY_PAUSE"
                )
            )
        }

        fun combinationsToJson(list: List<ButtonCombination>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun jsonToCombinations(jsonStr: String): List<ButtonCombination> {
            return try {
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<ButtonCombination>()
                for (i in 0 until arr.length()) {
                    list.add(fromJson(arr.getJSONObject(i)))
                }
                if (list.isEmpty()) getDefaultCombinations() else list
            } catch (_: Exception) {
                getDefaultCombinations()
            }
        }
    }
}
