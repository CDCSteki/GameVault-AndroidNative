package com.example.gamevault.data.repository

import com.example.gamevault.data.local.db.dao.SearchHistoryDao
import com.example.gamevault.data.local.entity.SearchHistoryEntity
import com.example.gamevault.data.local.preferences.AppPreferences
import com.example.gamevault.data.remote.api.Constants
import com.example.gamevault.data.remote.api.RawgApiService
import com.example.gamevault.data.remote.dto.GameDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first

class SearchRepository(
    private val searchHistoryDao: SearchHistoryDao,
    private val apiService: RawgApiService,
    private val appPreferences: AppPreferences
) {

    // --- REMOTE SEARCH ---
    suspend fun searchGames(
        query: String,
        genres: String? = null,
        platforms: String? = null,
        metacritic: String? = null,
        dates: String? = null,
        ordering: String? = null,
        page: Int = 1
    ): Result<List<GameDto>> {
        return try {
            val response = apiService.searchGames(
                apiKey = Constants.RAWG_API_KEY,
                query = query,
                genres = genres,
                platforms = platforms,
                metacritic = metacritic,
                dates = dates,
                ordering = ordering,
                page = page
            )
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- HISTORY ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> = appPreferences.loggedInUserId.flatMapLatest { userId ->
        if (userId != -1) searchHistoryDao.getRecentSearches(userId) else flowOf(emptyList())
    }

    suspend fun saveSearch(query: String) {
        if (query.isBlank()) return
        val userId = appPreferences.loggedInUserId.first()
        if (userId == -1) return

        if (searchHistoryDao.queryExists(query, userId) > 0) {
            searchHistoryDao.deleteByQuery(query, userId)
        }
        searchHistoryDao.insertSearch(
            SearchHistoryEntity(query = query, userId = userId)
        )
    }

    suspend fun deleteSearchById(id: Int) {
        val userId = appPreferences.loggedInUserId.first()
        searchHistoryDao.deleteById(id, userId)
    }

    suspend fun clearAllHistory() {
        val userId = appPreferences.loggedInUserId.first()
        searchHistoryDao.clearAllHistory(userId)
    }
}