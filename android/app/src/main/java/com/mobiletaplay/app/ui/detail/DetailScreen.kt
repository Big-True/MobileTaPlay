package com.mobiletaplay.app.ui.detail

import com.mobiletaplay.app.data.Tower
import com.mobiletaplay.app.data.TowerApi
import com.mobiletaplay.app.data.TowerStore
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobiletaplay.app.ui.home.toggleActivityOrientation
import com.mobiletaplay.app.ui.common.PadIconButton
import com.mobiletaplay.app.ui.common.padFocusRing
import com.mobiletaplay.app.ui.common.shellNav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    name: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartGame: (url: String, title: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val activity = LocalContext.current as? Activity
    var tower by remember { mutableStateOf<Tower?>(null) }
    var error by remember { mutableStateOf("") }
    var shotIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var shot by remember { mutableIntStateOf(0) }

    LaunchedEffect(name) {
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
    }

    Scaffold(
        modifier = Modifier.shellNav(
            onBack = onBack,
            onL = {
                val n = tower?.previews?.size?.takeIf { it > 1 }
                    ?: if (tower?.image != null) 1 else 0
                if (n > 1) shotIndex = (shotIndex - 1 + n) % n
            },
            onR = {
                val n = tower?.previews?.size?.takeIf { it > 1 }
                    ?: if (tower?.image != null) 1 else 0
                if (n > 1) shotIndex = (shotIndex + 1) % n
            },
        ),
        topBar = {
            TopAppBar(
                title = { Text(tower?.title ?: "关卡详情", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    PadIconButton(onClick = onBack, corner = 8.dp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    PadIconButton(onClick = { toggleActivityOrientation(activity) }, corner = 8.dp) {
                        Icon(Icons.Default.ScreenRotation, contentDescription = "横竖屏")
                    }
                    CacheButton(tower = tower, onDeleted = onBack)
                    PadIconButton(onClick = onOpenSettings, corner = 8.dp) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        bottomBar = {
            val t = tower
            if (t != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = { onStartGame(TowerApi.playUrl(t), t.title) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                            .padFocusRing(corner = 24.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("开始游戏")
                    }
                }
            }
        },
    ) { pad ->
        val t = tower
        when {
            error.isNotEmpty() -> Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                Text(error)
            }
            t == null -> Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> {
                val shots = t.previews.ifEmpty {
                    listOfNotNull(t.image)
                }
                val landscape =
                    LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                if (landscape) {
                    Row(
                        Modifier
                            .padding(pad)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        DetailMedia(shots, shotIndex, Modifier.weight(0.42f))
                        DetailInfo(t, shots, shotIndex, onSelect = { shotIndex = it }, Modifier.weight(0.58f))
                    }
                } else {
                    Column(
                        Modifier
                            .padding(pad)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        DetailMedia(shots, shotIndex, Modifier.fillMaxWidth())
                        DetailInfo(t, shots, shotIndex, onSelect = { shotIndex = it }, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMedia(shots: List<String>, index: Int, modifier: Modifier = Modifier) {
    if (shots.isEmpty()) return
    val safeIndex = index.coerceIn(0, shots.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeIndex, pageCount = { shots.size })
    LaunchedEffect(index) {
        if (index in shots.indices && pagerState.currentPage != index) {
            pagerState.scrollToPage(index)
        }
    }
    Column(modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
        ) { i ->
            AsyncImage(
                model = shots[i],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            shots.forEachIndexed { i, _ ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i == pagerState.currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun DetailInfo(
    t: Tower,
    shots: List<String>,
    index: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier.padding(12.dp)) {

        Text(t.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
        Text(
            t.author,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Stat("评分", t.rate ?: "—")
            Stat("在玩", t.people ?: "0")
            Stat("通关", t.win ?: "0")
            Stat("层数", t.floors ?: "?")
        }
        Spacer(Modifier.height(10.dp))
        Text("简介", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            plain(t.description ?: t.text ?: "暂无简介"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Card(Modifier.widthIn(min = 72.dp)) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun plain(html: String?): String {
    if (html.isNullOrBlank()) return ""
    return html
        .replace(Regex("<style[\\s\\S]*?</style>"), "")
        .replace(Regex("<script[\\s\\S]*?</script>"), "")
        .replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("</p>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .trim()
}






@Composable
private fun CacheButton(tower: Tower?, onDeleted: () -> Unit = {}) {
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
    TextButton(
        enabled = !busy,
        onClick = {
            scope.launch {
                busy = true
                try {
                    if (cached) {
                        com.mobiletaplay.app.data.GameCache.delete(context, tower.name)
                        store.removeCache(tower.name)
                        onDeleted()
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
        },
        modifier = Modifier.padFocusRing(corner = 8.dp),
    ) {
        Text(if (busy) "…" else if (cached) "删除缓存" + sizeLabel else "缓存")
    }
}
