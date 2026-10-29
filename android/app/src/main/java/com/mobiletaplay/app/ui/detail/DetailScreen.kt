package com.mobiletaplay.app.ui.detail

import com.mobiletaplay.app.data.Tower
import com.mobiletaplay.app.data.TowerApi
import com.mobiletaplay.app.data.TowerStore
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.focusable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusProperties
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    name: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartGame: (url: String, title: String) -> Unit,
    onOpenAuthor: (String) -> Unit = {},
    onOpenComments: (String, String) -> Unit = { _, _ -> },
    onOpenClearInfo: (String, List<String>, String) -> Unit = { _, _, _ -> },
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val activity = LocalContext.current as? Activity
    var tower by remember { mutableStateOf<Tower?>(null) }
    var error by remember { mutableStateOf("") }
    var shotIndex by remember { mutableIntStateOf(0) }
    val pageScroll = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    var shot by remember { mutableIntStateOf(0) }

    LaunchedEffect(name) {
        try {
            // 1) tower page SSR (always has meta + comments)
            val (ssrTower, _) = withContext(Dispatchers.IO) {
                TowerApi.fetchTowerPage(name)
            }
            val local = withContext(Dispatchers.IO) {
                TowerStore.get(ctx).snapshot(name)
            }
            if (ssrTower != null) {
                // SSR has people/win/comments/difficulty; snapshot fills image/floors/rate/previews
                val base = local ?: ssrTower
                tower = base.copy(
                    title = ssrTower.title.ifBlank { base.title },
                    author = ssrTower.author.ifBlank { base.author },
                    people = ssrTower.people ?: base.people,
                    win = ssrTower.win ?: base.win,
                    commentCount = if (ssrTower.commentCount > 0) ssrTower.commentCount else base.commentCount,
                    difficulty = ssrTower.difficulty ?: base.difficulty,
                    image = ssrTower.image ?: base.image,
                    floors = ssrTower.floors ?: base.floors,
                    rate = ssrTower.rate ?: base.rate,
                    previews = ssrTower.previews.ifEmpty { base.previews },
                    tags = if (ssrTower.tags.isNotEmpty()) ssrTower.tags else base.tags,
                    description = ssrTower.description ?: base.description,
                    text = ssrTower.text ?: base.text,
                    content = ssrTower.content ?: base.content,
                    thumbUp = if (ssrTower.thumbUp > 0) ssrTower.thumbUp else base.thumbUp,
                    uploadTime = ssrTower.uploadTime ?: base.uploadTime,
                    updateTime = ssrTower.updateTime ?: base.updateTime,
                    recommend = ssrTower.recommend || base.recommend,
                )
                return@LaunchedEffect
            }
            if (local != null) {
                tower = local
            }
            // 3) cached files
            if (tower == null && com.mobiletaplay.app.data.GameCache.isCached(ctx, name)) {
                tower = Tower(id = name.hashCode(), name = name, title = name, author = "已缓存")
            }
            if (tower == null) {
                error = "未找到该塔"
            }
        } catch (e: Exception) {
            if (tower == null) error = e.message ?: "加载失败"
        }
    }

    Scaffold(
        modifier = Modifier.shellNav(
            onBack = onBack,
            onScroll = { up ->
                val delta = 320
                val target = if (up) {
                    (pageScroll.value - delta).coerceAtLeast(0)
                } else {
                    (pageScroll.value + delta).coerceAtMost(pageScroll.maxValue)
                }
                if (target == pageScroll.value) {
                    false
                } else {
                    scope.launch { pageScroll.animateScrollTo(target) }
                    true
                }
            },
            onL = {
                val list = tower?.previews?.takeIf { it.isNotEmpty() }
                    ?: listOfNotNull(tower?.image)
                if (list.size > 1) shotIndex = (shotIndex - 1 + list.size) % list.size
            },
            onR = {
                val list = tower?.previews?.takeIf { it.isNotEmpty() }
                    ?: listOfNotNull(tower?.image)
                if (list.size > 1) shotIndex = (shotIndex + 1) % list.size
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
                            .verticalScroll(pageScroll),
                    ) {
                        DetailMedia(shots, shotIndex, Modifier.weight(0.42f))
                        DetailInfo(t, shots, shotIndex, onSelect = { shotIndex = it }, onOpenAuthor = onOpenAuthor, onOpenComments = onOpenComments, onOpenClearInfo = onOpenClearInfo, Modifier.weight(0.58f))
                    }
                } else {
                    Column(
                        Modifier
                            .padding(pad)
                            .fillMaxSize()
                            .verticalScroll(pageScroll),
                    ) {
                        DetailMedia(shots, shotIndex, Modifier.fillMaxWidth())
                        DetailInfo(t, shots, shotIndex, onSelect = { shotIndex = it }, onOpenAuthor = onOpenAuthor, onOpenComments = onOpenComments, onOpenClearInfo = onOpenClearInfo, Modifier.fillMaxWidth())
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
    Column(
        modifier.focusProperties {
            canFocus = false
        }
    ) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .focusable(false),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailInfo(
    t: Tower,
    shots: List<String>,
    index: Int,
    onSelect: (Int) -> Unit,
    onOpenAuthor: (String) -> Unit = {},
    onOpenComments: (String, String) -> Unit = { _, _ -> },
    onOpenClearInfo: (String, List<String>, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    val favContext = LocalContext.current
    val favStore = remember { com.mobiletaplay.app.data.TowerStore.get(favContext) }
    val favScope = rememberCoroutineScope()
    var favOn by remember(t.name) { mutableStateOf(false) }
    LaunchedEffect(t.name) {
        favOn = try {
            favStore.favorites().contains(t.name)
        } catch (_: Exception) {
            false
        }
    }
    Column(modifier.padding(12.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(t.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, modifier = Modifier.weight(1f))
            if (t.recommend) {
                Text(
                    "推荐",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { if (t.author.isNotBlank()) onOpenAuthor(t.author) },
                modifier = Modifier.padFocusRing(corner = 20.dp),
            ) {
                Text(
                    "作者 " + (t.author.ifBlank { "未知" }),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            TextButton(
                onClick = { onOpenComments(t.name, t.title) },
                modifier = Modifier.padFocusRing(corner = 20.dp),
            ) {
                Text("评论(${t.commentCount})")
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            Stat("评分", t.rate ?: "—", Modifier.weight(1f))
            Stat("层数", t.floors ?: "?", Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Stat("在玩", t.people ?: "0", Modifier.weight(1f))
            Stat("通关", t.win ?: "0", Modifier.weight(1f))
            Stat("点赞", t.thumbUp.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val diffLabel = buildString {
                append("难度系数 ")
                append(t.difficulty ?: "—")
                if (t.competition != 0) append(" · 比赛")
            }
            Text(
                diffLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = false),
            )
            TextButton(
                onClick = {
                    onOpenClearInfo(
                        t.title,
                        t.endings.filter { it.isNotBlank() },
                        t.content.orEmpty(),
                    )
                },
                modifier = Modifier.padFocusRing(corner = 20.dp),
            ) {
                Text("查看通关信息")
            }
        }
        if (t.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                t.tags.forEach { tag ->
                    SuggestionChip(
                        onClick = { onOpenAuthor(t.author) },
                        label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }
        }
        val intro = plain(t.description ?: t.text)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("简介 / 说明", style = MaterialTheme.typography.titleMedium)
            TextButton(
                onClick = {
                    favScope.launch {
                        favStore.toggleFavorite(t.name)
                        favOn = favStore.favorites().contains(t.name)
                    }
                },
                modifier = Modifier.padFocusRing(corner = 20.dp),
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = if (favOn) Color(0xFFF0B429) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(if (favOn) "已收藏" else "收藏")
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            intro.ifBlank { "暂无简介" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!t.uploadTime.isNullOrBlank() || !t.updateTime.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("发布", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        normalizeTime(t.uploadTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("更新", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        normalizeTime(t.updateTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier.padding(horizontal = 3.dp), contentAlignment = Alignment.Center) {
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(value, style = MaterialTheme.typography.titleMedium)
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun normalizeTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    // unify to yyyy-MM-dd HH:mm
    val s = raw.trim()
    return when {
        s.length >= 16 -> s.take(16)
        s.length >= 10 -> s.take(10) + " 00:00"
        else -> s
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
        modifier = Modifier.padFocusRing(corner = 20.dp),
    ) {
        Text(if (busy) "…" else if (cached) "删除缓存" + sizeLabel else "缓存")
    }
}
