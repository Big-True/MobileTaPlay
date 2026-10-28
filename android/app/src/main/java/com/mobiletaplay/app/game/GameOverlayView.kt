package com.mobiletaplay.app.game

import android.annotation.SuppressLint
import com.mobiletaplay.app.OrientationPrefs
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MtpBg = Color(0xFF0F1419)
private val MtpPanel = Color(0xFF1A222C)
private val MtpInk = Color(0xFFE7ECF1)
private val MtpMuted = Color(0xFF8B9BAB)
private val MtpAccent = Color(0xFF3D8BFD)

private data class KbKey(val code: Int, val label: String, val fn: String)

private val kbRows = listOf(
    listOf(KbKey(49, "1", "破"), KbKey(50, "2", "炸"), KbKey(51, "3", "飞"), KbKey(52, "4", "道具"), KbKey(53, "5", "回退"), KbKey(54, "6", "撤销"), KbKey(55, "7", "轻按")),
    listOf(KbKey(81, "Q", "装备"), KbKey(87, "W", "撤销"), KbKey(69, "E", "光标"), KbKey(82, "R", "录像"), KbKey(84, "T", "道具"), KbKey(88, "X", "手册"), KbKey(71, "G", "楼传")),
    listOf(KbKey(65, "A", "自动档"), KbKey(83, "S", "存档"), KbKey(68, "D", "读档"), KbKey(70, "F", "技能"), KbKey(86, "V", "商店"), KbKey(66, "B", "统计"), KbKey(72, "H", "帮助")),
    listOf(KbKey(90, "Z", "转向"), KbKey(67, "C", "数据"), KbKey(78, "N", "重开"), KbKey(77, "M", "笔记"), KbKey(32, "空", "轻按"), KbKey(27, "Esc", "菜单"), KbKey(13, "Ok", "确认")),
)
/**
 * Game chrome in the same Material3 language as Home/Detail.
 * Top: back · title · rotate · fullscreen · settings.
 * Optional keyboard-layout virtual keys (gamepad Y).
 */
@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class GameOverlayView(
    context: Context,
    private val title: String,
    private val onBack: () -> Unit,
    private val onRotate: () -> Unit,
    private val onToggleImmersive: () -> Unit,
    private val onSettings: () -> Unit,
    private val onSendKeyCode: (keyCode: Int, down: Boolean) -> Unit,
) : FrameLayout(context) {

    private var kbVisible = false
    private var kbIndex = 0
    private var showFn = false
    private var barVisible = true
    private val composeView = ComposeView(context)

    init {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            defaultFocusHighlightEnabled = false
        }
        isFocusable = false
        descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        addView(composeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        recompose()
    }

    private fun recompose() {
        val showKb = kbVisible
        val showBar = barVisible
        val idx = kbIndex
        val fn = showFn
        composeView.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                GameChrome(
                    title = title,
                    showBar = showBar,
                    kbVisible = showKb,
                    kbIndex = idx,
                    showFn = showFn,
                    onBack = onBack,
                    onRotate = onRotate,
                    onFullscreen = onToggleImmersive,
                    onSettings = onSettings,
                    onKeyPress = { fireKey(it) },
                )
            }
        }
    }

    fun setPadVisible(visible: Boolean) {}

    fun setBarVisible(visible: Boolean) {
        barVisible = visible
        recompose()
    }

    fun isKeyboardVisible(): Boolean = kbVisible

    fun toggleFnLabels(): Boolean {
        showFn = !showFn
        recompose()
        return showFn
    }

    fun toggleKeyboard(): Boolean {
        kbVisible = !kbVisible
        if (kbVisible) kbIndex = 0
        recompose()
        return kbVisible
    }

    fun closeKeyboard() {
        kbVisible = false
        recompose()
    }

    fun moveKeyboardSelection(dx: Int, dy: Int) {
        if (!kbVisible) return
        val cols = kbRows.first().size
        val n = kbRows.sumOf { it.size }
        var i = kbIndex
        if (dx != 0) i = ((i + dx) % n + n) % n
        if (dy != 0) i = ((i + dy * cols) % n + n) % n
        kbIndex = i
        recompose()
    }

    fun confirmKeyboard() {
        if (!kbVisible) return
        val flat = kbRows.flatten()
        if (kbIndex in flat.indices) fireKey(flat[kbIndex].code)
    }

    private fun fireKey(code: Int) {
        onSendKeyCode(code, true)
        postDelayed({ onSendKeyCode(code, false) }, 40)
    }
}

@Composable
private fun GameChrome(
    title: String,
    showBar: Boolean,
    kbVisible: Boolean,
    kbIndex: Int,
    showFn: Boolean,
    onBack: () -> Unit,
    onRotate: () -> Unit,
    onFullscreen: () -> Unit,
    onSettings: () -> Unit,
    onKeyPress: (Int) -> Unit,
) {
    // Offset below system status bar when not immersive
    val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation ==
        android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val statusH = run {
        val res = androidx.compose.ui.platform.LocalContext.current.resources
        val id = res.getIdentifier("status_bar_height", "dimen", "android")
        if (id > 0) res.getDimensionPixelSize(id) else 72
    }
    Column(Modifier.fillMaxSize().padding(top = with(androidx.compose.ui.platform.LocalDensity.current) { statusH.toDp() })) {
        if (showBar) {
        Surface(color = MtpBg, modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MtpIconBtn(Icons.Default.ArrowBack, "返回", onBack)
                    Spacer(Modifier.weight(1f))
                    MtpIconBtn(Icons.Default.ScreenRotation, "横竖屏", onRotate)
                    MtpIconBtn(Icons.Default.Fullscreen, "全屏", onFullscreen)
                    MtpIconBtn(Icons.Default.Settings, "设置", onSettings)
                }
                Text(
                    text = title,
                    color = MtpInk,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }
        }
        }
        if (kbVisible) {
            Surface(color = MtpBg.copy(alpha = 0.85f), modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("虚拟键盘 · 方向键选择 · A确定 · X切换显示 · B关闭", color = MtpMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    val cols = kbRows.first().size
                    kbRows.forEachIndexed { r, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEachIndexed { c, k ->
                                val idx = r * cols + c
                                Box(
                                    Modifier
                                        .size(
                                            width = if (isLandscape) 42.dp else 48.dp,
                                            height = if (isLandscape) 38.dp else 44.dp,
                                        )
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (idx == kbIndex) MtpAccent else MtpPanel)
                                        .clickable { onKeyPress(k.code) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(if (showFn) k.fn else k.label, color = MtpInk, fontSize = if (showFn) 11.sp else 14.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MtpIconBtn(icon: ImageVector, cd: String, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(4.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MtpPanel)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = cd, tint = MtpInk)
    }
}







