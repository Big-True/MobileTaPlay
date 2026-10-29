package com.mobiletaplay.app.ui.help

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiletaplay.app.ui.common.PadIconButton
import com.mobiletaplay.app.ui.common.shellNav
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
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
                title = { Text("手柄帮助") },
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
            HelpCard("通用") {
                HelpRow("十字 / 摇杆", "移动焦点；无焦点时滚动")
                HelpRow("A", "确认")
                HelpRow("B", "返回")
            }
            HelpCard("首页") {
                HelpRow("Y / Start", "开关侧栏")
                HelpRow("十字 在排序行", "切换热门 / 最新 / 最多人玩 / 最多好评")
                HelpRow("十字 在卡片", "选塔；A 进详情")
                HelpRow("? 按钮", "打开本帮助")
            }
            HelpCard("详情页") {
                HelpRow("A 在作者", "按作者搜索")
                HelpRow("A 在评论", "打开评论页")
                HelpRow("A 在查看通关信息", "打开通关信息页")
                HelpRow("A 在收藏", "收藏 / 取消收藏")
                HelpRow("L1 / R1", "上一张 / 下一张截图")
                HelpRow("十字 上下", "无按钮时滚动简介")
            }
            HelpCard("评论 / 通关信息 / 帮助") {
                HelpRow("十字 上下", "滚动")
                HelpRow("B", "返回")
            }
            HelpCard("设置") {
                HelpRow("A 在映射项", "开始绑定，再按手柄键")
                HelpRow("A 在卡片列数", "切换 2 / 3 / 4 列")
                HelpRow("A 在恢复默认", "恢复默认映射")
                HelpRow("A 在关于", "打开关于页")
            }
            HelpCard("游戏内") {
                HelpRow("Select", "窗口 → 全屏 → 顶栏焦点，循环")
                HelpRow("十字 在顶栏焦点", "选 返回 / 旋转 / 全屏 / 设置")
                HelpRow("A 在顶栏焦点", "执行当前项")
                HelpRow("B 在顶栏焦点", "退出焦点，回到操作游戏")
                HelpRow("其余按键", "按「设置 · 手柄映射」中的绑定执行")
            }
        }
    }
}

@Composable
private fun HelpCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun HelpRow(key: String, action: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            key,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            " → ",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            action,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
