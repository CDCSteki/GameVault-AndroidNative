package com.example.gamevault.ui.util

import java.text.SimpleDateFormat
import java.util.Locale

fun String?.formatReleaseDate(): String {
    if (this.isNullOrBlank()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (_: Exception) {
        this
    }
}

fun Int?.toPlaytimeString(): String {
    return if (this == null || this <= 0) "N/A" else "${this}h avg"
}

fun String?.firstGenre(): String {
    return this?.split(",")?.firstOrNull()?.trim() ?: ""
}

fun String?.firstPlatform(): String {
    return this?.split(",")?.firstOrNull()?.trim() ?: ""
}