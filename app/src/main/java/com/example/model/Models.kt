package com.example.model

import androidx.annotation.DrawableRes

enum class SectionTab(val label: String) {
    ALL_SONGS("Todas las canciones"),
    ARTISTS("Artistas"),
    ALBUMS("Álbumes"),
    FAVORITES("Favoritos")
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class ThemeMode(val label: String) {
    SYSTEM("Del sistema"),
    DARK("Modo oscuro"),
    LIGHT("Modo claro")
}

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val isFavorite: Boolean = false,
    @DrawableRes val drawableRes: Int? = null,
    val gradientColors: List<Long> = listOf(0xFF8B5CF6, 0xFFEC4899)
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    @DrawableRes val drawableRes: Int? = null,
    val gradientColors: List<Long> = listOf(0xFF6366F1, 0xFF06B6D4)
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int,
    val songCount: Int,
    @DrawableRes val drawableRes: Int? = null,
    val gradientColors: List<Long> = listOf(0xFFF43F5E, 0xFFF59E0B)
)
