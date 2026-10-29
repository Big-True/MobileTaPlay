package com.mobiletaplay.app.ui.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiletaplay.app.data.TowerApi
import com.mobiletaplay.app.data.TowerComment
import com.mobiletaplay.app.ui.common.PadIconButton
import com.mobiletaplay.app.ui.common.padFocusRing
import com.mobiletaplay.app.ui.common.shellNav
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    name: String,
    title: String,
    onBack: () -> Unit,
) {
    var comments by remember { mutableStateOf<List<TowerComment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(name) {
        loading = true
        error = ""
        comments = withContext(Dispatchers.IO) { TowerApi.fetchComments(name) }
        loading = false
        if (comments.isEmpty()) error = "暂无评论"
    }

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.shellNav(
            onBack = onBack,
            onScroll = { up ->
                val delta = 320
                val target = if (up) {
                    (scroll.value - delta).coerceAtLeast(0)
                } else {
                    (scroll.value + delta).coerceAtMost(scroll.maxValue)
                }
                if (target == scroll.value) {
                    false
                } else {
                    scope.launch { scroll.animateScrollTo(target) }
                    true
                }
            },
        ),
        topBar = {
            TopAppBar(
                title = { Text("评论 · " + title.take(12)) },
                navigationIcon = {
                    PadIconButton(onClick = onBack, corner = 8.dp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { pad ->
        when {
            loading -> Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            comments.isEmpty() -> Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                Text(error.ifBlank { "暂无评论" })
            }
            else -> androidx.compose.foundation.layout.Column(
                Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                comments.forEach { c ->
                    CommentCard(c)
                }
            }
        }
    }
}

@Composable
private fun CommentCard(c: TowerComment) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "#${c.id}  ${c.authorName}" + if (c.loveNum > 0) "  ♥${c.loveNum}" else "",
                style = MaterialTheme.typography.titleSmall,
            )
            if (c.timestamp.isNotBlank()) {
                Text(
                    c.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(c.text, style = MaterialTheme.typography.bodyMedium)
            if (c.replies.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                c.replies.forEach { r ->
                    Text(
                        "  └ ${r.authorName}: " + r.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}
