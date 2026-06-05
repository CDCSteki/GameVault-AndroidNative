package com.example.gamevault.data.local.entity

import androidx.room.Entity

enum class PlayStatus {
    NOT_PLAYED,
    PLAYING,
    PLAYED
}

@Entity(
    tableName = "games",
    primaryKeys = ["rawgId", "userId"]
)
data class GameEntity(
    val rawgId: Int,
    val userId: Int,
    val name: String,
    val coverImageUrl: String?,
    val backgroundImageUrl: String?,
    val description: String?,
    val developer: String?,
    val releaseDate: String?,
    val platforms: String?,
    val genres: String?,
    val storageSize: String?,
    val rating: Float = 0f,
    val userRating: Float = 0f,
    val userNotes: String? = null,
    val isInCollection: Boolean = false,
    val isInWishlist: Boolean = false,
    val isPlayed: Boolean = false,
    val playStatus: String = PlayStatus.NOT_PLAYED.name,
    val addedAt: Long = System.currentTimeMillis()
)