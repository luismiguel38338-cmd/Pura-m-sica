package com.example.viewmodel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DemoMusicProvider
import com.example.data.MediaAudioScanner
import com.example.data.MusicLocalRepository
import com.example.data.db.PuraMusicaDatabase
import com.example.model.Album
import com.example.model.Artist
import com.example.model.EqualizerPreset
import com.example.model.MusicFolder
import com.example.model.MusicGenre
import com.example.model.PlayHistoryItem
import com.example.model.Playlist
import com.example.model.RepeatMode
import com.example.model.SectionTab
import com.example.model.Song
import com.example.model.ThemeMode
import com.example.player.RealMusicPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MusicPlayerViewModel : ViewModel() {

    private var realMusicPlayer: RealMusicPlayer? = null
    private var localRepository: MusicLocalRepository? = null
    private var appContext: Context? = null

    private val _isPermissionGranted = MutableStateFlow(true)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _folders = MutableStateFlow<List<MusicFolder>>(emptyList())
    val folders: StateFlow<List<MusicFolder>> = _folders.asStateFlow()

    private val _genres = MutableStateFlow<List<MusicGenre>>(emptyList())
    val genres: StateFlow<List<MusicGenre>> = _genres.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _playHistory = MutableStateFlow<List<PlayHistoryItem>>(emptyList())
    val playHistory: StateFlow<List<PlayHistoryItem>> = _playHistory.asStateFlow()

    private val _topPlayedSongs = MutableStateFlow<List<Song>>(emptyList())
    val topPlayedSongs: StateFlow<List<Song>> = _topPlayedSongs.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _visualizerBands = MutableStateFlow(List(16) { 0.08f })
    val visualizerBands: StateFlow<List<Float>> = _visualizerBands.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _equalizerPreset = MutableStateFlow(EqualizerPreset.BALANCED)
    val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(0)
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    private val _selectedSongForDetails = MutableStateFlow<Song?>(null)
    val selectedSongForDetails: StateFlow<Song?> = _selectedSongForDetails.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _selectedTab = MutableStateFlow(SectionTab.ALL_SONGS)
    val selectedTab: StateFlow<SectionTab> = _selectedTab.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedFolder = MutableStateFlow<MusicFolder?>(null)
    val selectedFolder: StateFlow<MusicFolder?> = _selectedFolder.asStateFlow()

    private val _selectedGenre = MutableStateFlow<MusicGenre?>(null)
    val selectedGenre: StateFlow<MusicGenre?> = _selectedGenre.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

    private val _isSettingsDialogVisible = MutableStateFlow(false)
    val isSettingsDialogVisible: StateFlow<Boolean> = _isSettingsDialogVisible.asStateFlow()

    private val _isLowPowerMode = MutableStateFlow(false)
    val isLowPowerMode: StateFlow<Boolean> = _isLowPowerMode.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var sleepTimerJob: Job? = null

    fun initialize(context: Context) {
        if (realMusicPlayer != null) return
        appContext = context.applicationContext

        val db = PuraMusicaDatabase.getInstance(context)
        val repo = MusicLocalRepository(db.musicDao())
        localRepository = repo

        val player = RealMusicPlayer(context.applicationContext)
        realMusicPlayer = player

        player.setCallbacks(
            onTrackEnded = {
                playNextTrack(autoTriggered = true)
            },
            onMediaChanged = { mediaId ->
                val song = _songs.value.firstOrNull { it.id == mediaId }
                if (song != null) {
                    _currentSong.value = song
                    viewModelScope.launch {
                        localRepository?.recordPlay(song.id)
                    }
                }
            }
        )

        viewModelScope.launch {
            player.isPlaying.collect { playing ->
                _isPlaying.value = playing
            }
        }

        viewModelScope.launch {
            player.currentPositionMs.collect { pos ->
                _playbackPositionMs.value = pos
            }
        }

        viewModelScope.launch {
            player.visualizerBands.collect { bands ->
                _visualizerBands.value = bands
            }
        }

        // Collect Favorites from Room
        viewModelScope.launch {
            repo.favoriteSongIds.collect { favList ->
                val favSet = favList.toSet()
                _favoriteIds.value = favSet
                applyOverridesAndFavorites()
            }
        }

        // Collect Playlists from Room
        viewModelScope.launch {
            repo.allPlaylists.collect { playlistEntities ->
                repo.allPlaylistItems.collect { itemEntities ->
                    val allSongsMap = _songs.value.associateBy { it.id }
                    val mapped = playlistEntities.map { pEntity ->
                        val pItems = itemEntities.filter { it.playlistId == pEntity.id }
                            .sortedBy { it.orderIndex }
                        val pSongs = pItems.mapNotNull { allSongsMap[it.songId] }
                        Playlist(
                            id = pEntity.id,
                            name = pEntity.name,
                            songCount = pSongs.size,
                            songs = pSongs,
                            createdAt = pEntity.createdAt
                        )
                    }
                    _playlists.value = mapped
                    if (_selectedPlaylist.value != null) {
                        _selectedPlaylist.value = mapped.firstOrNull { it.id == _selectedPlaylist.value?.id }
                    }
                }
            }
        }

        // Collect History from Room
        viewModelScope.launch {
            repo.playHistory.collect { historyEntities ->
                val allSongsMap = _songs.value.associateBy { it.id }
                val historyList = historyEntities.mapNotNull { entity ->
                    allSongsMap[entity.songId]?.let { song ->
                        PlayHistoryItem(
                            song = song,
                            playCount = entity.playCount,
                            lastPlayedAt = entity.lastPlayedAt
                        )
                    }
                }
                _playHistory.value = historyList
            }
        }

        // Collect Top Played from Room
        viewModelScope.launch {
            repo.topPlayed.collect { topEntities ->
                val allSongsMap = _songs.value.associateBy { it.id }
                _topPlayedSongs.value = topEntities.mapNotNull { allSongsMap[it.songId] }
            }
        }

        // Collect Tag Overrides from Room
        viewModelScope.launch {
            repo.metadataOverrides.collect {
                applyOverridesAndFavorites()
            }
        }

        // Load Settings
        viewModelScope.launch {
            repo.getSetting("theme_mode")?.let { modeStr ->
                try {
                    _themeMode.value = ThemeMode.valueOf(modeStr)
                } catch (e: Exception) {
                    // default
                }
            }
            repo.getSetting("low_power_mode")?.let {
                _isLowPowerMode.value = it.toBoolean()
            }
        }

        scanDeviceMusic(context)
    }

    fun setPermissionGranted(granted: Boolean, context: Context? = null) {
        _isPermissionGranted.value = granted
        if (granted && context != null) {
            scanDeviceMusic(context)
        }
    }

    fun scanDeviceMusic(context: Context) {
        viewModelScope.launch {
            _isScanning.value = true
            val scannedSongs = MediaAudioScanner.scanDeviceAudio(context)
            val finalSongs = if (scannedSongs.isEmpty()) {
                try {
                    DemoMusicProvider.getOrCreateDemoTracks(context)
                } catch (e: Exception) {
                    Log.w("MusicPlayerViewModel", "Error al crear pistas de muestra: ${e.message}")
                    emptyList()
                }
            } else {
                scannedSongs
            }
            _rawSongs.value = finalSongs
            applyOverridesAndFavorites()

            if (_currentSong.value == null && _songs.value.isNotEmpty()) {
                _currentSong.value = _songs.value.first()
                _queue.value = _songs.value
            } else if (_queue.value.isEmpty()) {
                _queue.value = _songs.value
            }

            _isScanning.value = false
        }
    }

    fun loadDemoTracks(context: Context) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val demoTracks = DemoMusicProvider.getOrCreateDemoTracks(context)
                val current = _rawSongs.value.toMutableList()
                demoTracks.forEach { demo ->
                    if (current.none { it.id == demo.id }) {
                        current.add(demo)
                    }
                }
                _rawSongs.value = current
                applyOverridesAndFavorites()
                if (_currentSong.value == null && current.isNotEmpty()) {
                    playSong(current.first(), current, expandPlayer = false)
                }
                _userMessage.value = "Se cargaron pistas de muestra reales con sonido de estudio"
            } catch (e: Exception) {
                Log.e("MusicPlayerViewModel", "Error al cargar pistas de muestra", e)
                _userMessage.value = "No se pudieron cargar las pistas de muestra"
            }
            _isScanning.value = false
        }
    }

    fun importAudioUris(uris: List<Uri>, context: Context) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isScanning.value = true
            val newSongs = mutableListOf<Song>()
            for (uri in uris) {
                try {
                    var title = "Audio importado"
                    var artist = "Desconocido"
                    var album = "Importadas"
                    var durationSec = 180
                    var sizeBytes = 0L

                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameCol = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameCol >= 0) {
                                val name = cursor.getString(nameCol)
                                if (!name.isNullOrBlank()) {
                                    title = name.substringBeforeLast('.')
                                }
                            }
                            val sizeCol = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeCol >= 0) {
                                sizeBytes = cursor.getLong(sizeCol)
                            }
                        }
                    }

                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, uri)
                        val metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        val metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        val metaAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                        val metaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        if (!metaTitle.isNullOrBlank()) title = metaTitle
                        if (!metaArtist.isNullOrBlank()) artist = metaArtist
                        if (!metaAlbum.isNullOrBlank()) album = metaAlbum
                        metaDuration?.toLongOrNull()?.let { durationSec = (it / 1000).toInt().coerceAtLeast(1) }
                        retriever.release()
                    } catch (e: Exception) {
                        Log.w("MusicPlayerViewModel", "Metadatos no disponibles para $uri", e)
                    }

                    val song = Song(
                        id = "imported_${System.currentTimeMillis()}_${newSongs.size}",
                        title = title,
                        artist = artist,
                        album = album,
                        durationSeconds = durationSec,
                        contentUri = uri.toString(),
                        filePath = uri.path,
                        folderPath = "Importadas",
                        folderName = "Importadas",
                        genre = "Audio",
                        sizeBytes = sizeBytes,
                        isFavorite = false
                    )
                    newSongs.add(song)
                } catch (e: Exception) {
                    Log.e("MusicPlayerViewModel", "Error procesando archivo de audio: $uri", e)
                }
            }

            if (newSongs.isNotEmpty()) {
                val updated = _rawSongs.value + newSongs
                _rawSongs.value = updated
                applyOverridesAndFavorites()
                playSong(newSongs.first(), updated, expandPlayer = true)
                _userMessage.value = "Se importaron ${newSongs.size} canciones al reproductor"
            } else {
                _userMessage.value = "No se pudieron importar los archivos de audio seleccionados"
            }
            _isScanning.value = false
        }
    }

    private fun applyOverridesAndFavorites() {
        val favSet = _favoriteIds.value
        val updated = _rawSongs.value.map { song ->
            val isFav = favSet.contains(song.id)
            song.copy(isFavorite = isFav)
        }
        _songs.value = updated
        _artists.value = MediaAudioScanner.groupArtists(updated)
        _albums.value = MediaAudioScanner.groupAlbums(updated)
        _folders.value = MediaAudioScanner.groupFolders(updated)
        _genres.value = MediaAudioScanner.groupGenres(updated)

        // update current song and queue
        _currentSong.update { cur ->
            if (cur != null) updated.firstOrNull { it.id == cur.id } ?: cur else null
        }
        _queue.update { q ->
            q.map { song -> updated.firstOrNull { it.id == song.id } ?: song }
        }
    }

    fun playSong(song: Song, newQueue: List<Song>? = null, expandPlayer: Boolean = true) {
        val targetQueue = newQueue ?: if (_queue.value.isNotEmpty()) _queue.value else _songs.value
        _queue.value = targetQueue
        _currentSong.value = song
        _playbackPositionMs.value = 0L

        realMusicPlayer?.playSong(song, targetQueue) ?: run {
            _isPlaying.value = true
        }

        viewModelScope.launch {
            localRepository?.recordPlay(song.id)
        }

        if (expandPlayer) {
            _isPlayerExpanded.value = true
        }
    }

    fun togglePlayPause() {
        if (_currentSong.value == null && _songs.value.isNotEmpty()) {
            val first = _songs.value.first()
            playSong(first, _songs.value, expandPlayer = false)
        } else {
            realMusicPlayer?.togglePlayPause() ?: run {
                _isPlaying.update { !it }
            }
        }
    }

    fun playNextTrack(autoTriggered: Boolean = false) {
        val current = _currentSong.value ?: return
        val currentQueue = _queue.value.ifEmpty { _songs.value }
        if (currentQueue.isEmpty()) return

        if (_isShuffleEnabled.value) {
            val available = currentQueue.filter { it.id != current.id }
            val next = if (available.isNotEmpty()) available[Random.nextInt(available.size)] else current
            playSong(next, currentQueue, expandPlayer = !autoTriggered)
            return
        }

        val currentIndex = currentQueue.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % currentQueue.size else 0
        playSong(currentQueue[nextIndex], currentQueue, expandPlayer = !autoTriggered)
    }

    fun playPreviousTrack() {
        if (_playbackPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val current = _currentSong.value ?: return
        val currentQueue = _queue.value.ifEmpty { _songs.value }
        if (currentQueue.isEmpty()) return

        val currentIndex = currentQueue.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else currentQueue.size - 1
        playSong(currentQueue[prevIndex], currentQueue, expandPlayer = false)
    }

    fun seekTo(positionMs: Long) {
        _playbackPositionMs.value = positionMs
        realMusicPlayer?.seekTo(positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        realMusicPlayer?.setPlaybackSpeed(speed)
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val current = _playbackSpeed.value
        val currentIndex = speeds.indexOfFirst { kotlin.math.abs(it - current) < 0.05f }
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % speeds.size else 1
        val newSpeed = speeds[nextIndex]
        setPlaybackSpeed(newSpeed)
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
        realMusicPlayer?.setEqualizerPreset(preset)
    }

    fun setBassBoostStrength(strength: Int) {
        _bassBoostStrength.value = strength.coerceIn(0, 100)
        realMusicPlayer?.setBassBoostStrength(_bassBoostStrength.value)
    }

    fun setVirtualizerStrength(strength: Int) {
        _virtualizerStrength.value = strength.coerceIn(0, 100)
        realMusicPlayer?.setVirtualizerStrength(_virtualizerStrength.value)
    }

    fun selectSongForDetails(song: Song?) {
        _selectedSongForDetails.value = song
    }

    fun setVolume(vol: Float) {
        _volume.value = vol.coerceIn(0f, 1f)
        realMusicPlayer?.setVolume(_volume.value)
    }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimerMinutes.value = minutes
        sleepTimerJob?.cancel()
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (isActive && remaining > 0) {
                    delay(60_000L)
                    remaining--
                    _sleepTimerMinutes.value = remaining
                }
                realMusicPlayer?.pause()
                _isPlaying.value = false
                _sleepTimerMinutes.value = null
            }
        }
    }

    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            localRepository?.toggleFavorite(songId)
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.update { !it }
        realMusicPlayer?.setShuffle(_isShuffleEnabled.value)
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        realMusicPlayer?.setRepeatMode(next)
    }

    // --- PLAYLIST ACTIONS ---
    fun createPlaylist(name: String, songId: String? = null) {
        viewModelScope.launch {
            val id = localRepository?.createPlaylist(name)
            if (id != null && songId != null) {
                localRepository?.addSongToPlaylist(id, songId)
            }
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            localRepository?.renamePlaylist(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            localRepository?.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            localRepository?.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            localRepository?.removeSongFromPlaylist(playlistId, songId)
        }
    }

    // --- SONG TAG EDITING ---
    fun updateSongMetadata(songId: String, title: String?, artist: String?, album: String?, genre: String?) {
        viewModelScope.launch {
            localRepository?.updateSongMetadata(songId, title, artist, album, genre)
            _rawSongs.update { list ->
                list.map { song ->
                    if (song.id == songId) {
                        song.copy(
                            title = title?.takeIf { it.isNotBlank() } ?: song.title,
                            artist = artist?.takeIf { it.isNotBlank() } ?: song.artist,
                            album = album?.takeIf { it.isNotBlank() } ?: song.album,
                            genre = genre?.takeIf { it.isNotBlank() } ?: song.genre
                        )
                    } else song
                }
            }
            applyOverridesAndFavorites()
        }
    }

    // --- HISTORY ACTIONS ---
    fun clearPlayHistory() {
        viewModelScope.launch {
            localRepository?.clearHistory()
        }
    }

    // --- EXTERNAL URI PLAYBACK ---
    fun playExternalUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                var title = "Audio de carpeta"
                var artist = "Archivo local"
                var durationSec = 180

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameCol = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameCol >= 0) {
                            val name = cursor.getString(nameCol)
                            if (!name.isNullOrBlank()) {
                                title = name.substringBeforeLast('.')
                            }
                        }
                    }
                }

                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    val metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    val metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    val metaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    if (!metaTitle.isNullOrBlank()) title = metaTitle
                    if (!metaArtist.isNullOrBlank()) artist = metaArtist
                    metaDuration?.toLongOrNull()?.let { durationSec = (it / 1000).toInt().coerceAtLeast(1) }
                    retriever.release()
                } catch (e: Exception) {
                    Log.w("MusicPlayerViewModel", "No se pudieron extraer metadatos del URI", e)
                }

                val externalSong = Song(
                    id = uri.toString(),
                    title = title,
                    artist = artist,
                    album = "Carpeta",
                    durationSeconds = durationSec,
                    contentUri = uri.toString(),
                    folderPath = "Carpeta",
                    folderName = "Carpeta",
                    genre = "Audio",
                    sizeBytes = 0L
                )

                if (_rawSongs.value.none { it.id == externalSong.id }) {
                    _rawSongs.value = _rawSongs.value + externalSong
                    applyOverridesAndFavorites()
                }

                playSong(externalSong, listOf(externalSong), expandPlayer = true)
            } catch (e: Exception) {
                Log.e("MusicPlayerViewModel", "Error al abrir audio externo: ${e.message}", e)
                _userMessage.value = "No se pudo reproducir el archivo de la carpeta"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // --- NAVIGATION AND SELECTION ---
    fun selectTab(tab: SectionTab) {
        _selectedTab.value = tab
        _selectedArtist.value = null
        _selectedAlbum.value = null
        _selectedFolder.value = null
        _selectedGenre.value = null
        _selectedPlaylist.value = null
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun selectFolder(folder: MusicFolder?) {
        _selectedFolder.value = folder
    }

    fun selectGenre(genre: MusicGenre?) {
        _selectedGenre.value = genre
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch(active: Boolean? = null) {
        _isSearchActive.update { active ?: !it }
        if (_isSearchActive.value.not()) {
            _searchQuery.value = ""
        }
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _isQueueSheetVisible.value = visible
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _isSettingsDialogVisible.value = visible
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            localRepository?.setSetting("theme_mode", mode.name)
        }
    }

    fun toggleLowPowerMode() {
        _isLowPowerMode.update { !it }
        viewModelScope.launch {
            localRepository?.setSetting("low_power_mode", _isLowPowerMode.value.toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        realMusicPlayer?.release()
    }
}
