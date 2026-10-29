@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.focusRequester
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
import com.mobiletaplay.app.ui.common.padClickable
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
    val tags: List<String> = emptyList(),
    val colors: List<Int> = emptyList(),
    val filterOpen: Boolean = false,
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

    fun setSearchMode(m: String) {
        _ui.value = _ui.value.copy(searchMode = m)
        refresh()
    }

    fun toggleFilterOpen() {
        _ui.value = _ui.value.copy(filterOpen = !_ui.value.filterOpen)
    }

    fun setSections(list: List<Int>) {
        _ui.value = _ui.value.copy(sections = list.ifEmpty { listOf(0, 1, 2) })
        refresh()
    }

    fun toggleTag(tag: String) {
        val cur = _ui.value.tags.toMutableSet()
        if (!cur.add(tag)) cur.remove(tag)
        _ui.value = _ui.value.copy(tags = cur.toList())
        refresh()
    }

    fun toggleColor(c: Int) {
        val cur = _ui.value.colors.toMutableSet()
        if (!cur.add(c)) cur.remove(c)
        _ui.value = _ui.value.copy(colors = cur.toList())
        refresh()
    }

    fun resetFilters() {
        _ui.value = _ui.value.copy(
            search = "",
            searchMode = "title",
            sort = "hot",
            sections = listOf(0, 1, 2),
            tags = emptyList(),
            colors = emptyList(),
        )
        refresh()
    }

    fun applyQuery(q: SearchQuery) {
        _ui.value = _ui.value.copy(
            search = q.keyword,
            searchMode = q.mode,
            sort = q.sort,
            sections = q.sections.toList(),
            tags = q.tags.split(",", "，", " ").map { it.trim() }.filter { it.isNotEmpty() },
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
                val tagList = snap.tags
                val res = TowerApi.fetchTowers(
                    page = p,
                    sortmode = snap.sort,
                    sections = snap.sections,
                    tags = tagList,
                    colors = snap.colors,
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
private fun rememberGridColumns(): Int {
    val context = LocalContext.current
    var cols by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(3) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            com.mobiletaplay.app.data.SettingsRepository.get(context)
                .settings
                .collect { cols = it.gridColumns }
        } catch (_: Exception) {
        }
    }
    return cols
}

@Composable
fun HomeScreen(
    onOpenDetail: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: (SearchQuery) -> Unit = {},
    onOpenList: (String) -> Unit = {},
    onOpenHelp: () -> Unit = {},
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
    val firstChipFocus = androidx.compose.ui.focus.FocusRequester()
    var searchEditing by remember { mutableStateOf(false) }
    val searchFocus = androidx.compose.ui.focus.FocusRequester()
    val gridCols = rememberGridColumns()
    val drawerItemFocus = remember {
        List(4) { androidx.compose.ui.focus.FocusRequester() }
    }
    var drawerIndex by remember { mutableStateOf(0) }

    LaunchedEffect(drawer.isOpen) {
        if (drawer.isOpen) {
            kotlinx.coroutines.delay(180)
            drawerIndex = 0
            try {
                if (drawer.isOpen) drawerItemFocus[0].requestFocus()
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
                            try { drawerItemFocus[drawerIndex].requestFocus() } catch (_: Exception) {}
                        } catch (_: Exception) {
                        }
                    }
                    androidx.compose.ui.focus.FocusDirection.Up -> {
                        drawerIndex =
                            (drawerIndex + drawerItemFocus.size - 1) % drawerItemFocus.size
                        try {
                            try { drawerItemFocus[drawerIndex].requestFocus() } catch (_: Exception) {}
                        } catch (_: Exception) {
                        }
                    }
                    else -> {
                    }
                }
                true
            }
        } else {
            com.mobiletaplay.app.ui.common.ShellPad.onDirection = { dir ->
                try {
                    shellFocusMgr.moveFocus(dir)
                } catch (_: Throwable) {
                    false
                }
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
                }
            }
        },
    ) {
        Scaffold(
            modifier = Modifier
                .shellNav(
                    onBack = {
                        when {
                            searchEditing -> searchEditing = false
                            state.filterOpen -> vm.toggleFilterOpen()
                            drawer.isOpen -> scope.launch { drawer.close() }
                        }
                    },
                    onMenu = {
                        scope.launch {
                            if (drawer.isOpen) drawer.close() else drawer.open()
                        }
                    },
                ),
            topBar = {
                TopAppBar(
                    modifier = Modifier.focusProperties {
                        down = firstChipFocus
                    },
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
                        androidx.compose.foundation.layout.Row {
                            val topDown = androidx.compose.ui.focus.FocusRequester()
                            // attach down target on each chrome button
                        }
                        PadIconButton(
                            onClick = { toggleActivityOrientation(activity) },
                            corner = 8.dp,
                        ) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "横竖屏")
                        }
                        PadIconButton(onClick = { vm.refresh() }, corner = 8.dp) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                        PadIconButton(onClick = onOpenHelp, corner = 8.dp) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "帮助")
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
                FilterPanel(
                    state = state,
                    vm = vm,
                    firstChipFocus = firstChipFocus,
                    firstCardFocus = firstCardFocus,
                    searchEditing = searchEditing,
                    onSearchEditingChange = { searchEditing = it },
                )

                when {
                    state.error.isNotEmpty() -> ErrorPane(state.error, onRetry = { vm.refresh() })
                    state.towers.isEmpty() && state.loading -> SkeletonGrid()
                    state.towers.isEmpty() -> ErrorPane("无结果", onRetry = { vm.refresh() })
                    else -> androidx.compose.foundation.layout.Box(
                        Modifier.then(
                            if (state.filterOpen) {
                                Modifier.focusProperties { canFocus = false }
                            } else {
                                Modifier
                            },
                        ),
                    ) {
                    TowerGrid(
                        towers = state.towers,
                        favorites = state.favorites,
                        loading = state.loading,
                        hasMore = state.hasMore,
                        gridState = gridState,
                        firstCardFocus = firstCardFocus,
                        gridCols = gridCols,
                        cardsEnabled = !state.filterOpen,
                        onLoadMore = { vm.loadMore() },
                        onOpen = { t -> vm.onOpen(t); onOpenDetail(t.name) },
                        onFav = { vm.toggleFavorite(it) },
                    )
                    }
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

private val FilterSorts = listOf(
    "hot" to "最热",
    "new" to "最新",
    "map" to "最多楼层",
    "comment" to "最多评论",
    "lastcomment" to "最近评论",
    "play" to "最多人玩",
    "pass" to "最多通关",
)

private val FilterSections = listOf(0 to "原创", 1 to "小塔", 2 to "MOD")

private val FilterTags = listOf(
    "复刻塔", "处女作", "悬赏塔", "高难塔", "剧情向", "加点塔", "转换塔", "道具塔", "境界塔",
    "技能塔", "平面塔", "连载塔", "已完结", "机关塔", "小游戏", "解密塔", "随机塔", "多角色", "联机游戏",
)

private val FilterColors = listOf(
    0 to "蓝", 1 to "绿", 2 to "红", 3 to "橙", 4 to "白", 5 to "灰", 6 to "紫",
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FilterPanel(
    state: HomeUiState,
    vm: HomeViewModel,
    firstChipFocus: androidx.compose.ui.focus.FocusRequester,
    firstCardFocus: androidx.compose.ui.focus.FocusRequester,
    searchEditing: Boolean,
    onSearchEditingChange: (Boolean) -> Unit,
) {
    val searchFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("筛选", style = MaterialTheme.typography.titleSmall)
            Text(
                if (state.filterOpen) "收起 ▴" else "展开 ▾",
                modifier = Modifier
                    .padClickable(
                        onClick = {
                            val open = !state.filterOpen
                            vm.toggleFilterOpen()
                        },
                        focusRequester = firstChipFocus,
                        corner = 20.dp,
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (!state.filterOpen) {
            return
        }

        Column(
            Modifier
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
                .focusProperties {
                    // keep focus inside expanded filter panel
                    exit = { androidx.compose.ui.focus.FocusRequester.Cancel }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

        // keyword: A/click enters edit (IME); B leaves edit
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (searchEditing) {
                OutlinedTextField(
                    value = state.search,
                    onValueChange = { vm.setSearch(it) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .focusRequester(searchFocus),
                    label = { Text("关键词") },
                    singleLine = true,
                )
            } else {
                Box(
                    Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padClickable(
                            onClick = {
                                onSearchEditingChange(true)
                                try {
                                    searchFocus.requestFocus()
                                } catch (_: Throwable) {
                                }
                            },
                            corner = 8.dp,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        if (state.search.isBlank()) "关键词…" else "关键词 ${state.search}",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (state.search.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("title" to "标题", "author" to "作者", "card" to "简介").forEach { (id, label) ->
                FilterChip(
                    selected = state.searchMode == id,
                    onClick = { vm.setSearchMode(id) },
                    modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                    label = { Text(label) },
                )
            }
        }

        Text("排序", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterSorts.forEach { (id, label) ->
                FilterChip(
                    selected = state.sort == id,
                    onClick = { vm.setSort(id) },
                    modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                    label = { Text(label) },
                )
            }
        }

        Text("大类", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterSections.forEach { (id, label) ->
                FilterChip(
                    selected = state.sections.contains(id),
                    onClick = {
                        val s = state.sections.toMutableSet()
                        if (!s.add(id) && s.size > 1) s.remove(id)
                        vm.setSections(s.toList().sorted())
                    },
                    modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                    label = { Text(label) },
                )
            }
        }

        Text("标签", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterTags.forEach { tag ->
                FilterChip(
                    selected = state.tags.contains(tag),
                    onClick = { vm.toggleTag(tag) },
                    modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                    label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        Text("颜色", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterColors.forEach { (id, label) ->
                FilterChip(
                    selected = state.colors.contains(id),
                    onClick = { vm.toggleColor(id) },
                    modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                    label = { Text(label) },
                )
            }
        }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { vm.resetFilters() },
                    modifier = Modifier
                        .padFocusRing(corner = 20.dp)
                        .focusProperties {
                            down = androidx.compose.ui.focus.FocusRequester.Cancel
                        },
                ) { Text("重置") }
            }
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
    gridCols: Int = 3,
    cardsEnabled: Boolean = true,
    onLoadMore: () -> Unit,
    onOpen: (Tower) -> Unit,
    onFav: (String) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(gridCols),
        state = rememberLazyStaggeredGridState(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize(),
    ) {
        staggeredItems(towers, key = { it.id }) { t ->
            TowerCard(
                t = t,
                fav = favorites.contains(t.name),
                onClick = { onOpen(t) },
                onFav = { onFav(t.name) },
                focusRequester = if (t.name == towers.firstOrNull()?.name) firstCardFocus else null,
            )
        }
        if (towers.isNotEmpty()) {
            item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                LaunchedEffect(towers.size, loading) {
                    if (!loading && hasMore) onLoadMore()
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
