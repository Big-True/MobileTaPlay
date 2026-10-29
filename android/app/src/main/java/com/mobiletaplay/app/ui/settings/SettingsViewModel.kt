package com.mobiletaplay.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletaplay.app.data.AppSettings
import com.mobiletaplay.app.data.SettingsRepository
import com.mobiletaplay.app.input.MappingCodec
import com.mobiletaplay.app.input.PadActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = true,
    val mapping: Map<String, String> = emptyMap(),
    val bindingAction: String? = null,
    val error: String = "",
    val gridColumns: Int = 3,
)

/** L1: settings + pad binding state lives in one ViewModel. */
class SettingsViewModel(
    private val repo: SettingsRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val s = repo.current()
            val map = MappingCodec.decode(s.mappingJson)
            PadActions.applyMap(map)
            _ui.value = SettingsUiState(
                loading = false,
                mapping = map,
                gridColumns = s.gridColumns,
            )
        }
    }

    fun startBind(actionId: String) {
        _ui.value = _ui.value.copy(
            bindingAction = if (_ui.value.bindingAction == actionId) null else actionId,
        )
    }

    /** Called when a pad button is captured. */
    fun onBindButton(padButton: String?) {
        val action = _ui.value.bindingAction ?: return
        if (padButton == null) return
        PadActions.setButton(action, padButton)
        persist()
        _ui.value = _ui.value.copy(mapping = PadActions.all(), bindingAction = null)
    }

    fun setGridColumns(cols: Int) {
        val c = cols.coerceIn(2, 4)
        _ui.value = _ui.value.copy(gridColumns = c)
        viewModelScope.launch {
            val s = repo.current()
            repo.saveAll(s.copy(gridColumns = c))
        }
    }

    fun resetDefaults() {
        PadActions.reset()
        persist()
        _ui.value = _ui.value.copy(mapping = PadActions.all(), bindingAction = null)
    }

    private fun persist() {
        viewModelScope.launch {
            val json = MappingCodec.encode(PadActions.all())
            val s = repo.current()
            repo.saveAll(s.copy(mappingJson = json))
        }
    }
}
