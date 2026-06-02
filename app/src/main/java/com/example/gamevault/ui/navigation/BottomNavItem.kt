package com.example.gamevault.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = NavRoutes.Home.route,
        title = "HOME",
        icon = Icons.Default.Home
    )
    object Search : BottomNavItem(
        route = NavRoutes.Search.route,
        title = "SEARCH",
        icon = Icons.Default.Search
    )
    object Library : BottomNavItem(
        route = NavRoutes.Library.route,
        title = "LIBRARY",
        icon = Icons.Default.VideoLibrary
    )
    object Profile : BottomNavItem(
        route = NavRoutes.Profile.route,
        title = "PROFILE",
        icon = Icons.Default.Person
    )
    object Settings : BottomNavItem(
        route = NavRoutes.Settings.route,
        title = "SETTINGS",
        icon = Icons.Default.Settings
    )
}