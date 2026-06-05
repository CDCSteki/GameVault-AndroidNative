package com.example.gamevault.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gamevault.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY searchedAt DESC LIMIT 20")
    fun getRecentSearches(userId: Int): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearAllHistory(userId: Int)

    @Query("DELETE FROM search_history WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: Int, userId: Int)

    @Query("SELECT COUNT(*) FROM search_history WHERE `query` = :searchQuery AND userId = :userId")
    suspend fun queryExists(searchQuery: String, userId: Int): Int

    @Query("DELETE FROM search_history WHERE `query` = :searchQuery AND userId = :userId")
    suspend fun deleteByQuery(searchQuery: String, userId: Int)
}