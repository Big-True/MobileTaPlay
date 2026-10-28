package com.mobiletaplay.app.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mobiletaplay.app.ui.common.PadIconButton
import com.mobiletaplay.app.ui.common.padFocusRing
import com.mobiletaplay.app.ui.common.shellNav
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val versionLabel = remember {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = info.versionName ?: "—"
            val code = if (android.os.Build.VERSION.SDK_INT >= 28) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
            "版本 $version（$code）"
        } catch (_: Exception) {
            "版本 —"
        }
    }

    val scroll = androidx.compose.foundation.rememberScrollState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.shellNav(
            onBack = onBack,
            onScroll = { up ->
                val delta = 280
                val target = if (up) (scroll.value - delta).coerceAtLeast(0)
                else (scroll.value + delta).coerceAtMost(scroll.maxValue)
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
                title = { Text("关于 MobileTaPlay") },
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
            Text("MobileTaPlay", style = MaterialTheme.typography.headlineSmall)
            Text(versionLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            AboutCard("定位") {
                Text(
                    "第三方 Android 魔塔客户端，加载 h5mota 在线内容。无账号、打开即玩；支持手柄映射与触屏、离线缓存。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AboutCard("第三方声明") {
                Text(
                    "本应用为社区第三方客户端，与 h5mota.com 官方、mota-js 作者无隶属关系，不代表官方立场。\n\n" +
                        "塔（游戏）内容版权归各塔作者；客户端不打包、不重新分发游戏资源，仅在线加载。\n\n" +
                        "引擎 mota-js 为 BSD-3-Clause；本客户端源码为 MIT。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AboutCard("隐私摘要") {
                Text(
                    "· 无账号、无埋点、无崩溃上报\n" +
                        "· 网络仅用于访问 h5mota 列表/详情/游戏资源\n" +
                        "· 映射、收藏、缓存等仅存本机，可清除\n" +
                        "· 不收集通讯录、短信、位置等无关数据\n\n" +
                        "完整说明见仓库 docs/10-隐私与致谢.md。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AboutCard("致谢") {
                Text(
                    "· h5mota.com — HTML5 魔塔广场\n" +
                        "· mota-js — 游戏引擎（BSD-3-Clause）\n" +
                        "· 各塔作者 — 游戏内容与创意\n" +
                        "· Jetpack Compose / AndroidX\n" +
                        "· Coil、OkHttp、系统 WebView",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AboutCard("许可") {
                Text(
                    "MIT License\nCopyright (c) MobileTaPlay contributors\n\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AboutCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
