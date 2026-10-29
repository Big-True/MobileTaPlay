package com.mobiletaplay.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobiletaplay.app.ui.detail.DetailScreen
import com.mobiletaplay.app.game.GameActivity
import com.mobiletaplay.app.ui.home.HomeScreen
import com.mobiletaplay.app.ui.list.ListMode
import com.mobiletaplay.app.ui.list.ListScreen
import com.mobiletaplay.app.ui.search.SearchScreen
import com.mobiletaplay.app.ui.search.SearchQuery
import com.mobiletaplay.app.ui.settings.SettingsScreen
import com.mobiletaplay.app.ui.theme.MobileTaPlayTheme

class MainActivity : ComponentActivity() {
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        // Official: intercept first when binding — consume ALL keys (no source filter).
        if (com.mobiletaplay.app.ui.settings.BindController.active) {
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                com.mobiletaplay.app.ui.settings.dispatchBindKey(event.keyCode)
            }
            return true
        }

        // Map gamepad face/shoulder keys to keys Compose understands.
        val mappedCode = when (event.keyCode) {
            android.view.KeyEvent.KEYCODE_BUTTON_A -> android.view.KeyEvent.KEYCODE_DPAD_CENTER
            android.view.KeyEvent.KEYCODE_BUTTON_B -> android.view.KeyEvent.KEYCODE_BACK
            android.view.KeyEvent.KEYCODE_BUTTON_Y,
            android.view.KeyEvent.KEYCODE_BUTTON_START,
            android.view.KeyEvent.KEYCODE_MENU -> android.view.KeyEvent.KEYCODE_MENU
            android.view.KeyEvent.KEYCODE_BUTTON_L1 -> android.view.KeyEvent.KEYCODE_PAGE_UP
            android.view.KeyEvent.KEYCODE_BUTTON_R1 -> android.view.KeyEvent.KEYCODE_PAGE_DOWN
            else -> event.keyCode
        }

        // Shell pad: move Compose focus / back / menu before View focus steals arrows.
        if (com.mobiletaplay.app.ui.common.ShellPad.dispatch(mappedCode, event.action)) {
            return true
        }

        if (mappedCode != event.keyCode) {
            val mapped = android.view.KeyEvent(
                event.downTime,
                event.eventTime,
                event.action,
                mappedCode,
                event.repeatCount,
            )
            return super.dispatchKeyEvent(mapped)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (com.mobiletaplay.app.ui.settings.BindController.active) {
            com.mobiletaplay.app.ui.settings.dispatchBindKey(keyCode)
            return true
        }
        if (keyCode == android.view.KeyEvent.KEYCODE_BUTTON_B || keyCode == android.view.KeyEvent.KEYCODE_B) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (com.mobiletaplay.app.ui.settings.BindController.active) return true
        if (keyCode == android.view.KeyEvent.KEYCODE_BUTTON_B || keyCode == android.view.KeyEvent.KEYCODE_B) return true
        return super.onKeyUp(keyCode, event)
    }


    override fun onResume() {
        super.onResume()
        OrientationPrefs.apply(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            window.decorView.defaultFocusHighlightEnabled = false
        } catch (_: Exception) {
        }
        OrientationPrefs.apply(this)
        enableEdgeToEdge()
        setContent {
            MobileTaPlayTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onOpenDetail = { name -> nav.navigate("detail/$name") },
                                onOpenSettings = { nav.navigate("settings") },
                                onOpenSearch = { nav.navigate("search") },
                                onOpenList = { m -> nav.navigate("list/$m") },
                            )
                        }
                        composable(
                            "detail/{name}",
                            arguments = listOf(navArgument("name") { type = NavType.StringType }),
                        ) { entry ->
                            DetailScreen(
                                name = entry.arguments?.getString("name").orEmpty(),
                                onBack = { nav.popBackStack() },
                                onOpenSettings = { nav.navigate("settings") },
                                onStartGame = { url, title ->
                                    startActivity(
                                        GameActivity.intent(this@MainActivity, url, title)
                                            .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    )
                                },
                                onOpenAuthor = { author ->
                                    nav.navigate(
                                        "searchlist?mode=author&sort=hot&k=" + Uri.encode(author)
                                    )
                                },
                            )
                        }
                        composable("settings") {
                            SettingsScreen(onBack = { nav.popBackStack() })
                        }
                        composable("search") {
                            SearchScreen(
                                initial = SearchQuery(),
                                onBack = { nav.popBackStack() },
                                onApply = { q ->
                                    nav.navigate("searchlist?mode=" + q.mode + "&sort=" + q.sort + "&k=" + Uri.encode(q.keyword))
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
                            "searchlist?mode={mode}&sort={sort}&k={k}",
                            arguments = listOf(
                                navArgument("mode") { type = NavType.StringType },
                                navArgument("sort") { type = NavType.StringType },
                                navArgument("k") { type = NavType.StringType },
                            ),
                        ) { entry ->
                            ListScreen(
                                mode = ListMode.SEARCH,
                                keyword = try { java.net.URLDecoder.decode(entry.arguments?.getString("k") ?: "", "UTF-8") } catch (_: Exception) { entry.arguments?.getString("k").orEmpty() },
                                searchMode = entry.arguments?.getString("mode") ?: "title",
                                sort = entry.arguments?.getString("sort") ?: "hot",
                                onBack = { nav.popBackStack() },
                                onOpenDetail = { n -> nav.navigate("detail/$n") },
                            )
                        }
                    }
                }
            }
        }
    }
}


