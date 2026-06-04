package com.example.gamevault.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gamevault.ui.theme.GVTheme

@Composable
fun BottomNavigationBar(navController: NavController) {
    val colors = GVTheme.colors

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Library,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(
                width = 1.dp,
                color = colors.border.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ),
        containerColor = colors.backgroundSecondary,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.titleResId),
                        modifier = Modifier.size(22.dp),
                        tint = if (isSelected) colors.accent else colors.textMuted
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.titleResId),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = if (isSelected) colors.accent else colors.textMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    unselectedIconColor = colors.textMuted,
                    selectedTextColor = colors.accent,
                    unselectedTextColor = colors.textMuted,
                    indicatorColor = colors.accent.copy(alpha = 0.15f)
                )
            )
        }
    }
}