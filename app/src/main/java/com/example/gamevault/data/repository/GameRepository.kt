package com.example.gamevault.data.repository

import com.example.gamevault.data.local.db.dao.GameDao
import com.example.gamevault.data.local.entity.GameEntity
import com.example.gamevault.data.remote.api.Constants
import com.example.gamevault.data.remote.api.RawgApiService
import com.example.gamevault.data.remote.dto.GameDetailDto
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.remote.dto.GameMoviesResponse
import com.example.gamevault.data.remote.dto.GameScreenshotsResponse
import com.example.gamevault.ui.util.toPlaytimeString
import com.example.gamevault.ui.util.firstGenre
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameDao: GameDao,
    private val apiService: RawgApiService
) {

    // --- REMOTE ---
    suspend fun getGamesThisYear(dates: String): Result<List<GameDto>> = safeApiCall {
        apiService.getGamesThisYear(
            apiKey = Constants.RAWG_API_KEY,
            dates = dates
        ).results
    }

    suspend fun getAllTimeTopGames(): Result<List<GameDto>> = safeApiCall {
        apiService.getAllTimeTopGames(
            apiKey = Constants.RAWG_API_KEY
        ).results
    }

    suspend fun getGamesByGenre(genre: String): Result<List<GameDto>> = safeApiCall {
        apiService.getGamesByGenre(
            apiKey = Constants.RAWG_API_KEY,
            genres = genre
        ).results
    }

    suspend fun getPopularGames(): Result<List<GameDto>> = safeApiCall {
        apiService.getPopularGames(
            apiKey = Constants.RAWG_API_KEY
        ).results
    }

    suspend fun getGameDetails(gameId: Int): Result<GameDetailDto> = safeApiCall {
        apiService.getGameDetails(
            gameId = gameId,
            apiKey = Constants.RAWG_API_KEY
        )
    }

    suspend fun getGameMovies(gameId: Int): Result<GameMoviesResponse> = safeApiCall {
        apiService.getGameMovies(
            gameId = gameId,
            apiKey = Constants.RAWG_API_KEY
        )
    }

    suspend fun getGameScreenshots(gameId: Int): Result<GameScreenshotsResponse> = safeApiCall {
        apiService.getGameScreenshots(
            gameId = gameId,
            apiKey = Constants.RAWG_API_KEY
        )
    }

    // --- LOCAL - COLLECTION ---
    fun getCollection(): Flow<List<GameEntity>> = gameDao.getCollection()

    fun getPlayedGames(): Flow<List<GameEntity>> = gameDao.getPlayedGames()

    fun getNotPlayedGames(): Flow<List<GameEntity>> = gameDao.getNotPlayedGames()

    fun filterCollection(genre: String?): Flow<List<GameEntity>> =
        gameDao.filterCollection(genre)

    suspend fun addToCollection(game: GameEntity) {
        val existing = gameDao.getGameById(game.rawgId)
        if (existing != null) {
            gameDao.updateCollectionStatus(game.rawgId, true)
            // Daca era in wishlist, il scoatem
            gameDao.updateWishlistStatus(game.rawgId, false)
        } else {
            gameDao.insertGame(
                game.copy(
                    isInCollection = true,
                    isInWishlist = false
                )
            )
        }
    }

    suspend fun removeFromCollection(rawgId: Int) {
        gameDao.updateCollectionStatus(rawgId, false)
    }

    suspend fun updatePlayedStatus(rawgId: Int, isPlayed: Boolean) {
        gameDao.updatePlayedStatus(rawgId, isPlayed)
    }

    // --- LOCAL - WISHLIST ---
    fun getWishlist(): Flow<List<GameEntity>> = gameDao.getWishlist()

    fun filterWishlist(genre: String?): Flow<List<GameEntity>> =
        gameDao.filterWishlist(genre)

    suspend fun addToWishlist(game: GameEntity) {
        val existing = gameDao.getGameById(game.rawgId)
        if (existing != null) {
            gameDao.updateWishlistStatus(game.rawgId, true)
        } else {
            gameDao.insertGame(
                game.copy(
                    isInWishlist = true,
                    isInCollection = false
                )
            )
        }
    }

    suspend fun removeFromWishlist(rawgId: Int) {
        gameDao.updateWishlistStatus(rawgId, false)
    }

    // --- LOCAL - RATINGS & NOTES ---
    suspend fun updateUserRating(rawgId: Int, rating: Float) {
        gameDao.updateUserRating(rawgId, rating)
    }

    suspend fun updateUserNotes(rawgId: Int, notes: String?) {
        gameDao.updateUserNotes(rawgId, notes)
    }

    // --- LOCAL - OBSERVE ---
    fun observeGameById(rawgId: Int): Flow<GameEntity?> =
        gameDao.observeGameById(rawgId)

    suspend fun getGameById(rawgId: Int): GameEntity? =
        gameDao.getGameById(rawgId)

    // --- HELPER ---
    fun GameDetailDto.toEntity(): GameEntity = GameEntity(
        rawgId = id,
        name = name,
        coverImageUrl = backgroundImage,
        backgroundImageUrl = backgroundImage,
        description = descriptionRaw,
        developer = developers?.firstOrNull()?.name,
        releaseDate = released,
        platforms = platforms?.joinToString(",") { it.platform.name },
        genres = genres?.joinToString(",") { it.name },
        storageSize = playtime?.toPlaytimeString(),
        rating = rating
    )


    fun GameDto.toEntity(): GameEntity = GameEntity(
        rawgId = id,
        name = name,
        coverImageUrl = backgroundImage,
        backgroundImageUrl = backgroundImage,
        description = null,
        developer = null,
        releaseDate = released,
        platforms = platforms?.joinToString(",") { it.platform.name },
        genres = genres?.joinToString(",") { it.name },
        storageSize = null,
        rating = rating
    )

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}