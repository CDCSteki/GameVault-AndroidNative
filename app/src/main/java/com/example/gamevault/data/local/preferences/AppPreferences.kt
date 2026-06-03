package com.example.gamevault.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gamevault.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gamevault_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")
        private val KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_APP_THEME = stringPreferencesKey("app_theme")
    }

    // --- AUTH ---
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_IS_LOGGED_IN] ?: false }

    val loggedInUserId: Flow<Int> = context.dataStore.data
        .map { it[KEY_LOGGED_IN_USER_ID] ?: -1 }

    suspend fun saveLoggedInUser(userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN_USER_ID] = userId
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun clearLoggedInUser() {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN_USER_ID] = -1
            prefs[KEY_IS_LOGGED_IN] = false
        }
    }

    // --- THEME ---
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_IS_DARK_THEME] ?: true }

    val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { prefs ->
            when (prefs[KEY_APP_THEME]) {
                AppTheme.OCEAN_BLUE.name -> AppTheme.OCEAN_BLUE
                AppTheme.FOREST_GREEN.name -> AppTheme.FOREST_GREEN
                AppTheme.SUNSET.name -> AppTheme.SUNSET
                AppTheme.MIDNIGHT_RED.name -> AppTheme.MIDNIGHT_RED
                else -> AppTheme.CYBER_DARK
            }
        }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[KEY_IS_DARK_THEME] = isDark }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_THEME] = theme.name
            prefs[KEY_IS_DARK_THEME] = theme != AppTheme.CYBER_DARK
        }
    }

    // --- LANGUAGE ---
    val language: Flow<String> = context.dataStore.data
        .map { it[KEY_LANGUAGE] ?: "en" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }
}