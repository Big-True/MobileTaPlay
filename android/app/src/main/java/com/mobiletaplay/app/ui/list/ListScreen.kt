package com.mobiletaplay.app.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiletaplay.app.data.Tower
import com.mobiletaplay.app.data.TowerApi
import com.mobiletaplay.app.data.TowerStore
import com.mobiletaplay.app.ui.common.ErrorPane
import com.mobiletaplay.app.ui.common.PadIconButton
import com.mobiletaplay.app.ui.common.padFocusRing
import com.mobiletaplay.app.ui.common.shellNav
import com.mobiletaplay.app.ui.home.TowerCard
import kotlinx.coroutines.launch

enum class ListMode { RECENT, FAV, CACHED, SEARCH }

data class ListUiState(
    val title: String = "",
    val towers: List<Tower> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val loading: Boolean = true,
    val error: String = "",
)

class ListViewModel(
    private val context: android.content.Context,
    private val store: TowerStore,
    private val mode: ListMode,
    private val keyword: String,
    private val searchMode: String,
    private val sort: String,
) : ViewModel() {

    private val _ui = androidx.compose.runtime.mutableStateOf(ListUiState())
    val ui = _ui

    fun load() {
        _ui.value = _ui.value.copy(loading = true, error = "")
        viewModelScope.launch {
            try {
                val list: List<Tower> = when (mode) {
                    ListMode.RECENT -> resolve(store.recent())
                    ListMode.FAV -> resolve(store.favorites())
                    ListMode.CACHED -> {
                        val gameRoot = java.io.File(context.filesDir, "games")
                        val names = gameRoot.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
                        // prefer snapshot; synthesize minimal tower from dir name
                        names.map { n ->
                            store.snapshot(n) ?: Tower(id = n.hashCode(), name = n, title = n, author = "已缓存")
                        }
                    }
                    ListMode.SEARCH -> {
                        val res = TowerApi.fetchTowers(
                            page = 1,
                            sortmode = sort,
                            searchMode = searchMode,
                            searchStr = keyword,
                        )
                        res.towers
                    }
                }
                _ui.value = ListUiState(
                    title = title(),
                    towers = list,
                    favorites = store.favorites().toSet(),
                    loading = false,
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "加载失败")
            }
        }
    }

    fun toggleFavorite(name: String) {
        viewModelScope.launch {
            store.toggleFavorite(name)
            load()
        }
    }

    fun onOpen(t: Tower) {
        viewModelScope.launch {
            store.cacheTower(t)
            store.pushRecent(t.name)
        }
    }

    private fun title() = when (mode) {
        ListMode.RECENT -> "最近游玩"
        ListMode.FAV -> "星标收藏"
        ListMode.CACHED -> "已缓存"
        ListMode.SEARCH -> "搜索结果"
    }

    private suspend fun resolve(names: List<String>): List<Tower> {
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
    }
}

private class ListVmFactory(
    private val context: android.content.Context,
    private val store: TowerStore,
    private val mode: ListMode,
    private val keyword: String,
    private val searchMode: String,
    private val sort: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListViewModel(context, store, mode, keyword, searchMode, sort) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    mode: ListMode,
    keyword: String = "",
    searchMode: String = "title",
    sort: String = "hot",
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    val context = LocalContext.current
    val vm: ListViewModel = viewModel(
        key = "$mode:$keyword",
        factory = ListVmFactory(context, TowerStore.get(context), mode, keyword, searchMode, sort),
    )
    val ui by vm.ui

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        modifier = Modifier.shellNav(onBack = onBack),
        topBar = {
            TopAppBar(
                title = { Text(ui.title) },
                navigationIcon = {
                    PadIconButton(onClick = onBack, corner = 8.dp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize(),
        ) {
            when {
                ui.error.isNotEmpty() -> ErrorPane(ui.error, onRetry = { vm.load() })
                ui.loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ui.towers.isEmpty() -> ErrorPane("暂无内容")
                else -> {
                    val state = rememberLazyGridState()
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 170.dp),
                        state = state,
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            ,
                    ) {
                        items(ui.towers, key = { "${mode}_${it.name}" }) { t ->
                            TowerCard(
                                t = t,
                                fav = ui.favorites.contains(t.name),
                                onClick = { vm.onOpen(t); onOpenDetail(t.name) },
                                onFav = { vm.toggleFavorite(t.name) },
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
