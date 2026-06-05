package com.example.gamevault.di

import android.content.Context
import com.example.gamevault.data.local.db.GameVaultDatabase
import com.example.gamevault.data.local.preferences.AppPreferences
import com.example.gamevault.data.remote.api.RetrofitInstance
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.data.repository.SearchRepository

class AppContainer(context: Context) {

    private val database = GameVaultDatabase.getInstance(context)
    private val appPreferences = AppPreferences(context)
    private val apiService = RetrofitInstance.api

    val authRepository = AuthRepository(
        userDao = database.userDao(),
        appPreferences = appPreferences
    )

    val gameRepository = GameRepository(
        gameDao = database.gameDao(),
        userDao = database.userDao(),
        apiService = apiService,
        appPreferences = appPreferences
    )

    val searchRepository = SearchRepository(
        searchHistoryDao = database.searchHistoryDao(),
        apiService = apiService,
        appPreferences = appPreferences
    )

    val preferences = appPreferences
}