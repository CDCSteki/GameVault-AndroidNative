package com.example.gamevault.data.repository

import com.example.gamevault.data.local.db.dao.GameDao
import com.example.gamevault.data.local.db.dao.UserDao
import com.example.gamevault.data.local.entity.GameEntity
import com.example.gamevault.data.local.entity.PlayStatus
import com.example.gamevault.data.local.entity.UserEntity
import com.example.gamevault.data.local.preferences.AppPreferences
import com.example.gamevault.data.remote.api.Constants
import com.example.gamevault.data.remote.api.RawgApiService
import com.example.gamevault.data.remote.dto.GameDetailDto
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.remote.dto.GameMoviesResponse
import com.example.gamevault.data.remote.dto.GameScreenshotsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameRepository(
    private val gameDao: GameDao,
    private val userDao: UserDao,
    private val apiService: RawgApiService,
    private val appPreferences: AppPreferences
) {

    // --- REMOTE ---
    suspend fun getGamesThisYear(dates: String, pageSize: Int = 10): Result<List<GameDto>> = safeApiCall {
        apiService.getGamesThisYear(
            apiKey = Constants.RAWG_API_KEY,
            dates = dates,
            pageSize = pageSize
        ).results
    }

    suspend fun getAllTimeTopGames(pageSize: Int = 10): Result<List<GameDto>> = safeApiCall {
        apiService.getAllTimeTopGames(
            apiKey = Constants.RAWG_API_KEY,
            pageSize = pageSize
        ).results
    }

    suspend fun getGamesByGenre(genre: String, pageSize: Int = 10): Result<List<GameDto>> = safeApiCall {
        apiService.getGamesByGenre(
            apiKey = Constants.RAWG_API_KEY,
            genres = genre,
            pageSize = pageSize
        ).results
    }

    suspend fun getPopularGames(pageSize: Int = 10): Result<List<GameDto>> = safeApiCall {
        apiService.getPopularGames(
            apiKey = Constants.RAWG_API_KEY,
            pageSize = pageSize
        ).results
    }

    suspend fun getGameDetails(gameId: Int): Result<GameDetailDto> = safeApiCall {
        apiService.getGameDetails(gameId = gameId, apiKey = Constants.RAWG_API_KEY)
    }

    suspend fun getGameMovies(gameId: Int): Result<GameMoviesResponse> = safeApiCall {
        apiService.getGameMovies(gameId = gameId, apiKey = Constants.RAWG_API_KEY)
    }

    suspend fun getGameScreenshots(gameId: Int): Result<GameScreenshotsResponse> = safeApiCall {
        apiService.getGameScreenshots(gameId = gameId, apiKey = Constants.RAWG_API_KEY)
    }

    // --- LOCAL - COLLECTION ---
    fun getCollection(): Flow<List<GameEntity>> = gameDao.getCollection()
    fun getPlayedGames(): Flow<List<GameEntity>> = gameDao.getPlayedGames()
    fun getNotPlayedGames(): Flow<List<GameEntity>> = gameDao.getNotPlayedGames()
    fun getPlayingGames(): Flow<List<GameEntity>> = gameDao.getPlayingGames()
    fun filterCollection(genre: String?): Flow<List<GameEntity>> = gameDao.filterCollection(genre)

    suspend fun addToCollection(game: GameEntity): AddToCollectionResult {
        val existing = gameDao.getGameById(game.rawgId)
        return when {
            existing?.isInCollection == true -> AddToCollectionResult.AlreadyInCollection
            else -> {
                if (existing != null) {
                    gameDao.updateCollectionStatus(game.rawgId, true)
                    gameDao.updateWishlistStatus(game.rawgId, false)
                } else {
                    gameDao.insertGame(
                        game.copy(
                            isInCollection = true,
                            isInWishlist = false,
                            playStatus = PlayStatus.NOT_PLAYED.name
                        )
                    )
                }
                AddToCollectionResult.Success
            }
        }
    }

    suspend fun removeFromCollection(rawgId: Int) {
        gameDao.updateCollectionStatus(rawgId, false)
        gameDao.updatePlayStatus(rawgId, PlayStatus.NOT_PLAYED.name)
    }

    suspend fun updatePlayStatus(rawgId: Int, status: PlayStatus) {
        val isPlayed = status == PlayStatus.PLAYED
        gameDao.updatePlayedStatus(rawgId, isPlayed, status.name)
        if (isPlayed) recalculateUserLevel()
    }

    // --- LOCAL - WISHLIST ---
    fun getWishlist(): Flow<List<GameEntity>> = gameDao.getWishlist()
    fun filterWishlist(genre: String?): Flow<List<GameEntity>> = gameDao.filterWishlist(genre)

    suspend fun addToWishlist(game: GameEntity): AddToWishlistResult {
        val existing = gameDao.getGameById(game.rawgId)
        return when {
            existing?.isInCollection == true -> AddToWishlistResult.AlreadyInCollection
            existing?.isInWishlist == true -> AddToWishlistResult.AlreadyInWishlist
            else -> {
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
                AddToWishlistResult.Success
            }
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
    fun observeGameById(rawgId: Int): Flow<GameEntity?> = gameDao.observeGameById(rawgId)

    suspend fun getGameById(rawgId: Int): GameEntity? = gameDao.getGameById(rawgId)

    // --- LEVEL SYSTEM ---
    private suspend fun recalculateUserLevel() {
        val playedCount = gameDao.getPlayedGamesCount()
        val userId = appPreferences.loggedInUserId.first()
        if (userId == -1) return
        val user = userDao.getUserById(userId).first() ?: return
        val newLevel = UserEntity.calculateLevel(playedCount)
        val newTier = UserEntity.calculateTier(playedCount)
        if (user.level != newLevel || user.tier != newTier) {
            userDao.updateUser(user.copy(level = newLevel, tier = newTier))
        }
    }

    // --- HELPERS ---
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
        storageSize = null,
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

    // --- RESULT TYPES ---
    sealed class AddToCollectionResult {
        object Success : AddToCollectionResult()
        object AlreadyInCollection : AddToCollectionResult()
    }

    sealed class AddToWishlistResult {
        object Success : AddToWishlistResult()
        object AlreadyInWishlist : AddToWishlistResult()
        object AlreadyInCollection : AddToWishlistResult()
    }
}