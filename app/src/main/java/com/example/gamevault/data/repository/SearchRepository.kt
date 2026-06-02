package com.example.gamevault.data.repository

import com.example.gamevault.data.local.db.dao.SearchHistoryDao
import com.example.gamevault.data.local.entity.SearchHistoryEntity
import com.example.gamevault.data.remote.api.Constants
import com.example.gamevault.data.remote.api.RawgApiService
import com.example.gamevault.data.remote.dto.GameDto
import kotlinx.coroutines.flow.Flow

class SearchRepository(
    private val searchHistoryDao: SearchHistoryDao,
    private val apiService: RawgApiService
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
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> =
        searchHistoryDao.getRecentSearches()

    suspend fun saveSearch(query: String) {
        if (query.isBlank()) return
        if (searchHistoryDao.queryExists(query) > 0) {
            searchHistoryDao.deleteByQuery(query)
        }
        searchHistoryDao.insertSearch(
            SearchHistoryEntity(query = query)
        )
    }

    suspend fun deleteSearchById(id: Int) {
        searchHistoryDao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        searchHistoryDao.clearAllHistory()
    }
}