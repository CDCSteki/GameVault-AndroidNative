package com.example.gamevault.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gamevault.data.local.db.dao.GameDao
import com.example.gamevault.data.local.db.dao.SearchHistoryDao
import com.example.gamevault.data.local.db.dao.UserDao
import com.example.gamevault.data.local.entity.GameEntity
import com.example.gamevault.data.local.entity.SearchHistoryEntity
import com.example.gamevault.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        GameEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameVaultDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun gameDao(): GameDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: GameVaultDatabase? = null

        fun getInstance(context: Context): GameVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GameVaultDatabase::class.java,
                    "gamevault_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}