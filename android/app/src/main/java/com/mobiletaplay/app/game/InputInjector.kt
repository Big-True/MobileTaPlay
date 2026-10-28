package com.mobiletaplay.app.game

import android.webkit.WebView
import com.mobiletaplay.app.input.InjectJs

/**
 * A1/A2: keyboard injection with result feedback.
 */
class InputInjector(private val webView: WebView) {

    var onInjectFailed: (() -> Unit)? = null

    fun injectBridge() {
        webView.evaluateJavascript(InjectJs.bridge, null)
        webView.evaluateJavascript("!!(window.__MTP__ && window.__MTP__.ready)") { r ->
            val ok = r == "true"
            if (!ok) onInjectFailed?.invoke()
        }
    }

    fun sendKey(keyCode: Int, down: Boolean) {
        if (keyCode == 0) return
        webView.evaluateJavascript(InjectJs.key(keyCode, !down), null)
    }
}
