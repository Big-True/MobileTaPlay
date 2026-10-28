package com.mobiletaplay.app.ui.home

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobiletaplay.app.OrientationPrefs
import com.mobiletaplay.app.data.TowerStore
import com.mobiletaplay.app.data.Tower
import com.mobiletaplay.app.data.TowerApi
import com.mobiletaplay.app.ui.common.ErrorPane
import com.mobiletaplay.app.ui.common.PadIconButton
import com.mobiletaplay.app.ui.common.padFocusRing
import com.mobiletaplay.app.ui.common.rememberShellFocus
import com.mobiletaplay.app.ui.common.shellNav
import com.mobiletaplay.app.ui.search.SearchQuery
import kotlinx.coroutines.launch

fun toggleActivityOrientation(activity: Activity?) {
    if (activity == null) return
    OrientationPrefs.toggle(activity)
}

data class HomeUiState(
    val towers: List<Tower> = emptyList(),
    val total: Int = 0,
    val loading: Boolean = false,
    val error: String = "",
    val sort: String = "hot",
    val search: String = "",
    val searchMode: String = "title",
    val tags: String = "",
    val sections: List<Int> = listOf(0, 1, 2),
    val hasMore: Boolean = true,
    val favorites: Set<String> = emptySet(),
    val recentNames: List<String> = emptyList(),
)

class HomeViewModel(private val recentRepo: TowerStore) : ViewModel() {
    private val _ui = androidx.compose.runtime.mutableStateOf(HomeUiState())
    val ui = _ui
    private var page = 1
    private var loaded = false

    fun loadIfNeeded() {
        if (!loaded && _ui.value.towers.isEmpty() && !_ui.value.loading) {
            loaded = true
            refresh()
        }
    }

    fun refresh() {
        page = 1
        load(true)
    }

    fun loadMore() {
        if (_ui.value.loading || !_ui.value.hasMore) return
        load(false)
    }

    fun setSort(s: String) {
        _ui.value = _ui.value.copy(sort = s)
        refresh()
    }

    fun setSearch(q: String) {
        _ui.value = _ui.value.copy(search = q)
        refresh()
    }

    fun applyQuery(q: SearchQuery) {
        _ui.value = _ui.value.copy(
            search = q.keyword,
            searchMode = q.mode,
            sort = q.sort,
            sections = q.sections.toList(),
            tags = q.tags,
        )
        refresh()
    }

    fun toggleFavorite(name: String) {
        viewModelScope.launch {
            recentRepo.toggleFavorite(name)
            _ui.value = _ui.value.copy(favorites = recentRepo.favorites().toSet())
        }
    }

    fun onOpen(t: Tower) {
        viewModelScope.launch {
            recentRepo.cacheTower(t)
            recentRepo.pushRecent(t.name)
            _ui.value = _ui.value.copy(recentNames = recentRepo.recent())
        }
    }

    fun loadRecentFav() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                favorites = recentRepo.favorites().toSet(),
                recentNames = recentRepo.recent(),
            )
        }
    }

    private fun load(reset: Boolean) {
        val snap = _ui.value
        _ui.value = snap.copy(loading = true, error = "")
        viewModelScope.launch {
            try {
                val p = if (reset) 1 else page
                val tagList = snap.tags.split(",", "，", " ").map { it.trim() }.filter { it.isNotEmpty() }
                val res = TowerApi.fetchTowers(
                    page = p,
                    sortmode = snap.sort,
                    sections = snap.sections,
                    tags = tagList,
                    searchMode = snap.searchMode,
                    searchStr = snap.search,
                )
                val list = if (reset) res.towers else snap.towers + res.towers
                page = p + 1
                _ui.value = _ui.value.copy(
                    towers = list,
                    total = res.total,
                    loading = false,
                    hasMore = list.size < res.total,
                    favorites = recentRepo.favorites().toSet(),
                    recentNames = recentRepo.recent(),
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "加载失败")
            }
        }
    }
}

