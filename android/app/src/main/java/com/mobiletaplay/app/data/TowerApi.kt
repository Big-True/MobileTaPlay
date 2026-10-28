package com.mobiletaplay.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object TowerApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private const val LIST = "https://h5mota.com/backend/towers/list.php"

    suspend fun fetchTowers(
        page: Int = 1,
        sortmode: String = "hot",
        sections: List<Int> = listOf(0, 1, 2),
        tags: List<String> = emptyList(),
        searchMode: String = "title",
        searchStr: String = "",
    ): TowerPage = withContext(Dispatchers.IO) {
        val b = LIST.toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("sortmode", sortmode)
            .addQueryParameter("sections", JSONArray(sections).toString())
            .addQueryParameter("tags", JSONArray(tags).toString())
            .addQueryParameter("colors", "[]")
        if (searchStr.isNotBlank()) {
            // h5mota API uses mode name as query key: title= / author= / card=
            val key = when (searchMode) {
                "author" -> "author"
                "card" -> "card"
                else -> "title"
            }
            b.addQueryParameter(key, searchStr.trim())
        }
        val req = Request.Builder().url(b.build()).header("Accept", "application/json").build()
        client.newCall(req).execute().use { res ->
            val body = res.body?.string() ?: error("empty body")
            parsePage(JSONObject(body))
        }
    }

    private fun parsePage(o: JSONObject): TowerPage {
        val arr: JSONArray = o.optJSONArray("towers") ?: JSONArray()
        val towers = buildList {
            for (i in 0 until arr.length()) {
                val t = arr.getJSONObject(i)
                add(
                    Tower(
                        id = t.optInt("id"),
                        name = t.optString("name"),
                        title = t.optString("title"),
                        author = t.optString("author"),
                        image = t.optString("image").ifBlank { null },
                        floors = t.opt("floors")?.toString(),
                        people = t.opt("people")?.toString(),
                        win = t.opt("win")?.toString(),
                        rate = t.opt("rate")?.toString(),
                        tags = parseStringArray(t.opt("tags")),
                        description = t.optString("description").ifBlank { null },
                        text = t.optString("text").ifBlank { null },
                        link = t.optString("link").ifBlank { null },
                        previews = parseStringArray(t.opt("previews")),
                    )
                )
            }
        }
        return TowerPage(
            code = o.optInt("code"),
            message = o.optString("message"),
            total = o.optInt("total"),
            page = o.optInt("page"),
            towers = towers,
        )
    }

    private fun parseStringArray(v: Any?): List<String> {
        return when (v) {
            is JSONArray -> buildList {
                for (i in 0 until v.length()) {
                    val s = v.optString(i)
                    if (s.isNotBlank()) add(s)
                }
            }
            is String -> {
                if (v.isBlank()) emptyList()
                else try {
                    parseStringArray(JSONArray(v))
                } catch (_: Exception) {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }

    fun playUrl(tower: Tower): String {
        val path = tower.link?.takeIf { it.startsWith("/") } ?: "/games/${tower.name}/"
        return "https://h5mota.com$path"
    }
}
