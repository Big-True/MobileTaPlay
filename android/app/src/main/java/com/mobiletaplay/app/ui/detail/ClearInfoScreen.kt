package com.mobiletaplay.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiletaplay.app.ui.common.PadIconButton
import com.mobiletaplay.app.ui.common.shellNav
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearInfoScreen(
    title: String,
    endings: List<String>,
    content: String,
    onBack: () -> Unit,
) {
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
                if (target == scroll.value) false
                else {
                    scope.launch { scroll.animateScrollTo(target) }
                    true
                }
            },
        ),
        topBar = {
            TopAppBar(
                title = { Text("通关信息 · " + title.take(12)) },
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
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (endings.any { it.isNotBlank() }) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("通关线", style = MaterialTheme.typography.titleMedium)
                        endings.filter { it.isNotBlank() }.forEach { e ->
                            Text("· " + e, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("各难度 / 版本通关说明", style = MaterialTheme.typography.titleMedium)
                    Text(
                        content.ifBlank { "暂无说明" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
