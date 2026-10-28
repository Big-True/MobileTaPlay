from pathlib import Path
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\list\ListScreen.kt")
c = p.read_text(encoding="utf-8")

c = c.replace(
    """data class ListUiState(
    val title: String = "",
    val towers: List<Tower> = emptyList(),
    val loading: Boolean = true,
    val error: String = "",
)""",
    """data class ListUiState(
    val title: String = "",
    val towers: List<Tower> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val loading: Boolean = true,
    val error: String = "",
)""",
)

c = c.replace(
    """                _ui.value = ListUiState(
                    title = title(),
                    towers = list,
                    loading = false,
                )""",
    """                _ui.value = ListUiState(
                    title = title(),
                    towers = list,
                    favorites = store.favorites().toSet(),
                    loading = false,
                )""",
)

c = c.replace(
    """                            TowerCard(
                                t = t,
                                fav = false,""",
    """                            TowerCard(
                                t = t,
                                fav = ui.favorites.contains(t.name),""",
)

p.write_text(c, encoding="utf-8")
print("list patched")
