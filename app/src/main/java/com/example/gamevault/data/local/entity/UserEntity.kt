package com.example.gamevault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val profilePictureUri: String? = null,
    val level: Int = 1,
    val tier: String = "ROOKIE",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun calculateLevel(gamesPlayed: Int): Int {
            return when {
                gamesPlayed >= 100 -> 10
                gamesPlayed >= 75 -> 9
                gamesPlayed >= 50 -> 8
                gamesPlayed >= 40 -> 7
                gamesPlayed >= 30 -> 6
                gamesPlayed >= 20 -> 5
                gamesPlayed >= 15 -> 4
                gamesPlayed >= 10 -> 3
                gamesPlayed >= 5 -> 2
                else -> 1
            }
        }

        fun calculateTier(gamesPlayed: Int): String {
            return when {
                gamesPlayed >= 100 -> "LEGENDARY"
                gamesPlayed >= 75 -> "GRANDMASTER"
                gamesPlayed >= 50 -> "MASTER"
                gamesPlayed >= 40 -> "DIAMOND"
                gamesPlayed >= 30 -> "PLATINUM"
                gamesPlayed >= 20 -> "GOLD"
                gamesPlayed >= 15 -> "SILVER"
                gamesPlayed >= 10 -> "BRONZE"
                gamesPlayed >= 5 -> "IRON"
                else -> "ROOKIE"
            }
        }
    }
}