package com.example.gamevault.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GameMoviesResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<GameMovieDto>
)

data class GameMovieDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("preview") val preview: String?,   // thumbnail imagine
    @SerializedName("data") val data: MovieDataDto?
)

data class MovieDataDto(
    @SerializedName("480") val quality480: String?,    // URL video 480p
    @SerializedName("max") val qualityMax: String?     // URL video max quality
)