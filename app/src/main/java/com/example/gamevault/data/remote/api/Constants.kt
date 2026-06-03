package com.example.gamevault.data.remote.api

object Constants {
    const val RAWG_API_KEY = "ea61ca8358f246d1b662fdd8ce958fb1"

    // Genre slugs
    const val GENRE_INDIE = "indie"
    const val GENRE_ACTION = "action"
    const val GENRE_RPG = "rpg"
    const val GENRE_STRATEGY = "strategy"
    const val GENRE_SHOOTER = "shooter"
    const val GENRE_CO_OP = "cooperative"
    const val GENRE_RETRO = "arcade"

    // Platform IDs
    const val PLATFORM_PC = "4"
    const val PLATFORM_PS5 = "187"
    const val PLATFORM_PS4 = "18"
    const val PLATFORM_XBOX = "1"
    const val PLATFORM_NINTENDO = "7"
    const val PLATFORM_ANDROID = "21"
    const val PLATFORM_IOS = "3"

    // Suppress unused — vor fi folosite in filtre
    @Suppress("unused")
    val ALL_GENRES = listOf(GENRE_INDIE, GENRE_ACTION, GENRE_RPG, GENRE_STRATEGY, GENRE_SHOOTER, GENRE_CO_OP, GENRE_RETRO)
    @Suppress("unused")
    val ALL_PLATFORMS = listOf(PLATFORM_PC, PLATFORM_PS5, PLATFORM_PS4, PLATFORM_XBOX, PLATFORM_NINTENDO, PLATFORM_ANDROID, PLATFORM_IOS)
}