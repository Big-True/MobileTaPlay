package com.mobiletaplay.app.ui.common

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Shape-aligned focus ring (outline + optional clip) and shell pad navigation.
 *
 * Each control passes the **same corner radius** used by its visual shape so the
 * ring, clip, and ripple agree (IconButton 8dp, pill 24dp, chip 8dp, …).
 */
object ShellPad {
    @Volatile
    var onDirection: ((FocusDirection) -> Unit)? = null

    @Volatile
    var onBack: (() -> Unit)? = null

    @Volatile
    var onMenu: (() -> Unit)? = null

    @Volatile
    var onPage: ((Int) -> Unit)? = null

    @Volatile
    var onScroll: ((Boolean) -> Boolean)? = null

    fun clear() {
        onDirection = null
        onBack = null
        onMenu = null
        onPage = null
        onScroll = null
    }

    fun dispatch(keyCode: Int, action: Int): Boolean {
        if (action != AndroidKeyEvent.ACTION_DOWN) return false
        return when (keyCode) {
            AndroidKeyEvent.KEYCODE_DPAD_UP -> {
                if (onScroll?.invoke(true) == true) true
                else {
                    onDirection?.invoke(FocusDirection.Up); true
                }
            }
            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
                if (onScroll?.invoke(false) == true) true
                else {
                    onDirection?.invoke(FocusDirection.Down); true
                }
            }
            AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
                onDirection?.invoke(FocusDirection.Left); true
            }
            AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                onDirection?.invoke(FocusDirection.Right); true
            }
            AndroidKeyEvent.KEYCODE_DPAD_CENTER,
            AndroidKeyEvent.KEYCODE_ENTER -> false
            AndroidKeyEvent.KEYCODE_BUTTON_B,
            AndroidKeyEvent.KEYCODE_BACK -> {
                onBack?.invoke(); true
            }
            AndroidKeyEvent.KEYCODE_BUTTON_Y,
            AndroidKeyEvent.KEYCODE_BUTTON_START,
            AndroidKeyEvent.KEYCODE_MENU -> {
                onMenu?.invoke(); true
            }
            AndroidKeyEvent.KEYCODE_BUTTON_L1,
            AndroidKeyEvent.KEYCODE_PAGE_UP -> {
                onPage?.invoke(-1); true
            }
            AndroidKeyEvent.KEYCODE_BUTTON_R1,
            AndroidKeyEvent.KEYCODE_PAGE_DOWN -> {
                onPage?.invoke(1); true
            }
            else -> false
        }
    }
}

private fun safeMoveFocus(focusManager: FocusManager, dir: FocusDirection) {
    try {
        focusManager.moveFocus(dir)
    } catch (_: Throwable) {
    }
}

@Composable
fun rememberShellFocus(): FocusRequester {
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            fr.requestFocus()
        } catch (_: Exception) {
        }
    }
    return fr
}

@Composable
fun Modifier.shellNav(
    onBack: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    onL: (() -> Unit)? = null,
    onR: (() -> Unit)? = null,
    onA: (() -> Unit)? = null,
    onScroll: ((Boolean) -> Boolean)? = null,
): Modifier {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(onBack, onMenu, onL, onR, onScroll) {
        ShellPad.onDirection = { dir -> safeMoveFocus(focusManager, dir) }
        ShellPad.onBack = onBack
        ShellPad.onMenu = onMenu
        ShellPad.onPage = { d -> if (d < 0) onL?.invoke() else onR?.invoke() }
        ShellPad.onScroll = onScroll
    }
    return this.onPreviewKeyEvent { e ->
        if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when (e.key) {
            Key.DirectionUp -> {
                if (onScroll?.invoke(true) == true) true
                else {
                    safeMoveFocus(focusManager, FocusDirection.Up); true
                }
            }
            Key.DirectionDown -> {
                if (onScroll?.invoke(false) == true) true
                else {
                    safeMoveFocus(focusManager, FocusDirection.Down); true
                }
            }
            Key.DirectionLeft -> {
                safeMoveFocus(focusManager, FocusDirection.Left); true
            }
            Key.DirectionRight -> {
                safeMoveFocus(focusManager, FocusDirection.Right); true
            }
            Key.DirectionCenter, Key.NumPadEnter, Key.Enter -> {
                onA?.invoke(); false
            }
            Key.Back, Key.Escape -> {
                onBack?.invoke(); true
            }
            Key.Menu -> {
                onMenu?.invoke(); true
            }
            Key.PageUp -> {
                onL?.invoke(); true
            }
            Key.PageDown -> {
                onR?.invoke(); true
            }
            else -> false
        }
    }
}

