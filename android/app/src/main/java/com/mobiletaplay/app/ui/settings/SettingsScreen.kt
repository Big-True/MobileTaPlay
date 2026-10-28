package com.mobiletaplay.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mobiletaplay.app.data.SettingsRepository
import com.mobiletaplay.app.input.PadActions
import com.mobiletaplay.app.ui.about.AboutScreen

/** Shared: activities feed pad keys here while binding. */
object BindController {
    @Volatile
    var onPadButton: ((String) -> Unit)? = null

    @Volatile
    var active = false

    fun acceptKeyCode(keyCode: Int): Boolean {
        if (!active) return false
        val pad = PadActions.padName(keyCode) ?: return true
        onPadButton?.invoke(pad)
        return true
    }
}

fun dispatchBindKey(keyCode: Int): Boolean = BindController.acceptKeyCode(keyCode)

val pendingBindControl: String?
    get() = if (BindController.active) "binding" else null

private class SettingsVmFactory(private val repo: SettingsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repo) as T
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm: SettingsViewModel = viewModel(
        factory = SettingsVmFactory(SettingsRepository.get(context)),
    )
    var showAbout by remember { mutableStateOf(false) }
    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
    } else {
        SettingsScreenContent(onBack = onBack, vm = vm, onOpenAbout = { showAbout = true })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    onBack: () -> Unit,
    vm: SettingsViewModel,
    onOpenAbout: () -> Unit = {},
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(ui.bindingAction) {
        BindController.active = ui.bindingAction != null
        BindController.onPadButton = { pad -> vm.onBindButton(pad) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = {
                        BindController.active = false
                        onBack()
                    }) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (ui.bindingAction != null)
                    "请按手柄键绑定「${PadActions.actions.firstOrNull { it.id == ui.bindingAction }?.label}」…"
                else
                    "点左侧操作，再按手柄键绑定",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PadActions.actions.forEach { action ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { vm.startBind(action.id) },
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(action.label, style = MaterialTheme.typography.titleMedium)
                        val btn = ui.mapping[action.id].orEmpty()
                        Text(
                            btn.ifEmpty { "未绑定" },
                            color = if (ui.bindingAction == action.id)
                                MaterialTheme.colorScheme.primary
                            else if (btn.isEmpty())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { vm.resetDefaults() }) {
                Text("恢复默认映射")
            }
            TextButton(onClick = onOpenAbout) {
                Text("关于 MobileTaPlay")
            }
        }
    }
}
