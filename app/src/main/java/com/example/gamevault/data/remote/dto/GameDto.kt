package com.example.gamevault.data.remote.dto

import com.google.gson.annotations.SerializedName

// Response pentru lista de jocuri (search, popular, etc.)
data class GamesListResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<GameDto>
)

data class GameDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("rating") val rating: Float,
    @SerializedName("released") val released: String?,
    @SerializedName("genres") val genres: List<GenreDto>?,
    @SerializedName("platforms") val platforms: List<PlatformWrapperDto>?,
    @SerializedName("playtime") val playtime: Int?
)

data class GenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class PlatformWrapperDto(
    @SerializedName("platform") val platform: PlatformDto,
    @SerializedName("requirements") val requirements: RequirementsDto? = null,
    @SerializedName("requirements_en") val requirementsEn: RequirementsDto? = null
)

data class RequirementsDto(
    @SerializedName("minimum") val minimum: String?,
    @SerializedName("recommended") val recommended: String?
)

data class PlatformDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)