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
        colors: List<Int> = emptyList(),
        searchMode: String = "title",
        searchStr: String = "",
    ): TowerPage = withContext(Dispatchers.IO) {
        val b = LIST.toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("page", page.toString())
            .addQueryParameter("sortmode", sortmode)
            .addQueryParameter("sections", JSONArray(sections).toString())
            .addQueryParameter("tags", JSONArray(tags).toString())
            .addQueryParameter("colors", JSONArray(colors).toString())
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
                        content = t.optString("content").ifBlank { null },
                        commentCount = t.opt("comment")?.toString()?.trim()?.toIntOrNull()
                            ?: t.optInt("comment"),
                        thumbUp = t.optInt("thumb_up"),
                        difficulty = t.optString("difficultrate").ifBlank { null },
                        uploadTime = t.optString("uploadtime").ifBlank { null },
                        updateTime = formatMaybeTime(
                            t.optString("lasttime").ifBlank { t.optString("updatetime") }
                        ),
                        recommend = t.optBoolean("recommend"),
                        similarIds = t.optString("similar_tower_ids")
                            .split(",").mapNotNull { it.trim().toIntOrNull() },
                        section = t.optInt("section"),
                        classify = t.optString("classify").ifBlank { null },
                        color = if (t.has("color")) t.optInt("color") else null,
                        competition = t.optInt("competition"),
                        difficulties = parseStringArray(t.opt("difficultyList")),
                        endings = parseStringArray(t.opt("endingList")),
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

    /** Load tower meta + comments from the tower page SSR blob. */
    suspend fun fetchTowerPage(name: String): Pair<Tower?, List<TowerComment>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("https://h5mota.com/tower/?name=" + java.net.URLEncoder.encode(name, "UTF-8"))
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) Mobile MobileTaPlay")
                .header("Accept", "text/html")
                .build()
            val html = client.newCall(req).execute().use { it.body?.string().orEmpty() }
            val marker = "data-ssr=\"tower\" type=\"application/json\">"
            val start = html.indexOf(marker)
            if (start < 0) return@withContext null to emptyList()
            val bodyStart = start + marker.length
            val end = html.indexOf("</script>", bodyStart)
            if (end < 0) return@withContext null to emptyList()
            val obj = JSONObject(html.substring(bodyStart, end).trim())
            val tower = parseTowerFromSsr(obj)
            val comments = obj.optJSONArray("comments")?.let { parseComments(it) } ?: emptyList()
            tower to comments
        } catch (_: Exception) {
            null to emptyList()
        }
    }

    private fun parseTowerFromSsr(root: JSONObject): Tower? {
        val tw = root.optJSONObject("tower") ?: JSONObject()
        val rating = root.optJSONObject("rating") ?: JSONObject()
        val name = tw.optString("name").ifBlank { root.optString("name") }
        if (name.isBlank()) return null

        // rating.difficultyrating = [score, ...]
        val diffArr = rating.optJSONArray("difficultyrating")
            ?: rating.optJSONObject("stat")?.optJSONArray("difficultyrating")
        val difficulty = diffArr?.optDouble(0)?.takeIf { !it.isNaN() }?.toString()

        val commentCount = root.optJSONArray("comments")?.length()
            ?: tw.opt("comment")?.toString()?.toIntOrNull()
            ?: 0

        return Tower(
            id = tw.optString("id").toIntOrNull() ?: root.optString("id").toIntOrNull() ?: name.hashCode(),
            name = name,
            title = tw.optString("title").ifBlank { name },
            author = tw.optString("author"),
            image = tw.optString("image").ifBlank { null },
            floors = tw.optString("floors").ifBlank { null },
            people = tw.optString("people").ifBlank { null },
            win = tw.optString("win").ifBlank { null },
            rate = tw.optString("rate").ifBlank { null },
            tags = parseStringArray(tw.opt("tags")),
            description = tw.optString("description").ifBlank { null },
            text = tw.optString("text").ifBlank { null },
            content = tw.optString("content").ifBlank { null },
            link = tw.optString("link").ifBlank { null },
            previews = parseStringArray(tw.opt("previews")),
            commentCount = commentCount,
            thumbUp = tw.optString("thumb_up").toIntOrNull() ?: 0,
            difficulty = difficulty,
            uploadTime = tw.optString("uploadtime").ifBlank { null },
            updateTime = formatMaybeTime(tw.optString("updatetime").ifBlank { tw.optString("createtime") }),
            recommend = tw.optBoolean("recommend"),
            similarIds = emptyList(),
            section = tw.optString("section").toIntOrNull() ?: 0,
            classify = tw.optString("tag").ifBlank { null },
            color = root.optJSONObject("rating")?.optJSONArray("colorrate")?.optInt(0),
            competition = tw.optString("competition").toIntOrNull() ?: 0,
            difficulties = emptyList(),
            endings = emptyList(),
        )
    }

    /**
     * Comments live in the tower page SSR blob (discussion API is separately gated).
     * Parses data-ssr="tower" JSON.
     */
    suspend fun fetchComments(name: String): List<TowerComment> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("https://h5mota.com/tower/?name=" + java.net.URLEncoder.encode(name, "UTF-8"))
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) Mobile MobileTaPlay")
                .header("Accept", "text/html")
                .build()
            val html = client.newCall(req).execute().use { it.body?.string().orEmpty() }
            val marker = "data-ssr=\"tower\" type=\"application/json\">"
            val start = html.indexOf(marker)
            if (start < 0) return@withContext emptyList()
            val bodyStart = start + marker.length
            val end = html.indexOf("</script>", bodyStart)
            if (end < 0) return@withContext emptyList()
            val obj = JSONObject(html.substring(bodyStart, end).trim())
            val arr = obj.optJSONArray("comments") ?: return@withContext emptyList()
            parseComments(arr)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseComments(arr: JSONArray): List<TowerComment> = buildList {
        for (i in 0 until arr.length()) {
            val c = arr.optJSONObject(i) ?: continue
            val a = c.optJSONObject("author")
            val replies = c.optJSONArray("replies")?.let { parseComments(it) } ?: emptyList()
            add(
                TowerComment(
                    id = c.optInt("id"),
                    authorName = a?.optString("name") ?: "匿名",
                    authorId = a?.optInt("id") ?: 0,
                    text = c.optString("comment"),
                    timestamp = formatTime(c.optLong("timestamp")),
                    loveNum = c.optInt("loveNum"),
                    replies = replies,
                )
            )
        }
    }

    /** Accepts unix seconds or already-formatted date string. */
    private fun formatMaybeTime(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val n = raw.trim().toLongOrNull()
        return if (n != null && n > 1_000_000_000L) formatTime(n) else raw
    }

    private fun formatTime(sec: Long): String {
        if (sec <= 0) return ""
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(sec * 1000))
    }

    fun playUrl(tower: Tower): String {
        val path = tower.link?.takeIf { it.startsWith("/") } ?: "/games/${tower.name}/"
        return "https://h5mota.com$path"
    }
}
