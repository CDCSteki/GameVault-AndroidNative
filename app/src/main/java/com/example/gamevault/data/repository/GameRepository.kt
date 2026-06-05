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
import com.example.gamevault.data.remote.dto.GameScreenshotsResponse
import com.example.gamevault.data.remote.dto.DeveloperDto
import com.example.gamevault.data.remote.dto.GenreDto
import com.example.gamevault.data.remote.dto.PlatformDto
import com.example.gamevault.data.remote.dto.PlatformWrapperDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

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

    suspend fun getGamesWithFilters(
        genres: String? = null,
        tags: String? = null,
        dates: String? = null,
        pageSize: Int = 10
    ): Result<List<GameDto>> = safeApiCall {
        apiService.getGamesByFilters(
            apiKey = Constants.RAWG_API_KEY,
            genres = genres,
            tags = tags,
            dates = dates,
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

    suspend fun getGameScreenshots(gameId: Int): Result<GameScreenshotsResponse> = safeApiCall {
        apiService.getGameScreenshots(gameId = gameId, apiKey = Constants.RAWG_API_KEY)
    }

    // --- LOCAL - COLLECTION ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCollection(): Flow<List<GameEntity>> = appPreferences.loggedInUserId.flatMapLatest { userId ->
        if (userId != -1) gameDao.getCollection(userId) else flowOf(emptyList())
    }

    fun GameEntity.toDetailDto(): GameDetailDto = GameDetailDto(
        id = rawgId,
        name = name,
        descriptionRaw = description,
        backgroundImage = backgroundImageUrl,
        released = releaseDate,
        rating = rating,
        ratingsCount = 0,
        playtime = null,
        platforms = platforms?.split(",")?.map { platformName ->
            PlatformWrapperDto(platform = PlatformDto(id = 0, name = platformName.trim()))
        },
        genres = genres?.split(",")?.map { genreName ->
            GenreDto(id = 0, name = genreName.trim())
        },
        developers = developer?.let { listOf(DeveloperDto(id = 0, name = it)) },
        publishers = null,
        ratings = null,
        esrbRating = null
    )

    suspend fun addToCollection(game: GameEntity): AddToCollectionResult {
        val userId = appPreferences.loggedInUserId.first()
        if (userId == -1) return AddToCollectionResult.AlreadyInCollection

        val gameWithUser = game.copy(userId = userId)
        val existing = gameDao.getGameById(gameWithUser.rawgId, userId)

        return when {
            existing?.isInCollection == true -> AddToCollectionResult.AlreadyInCollection
            else -> {
                if (existing != null) {
                    gameDao.updateCollectionStatus(gameWithUser.rawgId, userId, true)
                    gameDao.updateWishlistStatus(gameWithUser.rawgId, userId, false)
                } else {
                    gameDao.insertGame(
                        gameWithUser.copy(
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
        val userId = appPreferences.loggedInUserId.first()
        gameDao.updateCollectionStatus(rawgId, userId, false)
        gameDao.updatePlayStatus(rawgId, userId, PlayStatus.NOT_PLAYED.name)
    }

    suspend fun updatePlayStatus(rawgId: Int, status: PlayStatus) {
        val userId = appPreferences.loggedInUserId.first()
        val isPlayed = status == PlayStatus.PLAYED
        gameDao.updatePlayedStatus(rawgId, userId, isPlayed, status.name)
        if (isPlayed) recalculateUserLevel()
    }

    // --- LOCAL - WISHLIST ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWishlist(): Flow<List<GameEntity>> = appPreferences.loggedInUserId.flatMapLatest { userId ->
        if (userId != -1) gameDao.getWishlist(userId) else flowOf(emptyList())
    }

    suspend fun addToWishlist(game: GameEntity): AddToWishlistResult {
        val userId = appPreferences.loggedInUserId.first()
        if (userId == -1) return AddToWishlistResult.AlreadyInWishlist

        val gameWithUser = game.copy(userId = userId)
        val existing = gameDao.getGameById(gameWithUser.rawgId, userId)

        return when {
            existing?.isInCollection == true -> AddToWishlistResult.AlreadyInCollection
            existing?.isInWishlist == true -> AddToWishlistResult.AlreadyInWishlist
            else -> {
                if (existing != null) {
                    gameDao.updateWishlistStatus(gameWithUser.rawgId, userId, true)
                } else {
                    gameDao.insertGame(
                        gameWithUser.copy(
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
        val userId = appPreferences.loggedInUserId.first()
        gameDao.updateWishlistStatus(rawgId, userId, false)
    }

    // --- LOCAL - RATINGS & NOTES ---
    suspend fun updateUserRating(rawgId: Int, rating: Float) {
        val userId = appPreferences.loggedInUserId.first()
        gameDao.updateUserRating(rawgId, userId, rating)
    }

    suspend fun updateUserNotes(rawgId: Int, notes: String?) {
        val userId = appPreferences.loggedInUserId.first()
        gameDao.updateUserNotes(rawgId, userId, notes)
    }

    // --- LOCAL - OBSERVE ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeGameById(rawgId: Int): Flow<GameEntity?> = appPreferences.loggedInUserId.flatMapLatest { userId ->
        gameDao.observeGameById(rawgId, userId)
    }

    suspend fun getGameById(rawgId: Int): GameEntity? {
        val userId = appPreferences.loggedInUserId.first()
        return gameDao.getGameById(rawgId, userId)
    }

    // --- LEVEL SYSTEM ---
    private suspend fun recalculateUserLevel() {
        val userId = appPreferences.loggedInUserId.first()
        if (userId == -1) return
        val playedCount = gameDao.getPlayedGamesCount(userId)
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
        userId = -1,
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