package com.mobiletaplay.app.data

import android.content.Context
import android.webkit.MimeTypeMap
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Download a mota-js game tree (/games/<id>/) for offline play and serve it
 * through WebViewAssetLoader.
 */
object GameCache {

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val assetExt = setOf(
        "js", "css", "png", "jpg", "jpeg", "gif", "webp", "svg",
        "mp3", "ogg", "wav", "m4a", "json", "txt", "ttf", "woff", "woff2",
        "html", "htm",
    )

    fun gameDir(context: Context, name: String): File =
        File(context.filesDir, "games/$name")

    fun isCached(context: Context, name: String): Boolean =
        File(gameDir(context, name), "index.html").exists()

    fun sizeBytes(context: Context, name: String): Long {
        val root = gameDir(context, name)
        if (!root.exists()) return 0L
        return root.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.0f KB", kb)
        val mb = kb / 1024.0
        return String.format("%.1f MB", mb)
    }

    fun delete(context: Context, name: String) {
        gameDir(context, name).deleteRecursively()
    }

    /** Download index + linked assets. Returns total bytes. */
    suspend fun download(context: Context, gamePath: String, name: String): Long =
        withContext(Dispatchers.IO) {
            val base = if (gamePath.endsWith("/")) gamePath else "$gamePath/"
            val root = gameDir(context, name)
            root.mkdirs()
            val seen = mutableSetOf<String>()

            fun shouldFetch(url: String): Boolean {
                val path = url.substringAfterLast("?").substringAfterLast("#")
                val ext = path.substringAfterLast(".", "").lowercase()
                return ext in assetExt
            }

            suspend fun fetchRel(rel: String) {
                val key = rel.trim().trimStart('/')
                if (!shouldFetch(key) || key in seen || key.contains("..")) return
                seen.add(key)
                val url = base + key
                try {
                    val req = okhttp3.Request.Builder().url(url).build()
                    http.newCall(req).execute().use { res ->
                        if (!res.isSuccessful) return
                        val body = res.body?.bytes() ?: return
                        val out = File(root, key)
                        out.parentFile?.mkdirs()
                        out.writeBytes(body)
                        val text = if (key.endsWith(".html") || key.endsWith(".js") || key.endsWith(".css"))
                            body.toString(Charsets.UTF_8) else null
                        if (text != null) {
                            val re = Regex("""(?:src|href)=["']([^"']+)["']""")
                            re.findAll(text).forEach { m ->
                                var u = m.groupValues[1].trim()
                                if (u.startsWith("http") || u.startsWith("data:")) return@forEach
                                if (u.startsWith("./")) u = u.removePrefix("./")
                                if (u.startsWith("/")) u = u.trimStart('/')
                                if (!u.contains("://")) fetchRel(u)
                            }
                            val re2 = Regex("""["']((?:[\w./-]+)\.(?:js|css|png|jpg|jpeg|gif|webp|svg|mp3|ogg|wav|json|ttf|woff2?))["']""")
                            re2.findAll(text).forEach { m ->
                                var u = m.groupValues[1].trim()
                                if (u.startsWith("http") || u.startsWith("data:")) return@forEach
                                if (u.startsWith("./")) u = u.removePrefix("./")
                                if (!u.contains("://")) fetchRel(u)
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }

            fetchRel("index.html")
            // common mota-js entry points
            listOf("main.js", "styles.css", "style.css", "libs/thirdparty/jquery.js").forEach { fetchRel(it) }
            sizeBytes(context, name)
        }

    /** Intercept HTTPS appassets host and serve from filesDir/games/<name>/ */
    fun installOfflineLoader(webView: WebView, context: Context) {
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler(
                "/games/",
                object : WebViewAssetLoader.PathHandler {
                    override fun handle(path: String): WebResourceResponse {
                        val file = File(context.filesDir, "games/$path")
                        if (!file.exists() || !file.canonicalPath.startsWith(File(context.filesDir, "games").canonicalPath)) {
                            return WebResourceResponse("text/plain", "utf-8", 404, "Not Found", emptyMap(), null)
                        }
                        val mime = guessMime(file.name)
                        return WebResourceResponse(mime, "utf-8", 200, "OK", emptyMap(), file.inputStream())
                    }
                },
            )
            .setDomain("appassets.androidplatform.net")
            .build()

        val current = webView.webViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: android.webkit.WebResourceRequest?): WebResourceResponse? {
                val url = request?.url ?: return super.shouldInterceptRequest(view, request)
                if (url.host == "appassets.androidplatform.net") {
                    return loader.shouldInterceptRequest(url)
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                return current.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, u: String?) {
                current.onPageFinished(view, u)
            }
        }
    }

    fun localUrl(name: String): String =
        "https://appassets.androidplatform.net/games/$name/index.html"

    private fun guessMime(fileName: String): String {
        val ext = fileName.substringAfterLast(".", "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
