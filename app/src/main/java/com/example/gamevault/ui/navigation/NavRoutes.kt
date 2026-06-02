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
}