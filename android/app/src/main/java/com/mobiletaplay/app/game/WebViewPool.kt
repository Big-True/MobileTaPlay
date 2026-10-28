package com.mobiletaplay.app.game

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView

/**
 * Keep one game WebView alive across GameActivity instances
 * so reopening a tower does not re-create the engine from scratch.
 */
object WebViewPool {
    @Volatile
    private var cached: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun acquire(activity: android.app.Activity): WebView {
        val existing = cached
        if (existing != null) {
            val parent = existing.parent as? ViewGroup
            parent?.removeView(existing)
            return existing
        }
        val wv = WebView(activity.applicationContext).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
        }
        cached = wv
        return wv
    }

    fun destroy() {
        try {
            cached?.destroy()
        } catch (_: Exception) {
        }
        cached = null
    }
}
