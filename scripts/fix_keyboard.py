from pathlib import Path
import re

p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\game\GameActivity.kt")
c = p.read_text(encoding="utf-8")
c = re.sub(r'(?m)^\s*android\.util\.Log\.i\("MtpKey".*\n', "", c)

old = """        for ((control, active) in states) {
            val key = "stick_" + control
            val was = stickHeld.contains(key)
            if (active && !was) {
                stickHeld.add(key)
                emitControl(control, true)
                handled = true
            } else if (!active && was) {
                stickHeld.remove(key)
                emitControl(control, false)
                handled = true
            }
        }"""
new = """        val kbMode = gameOverlay.isKeyboardVisible()
        for ((control, active) in states) {
            val key = "stick_" + control
            val was = stickHeld.contains(key)
            if (active && !was) {
                stickHeld.add(key)
                if (kbMode) {
                    when (control) {
                        "dpad_up" -> gameOverlay.moveKeyboardSelection(0, -1)
                        "dpad_down" -> gameOverlay.moveKeyboardSelection(0, 1)
                        "dpad_left" -> gameOverlay.moveKeyboardSelection(-1, 0)
                        "dpad_right" -> gameOverlay.moveKeyboardSelection(1, 0)
                    }
                } else emitControl(control, true)
                handled = true
            } else if (!active && was) {
                stickHeld.remove(key)
                if (!kbMode) emitControl(control, false)
                handled = true
            }
        }"""
if old in c:
    c = c.replace(old, new)
    print("motion ok")
else:
    print("motion block not found")

kb_new = """        if (gameOverlay.isKeyboardVisible()) {
            val kbPad = PadActions.padName(event.keyCode)
            when {
                !down -> return true
                kbPad == "A" -> { gameOverlay.confirmKeyboard(); return true }
                kbPad == "B" || kbPad == "Y" -> { gameOverlay.closeKeyboard(); return true }
                kbPad == "X" -> { gameOverlay.toggleFnLabels(); return true }
                kbPad == "↑" -> { gameOverlay.moveKeyboardSelection(0, -1); return true }
                kbPad == "↓" -> { gameOverlay.moveKeyboardSelection(0, 1); return true }
                kbPad == "←" -> { gameOverlay.moveKeyboardSelection(-1, 0); return true }
                kbPad == "→" -> { gameOverlay.moveKeyboardSelection(1, 0); return true }
                else -> return true
            }
        }

        // Pad buttons"""
c2, n = re.subn(
    r"        if \(gameOverlay\.isKeyboardVisible\(\)\) \{.*?\n        // Pad buttons",
    kb_new,
    c,
    count=1,
    flags=re.S,
)
print("keyboard replace", n)
p.write_text(c2, encoding="utf-8")
print("logs left", "MtpKey" in c2)
