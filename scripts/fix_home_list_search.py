from pathlib import Path
import re

# --- HomeScreen: single gridState + cacheTower on open ---
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\home\HomeScreen.kt")
c = p.read_text(encoding="utf-8")

# TowerGrid should accept gridState from parent
c = c.replace(
"""private fun TowerGrid(
    towers: List<Tower>,
    favorites: Set<String>,
    loading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onOpen: (Tower) -> Unit,
    onFav: (String) -> Unit,
) {
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(""",
"""private fun TowerGrid(
    towers: List<Tower>,
    favorites: Set<String>,
    loading: Boolean,
    hasMore: Boolean,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onLoadMore: () -> Unit,
    onOpen: (Tower) -> Unit,
    onFav: (String) -> Unit,
) {
    LazyVerticalGrid(""",
)

# call site pass gridState
c = c.replace(
"""                    else -> TowerGrid(
                        towers = state.towers,
                        favorites = state.favorites,
                        loading = state.loading,
                        hasMore = state.hasMore,
                        onLoadMore = { vm.loadMore() },
                        onOpen = { t -> vm.onOpen(t); onOpenDetail(t.name) },
                        onFav = { vm.toggleFavorite(it) },
                    )""",
"""                    else -> TowerGrid(
                        towers = state.towers,
                        favorites = state.favorites,
                        loading = state.loading,
                        hasMore = state.hasMore,
                        gridState = gridState,
                        onLoadMore = { vm.loadMore() },
                        onOpen = { t -> vm.onOpen(t); onOpenDetail(t.name) },
                        onFav = { vm.toggleFavorite(it) },
                    )""",
)

# onOpen cacheTower
c = c.replace(
"""    fun onOpen(t: Tower) {
        viewModelScope.launch { recentRepo.pushRecent(t.name) }
    }""",
"""    fun onOpen(t: Tower) {
        viewModelScope.launch {
            recentRepo.cacheTower(t)
            recentRepo.pushRecent(t.name)
            _ui.value = _ui.value.copy(recentNames = recentRepo.recent())
        }
    }""",
)
p.write_text(c, encoding="utf-8")
print("home ok")

# --- ListViewModel: cacheTower on open already; resolve fallback fetch by name ---
p2 = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\list\ListScreen.kt")
c2 = p2.read_text(encoding="utf-8")
c2 = c2.replace(
"""    private suspend fun resolve(names: List<String>): List<Tower> {
        val out = mutableListOf<Tower>()
        names.forEach { n ->
            store.snapshot(n)?.let { out.add(it) }
        }
        return out
    }""",
"""    private suspend fun resolve(names: List<String>): List<Tower> {
        val out = mutableListOf<Tower>()
        names.forEach { n ->
            val snap = store.snapshot(n)
            if (snap != null) out.add(snap)
            else {
                // fallback: find in hot pages
                run page@{
                    for (pg in 1..5) {
                        val res = TowerApi.fetchTowers(page = pg, sortmode = "hot")
                        val hit = res.towers.firstOrNull { it.name == n }
                        if (hit != null) {
                            store.cacheTower(hit)
                            out.add(hit)
                            return@page
                        }
                    }
                }
            }
        }
        return out
    }""",
)
p2.write_text(c2, encoding="utf-8")
print("list ok")

# --- MainActivity search route decode ---
p3 = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\MainActivity.kt")
c3 = p3.read_text(encoding="utf-8")
# ensure onApply uses Uri.encode and listscreen decodes
if "Uri.encode(q.keyword)" in c3:
    print("search encode already")
else:
    print("search encode missing?")
p3.write_text(c3, encoding="utf-8")
