package com.example.model

enum class SectionTab(val label: String) {
    ALL_SONGS("Canciones"),
    ARTISTS("Artistas"),
    ALBUMS("Álbumes"),
    FOLDERS("Carpetas"),
    GENRES("Géneros"),
    PLAYLISTS("Playlists"),
    FAVORITES("Favoritos"),
    HISTORY("Historial")
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

enum class EqualizerPreset(val label: String, val bassMultiplier: Float, val trebleMultiplier: Float) {
    BALANCED("Equilibrado", 1.0f, 1.0f),
    BASS_BOOST("Refuerzo de graves", 1.5f, 0.9f),
    VOCAL("Claridad vocal", 0.8f, 1.4f),
    ACOUSTIC("Acústico", 1.1f, 1.2f),
    ROCK("Rock", 1.3f, 1.3f),
    POP("Pop", 1.1f, 1.2f),
    JAZZ("Jazz", 1.1f, 1.1f),
    ELECTRONIC("Electrónica", 1.4f, 1.3f),
    CLASSICAL("Clásica", 1.1f, 1.2f)
}

data class LyricLine(
    val timeSeconds: Int,
    val text: String
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val contentUri: String,
    val albumArtUri: String? = null,
    val albumId: Long = 0L,
    val filePath: String? = null,
    val folderPath: String = "",
    val folderName: String = "Música",
    val genre: String = "Varios",
    val sizeBytes: Long = 0L,
    val isFavorite: Boolean = false,
    val gradientColors: List<Long> = listOf(0xFF8B5CF6, 0xFFEC4899),
    val lyrics: List<LyricLine> = emptyList()
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
    val albumArtUri: String? = null,
    val gradientColors: List<Long> = listOf(0xFF6366F1, 0xFF06B6D4)
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int,
    val songCount: Int,
    val albumArtUri: String? = null,
    val gradientColors: List<Long> = listOf(0xFFF43F5E, 0xFFF59E0B)
)

data class MusicFolder(
    val path: String,
    val name: String,
    val songCount: Int,
    val songs: List<Song>
)

data class MusicGenre(
    val name: String,
    val songCount: Int,
    val songs: List<Song>
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class PlayHistoryItem(
    val song: Song,
    val playCount: Int,
    val lastPlayedAt: Long
)

