package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaAudioScanner
import com.example.data.MusicRepository
import com.example.model.Album
import com.example.model.Artist
import com.example.model.EqualizerPreset
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
    private var appContext: Context? = null

    private val _isPermissionGranted = MutableStateFlow(true)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

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

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private var sleepTimerJob: Job? = null

    fun initialize(context: Context) {
        if (realMusicPlayer != null) return
        appContext = context.applicationContext
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
            val favIds = MusicRepository.getFavoriteIds(context)

            val updatedSongs = scannedSongs.map { song ->
                if (favIds.contains(song.id)) song.copy(isFavorite = true) else song
            }

            _songs.value = updatedSongs
            _artists.value = MediaAudioScanner.groupArtists(updatedSongs)
            _albums.value = MediaAudioScanner.groupAlbums(updatedSongs)

            if (_currentSong.value == null && updatedSongs.isNotEmpty()) {
                _currentSong.value = updatedSongs.first()
                _queue.value = updatedSongs
            } else if (_queue.value.isEmpty()) {
                _queue.value = updatedSongs
            }

            _isScanning.value = false
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
        var isFav = false
        _songs.update { list ->
            list.map { song ->
                if (song.id == songId) {
                    isFav = !song.isFavorite
                    song.copy(isFavorite = isFav)
                } else song
            }
        }
        _queue.update { list ->
            list.map { song ->
                if (song.id == songId) song.copy(isFavorite = isFav) else song
            }
        }
        _currentSong.update { current ->
            if (current?.id == songId) current.copy(isFavorite = isFav) else current
        }

        appContext?.let { ctx ->
            val favIds = _songs.value.filter { it.isFavorite }.map { it.id }.toSet()
            MusicRepository.saveFavoriteIds(ctx, favIds)
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

    fun selectTab(tab: SectionTab) {
        _selectedTab.value = tab
        _selectedArtist.value = null
        _selectedAlbum.value = null
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
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
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        realMusicPlayer?.release()
    }
}
