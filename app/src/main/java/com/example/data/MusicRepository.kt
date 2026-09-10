package com.example.data

import com.example.R
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song

object MusicRepository {

    val initialSongs: List<Song> = listOf(
        Song(
            id = "song_1",
            title = "Midnight Echoes",
            artist = "Aura Synthetica",
            album = "Neon Horizons",
            durationSeconds = 234,
            isFavorite = true,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF8B5CF6, 0xFF06B6D4)
        ),
        Song(
            id = "song_2",
            title = "Sunset Boulevard",
            artist = "Solaris Wave",
            album = "Golden Hour",
            durationSeconds = 198,
            isFavorite = true,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF97316, 0xFFEC4899)
        ),
        Song(
            id = "song_3",
            title = "Velvet Rain",
            artist = "Lofi Bloom",
            album = "Midnight Coffee",
            durationSeconds = 172,
            isFavorite = false,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFF10B981, 0xFF3B82F6)
        ),
        Song(
            id = "song_4",
            title = "Starlight Odyssey",
            artist = "Aura Synthetica",
            album = "Neon Horizons",
            durationSeconds = 256,
            isFavorite = true,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF6366F1, 0xFFA855F7)
        ),
        Song(
            id = "song_5",
            title = "Crimson Skyline",
            artist = "Solaris Wave",
            album = "Golden Hour",
            durationSeconds = 210,
            isFavorite = false,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFEF4444, 0xFFF59E0B)
        ),
        Song(
            id = "song_6",
            title = "Deep Resonance",
            artist = "Subtle Pulse",
            album = "Acoustic Silence",
            durationSeconds = 245,
            isFavorite = false,
            drawableRes = null,
            gradientColors = listOf(0xFF14B8A6, 0xFF0EA5E9)
        ),
        Song(
            id = "song_7",
            title = "Tokyo Reverie",
            artist = "Lofi Bloom",
            album = "Midnight Coffee",
            durationSeconds = 188,
            isFavorite = true,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFFEC4899, 0xFF8B5CF6)
        ),
        Song(
            id = "song_8",
            title = "Electric Aurora",
            artist = "Aura Synthetica",
            album = "Cyber Pulse",
            durationSeconds = 222,
            isFavorite = false,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF3B82F6, 0xFF06B6D4)
        ),
        Song(
            id = "song_9",
            title = "Golden Mirage",
            artist = "Solaris Wave",
            album = "Golden Hour",
            durationSeconds = 265,
            isFavorite = false,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF59E0B, 0xFFEF4444)
        ),
        Song(
            id = "song_10",
            title = "Whispers in the Mist",
            artist = "Subtle Pulse",
            album = "Acoustic Silence",
            durationSeconds = 195,
            isFavorite = true,
            drawableRes = null,
            gradientColors = listOf(0xFF64748B, 0xFF334155)
        )
    )

    val artists: List<Artist> = listOf(
        Artist(
            name = "Aura Synthetica",
            songCount = 3,
            albumCount = 2,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF8B5CF6, 0xFF06B6D4)
        ),
        Artist(
            name = "Solaris Wave",
            songCount = 3,
            albumCount = 1,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF97316, 0xFFEC4899)
        ),
        Artist(
            name = "Lofi Bloom",
            songCount = 2,
            albumCount = 1,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFF10B981, 0xFF3B82F6)
        ),
        Artist(
            name = "Subtle Pulse",
            songCount = 2,
            albumCount = 1,
            drawableRes = null,
            gradientColors = listOf(0xFF14B8A6, 0xFF6366F1)
        )
    )

    val albums: List<Album> = listOf(
        Album(
            id = "album_1",
            title = "Neon Horizons",
            artist = "Aura Synthetica",
            year = 2025,
            songCount = 2,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF8B5CF6, 0xFF06B6D4)
        ),
        Album(
            id = "album_2",
            title = "Golden Hour",
            artist = "Solaris Wave",
            year = 2024,
            songCount = 3,
            drawableRes = R.drawable.cover_sunset_vibes,
            gradientColors = listOf(0xFFF97316, 0xFFEC4899)
        ),
        Album(
            id = "album_3",
            title = "Midnight Coffee",
            artist = "Lofi Bloom",
            year = 2025,
            songCount = 2,
            drawableRes = R.drawable.cover_midnight_echoes,
            gradientColors = listOf(0xFF10B981, 0xFF3B82F6)
        ),
        Album(
            id = "album_4",
            title = "Cyber Pulse",
            artist = "Aura Synthetica",
            year = 2026,
            songCount = 1,
            drawableRes = R.drawable.cover_neon_dreams,
            gradientColors = listOf(0xFF3B82F6, 0xFF8B5CF6)
        ),
        Album(
            id = "album_5",
            title = "Acoustic Silence",
            artist = "Subtle Pulse",
            year = 2024,
            songCount = 2,
            drawableRes = null,
            gradientColors = listOf(0xFF14B8A6, 0xFF0EA5E9)
        )
    )
}
