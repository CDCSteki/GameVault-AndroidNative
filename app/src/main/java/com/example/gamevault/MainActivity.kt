package com.example.gamevault

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gamevault.ui.navigation.BottomNavigationBar
import com.example.gamevault.ui.navigation.NavGraph
import com.example.gamevault.ui.navigation.NavRoutes
import com.example.gamevault.ui.theme.AppTheme
import com.example.gamevault.ui.theme.GameVaultTheme
import com.example.gamevault.ui.util.LocaleHelper
import androidx.core.content.edit

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Citim limba sincron din SharedPreferences (DataStore e async, folosim SP pentru attachBaseContext)
        val prefs = newBase.getSharedPreferences("gamevault_locale", MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as GameVaultApplication).container

        setContent {
            val appTheme by appContainer.preferences.appTheme
                .collectAsState(initial = AppTheme.CYBER_DARK)
            val language by appContainer.preferences.language
                .collectAsState(initial = "en")

            // Când limba se schimbă, recreăm Activity pentru a reîncărca stringurile
            var lastLanguage by remember { mutableStateOf(language) }
            LaunchedEffect(language) {
                if (language != lastLanguage) {
                    // Salvăm în SharedPreferences pentru attachBaseContext la recreare
                    getSharedPreferences("gamevault_locale", MODE_PRIVATE)
                        .edit {
                            putString("language", language)
                        }
                    lastLanguage = language
                    recreate() // Recreăm Activity — stringurile se reîncarcă
                }
            }

            GameVaultTheme(darkTheme = true, appTheme = appTheme) {
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