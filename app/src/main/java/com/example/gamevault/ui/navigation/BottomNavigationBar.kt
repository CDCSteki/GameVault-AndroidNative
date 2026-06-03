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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gamevault.ui.theme.DarkNavySecondary
import com.example.gamevault.ui.theme.NeonCyan
import com.example.gamevault.ui.theme.NeonPurple
import com.example.gamevault.ui.theme.TextMuted

@Composable
fun BottomNavigationBar(navController: NavController) {
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
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonPurple.copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        containerColor = DarkNavySecondary,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp),
                        tint = if (isSelected) NeonPurple else TextMuted
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = if (isSelected) NeonPurple else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonPurple,
                    unselectedIconColor = TextMuted,
                    selectedTextColor = NeonPurple,
                    unselectedTextColor = TextMuted,
                    indicatorColor = NeonPurple.copy(alpha = 0.15f)
                )
            )
        }
    }
}