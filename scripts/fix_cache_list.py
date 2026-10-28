from pathlib import Path

# --- ListViewModel: cache mode local only; load from GameCache + snapshots ---
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\list\ListScreen.kt")
c = p.read_text(encoding="utf-8")

c = c.replace(
    "class ListViewModel(\n    private val store: TowerStore,",
    "class ListViewModel(\n    private val context: android.content.Context,\n    private val store: TowerStore,",
)

c = c.replace(
    "                    ListMode.CACHED -> store.cached().mapNotNull { store.snapshot(it) }",
    """                    ListMode.CACHED -> {
                        val gameRoot = com.mobiletaplay.app.data.GameCache.gameDir(context, "").parentFile
                        val names = gameRoot?.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
                        // prefer snapshot; synthesize minimal tower from dir name
                        names.map { n ->
                            store.snapshot(n) ?: Tower(id = n.hashCode(), name = n, title = n, author = "已缓存")
                        }
                    }""",
)

c = c.replace(
    "        return ListViewModel(store, mode, keyword, searchMode, sort) as T",
    "        return ListViewModel(context, store, mode, keyword, searchMode, sort) as T",
)
c = c.replace(
    "    private val store: TowerStore,\n    private val mode: ListMode,\n    private val keyword: String,\n    private val searchMode: String,\n    private val sort: String,\n) : ViewModelProvider.Factory {",
    "    private val context: android.content.Context,\n    private val store: TowerStore,\n    private val mode: ListMode,\n    private val keyword: String,\n    private val searchMode: String,\n    private val sort: String,\n) : ViewModelProvider.Factory {",
)
c = c.replace(
    "        factory = ListVmFactory(TowerStore.get(context), mode, keyword, searchMode, sort),",
    "        factory = ListVmFactory(context, TowerStore.get(context), mode, keyword, searchMode, sort),",
)

p.write_text(c, encoding="utf-8")
print("list ok")

# --- DetailScreen: load from local snapshot first ---
p2 = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\detail\DetailScreen.kt")
c2 = p2.read_text(encoding="utf-8")
old_launched = """    LaunchedEffect(name) {
        try {
            for (p in 1..8) {
                val res = withContext(Dispatchers.IO) {
                    TowerApi.fetchTowers(page = p, sortmode = "hot")
                }
                val hit = res.towers.firstOrNull { it.name == name }
                if (hit != null) {
                    tower = hit
                    return@LaunchedEffect
                }
            }
            error = "未找到该塔"
        } catch (e: Exception) {
            error = e.message ?: "加载失败"
        }
    }"""
new_launched = """    LaunchedEffect(name) {
        try {
            // Local snapshot first (cached list / offline)
            val local = withContext(Dispatchers.IO) {
                TowerStore.get(ctx).snapshot(name)
            }
            if (local != null) {
                tower = local
                return@LaunchedEffect
            }
            for (p in 1..8) {
                val res = withContext(Dispatchers.IO) {
                    TowerApi.fetchTowers(page = p, sortmode = "hot")
                }
                val hit = res.towers.firstOrNull { it.name == name }
                if (hit != null) {
                    tower = hit
                    return@LaunchedEffect
                }
            }
            // last resort: file-only cache dir
            val ctx2 = ctx
            val has = com.mobiletaplay.app.data.GameCache.isCached(ctx2, name)
            if (has) {
                tower = Tower(id = name.hashCode(), name = name, title = name, author = "已缓存")
            } else {
                error = "未找到该塔"
            }
        } catch (e: Exception) {
            error = e.message ?: "加载失败"
        }
    }"""
if old_launched in c2:
    c2 = c2.replace(old_launched, new_launched)
    print("detail ok")
else:
    print("launched block missing")

if "val ctx = LocalContext.current" not in c2:
    c2 = c2.replace(
        "    val activity = LocalContext.current as? Activity",
        "    val ctx = LocalContext.current\n    val activity = LocalContext.current as? Activity",
    )

p2.write_text(c2, encoding="utf-8")
