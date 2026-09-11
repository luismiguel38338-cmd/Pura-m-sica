package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // --- FAVORITES ---
    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: String): Boolean

    // --- PLAYLISTS ---
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun updatePlaylistName(playlistId: Long, newName: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    // --- PLAYLIST ITEMS ---
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC, addedAt ASC")
    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items")
    fun getAllPlaylistItems(): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deletePlaylistItem(playlistId: Long, songId: String)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun deleteAllPlaylistItems(playlistId: Long)

    @Transaction
    suspend fun deletePlaylistAndItems(playlistId: Long) {
        deleteAllPlaylistItems(playlistId)
        deletePlaylist(playlistId)
    }

    // --- PLAY HISTORY & STATS ---
    @Query("SELECT * FROM play_history ORDER BY lastPlayedAt DESC")
    fun getPlayHistory(): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history ORDER BY playCount DESC LIMIT 50")
    fun getTopPlayed(): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE songId = :songId")
    suspend fun getPlayHistoryForSong(songId: String): PlayHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(entity: PlayHistoryEntity)

    @Transaction
    suspend fun recordSongPlay(songId: String) {
        val existing = getPlayHistoryForSong(songId)
        val newCount = (existing?.playCount ?: 0) + 1
        insertPlayHistory(
            PlayHistoryEntity(
                songId = songId,
                playCount = newCount,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }

    @Query("DELETE FROM play_history")
    suspend fun clearPlayHistory()

    // --- SONG METADATA OVERRIDES (EDIT TAGS) ---
    @Query("SELECT * FROM song_metadata_overrides")
    fun getAllMetadataOverrides(): Flow<List<SongMetadataOverrideEntity>>

    @Query("SELECT * FROM song_metadata_overrides WHERE songId = :songId")
    suspend fun getMetadataOverride(songId: String): SongMetadataOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadataOverride(override: SongMetadataOverrideEntity)

    // --- SETTINGS ---
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)
}
