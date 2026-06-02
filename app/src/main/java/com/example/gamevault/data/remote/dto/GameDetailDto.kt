package com.example.gamevault.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GameDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description_raw") val descriptionRaw: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("released") val released: String?,
    @SerializedName("rating") val rating: Float,
    @SerializedName("ratings_count") val ratingsCount: Int,
    @SerializedName("playtime") val playtime: Int?,
    @SerializedName("platforms") val platforms: List<PlatformWrapperDto>?,
    @SerializedName("genres") val genres: List<GenreDto>?,
    @SerializedName("developers") val developers: List<DeveloperDto>?,
    @SerializedName("publishers") val publishers: List<PublisherDto>?,
    @SerializedName("ratings") val ratings: List<RatingDto>?,
    @SerializedName("esrb_rating") val esrbRating: EsrbRatingDto?
)

data class DeveloperDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class PublisherDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class RatingDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("count") val count: Int,
    @SerializedName("percent") val percent: Float
)

data class EsrbRatingDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)