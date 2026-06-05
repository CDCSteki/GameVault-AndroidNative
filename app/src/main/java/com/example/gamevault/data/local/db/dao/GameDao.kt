package com.example.gamevault.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gamevault.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Update
    suspend fun updateGame(game: GameEntity)

    // --- COLLECTION ---
    @Query("SELECT * FROM games WHERE isInCollection = 1 AND userId = :userId ORDER BY addedAt DESC")
    fun getCollection(userId: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isInCollection = 1 AND playStatus = 'PLAYED' AND userId = :userId ORDER BY addedAt DESC")
    fun getPlayedGames(userId: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isInCollection = 1 AND playStatus = 'NOT_PLAYED' AND userId = :userId ORDER BY addedAt DESC")
    fun getNotPlayedGames(userId: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isInCollection = 1 AND playStatus = 'PLAYING' AND userId = :userId ORDER BY addedAt DESC")
    fun getPlayingGames(userId: Int): Flow<List<GameEntity>>

    // --- WISHLIST ---
    @Query("SELECT * FROM games WHERE isInWishlist = 1 AND userId = :userId ORDER BY addedAt DESC")
    fun getWishlist(userId: Int): Flow<List<GameEntity>>

    // --- SINGLE GAME ---
    @Query("SELECT * FROM games WHERE rawgId = :rawgId AND userId = :userId LIMIT 1")
    suspend fun getGameById(rawgId: Int, userId: Int): GameEntity?

    @Query("SELECT * FROM games WHERE rawgId = :rawgId AND userId = :userId LIMIT 1")
    fun observeGameById(rawgId: Int, userId: Int): Flow<GameEntity?>

    // --- STATUS UPDATES ---
    @Query("UPDATE games SET isInCollection = :inCollection WHERE rawgId = :rawgId AND userId = :userId")
    suspend fun updateCollectionStatus(rawgId: Int, userId: Int, inCollection: Boolean)

    @Query("UPDATE games SET isInWishlist = :inWishlist WHERE rawgId = :rawgId AND userId = :userId")
    suspend fun updateWishlistStatus(rawgId: Int, userId: Int, inWishlist: Boolean)

    @Query("UPDATE games SET isPlayed = :isPlayed, playStatus = :playStatus WHERE rawgId = :rawgId AND userId = :userId")
    suspend fun updatePlayedStatus(rawgId: Int, userId: Int, isPlayed: Boolean, playStatus: String)

    @Query("UPDATE games SET playStatus = :playStatus WHERE rawgId = :rawgId AND userId = :userId")
    suspend fun updatePlayStatus(rawgId: Int, userId: Int, playStatus: String)

    @Query("UPDATE games SET userRating = :rating WHERE rawgId = :rawgId AND userId = :userId")
    suspend fun updateUserRating(rawgId: Int, userId: Int, rating: Float)

    @Query("UPDATE games SET userNotes = :notes WHERE rawgId = :rawgId AND userId = :userId")
    suspend fun updateUserNotes(rawgId: Int, userId: Int, notes: String?)

    // --- DELETE ---
    @Query("DELETE FROM games WHERE rawgId = :rawgId AND userId = :userId")
    suspend fun deleteGame(rawgId: Int, userId: Int)

    // --- COUNTS ---
    @Query("SELECT COUNT(*) FROM games WHERE isInCollection = 1 AND playStatus = 'PLAYED' AND userId = :userId")
    suspend fun getPlayedGamesCount(userId: Int): Int

    // --- FILTER ---
    @Query("""
        SELECT * FROM games 
        WHERE isInCollection = 1 AND userId = :userId
        AND (:genre IS NULL OR genres LIKE '%' || :genre || '%')
        ORDER BY addedAt DESC
    """)
    fun filterCollection(userId: Int, genre: String?): Flow<List<GameEntity>>

    @Query("""
        SELECT * FROM games 
        WHERE isInWishlist = 1 AND userId = :userId
        AND (:genre IS NULL OR genres LIKE '%' || :genre || '%')
        ORDER BY addedAt DESC
    """)
    fun filterWishlist(userId: Int, genre: String?): Flow<List<GameEntity>>
}