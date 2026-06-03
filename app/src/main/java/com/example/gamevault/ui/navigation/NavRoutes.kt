package com.example.gamevault.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Home : NavRoutes("home")
    object Search : NavRoutes("search")
    object Library : NavRoutes("library")
    object Profile : NavRoutes("profile")
    object Settings : NavRoutes("settings")
    object GameDetail : NavRoutes("game_detail/{gameId}") {
        fun createRoute(gameId: Int) = "game_detail/$gameId"
    }
    object GameList : NavRoutes("game_list/{listType}") {
        fun createRoute(listType: String) = "game_list/$listType"
        const val TYPE_THIS_YEAR = "this_year"
        const val TYPE_ALL_TIME = "all_time"
        fun createGenreRoute(genre: String) = "game_list/genre_$genre"
    }
}