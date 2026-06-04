package com.example.gamevault.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.gamevault.R

sealed class BottomNavItem(
    val route: String,
    @param:StringRes val titleResId: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = NavRoutes.Home.route,
        titleResId = R.string.nav_home,
        icon = Icons.Default.Home
    )
    object Search : BottomNavItem(
        route = NavRoutes.Search.route,
        titleResId = R.string.nav_search,
        icon = Icons.Default.Search
    )
    object Library : BottomNavItem(
        route = NavRoutes.Library.route,
        titleResId = R.string.nav_library,
        icon = Icons.Default.VideoLibrary
    )
    object Profile : BottomNavItem(
        route = NavRoutes.Profile.route,
        titleResId = R.string.nav_profile,
        icon = Icons.Default.Person
    )
    object Settings : BottomNavItem(
        route = NavRoutes.Settings.route,
        titleResId = R.string.nav_settings,
        icon = Icons.Default.Settings
    )
}