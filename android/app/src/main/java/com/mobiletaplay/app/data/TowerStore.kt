package com.mobiletaplay.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.json.JSONObject

/** Local tower snapshots for favorites / recent / cached pages. */
class TowerStore(private val context: Context) {

    private object K {
        val fav = stringPreferencesKey("mtp.favorites")
        val recent = stringPreferencesKey("mtp.recent")
        val cached = stringPreferencesKey("mtp.cached")
        val snapshots = stringPreferencesKey("mtp.snapshots")
    }

    suspend fun favorites(): List<String> = parseNames(context.mtpDataStore.data.first()[K.fav])
    suspend fun recent(): List<String> = parseNames(context.mtpDataStore.data.first()[K.recent])
    suspend fun cached(): List<String> = parseNames(context.mtpDataStore.data.first()[K.cached])

    suspend fun snapshot(name: String): Tower? {
        val raw = context.mtpDataStore.data.first()[K.snapshots] ?: return null
        return try {
            val o = JSONObject(raw).optJSONObject(name) ?: return null
            Tower(
                id = o.optInt("id"),
                name = o.optString("name"),
                title = o.optString("title"),
                author = o.optString("author"),
                image = o.optString("image").ifBlank { null },
                floors = o.opt("floors")?.toString(),
                people = o.opt("people")?.toString(),
                win = o.opt("win")?.toString(),
                rate = o.opt("rate")?.toString(),
                                description = o.optString("description").ifBlank { null },
                text = o.optString("text").ifBlank { null },
                link = o.optString("link").ifBlank { null },
                tags = parseArr(o.optString("tags")),
                previews = parseArr(o.optString("previews")),
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun isCached(name: String): Boolean = cached().contains(name)

    suspend fun cacheTower(t: Tower) {
        val raw = context.mtpDataStore.data.first()[K.snapshots]
        val root = try {
            if (raw.isNullOrBlank()) JSONObject() else JSONObject(raw)
        } catch (_: Exception) {
            JSONObject()
        }
        val o = JSONObject()
        o.put("id", t.id)
        o.put("name", t.name)
        o.put("title", t.title)
        o.put("author", t.author)
        o.put("image", t.image ?: "")
        o.put("floors", t.floors ?: "")
        o.put("people", t.people ?: "")
        o.put("win", t.win ?: "")
        o.put("rate", t.rate ?: "")
        o.put("description", t.description ?: "")
        o.put("text", t.text ?: "")
        o.put("link", t.link ?: "")
        o.put("previews", org.json.JSONArray(t.previews))
        o.put("tags", org.json.JSONArray(t.tags))
        root.put(t.name, o)
        val names = (cached() + t.name).distinct()
        context.mtpDataStore.edit {
            it[K.snapshots] = root.toString()
            it[K.cached] = names.joinToString(",")
        }
    }

    suspend fun removeCache(name: String) {
        val names = cached().filter { it != name }
        val raw = context.mtpDataStore.data.first()[K.snapshots]
        val root = try {
            if (raw.isNullOrBlank()) JSONObject() else JSONObject(raw)
        } catch (_: Exception) {
            JSONObject()
        }
        root.remove(name)
        context.mtpDataStore.edit {
            it[K.cached] = names.joinToString(",")
            it[K.snapshots] = root.toString()
        }
    }

    suspend fun toggleFavorite(name: String) {
        val cur = favorites().toMutableSet()
        if (!cur.add(name)) cur.remove(name)
        context.mtpDataStore.edit { it[K.fav] = cur.joinToString(",") }
    }

    suspend fun pushRecent(name: String) {
        val cur = recent().toMutableList().apply {
            remove(name)
            add(0, name)
            if (size > 30) removeAt(size - 1)
        }
        context.mtpDataStore.edit { it[K.recent] = cur.joinToString(",") }
    }

    private fun parseArr(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val a = org.json.JSONArray(raw)
            (0 until a.length()).map { a.optString(it) }.filter { it.isNotEmpty() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseNames(raw: String?): List<String> =
        raw?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    companion object {
        @Volatile
        private var instance: TowerStore? = null
        fun get(context: Context): TowerStore =
            instance ?: synchronized(this) {
                instance ?: TowerStore(context.applicationContext).also { instance = it }
            }
    }
}
