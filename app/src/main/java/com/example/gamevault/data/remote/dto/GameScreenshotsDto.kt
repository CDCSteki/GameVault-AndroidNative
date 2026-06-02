package com.example.gamevault.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GameScreenshotsResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<GameScreenshotDto>
)

data class GameScreenshotDto(
    @SerializedName("id") val id: Int,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("is_deleted") val isDeleted: Boolean?
)