package com.example.gamevault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
    val rawgId: Int,
    val name: String,
    val coverImageUrl: String?,
    val backgroundImageUrl: String?,
    val description: String?,
    val developer: String?,
    val releaseDate: String?,
    val platforms: String?,        // salvat ca JSON string ex: "PC,PS5,Xbox"
    val genres: String?,           // salvat ca JSON string ex: "RPG,Action"
    val storageSize: String?,
    val rating: Float = 0f,
    val userRating: Float = 0f,    // ratingul dat de utilizator
    val userNotes: String? = null, // notele private ale utilizatorului
    val isInCollection: Boolean = false,
    val isInWishlist: Boolean = false,
    val isPlayed: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)