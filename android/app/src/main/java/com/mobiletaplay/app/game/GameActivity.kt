package com.mobiletaplay.app.game

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mobiletaplay.app.OrientationPrefs
import com.mobiletaplay.app.data.GameCache
import com.mobiletaplay.app.data.SettingsRepository
import com.mobiletaplay.app.input.GamepadMapper
import com.mobiletaplay.app.input.InjectJs
import com.mobiletaplay.app.input.MappingStore
import com.mobiletaplay.app.input.PadActions
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
class GameActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var gameOverlay: GameOverlayView
    private var immersive = false
    private val stickHeld = mutableSetOf<String>()
    private lateinit var injector: InputInjector

    companion object {
        fun intent(ctx: Context, url: String, title: String): Intent =
            Intent(ctx, GameActivity::class.java)
                .putExtra("url", url)
                .putExtra("title", title)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OrientationPrefs.apply(this)
        PadActions.reset()
        MappingStore.reset()
        val url = intent.getStringExtra("url") ?: "https://h5mota.com/"
        val title = intent.getStringExtra("title") ?: "MobileTaPlay"

        webView = WebViewPool.acquire(this)
        webView.setBackgroundColor(Color.parseColor("#0F1419"))
        injector = InputInjector(webView)
        injector.onInjectFailed = {
            android.widget.Toast.makeText(this, "输入桥注入失败，请重进游戏", android.widget.Toast.LENGTH_LONG).show()
        }
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, u: String?) {
                super.onPageFinished(view, u)
                injectBridge()
            }
        }

        gameOverlay = GameOverlayView(
            context = this,
            title = title,
            onBack = { handleBack() },
            onRotate = { toggleOrientation() },
            onToggleImmersive = { toggleImmersive() },
            onSettings = { showSettingsDialog() },
            onSendKeyCode = { code, down -> sendKeyCode(code, down) },
        )

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#0F1419"))
            addView(
                webView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                ),
            )
            addView(gameOverlay)
        }
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            webView.defaultFocusHighlightEnabled = false
            root.defaultFocusHighlightEnabled = false
            root.isFocusable = true
            root.isFocusableInTouchMode = true
        }
        setContentView(root)
        OrientationPrefs.apply(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = handleBack()
        })

        lifecycleScope.launch {
            try {
                val s = SettingsRepository.get(this@GameActivity).current()
                gameOverlay.setPadVisible(s.virtualPad)
                PadActions.applyMap(com.mobiletaplay.app.input.MappingCodec.decode(s.mappingJson))
            } catch (_: Exception) {
            }
        }

        applyImmersive(false)
        applyChromeInsets()
        GameCache.installOfflineLoader(webView, this)
        val name = try {
            val u = java.net.URI(url)
            u.path.split('/').filter { it.isNotBlank() }.getOrNull(2) ?: ""
        } catch (_: Exception) {
            ""
        }
        val offline = name.isNotEmpty() && GameCache.isCached(this, name)
        webView.loadUrl(if (offline) GameCache.localUrl(name) else url)
    }

    private fun handleBack() {
        if (::gameOverlay.isInitialized && gameOverlay.isKeyboardVisible()) {
            gameOverlay.closeKeyboard()
            return
        }
        if (immersive) {
            toggleImmersive()
            return
        }
        if (webView.canGoBack()) webView.goBack() else finish()
    }

    private fun toggleOrientation() {
        OrientationPrefs.toggle(this)
    }

    private fun showSettingsDialog() {
        val dialog = android.app.Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        val compose = androidx.compose.ui.platform.ComposeView(this)
        val owner = this
        compose.setViewTreeLifecycleOwner(owner)
        compose.setViewTreeSavedStateRegistryOwner(owner)
        compose.setViewTreeViewModelStoreOwner(owner)
        compose.setContent {
            androidx.compose.material3.MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme()
            ) {
                com.mobiletaplay.app.ui.settings.SettingsScreen(onBack = { dialog.dismiss() })
            }
        }
        dialog.setContentView(compose)
        try {
            dialog.show()
        } catch (e: Exception) {
        }
    }

    private fun sendKeyCode(code: Int, down: Boolean) {
        if (code == 0) return
        injector.sendKey(code, down)
    }

    /** Virtual keyboard navigation from stick/hat when keyboard is open. */
    private fun keyboardNav(control: String) {
        when (control) {
            "dpad_up" -> gameOverlay.moveKeyboardSelection(0, -1)
            "dpad_down" -> gameOverlay.moveKeyboardSelection(0, 1)
            "dpad_left" -> gameOverlay.moveKeyboardSelection(-1, 0)
            "dpad_right" -> gameOverlay.moveKeyboardSelection(1, 0)
        }
    }

    override fun dispatchGenericMotionEvent(event: android.view.MotionEvent?): Boolean {
        val e = event ?: return super.dispatchGenericMotionEvent(null)
        if (e.source and android.view.InputDevice.SOURCE_JOYSTICK == 0) {
            return super.dispatchGenericMotionEvent(e)
        }
        val hatX = e.getAxisValue(android.view.MotionEvent.AXIS_HAT_X)
        val hatY = e.getAxisValue(android.view.MotionEvent.AXIS_HAT_Y)
        val x = e.getAxisValue(android.view.MotionEvent.AXIS_X)
        val y = e.getAxisValue(android.view.MotionEvent.AXIS_Y)
        val dz = 0.35f
        val states = listOf(
            "dpad_left" to (hatX < -0.5f || x < -dz),
            "dpad_right" to (hatX > 0.5f || x > dz),
            "dpad_up" to (hatY < -0.5f || y < -dz),
            "dpad_down" to (hatY > 0.5f || y > dz),
        )
        val kbMode = gameOverlay.isKeyboardVisible()
        var handled = false
        for ((control, active) in states) {
            val key = "stick_" + control
            val was = stickHeld.contains(key)
            if (active && !was) {
                stickHeld.add(key)
                if (kbMode) keyboardNav(control) else sendKeyCode(MappingStore.keyCode(control), true)
                handled = true
            } else if (!active && was) {
                stickHeld.remove(key)
                if (!kbMode) sendKeyCode(MappingStore.keyCode(control), false)
                handled = true
            }
        }
        return if (handled) true else super.dispatchGenericMotionEvent(e)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val down = event.action == KeyEvent.ACTION_DOWN
        val up = event.action == KeyEvent.ACTION_UP
        if (!down && !up) return super.dispatchKeyEvent(event)

        // Virtual keyboard mode: pad controls keyboard only
        if (gameOverlay.isKeyboardVisible()) {
            val pad = PadActions.padName(event.keyCode)
            when {
                !down -> return true
                pad == "A" -> { gameOverlay.confirmKeyboard(); return true }
                pad == "B" -> { gameOverlay.closeKeyboard(); return true }
                pad == "X" -> { gameOverlay.toggleFnLabels(); return true }
                pad == "↑" -> { gameOverlay.moveKeyboardSelection(0, -1); return true }
                pad == "↓" -> { gameOverlay.moveKeyboardSelection(0, 1); return true }
                pad == "←" -> { gameOverlay.moveKeyboardSelection(-1, 0); return true }
                pad == "→" -> { gameOverlay.moveKeyboardSelection(1, 0); return true }
                else -> return true
            }
        }

        val padBtn = PadActions.padName(event.keyCode)
        if (padBtn != null) {
            val action = PadActions.actionForButton(padBtn)
            if (down) {
                if (event.repeatCount == 0) {
                    when {
                        action == "vkey" -> { gameOverlay.toggleKeyboard(); return true }
                        action == "fs" -> { toggleImmersive(); return true }
                        action != null -> {
                            val code = PadActions.keyCodeForAction(action)
                            if (code != 0) {
                                sendKeyCode(code, true)
                                stickHeld.add("pad_" + padBtn)
                            }
                            return true
                        }
                        else -> {
                            // fallback standard keys
                            val fb = when (padBtn) {
                                "A" -> 32
                                "B" -> 27
                                "X" -> 71
                                "L1" -> 83
                                "R1" -> 68
                                "L2" -> 84
                                "R2" -> 81
                                "Start" -> 65
                                "↑" -> 38
                                "↓" -> 40
                                "←" -> 37
                                "→" -> 39
                                else -> 0
                            }
                            if (fb != 0) {
                                sendKeyCode(fb, true)
                                stickHeld.add("pad_" + padBtn)
                            }
                            return true
                        }
                    }
                } else {
                    return true
                }
            } else {
                if (stickHeld.remove("pad_" + padBtn)) {
                    val a = PadActions.actionForButton(padBtn)
                    if (a != null && a != "vkey" && a != "fs") {
                        sendKeyCode(PadActions.keyCodeForAction(a), false)
                    }
                }
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    private fun toggleImmersive() {
        immersive = !immersive
        applyImmersive(immersive)
        gameOverlay.setBarVisible(!immersive)
    }

    private fun applyImmersive(on: Boolean) {
        val w = window
        val controller = WindowCompat.getInsetsController(w, w.decorView)
        if (on) {
            WindowCompat.setDecorFitsSystemWindows(w, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            WindowCompat.setDecorFitsSystemWindows(w, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
            w.statusBarColor = Color.parseColor("#0F1419")
            w.navigationBarColor = Color.parseColor("#0F1419")
        }
        applyChromeInsets()
    }

    private fun applyChromeInsets() {
        val density = resources.displayMetrics.density
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusH = if (id > 0) resources.getDimensionPixelSize(id) else (24 * density).toInt()
        val barH = ((40 + 28 + 20) * density).toInt()
        val top = if (immersive) 0 else statusH + barH
        val lp = webView.layoutParams as? FrameLayout.LayoutParams
        if (lp != null) {
            lp.topMargin = top
            webView.layoutParams = lp
        }
    }

    private fun injectBridge() {
        injector.injectBridge()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        OrientationPrefs.apply(this)
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        (webView.parent as? android.view.ViewGroup)?.removeView(webView)
        super.onDestroy()
    }
}
