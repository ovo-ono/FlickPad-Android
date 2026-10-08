package com.example.data

import org.json.JSONArray
import org.json.JSONObject

data class KeyboardLayout(
    val id: String, // "K1", "K2", ... "K16"
    val name: String,
    val layers: Map<String, List<ZoneData>> // "Base", "Shift", "Alt", "Ctrl", "Fn"
) {
    fun getZonesForModifier(modifier: String): List<ZoneData> {
        return layers[modifier] ?: layers["Base"] ?: DefaultProfiles.jsonToZones(DefaultProfiles.getDefaultZonesJson())
    }

    fun withUpdatedZones(modifier: String, zones: List<ZoneData>): KeyboardLayout {
        val updatedMap = layers.toMutableMap()
        updatedMap[modifier] = zones
        return copy(layers = updatedMap)
    }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        val layersObj = JSONObject()
        layers.forEach { (mod, zList) ->
            layersObj.put(mod, JSONArray(DefaultProfiles.zonesToJson(zList)))
        }
        obj.put("layers", layersObj)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): KeyboardLayout {
            val id = obj.optString("id", "K1")
            val name = obj.optString("name", "Layout $id")
            val layersMap = mutableMapOf<String, List<ZoneData>>()

            if (obj.has("layers")) {
                val layersObj = obj.getJSONObject("layers")
                val keys = layersObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val arrStr = layersObj.getJSONArray(key).toString()
                    layersMap[key] = DefaultProfiles.jsonToZones(arrStr)
                }
            } else if (obj.has("zones")) {
                // legacy format
                val arrStr = obj.getJSONArray("zones").toString()
                layersMap["Base"] = DefaultProfiles.jsonToZones(arrStr)
            }

            if (!layersMap.containsKey("Base")) {
                layersMap["Base"] = DefaultProfiles.jsonToZones(DefaultProfiles.getDefaultZonesJson())
            }
            return KeyboardLayout(id, name, layersMap)
        }

        fun createDefaultLayouts(): List<KeyboardLayout> {
            val baseZones = DefaultProfiles.jsonToZones(DefaultProfiles.getDefaultZonesJson())
            val shiftZones = baseZones.map { zone ->
                zone.copy(symbols = zone.symbols.map { s -> if (s.length == 1 && s[0].isLetter()) s.uppercase() else s })
            }
            val altZones = baseZones.map { zone ->
                zone.copy(symbols = zone.symbols.map { s ->
                    when (s) {
                        "e" -> "€"; "t" -> "~"; "a" -> "@"; "o" -> "ø"; "i" -> "!"; "n" -> "ñ"; "s" -> "ß"; "r" -> "®"
                        else -> s
                    }
                })
            }
            val ctrlZones = baseZones.map { zone ->
                zone.copy(symbols = zone.symbols.map { s -> "^$s" })
            }
            val fnZones = DefaultProfiles.jsonToZones(DefaultProfiles.getGamingZonesJson())

            val defaultLayers = mapOf(
                "Base" to baseZones,
                "Shift" to shiftZones,
                "Alt" to altZones,
                "Ctrl" to ctrlZones,
                "Fn" to fnZones
            )

            val gamingLayers = mapOf(
                "Base" to fnZones,
                "Shift" to shiftZones,
                "Alt" to altZones,
                "Ctrl" to ctrlZones,
                "Fn" to baseZones
            )

            val list = mutableListOf<KeyboardLayout>()
            list.add(KeyboardLayout("K1", "MessagEase Core", defaultLayers))
            list.add(KeyboardLayout("K2", "Gaming Tactical", gamingLayers))

            // Pre-seed K3..K16 so user has 16 quick customizable slots!
            for (i in 3..16) {
                list.add(KeyboardLayout("K$i", "Custom Layout K$i", defaultLayers))
            }
            return list
        }

        fun layoutsToJson(list: List<KeyboardLayout>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun jsonToLayouts(jsonStr: String): List<KeyboardLayout> {
            return try {
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<KeyboardLayout>()
                for (i in 0 until arr.length()) {
                    list.add(fromJson(arr.getJSONObject(i)))
                }
                if (list.isEmpty()) createDefaultLayouts() else list
            } catch (_: Exception) {
                createDefaultLayouts()
            }
        }
    }
}
