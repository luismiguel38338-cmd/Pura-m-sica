package com.example.data

import com.example.data.db.AppSettingsEntity
import com.example.data.db.FavoriteEntity
import com.example.data.db.MusicDao
import com.example.data.db.PlayHistoryEntity
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistItemEntity
import com.example.data.db.SongMetadataOverrideEntity
import kotlinx.coroutines.flow.Flow

class MusicLocalRepository(private val dao: MusicDao) {

    // Favorites
    val favoriteSongIds: Flow<List<String>> = dao.getAllFavoriteIds()

    suspend fun toggleFavorite(songId: String) {
        if (dao.isFavorite(songId)) {
            dao.deleteFavorite(songId)
        } else {
            dao.insertFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun setFavorite(songId: String, isFav: Boolean) {
        if (isFav) {
            dao.insertFavorite(FavoriteEntity(songId = songId))
        } else {
            dao.deleteFavorite(songId)
        }
    }

    // Playlists
    val allPlaylists: Flow<List<PlaylistEntity>> = dao.getAllPlaylists()
    val allPlaylistItems: Flow<List<PlaylistItemEntity>> = dao.getAllPlaylistItems()

    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> =
        dao.getPlaylistItems(playlistId)

    suspend fun createPlaylist(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1L
        return dao.insertPlaylist(PlaylistEntity(name = trimmed))
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            dao.updatePlaylistName(playlistId, trimmed)
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylistAndItems(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String, orderIndex: Int = 0) {
        dao.insertPlaylistItem(
            PlaylistItemEntity(
                playlistId = playlistId,
                songId = songId,
                orderIndex = orderIndex
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        dao.deletePlaylistItem(playlistId, songId)
    }

    // Play History & Top Played
    val playHistory: Flow<List<PlayHistoryEntity>> = dao.getPlayHistory()
    val topPlayed: Flow<List<PlayHistoryEntity>> = dao.getTopPlayed()

    suspend fun recordPlay(songId: String) {
        dao.recordSongPlay(songId)
    }

    suspend fun clearHistory() {
        dao.clearPlayHistory()
    }

    // Metadata Overrides (Tag Editing)
    val metadataOverrides: Flow<List<SongMetadataOverrideEntity>> = dao.getAllMetadataOverrides()

    suspend fun updateSongMetadata(
        songId: String,
        title: String?,
        artist: String?,
        album: String?,
        genre: String?
    ) {
        dao.insertMetadataOverride(
            SongMetadataOverrideEntity(
                songId = songId,
                customTitle = title?.trim()?.takeIf { it.isNotBlank() },
                customArtist = artist?.trim()?.takeIf { it.isNotBlank() },
                customAlbum = album?.trim()?.takeIf { it.isNotBlank() },
                customGenre = genre?.trim()?.takeIf { it.isNotBlank() }
            )
        )
    }

    // Settings
    suspend fun getSetting(key: String): String? = dao.getSetting(key)

    suspend fun setSetting(key: String, value: String) {
        dao.setSetting(AppSettingsEntity(key = key, value = value))
    }
}
