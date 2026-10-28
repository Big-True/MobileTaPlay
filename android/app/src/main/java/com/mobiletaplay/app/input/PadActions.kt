package com.mobiletaplay.app.input

/**
 * Action-centric mapping: each game action is bound to one pad button.
 * Special actions: "virtual" (virtual keyboard), "fullscreen".
 */
object PadActions {

    data class ActionDef(
        val id: String,
        val label: String,
        /** mota key or special: virtual | fullscreen | "" */
        val defaultKey: String,
    )

    val actions = listOf(
        ActionDef("up", "上", "ArrowUp"),
        ActionDef("down", "下", "ArrowDown"),
        ActionDef("left", "左", "ArrowLeft"),
        ActionDef("right", "右", "ArrowRight"),
        ActionDef("confirm", "确认/轻按", "Space"),
        ActionDef("menu", "菜单", "Escape"),
        ActionDef("book", "怪物手册", "x"),
        ActionDef("fly", "楼传", "g"),
        ActionDef("tool", "道具", "t"),
        ActionDef("equip", "装备", "q"),
        ActionDef("save", "存档", "s"),
        ActionDef("load", "读档", "d"),
        ActionDef("autosave", "读自动档", "a"),
        ActionDef("shop", "快捷商店", "v"),
        ActionDef("turn", "转向", "z"),
        ActionDef("replay", "录像", "r"),
        ActionDef("stat", "数据统计", "b"),
        ActionDef("help", "帮助", "h"),
        ActionDef("cursor", "光标", "e"),
        ActionDef("restart", "重新开始", "n"),
        ActionDef("note", "存档笔记", "m"),
        ActionDef("item1", "道具1 破", "1"),
        ActionDef("item2", "道具2 炸", "2"),
        ActionDef("item3", "道具3 飞", "3"),
        ActionDef("vkey", "虚拟键盘", "virtual"),
        ActionDef("fs", "全屏", "fullscreen"),
    )

    /** Which pad button is assigned to each action (empty = unbound). */
    @Volatile
    private var actionToButton: Map<String, String> = defaults()

    /** Reverse: button → action id */
    private val padButtons = listOf(
        "A", "B", "X", "Y", "L1", "R1", "L2", "R2", "Start", "Select",
        "↑", "↓", "←", "→",
    )

    fun defaults(): Map<String, String> = linkedMapOf(
        "up" to "↑",
        "down" to "↓",
        "left" to "←",
        "right" to "→",
        "confirm" to "A",
        "menu" to "B",
        "book" to "",
        "fly" to "X",
        "tool" to "L2",
        "equip" to "R2",
        "save" to "L1",
        "load" to "R1",
        "autosave" to "Start",
        "shop" to "",
        "turn" to "",
        "replay" to "",
        "stat" to "",
        "help" to "",
        "cursor" to "",
        "restart" to "",
        "note" to "",
        "item1" to "",
        "item2" to "",
        "item3" to "",
        "vkey" to "Y",
        "fs" to "Select",
    )

    fun buttonFor(actionId: String): String = actionToButton[actionId] ?: ""

    fun actionForButton(button: String): String? =
        actionToButton.entries.firstOrNull { it.value == button && button.isNotEmpty() }?.key

    fun setButton(actionId: String, button: String) {
        val m = actionToButton.toMutableMap()
        // one button -> one action
        m.entries.removeAll { it.value == button && it.key != actionId }
        m[actionId] = button
        actionToButton = m
    }

    fun reset() {
        actionToButton = defaults()
    }

    fun applyMap(map: Map<String, String>) {
        val m = defaults().toMutableMap()
        map.forEach { (k, v) -> if (m.containsKey(k) && v.isNotEmpty()) m[k] = v }
        actionToButton = m
    }

    fun all(): Map<String, String> = actionToButton

    fun keyForAction(actionId: String): String =
        actions.firstOrNull { it.id == actionId }?.defaultKey ?: ""

    /** Pad button name from Android keycode. */
    fun padName(keyCode: Int): String? = when (keyCode) {
        android.view.KeyEvent.KEYCODE_BUTTON_A,
        android.view.KeyEvent.KEYCODE_ENTER,
        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
        android.view.KeyEvent.KEYCODE_SPACE -> "A"
        android.view.KeyEvent.KEYCODE_BUTTON_B -> "B"
        android.view.KeyEvent.KEYCODE_BUTTON_X -> "X"
        android.view.KeyEvent.KEYCODE_BUTTON_Y -> "Y"
        android.view.KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
        android.view.KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
        android.view.KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
        android.view.KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
        android.view.KeyEvent.KEYCODE_BUTTON_START, android.view.KeyEvent.KEYCODE_MENU -> "Start"
        android.view.KeyEvent.KEYCODE_BUTTON_SELECT -> "Select"
        android.view.KeyEvent.KEYCODE_DPAD_UP -> "↑"
        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> "↓"
        android.view.KeyEvent.KEYCODE_DPAD_LEFT -> "←"
        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> "→"
        else -> null
    }

    /** mota keyCode for action; 0 if unbound or special. */
    fun keyCodeForAction(actionId: String): Int {
        val k = keyForAction(actionId)
        return MappingStore.codeForKey(k)
    }

    fun isVirtual(actionId: String) = keyForAction(actionId) == "virtual"
    fun isFullscreen(actionId: String) = keyForAction(actionId) == "fullscreen"
}
