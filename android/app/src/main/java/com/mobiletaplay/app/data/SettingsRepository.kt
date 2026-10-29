package com.mobiletaplay.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class AppSettings(
    val virtualPad: Boolean = true,
    val gamepad: Boolean = true,
    val hidePadOnGamepad: Boolean = false,
    val mappingJson: String = "",
    /** Card grid columns on Home/List: 2 | 3 | 4 */
    val gridColumns: Int = 3,
)

/** Single source of truth for shell settings (replaces scattered Preferences access). */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val virtualPad = booleanPreferencesKey("mtp.virtualPad")
        val gamepad = booleanPreferencesKey("mtp.gamepad")
        val hidePadOnGamepad = booleanPreferencesKey("mtp.hidePadOnGamepad")
        val mapping = stringPreferencesKey("mtp.mapping")
        val gridColumns = intPreferencesKey("mtp.gridColumns")
    }

    val settings: Flow<AppSettings> = context.mtpDataStore.data.map { p ->
        AppSettings(
            virtualPad = p[Keys.virtualPad] ?: true,
            gamepad = p[Keys.gamepad] ?: true,
            hidePadOnGamepad = p[Keys.hidePadOnGamepad] ?: false,
            mappingJson = p[Keys.mapping] ?: "",
            gridColumns = (p[Keys.gridColumns] ?: 3).coerceIn(2, 4),
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setVirtualPad(enabled: Boolean) {
        context.mtpDataStore.edit { it[Keys.virtualPad] = enabled }
    }

    suspend fun setGamepad(enabled: Boolean) {
        context.mtpDataStore.edit { it[Keys.gamepad] = enabled }
    }

    suspend fun setHidePadOnGamepad(enabled: Boolean) {
        context.mtpDataStore.edit { it[Keys.hidePadOnGamepad] = enabled }
    }

    suspend fun setMapping(json: String) {
        context.mtpDataStore.edit { it[Keys.mapping] = json }
    }

    suspend fun setGridColumns(cols: Int) {
        context.mtpDataStore.edit { it[Keys.gridColumns] = cols.coerceIn(2, 4) }
    }

    suspend fun saveAll(settings: AppSettings) {
        context.mtpDataStore.edit {
            it[Keys.virtualPad] = settings.virtualPad
            it[Keys.gamepad] = settings.gamepad
            it[Keys.hidePadOnGamepad] = settings.hidePadOnGamepad
            it[Keys.mapping] = settings.mappingJson
            it[Keys.gridColumns] = settings.gridColumns.coerceIn(2, 4)
        }
    }

    companion object {
        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
    }
}