private class HomeVmFactory(private val repo: TowerStore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repo) as T
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    onOpenDetail: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: (SearchQuery) -> Unit = {},
    onOpenList: (String) -> Unit = {},
) {
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = HomeVmFactory(TowerStore.get(context)))
    val state by vm.ui
    val gridState = rememberLazyGridState()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var drawerTab by remember { mutableStateOf("recent") }
    val shellFocus = rememberShellFocus()
    val drawerFocus = androidx.compose.ui.focus.FocusRequester()
    val firstCardFocus = androidx.compose.ui.focus.FocusRequester()
    val drawerItemFocus = remember {
        List(4) { androidx.compose.ui.focus.FocusRequester() }
    }
    var drawerIndex by remember { mutableStateOf(0) }

    LaunchedEffect(drawer.isOpen) {
        if (drawer.isOpen) {
            kotlinx.coroutines.delay(180)
            drawerIndex = 0
            try {
                drawerItemFocus[0].requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    // Hard trap: while drawer is open, d-pad only walks menu items.
    val shellFocusMgr = androidx.compose.ui.platform.LocalFocusManager.current
    LaunchedEffect(drawer.isOpen) {
        if (drawer.isOpen) {
            com.mobiletaplay.app.ui.common.ShellPad.onDirection = { dir ->
                when (dir) {
                    androidx.compose.ui.focus.FocusDirection.Down -> {
                        drawerIndex = (drawerIndex + 1) % drawerItemFocus.size
                        try {
                            drawerItemFocus[drawerIndex].requestFocus()
                        } catch (_: Exception) {
                        }
                    }
                    androidx.compose.ui.focus.FocusDirection.Up -> {
                        drawerIndex =
                            (drawerIndex + drawerItemFocus.size - 1) % drawerItemFocus.size
                        try {
                            drawerItemFocus[drawerIndex].requestFocus()
                        } catch (_: Exception) {
                        }
                    }
                    else -> {
                    }
                }
            }
        } else {
            com.mobiletaplay.app.ui.common.ShellPad.onDirection = { dir ->
                shellFocusMgr.moveFocus(dir)
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.loadIfNeeded()
        vm.loadRecentFav()
    }

    ModalNavigationDrawer(
        drawerState = drawer,
        gesturesEnabled = drawer.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.focusProperties {
                    canFocus = drawer.isOpen
                    if (drawer.isOpen) {
                        exit = { androidx.compose.ui.focus.FocusRequester.Cancel }
                    }
                },
            ) {
                Text(
                    "MobileTaPlay",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
                if (drawer.isOpen) {
                    NavigationDrawerItem(
                        label = { Text("最近游玩") },
                        selected = false,
                        modifier = Modifier.padFocusRing(focusRequester = drawerItemFocus[0], corner = 28.dp),
                        onClick = {
                            scope.launch { drawer.close() }
                            onOpenList("recent")
                        },
                    )
                    NavigationDrawerItem(
                        label = { Text("星标收藏") },
                        selected = false,
                        modifier = Modifier.padFocusRing(focusRequester = drawerItemFocus[1], corner = 28.dp),
                        onClick = {
                            scope.launch { drawer.close() }
                            onOpenList("fav")
                        },
                    )
                    NavigationDrawerItem(
                        label = { Text("已缓存") },
                        selected = false,
                        modifier = Modifier.padFocusRing(focusRequester = drawerItemFocus[2], corner = 28.dp),
                        onClick = {
                            scope.launch { drawer.close() }
                            onOpenList("cached")
                        },
                    )
                    NavigationDrawerItem(
                        label = { Text("高级搜索") },
                        selected = false,
                        modifier = Modifier.padFocusRing(focusRequester = drawerItemFocus[3], corner = 28.dp),
                        onClick = {
                            scope.launch { drawer.close() }
                            onOpenSearch(SearchQuery())
                        },
                    )
                }
            }
        },
    ) {
        Scaffold(
            modifier = Modifier
                .focusProperties {
                    // Entire chrome (topBar + content) disabled while drawer is open.
                    canFocus = !drawer.isOpen
                }
                .shellNav(
                    onBack = {
                        if (drawer.isOpen) scope.launch { drawer.close() }
                    },
                    onMenu = {
                        scope.launch {
                            if (drawer.isOpen) drawer.close() else drawer.open()
                        }
                    },
                ),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("MobileTaPlay")
                            Text(
                                "魔塔广场 · 共 ${state.total} 座",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        PadIconButton(
                            onClick = { scope.launch { drawer.open() } },
                            focusRequester = shellFocus,
                            corner = 8.dp,
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    },
                    actions = {
                        PadIconButton(
                            onClick = { toggleActivityOrientation(activity) },
                            corner = 8.dp,
                        ) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "横竖屏")
                        }
                        PadIconButton(onClick = { vm.refresh() }, corner = 8.dp) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                        PadIconButton(onClick = onOpenSettings, corner = 8.dp) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    },
                )
            },
        ) { pad ->
            Column(
                Modifier
                    .padding(pad)
                    .fillMaxSize()
                    ,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusProperties {
                            // Chip row → first tower card
                            down = firstCardFocus
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("hot" to "热门", "new" to "最新", "play" to "最多人玩", "rate" to "最多好评").forEach { (id, label) ->
                        androidx.compose.material3.FilterChip(
                            selected = state.sort == id,
                            onClick = {
                                vm.setSort(id)
                                scope.launch { gridState.scrollToItem(0) }
                            },
                            modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                            label = { Text(label) },
                        )
                    }
                }

                when {
                    state.error.isNotEmpty() -> ErrorPane(state.error, onRetry = { vm.refresh() })
                    state.towers.isEmpty() && state.loading -> SkeletonGrid()
                    state.towers.isEmpty() -> ErrorPane("无结果", onRetry = { vm.refresh() })
                    else -> TowerGrid(
                        towers = state.towers,
                        favorites = state.favorites,
                        loading = state.loading,
                        hasMore = state.hasMore,
                        gridState = gridState,
                        firstCardFocus = firstCardFocus,
                        onLoadMore = { vm.loadMore() },
                        onOpen = { t -> vm.onOpen(t); onOpenDetail(t.name) },
                        onFav = { vm.toggleFavorite(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonGrid() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(4) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
            )
        }
    }
}

@Composable
private fun TowerGrid(
    towers: List<Tower>,
    favorites: Set<String>,
    loading: Boolean,
    hasMore: Boolean,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    firstCardFocus: androidx.compose.ui.focus.FocusRequester? = null,
    onLoadMore: () -> Unit,
    onOpen: (Tower) -> Unit,
    onFav: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 170.dp),
        state = gridState,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            ,
    ) {
        items(towers, key = { it.id }) { t ->
            TowerCard(
                t = t,
                fav = favorites.contains(t.name),
                onClick = { onOpen(t) },
                onFav = { onFav(t.name) },
                focusRequester = if (t.name == towers.firstOrNull()?.name) firstCardFocus else null,
            )
        }
        if (towers.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LaunchedEffect(gridState.layoutInfo, towers.size, loading) {
                    val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    if (!loading && hasMore && last >= towers.size - 4) onLoadMore()
                }
                if (loading) {
                    CircularProgressIndicator(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}
