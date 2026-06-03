package com.example.gamevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gamevault.ui.navigation.BottomNavigationBar
import com.example.gamevault.ui.navigation.NavGraph
import com.example.gamevault.ui.navigation.NavRoutes
import com.example.gamevault.ui.theme.GameVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as GameVaultApplication).container

        setContent {
            val isDarkTheme by appContainer.preferences.isDarkTheme
                .collectAsState(initial = true)
            val appTheme by appContainer.preferences.appTheme
                .collectAsState(initial = com.example.gamevault.ui.theme.AppTheme.CYBER_DARK)

            GameVaultTheme(darkTheme = isDarkTheme, appTheme = appTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute !in listOf(
                    NavRoutes.Login.route,
                    NavRoutes.Register.route,
                    NavRoutes.Splash.route
                ) && !currentRoute.orEmpty().startsWith("game_detail")
                        && !currentRoute.orEmpty().startsWith("game_list")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        startDestination = NavRoutes.Splash.route,
                        appContainer = appContainer,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}