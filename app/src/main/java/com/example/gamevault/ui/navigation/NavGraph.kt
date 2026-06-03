package com.example.gamevault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gamevault.di.AppContainer
import com.example.gamevault.ui.screens.auth.LoginScreen
import com.example.gamevault.ui.screens.auth.RegisterScreen
import com.example.gamevault.ui.screens.detail.GameDetailScreen
import com.example.gamevault.ui.screens.gamelist.GameListScreen
import com.example.gamevault.ui.screens.home.HomeScreen
import com.example.gamevault.ui.screens.library.LibraryScreen
import com.example.gamevault.ui.screens.profile.ProfileScreen
import com.example.gamevault.ui.screens.search.SearchScreen
import com.example.gamevault.ui.screens.settings.SettingsScreen
import com.example.gamevault.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavRoutes.Splash.route) {
            val isLoggedIn by appContainer.authRepository.isLoggedIn
                .collectAsState(initial = false)

            SplashScreen(
                isLoggedIn = isLoggedIn,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Login.route) {
            LoginScreen(
                authRepository = appContainer.authRepository,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.Register.route)
                }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                authRepository = appContainer.authRepository,
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.Home.route) {
            HomeScreen(
                gameRepository = appContainer.gameRepository,
                authRepository = appContainer.authRepository,
                onGameClick = { gameId ->
                    navController.navigate(NavRoutes.GameDetail.createRoute(gameId))
                },
                onViewAllClick = { listType ->
                    navController.navigate("game_list/$listType")
                }
            )
        }

        composable(NavRoutes.Search.route) {
            SearchScreen(
                searchRepository = appContainer.searchRepository,
                gameRepository = appContainer.gameRepository,
                onGameClick = { gameId ->
                    navController.navigate(NavRoutes.GameDetail.createRoute(gameId))
                }
            )
        }

        composable(NavRoutes.Library.route) {
            LibraryScreen(
                gameRepository = appContainer.gameRepository,
                onGameClick = { gameId ->
                    navController.navigate(NavRoutes.GameDetail.createRoute(gameId))
                }
            )
        }

        composable(NavRoutes.Profile.route) {
            ProfileScreen(
                authRepository = appContainer.authRepository,
                onLogout = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                appPreferences = appContainer.preferences,
                authRepository = appContainer.authRepository,
                searchRepository = appContainer.searchRepository,
                onAccountDeleted = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.GameDetail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId") ?: return@composable
            GameDetailScreen(
                gameId = gameId,
                gameRepository = appContainer.gameRepository,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "game_list/{listType}",
            arguments = listOf(
                navArgument("listType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listType = backStackEntry.arguments?.getString("listType") ?: return@composable
            GameListScreen(
                listType = listType,
                gameRepository = appContainer.gameRepository,
                onGameClick = { gameId ->
                    navController.navigate(NavRoutes.GameDetail.createRoute(gameId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}