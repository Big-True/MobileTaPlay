package com.mobiletaplay.app.input

import org.json.JSONObject

/**
 * Mutable mapping loaded from settings JSON; GameActivity applies it live.
 */
object MappingStore {
    data class Binding(val key: String)

    private val defaults = linkedMapOf(
        "dpad_up" to Binding("ArrowUp"),
        "dpad_down" to Binding("ArrowDown"),
        "dpad_left" to Binding("ArrowLeft"),
        "dpad_right" to Binding("ArrowRight"),
        "button_a" to Binding("Space"),
        "button_b" to Binding("Escape"),
        "button_x" to Binding("g"),
        "button_y" to Binding(""),
        "lb" to Binding("s"),
        "rb" to Binding("d"),
        "lt" to Binding("t"),
        "rt" to Binding("q"),
        "start" to Binding("a"),
        "select" to Binding("fullscreen"),
        "k_book" to Binding(""),
        "k_shop" to Binding(""),
        "k_tool" to Binding(""),
        "k_equip" to Binding(""),
        "k_stat" to Binding(""),
        "k_help" to Binding(""),
        "k_replay" to Binding(""),
        "k_turn" to Binding(""),
        "k_fly" to Binding(""),
        "k_cursor" to Binding(""),
        "k_restart" to Binding(""),
        "k_note" to Binding(""),
        "k_item1" to Binding(""),
        "k_item2" to Binding(""),
        "k_item3" to Binding(""),
    )

    private val KEY_CODE = mapOf(
        "ArrowLeft" to 37, "ArrowUp" to 38, "ArrowRight" to 39, "ArrowDown" to 40,
        "Enter" to 13, "Space" to 32, "Escape" to 27, "Backspace" to 8, "Tab" to 9,
        "1" to 49, "2" to 50, "3" to 51, "4" to 52, "5" to 53, "6" to 54, "7" to 55,
        "a" to 65, "b" to 66, "d" to 68, "e" to 69, "f" to 70, "g" to 71, "h" to 72,
        "m" to 77, "n" to 78, "q" to 81, "r" to 82, "s" to 83, "t" to 84,
        "v" to 86, "w" to 87, "x" to 88, "y" to 89, "z" to 90,
    )

    @Volatile
    private var current: Map<String, Binding> = defaults.toMap()

    val keys: List<String> get() = defaults.keys.toList()

    fun binding(control: String): Binding = current[control] ?: Binding("")

    fun mapAll(): Map<String, Binding> = current

    fun defaultsSnapshot(): Map<String, Binding> = defaults.toMap()

    /** Resolve a key name (e.g. "Space") to mota keyCode. */
    fun codeForKey(key: String): Int {
        if (key.isEmpty()) return 0
        KEY_CODE[key]?.let { return it }
        return if (key.length == 1) key[0].uppercaseChar().code else 0
    }

    fun keyCode(control: String): Int {
        return codeForKey(binding(control).key)
    }

    fun applyJson(json: String?) {
        if (json.isNullOrBlank()) {
            current = defaults.toMap()
            return
        }
        try {
            val o = JSONObject(json)
            val next = defaults.toMutableMap()
            for (k in defaults.keys) {
                val key = o.optString(k)
                if (key.isNotEmpty()) next[k] = Binding(key)
            }
            current = next
        } catch (_: Exception) {
            current = defaults.toMap()
        }
    }

    fun toJson(): String {
        val o = JSONObject()
        current.forEach { (k, v) -> o.put(k, v.key) }
        return o.toString()
    }

    fun set(control: String, key: String) {
        val m = current.toMutableMap()
        m[control] = Binding(key)
        current = m
    }

    fun reset() {
        current = defaults.toMap()
    }
}
