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
    @Query("SELECT * FROM games WHERE isInCollection = 1 ORDER BY addedAt DESC")
    fun getCollection(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isInCollection = 1 AND isPlayed = 1 ORDER BY addedAt DESC")
    fun getPlayedGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isInCollection = 1 AND isPlayed = 0 ORDER BY addedAt DESC")
    fun getNotPlayedGames(): Flow<List<GameEntity>>

    // --- WISHLIST ---
    @Query("SELECT * FROM games WHERE isInWishlist = 1 ORDER BY addedAt DESC")
    fun getWishlist(): Flow<List<GameEntity>>

    // --- SINGLE GAME ---
    @Query("SELECT * FROM games WHERE rawgId = :rawgId LIMIT 1")
    suspend fun getGameById(rawgId: Int): GameEntity?

    @Query("SELECT * FROM games WHERE rawgId = :rawgId LIMIT 1")
    fun observeGameById(rawgId: Int): Flow<GameEntity?>

    // --- STATUS UPDATES ---
    @Query("UPDATE games SET isInCollection = :inCollection WHERE rawgId = :rawgId")
    suspend fun updateCollectionStatus(rawgId: Int, inCollection: Boolean)

    @Query("UPDATE games SET isInWishlist = :inWishlist WHERE rawgId = :rawgId")
    suspend fun updateWishlistStatus(rawgId: Int, inWishlist: Boolean)

    @Query("UPDATE games SET isPlayed = :isPlayed WHERE rawgId = :rawgId")
    suspend fun updatePlayedStatus(rawgId: Int, isPlayed: Boolean)

    @Query("UPDATE games SET userRating = :rating WHERE rawgId = :rawgId")
    suspend fun updateUserRating(rawgId: Int, rating: Float)

    @Query("UPDATE games SET userNotes = :notes WHERE rawgId = :rawgId")
    suspend fun updateUserNotes(rawgId: Int, notes: String?)

    // --- DELETE ---
    @Query("DELETE FROM games WHERE rawgId = :rawgId")
    suspend fun deleteGame(rawgId: Int)

    // --- FILTER / SEARCH in local ---
    @Query("""
        SELECT * FROM games 
        WHERE isInCollection = 1 
        AND (:genre IS NULL OR genres LIKE '%' || :genre || '%')
        ORDER BY addedAt DESC
    """)
    fun filterCollection(genre: String?): Flow<List<GameEntity>>

    @Query("""
        SELECT * FROM games 
        WHERE isInWishlist = 1 
        AND (:genre IS NULL OR genres LIKE '%' || :genre || '%')
        ORDER BY addedAt DESC
    """)
    fun filterWishlist(genre: String?): Flow<List<GameEntity>>
}