package com.mobiletaplay.app.input

import android.view.KeyEvent

/** All bindable game operations (left) → default pad key (label). */
val ControlNames = linkedMapOf(
    "dpad_up" to "上",
    "dpad_down" to "下",
    "dpad_left" to "左",
    "dpad_right" to "右",
    "button_a" to "确认/轻按",
    "button_b" to "菜单",
    "button_x" to "楼传",
    "button_y" to "虚拟键盘",
    "lb" to "存档",
    "rb" to "读档",
    "lt" to "道具",
    "rt" to "装备",
    "start" to "读自动档",
    "select" to "全屏",
    "k_book" to "怪物手册",
    "k_shop" to "快捷商店",
    "k_tool" to "道具栏",
    "k_equip" to "装备栏",
    "k_stat" to "数据统计",
    "k_help" to "帮助",
    "k_replay" to "录像",
    "k_turn" to "转向",
    "k_fly" to "楼传键",
    "k_cursor" to "光标",
    "k_restart" to "重新开始",
    "k_note" to "存档笔记",
    "k_item1" to "道具1 破",
    "k_item2" to "道具2 炸",
    "k_item3" to "道具3 飞",
)

/** KEYCODE → (mota key, display label). */
fun bindFromEvent(keyCode: Int): Pair<String, String>? = when (keyCode) {
    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> "ArrowUp" to "上"
    KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> "ArrowDown" to "下"
    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> "ArrowLeft" to "左"
    KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> "ArrowRight" to "右"
    KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER -> "Space" to "A"
    KeyEvent.KEYCODE_BUTTON_B -> "Escape" to "B"
    KeyEvent.KEYCODE_BUTTON_X -> "g" to "X"
    KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_Y -> "y" to "Y"
    KeyEvent.KEYCODE_BUTTON_L1 -> "s" to "L1"
    KeyEvent.KEYCODE_BUTTON_R1 -> "d" to "R1"
    KeyEvent.KEYCODE_BUTTON_L2 -> "t" to "L2"
    KeyEvent.KEYCODE_BUTTON_R2 -> "q" to "R2"
    KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU -> "a" to "Start"
    KeyEvent.KEYCODE_BUTTON_SELECT -> "fullscreen" to "Select"
    KeyEvent.KEYCODE_SPACE -> "Space" to "空格"
    KeyEvent.KEYCODE_ESCAPE -> "Escape" to "Esc"
    else -> {
        if (keyCode in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z) {
            val ch = (keyCode - KeyEvent.KEYCODE_A + 'a'.code).toChar().toString()
            ch to ch.uppercase()
        } else null
    }
}

fun keyNameFromEvent(keyCode: Int): String = bindFromEvent(keyCode)?.first ?: ""