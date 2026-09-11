package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.model.Album
import com.example.model.Artist
import com.example.model.MusicFolder
import com.example.model.MusicGenre
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

object MediaAudioScanner {

    private val PRESET_GRADIENTS = listOf(
        listOf(0xFF8B5CF6, 0xFFEC4899),
        listOf(0xFF6366F1, 0xFF06B6D4),
        listOf(0xFFF97316, 0xFFEC4899),
        listOf(0xFF10B981, 0xFF3B82F6),
        listOf(0xFFF43F5E, 0xFFF59E0B),
        listOf(0xFF3B82F6, 0xFF8B5CF6),
        listOf(0xFF14B8A6, 0xFF6366F1),
        listOf(0xFFE11D48, 0xFF7C3AED)
    )

    private fun getGradientForString(key: String): List<Long> {
        val index = abs(key.hashCode()) % PRESET_GRADIENTS.size
        return PRESET_GRADIENTS[index]
    }

    suspend fun scanDeviceAudio(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val songsList = mutableListOf<Song>()
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 1000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val rawTitle = cursor.getString(titleCol)
                    val rawArtist = cursor.getString(artistCol)
                    val rawAlbum = cursor.getString(albumCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val durationMs = cursor.getLong(durationCol)
                    val filePath = if (dataCol >= 0) cursor.getString(dataCol) else null
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L

                    val title = if (!rawTitle.isNullOrBlank()) rawTitle else (filePath?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Pista $id")
                    val artist = if (!rawArtist.isNullOrBlank() && rawArtist != "<unknown>") rawArtist else "Artista desconocido"
                    val album = if (!rawAlbum.isNullOrBlank() && rawAlbum != "<unknown>") rawAlbum else "Álbum desconocido"

                    val folderPath = if (!filePath.isNullOrBlank()) {
                        try {
                            File(filePath).parent ?: "/Música"
                        } catch (e: Exception) {
                            "/Música"
                        }
                    } else {
                        "/Música"
                    }
                    val folderName = folderPath.substringAfterLast('/').ifBlank { "Música" }

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    val durationSeconds = (durationMs / 1000).toInt()

                    songsList.add(
                        Song(
                            id = id.toString(),
                            title = title,
                            artist = artist,
                            album = album,
                            durationSeconds = durationSeconds,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            albumId = albumId,
                            filePath = filePath,
                            folderPath = folderPath,
                            folderName = folderName,
                            genre = "Música",
                            sizeBytes = size,
                            gradientColors = getGradientForString(title)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songsList
    }

    fun groupArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
            val uniqueAlbums = artistSongs.map { it.album }.distinct().size
            val albumArt = artistSongs.firstOrNull { it.albumArtUri != null }?.albumArtUri
            Artist(
                name = artistName,
                songCount = artistSongs.size,
                albumCount = uniqueAlbums,
                albumArtUri = albumArt,
                gradientColors = getGradientForString(artistName)
            )
        }.sortedBy { it.name }
    }

    fun groupAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId to it.album }.map { (key, albumSongs) ->
            val (albumId, albumTitle) = key
            val artistName = albumSongs.firstOrNull()?.artist ?: "Varios Artistas"
            val albumArt = albumSongs.firstOrNull { it.albumArtUri != null }?.albumArtUri
            Album(
                id = albumId.toString(),
                title = albumTitle,
                artist = artistName,
                year = 0,
                songCount = albumSongs.size,
                albumArtUri = albumArt,
                gradientColors = getGradientForString(albumTitle)
            )
        }.sortedBy { it.title }
    }

    fun groupFolders(songs: List<Song>): List<MusicFolder> {
        return songs.groupBy { it.folderPath }.map { (folderPath, folderSongs) ->
            val folderName = folderSongs.firstOrNull()?.folderName ?: folderPath.substringAfterLast('/')
            MusicFolder(
                path = folderPath,
                name = folderName,
                songCount = folderSongs.size,
                songs = folderSongs
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun groupGenres(songs: List<Song>): List<MusicGenre> {
        return songs.groupBy { it.genre }.map { (genreName, genreSongs) ->
            MusicGenre(
                name = genreName,
                songCount = genreSongs.size,
                songs = genreSongs
            )
        }.sortedBy { it.name.lowercase() }
    }
}
