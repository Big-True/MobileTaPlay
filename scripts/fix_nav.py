from pathlib import Path
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\MainActivity.kt")
c = p.read_text(encoding="utf-8")

old_home = """                        composable("home") {
                            HomeScreen(
                                onOpenDetail = { name -> nav.navigate("detail/$name") },
                                onOpenSettings = { nav.navigate("settings") },
                            )
                        }"""
new_home = """                        composable("home") {
                            HomeScreen(
                                onOpenDetail = { name -> nav.navigate("detail/$name") },
                                onOpenSettings = { nav.navigate("settings") },
                                onOpenSearch = { nav.navigate("search") },
                                onOpenList = { m -> nav.navigate("list/$m") },
                            )
                        }"""
if old_home in c:
    c = c.replace(old_home, new_home)
    print("home ok")
else:
    print("home block missing")

old_set = """                        composable("settings") {
                            SettingsScreen(onBack = { nav.popBackStack() })
                        }
                    }"""
new_set = """                        composable("settings") {
                            SettingsScreen(onBack = { nav.popBackStack() })
                        }
                        composable("search") {
                            SearchScreen(
                                initial = SearchQuery(),
                                onBack = { nav.popBackStack() },
                                onApply = { q ->
                                    nav.navigate("searchlist/${q.mode}/${q.sort}/${q.keyword}")
                                },
                            )
                        }
                        composable(
                            "list/{mode}",
                            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
                        ) { entry ->
                            val m = when (entry.arguments?.getString("mode")) {
                                "fav" -> ListMode.FAV
                                "cached" -> ListMode.CACHED
                                else -> ListMode.RECENT
                            }
                            ListScreen(
                                mode = m,
                                onBack = { nav.popBackStack() },
                                onOpenDetail = { n -> nav.navigate("detail/$n") },
                            )
                        }
                        composable(
                            "searchlist/{mode}/{sort}/{k}",
                            arguments = listOf(
                                navArgument("mode") { type = NavType.StringType },
                                navArgument("sort") { type = NavType.StringType },
                                navArgument("k") { type = NavType.StringType },
                            ),
                        ) { entry ->
                            ListScreen(
                                mode = ListMode.SEARCH,
                                keyword = entry.arguments?.getString("k").orEmpty(),
                                searchMode = entry.arguments?.getString("mode") ?: "title",
                                sort = entry.arguments?.getString("sort") ?: "hot",
                                onBack = { nav.popBackStack() },
                                onOpenDetail = { n -> nav.navigate("detail/$n") },
                            )
                        }
                    }"""
if old_set in c:
    c = c.replace(old_set, new_set)
    print("nav ok")
else:
    print("settings block missing")

if "import com.mobiletaplay.app.ui.list.ListMode" not in c:
    c = c.replace(
        "import com.mobiletaplay.app.ui.settings.SettingsScreen",
        "import com.mobiletaplay.app.ui.list.ListMode\nimport com.mobiletaplay.app.ui.list.ListScreen\nimport com.mobiletaplay.app.ui.search.SearchQuery\nimport com.mobiletaplay.app.ui.search.SearchScreen\nimport com.mobiletaplay.app.ui.settings.SettingsScreen",
    )

p.write_text(c, encoding="utf-8")
print("done")
