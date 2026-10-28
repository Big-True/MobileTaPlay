package com.mobiletaplay.app.input

import org.json.JSONObject

/** Stable JSON codec for action→padButton map. */
object MappingCodec {
    fun encode(map: Map<String, String>): String {
        val o = JSONObject()
        map.forEach { (k, v) -> o.put(k, v) }
        return o.toString()
    }

    fun decode(json: String?): Map<String, String> {
        if (json.isNullOrBlank()) return PadActions.defaults()
        return try {
            val o = JSONObject(json)
            val out = PadActions.defaults().toMutableMap()
            for (key in PadActions.actions.map { it.id }) {
                val v = o.optString(key)
                if (v.isNotEmpty()) out[key] = v
            }
            out
        } catch (_: Exception) {
            PadActions.defaults()
        }
    }
}
