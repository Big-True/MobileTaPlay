package com.mobiletaplay.app.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MappingCodecTest {
    @Test
    fun encodeDecodeRoundTrip() {
        val map = linkedMapOf("confirm" to "A", "save" to "L1", "vkey" to "Y")
        val json = MappingCodec.encode(map)
        val back = MappingCodec.decode(json)
        assertEquals("A", back["confirm"])
        assertEquals("L1", back["save"])
        assertEquals("Y", back["vkey"])
    }

    @Test
    fun decodeEmptyUsesDefaults() {
        val back = MappingCodec.decode(null)
        assertEquals("A", back["confirm"])
        assertEquals("Y", back["vkey"])
    }

    @Test
    fun padNameMapsButtonA() {
        assertEquals("A", PadActions.padName(android.view.KeyEvent.KEYCODE_BUTTON_A))
    }
}
