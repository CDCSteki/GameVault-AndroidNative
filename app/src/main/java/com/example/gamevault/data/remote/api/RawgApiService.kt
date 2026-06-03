package com.example.gamevault.data.remote.api

import com.example.gamevault.data.remote.dto.GameDetailDto
import com.example.gamevault.data.remote.dto.GameScreenshotsResponse
import com.example.gamevault.data.remote.dto.GamesListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApiService {

    // Home - Popular anul acesta
    @GET("games")
    suspend fun getGamesThisYear(
        @Query("key") apiKey: String,
        @Query("dates") dates: String,
        @Query("ordering") ordering: String = "-rating",
        @Query("page_size") pageSize: Int = 10
    ): GamesListResponse

    // Home - Cele mai populare din toate timpurile
    @GET("games")
    suspend fun getAllTimeTopGames(
        @Query("key") apiKey: String,
        @Query("ordering") ordering: String = "-rating",
        @Query("metacritic") metacritic: String = "90,100",
        @Query("page_size") pageSize: Int = 10
    ): GamesListResponse

    // Home - Discover
    @GET("games")
    suspend fun getGamesByFilters(
        @Query("key") apiKey: String,
        @Query("genres") genres: String? = null,
        @Query("tags") tags: String? = null,
        @Query("dates") dates: String? = null,
        @Query("ordering") ordering: String = "-rating",
        @Query("page_size") pageSize: Int = 10
    ): GamesListResponse

    // Search cu filtre avansate
    @GET("games")
    suspend fun searchGames(
        @Query("key") apiKey: String,
        @Query("search") query: String,
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("metacritic") metacritic: String? = null,
        @Query("dates") dates: String? = null,
        @Query("ordering") ordering: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): GamesListResponse

    // Game Details
    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String
    ): GameDetailDto

    // Screenshots
    @GET("games/{id}/screenshots")
    suspend fun getGameScreenshots(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String
    ): GameScreenshotsResponse

    // Popular / Trending general
    @GET("games")
    suspend fun getPopularGames(
        @Query("key") apiKey: String,
        @Query("ordering") ordering: String = "-added",
        @Query("page_size") pageSize: Int = 10,
        @Query("page") page: Int = 1
    ): GamesListResponse
}