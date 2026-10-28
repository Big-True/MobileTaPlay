from pathlib import Path
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\data\TowerApi.kt")
c = p.read_text(encoding="utf-8")
old = """        if (searchStr.isNotBlank()) {
            b.addQueryParameter("searchMode", searchMode)
            b.addQueryParameter("searchStr", searchStr.trim())
        }"""
new = """        if (searchStr.isNotBlank()) {
            // h5mota API uses mode name as query key: title= / author= / card=
            val key = when (searchMode) {
                "author" -> "author"
                "card" -> "card"
                else -> "title"
            }
            b.addQueryParameter(key, searchStr.trim())
        }"""
if old in c:
    c = c.replace(old, new)
    print("api ok")
else:
    print("block missing")
p.write_text(c, encoding="utf-8")
