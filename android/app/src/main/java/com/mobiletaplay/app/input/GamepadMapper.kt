package com.mobiletaplay.app.input

import android.view.KeyEvent

/** Android key → control id (single place). */
object GamepadMapper {
    fun fromAndroidKey(keyCode: Int): String? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> "dpad_up"
        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> "dpad_down"
        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> "dpad_left"
        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> "dpad_right"
        KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_SPACE -> "button_a"
        KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_B -> "button_b"
        KeyEvent.KEYCODE_BUTTON_X -> "button_x"
        KeyEvent.KEYCODE_BUTTON_Y -> "button_y"
        KeyEvent.KEYCODE_BUTTON_L1 -> "lb"
        KeyEvent.KEYCODE_BUTTON_R1 -> "rb"
        KeyEvent.KEYCODE_BUTTON_L2 -> "lt"
        KeyEvent.KEYCODE_BUTTON_R2 -> "rt"
        KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU -> "start"
        KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BACK -> "select"
        else -> null
    }

    fun controlToKeyCode(control: String): Int = MappingStore.keyCode(control)
}
