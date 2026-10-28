from pathlib import Path

p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\data\TowerStore.kt")
c = p.read_text(encoding="utf-8")

c = c.replace(
    "previews = emptyList(),",
    'previews = parseArr(o.optString("previews")), tags = parseArr(o.optString("tags")),',
)
c = c.replace(
    'o.put("link", t.link ?: "")',
    'o.put("link", t.link ?: "")\n        o.put("previews", org.json.JSONArray(t.previews))\n        o.put("tags", org.json.JSONArray(t.tags))',
)

if "private fun parseArr" not in c:
    c = c.replace(
        "    private fun parseNames",
        """    private fun parseArr(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val a = org.json.JSONArray(raw)
            (0 until a.length()).map { a.optString(it) }.filter { it.isNotEmpty() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseNames""",
    )

if "fun isCached" not in c:
    c = c.replace(
        "    suspend fun cacheTower(t: Tower) {",
        "    suspend fun isCached(name: String): Boolean = cached().contains(name)\n\n    suspend fun cacheTower(t: Tower) {",
    )

if "fun removeCache" not in c:
    c = c.replace(
        "    suspend fun toggleFavorite(name: String) {",
        """    suspend fun removeCache(name: String) {
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

    suspend fun toggleFavorite(name: String) {""",
    )

p.write_text(c, encoding="utf-8")
print("store ok")
