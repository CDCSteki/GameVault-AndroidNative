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
)