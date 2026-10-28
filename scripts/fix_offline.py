from pathlib import Path

# Detail CacheButton
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\detail\DetailScreen.kt")
c = p.read_text(encoding="utf-8")
old = """@Composable
private fun CacheButton(tower: Tower?) {
    if (tower == null) return
    val context = LocalContext.current
    val store = remember { TowerStore.get(context) }
    val scope = rememberCoroutineScope()
    var cached by remember(tower.name) { mutableStateOf(false) }
    LaunchedEffect(tower.name) {
        cached = store.isCached(tower.name)
    }
    TextButton(onClick = {
        scope.launch {
            if (cached) {
                store.removeCache(tower.name)
                cached = false
            } else {
                store.cacheTower(tower)
                cached = true
            }
        }
    }) {
        Text(if (cached) "删除缓存" else "缓存")
    }
}"""
new = """@Composable
private fun CacheButton(tower: Tower?) {
    if (tower == null) return
    val context = LocalContext.current
    val store = remember { TowerStore.get(context) }
    val scope = rememberCoroutineScope()
    var cached by remember(tower.name) { mutableStateOf(false) }
    var sizeLabel by remember(tower.name) { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(tower.name) {
        cached = com.mobiletaplay.app.data.GameCache.isCached(context, tower.name)
        val bytes = com.mobiletaplay.app.data.GameCache.sizeBytes(context, tower.name)
        sizeLabel = if (cached) "(" + com.mobiletaplay.app.data.GameCache.formatSize(bytes) + ")" else ""
    }
    TextButton(enabled = !busy, onClick = {
        scope.launch {
            busy = true
            try {
                if (cached) {
                    com.mobiletaplay.app.data.GameCache.delete(context, tower.name)
                    store.removeCache(tower.name)
                } else {
                    store.cacheTower(tower)
                    val path = tower.link?.takeIf { it.startsWith("/") } ?: "/games/" + tower.name + "/"
                    com.mobiletaplay.app.data.GameCache.download(context, "https://h5mota.com" + path, tower.name)
                }
                cached = com.mobiletaplay.app.data.GameCache.isCached(context, tower.name)
                val bytes = com.mobiletaplay.app.data.GameCache.sizeBytes(context, tower.name)
                sizeLabel = if (cached) "(" + com.mobiletaplay.app.data.GameCache.formatSize(bytes) + ")" else ""
            } finally {
                busy = false
            }
        }
    }) {
        Text(if (busy) "…" else if (cached) "删除缓存" + sizeLabel else "缓存")
    }
}"""
if old in c:
    c = c.replace(old, new)
    print("btn ok")
else:
    print("btn missing")
p.write_text(c, encoding="utf-8")

# GameActivity offline loader
p2 = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\game\GameActivity.kt")
c2 = p2.read_text(encoding="utf-8")
if "GameCache" not in c2:
    c2 = c2.replace(
        "import com.mobiletaplay.app.data.SettingsRepository",
        "import com.mobiletaplay.app.data.GameCache\nimport com.mobiletaplay.app.data.SettingsRepository",
    )
old2 = "        webView.loadUrl(url)"
new2 = """        GameCache.installOfflineLoader(webView, this)
        val name = try {
            val u = java.net.URI(url)
            u.path.split('/').filter { it.isNotBlank() }.getOrNull(2) ?: ""
        } catch (_: Exception) {
            ""
        }
        val offline = name.isNotEmpty() && GameCache.isCached(this, name)
        webView.loadUrl(if (offline) GameCache.localUrl(name) else url)"""
if old2 in c2:
    c2 = c2.replace(old2, new2)
    print("game ok")
else:
    print("loadUrl missing")
p2.write_text(c2, encoding="utf-8")