/** Outline matching [corner] of the control's own shape. */
@Composable
internal fun Modifier.focusRing(
    focused: Boolean,
    corner: Dp = 8.dp,
    stroke: Dp = 3.dp,
    vInset: Dp = 0.dp,
    hInset: Dp = 0.dp,
): Modifier {
    if (!focused) return this
    val color = MaterialTheme.colorScheme.primary
    val strokePx = with(LocalDensity.current) { stroke.toPx() }
    val radiusPx = with(LocalDensity.current) { corner.toPx() }
    val v = with(LocalDensity.current) { vInset.toPx() }
    val h = with(LocalDensity.current) { hInset.toPx() }
    val inset = strokePx / 2 // stroke straddles the box edge — matches control outline
    return this.drawWithContent {
        drawContent()
        val left = h + inset
        val top = v + inset
        val w = size.width - 2 * h - strokePx
        val hh = size.height - 2 * v - strokePx
        if (w > 0 && hh > 0) {
            drawRoundRect(
                color = color,
                cornerRadius = CornerRadius(radiusPx),
                style = Stroke(width = strokePx),
                topLeft = Offset(left, top),
                size = Size(w, hh),
            )
        }
    }
}

/**
 * Ring only — component already has click/focus.
 * Pass [corner] equal to the control's visual corner (icon 8, chip 8, pill 24…).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.padFocusRing(
    focusRequester: FocusRequester? = null,
    corner: Dp = 8.dp,
    ringVInset: Dp = 0.dp,
    ringHInset: Dp = 0.dp,
    onFocus: (() -> Unit)? = null,
): Modifier {
    val bring = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }
    return this
        .bringIntoViewRequester(bring)
        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        .onFocusChanged { st ->
            focused = st.isFocused
            if (st.isFocused) {
                onFocus?.invoke()
                scope.launch {
                    try {
                        bring.bringIntoView()
                    } catch (_: Throwable) {
                    }
                }
            }
        }
        .focusRing(focused, corner = corner, vInset = ringVInset, hInset = ringHInset)
}

/**
 * Click + focus + ring. Clip uses [corner] so ripple and ring share the shape.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.padClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
    corner: Dp = 8.dp,
    onFocus: (() -> Unit)? = null,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val bring = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }
    val interactionFocused by interaction.collectIsFocusedAsState()
    return this
        .bringIntoViewRequester(bring)
        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        .focusable(enabled = enabled, interactionSource = interaction)
        .onFocusChanged { st ->
            focused = st.isFocused
            if (st.isFocused) {
                onFocus?.invoke()
                scope.launch {
                    try {
                        bring.bringIntoView()
                    } catch (_: Throwable) {
                    }
                }
            }
        }
        .clip(RoundedCornerShape(corner))
        .clickable(enabled = enabled, interactionSource = interaction, indication = ripple(), onClick = onClick)
        .focusRing(focused || interactionFocused, corner = corner)
}

/**
 * Icon button whose press/focus chrome matches [corner] (default 8dp rounded rect).
 * Use instead of IconButton when ring + highlight must agree.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PadIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    corner: Dp = 8.dp,
    focusRequester: FocusRequester? = null,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val bring = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }
    val interactionFocused by interaction.collectIsFocusedAsState()
    Box(
        modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .bringIntoViewRequester(bring)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .focusable(interactionSource = interaction)
            .onFocusChanged { st ->
                focused = st.isFocused
                if (st.isFocused) {
                    scope.launch {
                        try {
                            bring.bringIntoView()
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            .clip(RoundedCornerShape(corner))
            .clickable(interactionSource = interaction, indication = ripple(), onClick = onClick)
            .focusRing(focused || interactionFocused, corner = corner),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
