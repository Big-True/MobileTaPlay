from pathlib import Path

# Fix TowerStore snapshot constructor
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\data\TowerStore.kt")
c = p.read_text(encoding="utf-8")
# remove tags from previews line if duplicated
c = c.replace(
    'previews = parseArr(o.optString("previews")), tags = parseArr(o.optString("tags")),',
    'previews = parseArr(o.optString("previews")),',
)
# ensure single tags assignment exists in constructor
if "tags = parseArr" not in c:
    c = c.replace(
        "                link = o.optString(\"link\").ifBlank { null },",
        "                link = o.optString(\"link\").ifBlank { null },\n                tags = parseArr(o.optString(\"tags\")),",
    )
p.write_text(c, encoding="utf-8")

# Detail cache button
p2 = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\detail\DetailScreen.kt")
c2 = p2.read_text(encoding="utf-8")
if "删除缓存" not in c2:
    c2 = c2.replace(
        "import com.mobiletaplay.app.data.Tower",
        "import com.mobiletaplay.app.data.Tower\nimport com.mobiletaplay.app.data.TowerStore",
    )
    c2 = c2.replace(
        """                actions = {
                    IconButton(onClick = { toggleActivityOrientation(activity) }) {
                        Icon(Icons.Default.ScreenRotation, contentDescription = "横竖屏")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },""",
        """                actions = {
                    IconButton(onClick = { toggleActivityOrientation(activity) }) {
                        Icon(Icons.Default.ScreenRotation, contentDescription = "横竖屏")
                    }
                    CacheButton(tower = tower)
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },""",
    )
    c2 += """
@Composable
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
}
"""
    if "rememberCoroutineScope" not in c2:
        c2 = c2.replace(
            "import androidx.compose.runtime.mutableStateOf",
            "import androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.rememberCoroutineScope",
        )
p2.write_text(c2, encoding="utf-8")
print("detail ok")
