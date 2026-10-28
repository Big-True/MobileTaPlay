package com.mobiletaplay.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiletaplay.app.ui.common.padFocusRing
import com.mobiletaplay.app.ui.common.shellNav

data class SearchQuery(
    val keyword: String = "",
    val mode: String = "title",
    val sort: String = "hot",
    val sections: Set<Int> = setOf(0, 1, 2),
    val tags: String = "",
)

/** Advanced search (matches h5mota list filters). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initial: SearchQuery,
    onBack: () -> Unit,
    onApply: (SearchQuery) -> Unit,
) {
    var q by remember { mutableStateOf(initial) }

    Scaffold(
        modifier = Modifier.shellNav(onBack = onBack),
        topBar = {
            TopAppBar(
                title = { Text("高级搜索") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.padFocusRing(corner = 24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onApply(q) }, modifier = Modifier.padFocusRing(corner = 24.dp)) {
                        Text("搜索", color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = q.keyword,
                onValueChange = { q = q.copy(keyword = it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padFocusRing(),
                label = { Text("关键词") },
                singleLine = true,
            )
            Text("搜索字段", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("title" to "标题", "author" to "作者", "card" to "简介").forEach { (id, label) ->
                    FilterChip(
                        selected = q.mode == id,
                        onClick = { q = q.copy(mode = id) },
                        modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                        label = { Text(label) },
                    )
                }
            }
            Text("排序", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "hot" to "热门", "new" to "最新", "play" to "在玩",
                    "pass" to "通关", "rate" to "好评", "comment" to "评论",
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = q.sort == id,
                        onClick = { q = q.copy(sort = id) },
                        modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                        label = { Text(label) },
                    )
                }
            }
            Text("分区", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "分区0", 1 to "分区1", 2 to "分区2").forEach { (id, label) ->
                    FilterChip(
                        selected = q.sections.contains(id),
                        onClick = {
                            val s = q.sections.toMutableSet()
                            if (!s.add(id) && s.size > 1) s.remove(id)
                            q = q.copy(sections = s)
                        },
                        modifier = Modifier.padFocusRing(corner = 8.dp, ringVInset = 6.dp),
                        label = { Text(label) },
                    )
                }
            }
            OutlinedTextField(
                value = q.tags,
                onValueChange = { q = q.copy(tags = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标签，逗号分隔") },
                singleLine = true,
            )
        }
    }
}
