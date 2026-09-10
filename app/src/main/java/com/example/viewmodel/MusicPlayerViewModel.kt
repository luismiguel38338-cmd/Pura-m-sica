package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioSynthesisEngine
import com.example.data.MusicRepository
import com.example.model.Album
import com.example.model.Artist
import com.example.model.EqualizerPreset
import com.example.model.RepeatMode
import com.example.model.SectionTab
import com.example.model.Song
import com.example.model.ThemeMode
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

    private val audioEngine = AudioSynthesisEngine(viewModelScope)
    val visualizerBands: StateFlow<List<Float>> = audioEngine.visualizerBands

    private val _songs = MutableStateFlow<List<Song>>(MusicRepository.initialSongs)
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(MusicRepository.artists)
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(MusicRepository.albums)
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(MusicRepository.initialSongs.firstOrNull())
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

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

    private val _queue = MutableStateFlow<List<Song>>(MusicRepository.initialSongs)
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

    private var playbackJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        startPlaybackProgressTicker()
    }

    private fun startPlaybackProgressTicker() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(200L)
                if (_isPlaying.value) {
                    val song = _currentSong.value ?: continue
                    val maxDurationMs = song.durationSeconds * 1000L
                    val stepMs = (200L * _playbackSpeed.value).toLong()
                    val nextPos = _playbackPositionMs.value + stepMs

                    if (nextPos >= maxDurationMs) {
                        when (_repeatMode.value) {
                            RepeatMode.ONE -> {
                                _playbackPositionMs.value = 0L
                                audioEngine.playSong(song)
                            }
                            RepeatMode.ALL -> {
                                playNextTrack(autoTriggered = true)
                            }
                            RepeatMode.OFF -> {
                                val currentIdx = _queue.value.indexOfFirst { it.id == song.id }
                                if (currentIdx < _queue.value.size - 1) {
                                    playNextTrack(autoTriggered = true)
                                } else {
                                    _isPlaying.value = false
                                    _playbackPositionMs.value = maxDurationMs
                                    audioEngine.pause()
                                }
                            }
                        }
                    } else {
                        _playbackPositionMs.value = nextPos
                    }
                }
            }
        }
    }

    fun playSong(song: Song, newQueue: List<Song>? = null, expandPlayer: Boolean = true) {
        if (newQueue != null) {
            _queue.value = newQueue
        }
        _currentSong.value = song
        _playbackPositionMs.value = 0L
        _isPlaying.value = true
        audioEngine.playSong(song)
        if (expandPlayer) {
            _isPlayerExpanded.value = true
        }
    }

    fun togglePlayPause() {
        if (_currentSong.value == null && _songs.value.isNotEmpty()) {
            val first = _songs.value.first()
            _currentSong.value = first
            _playbackPositionMs.value = 0L
            _isPlaying.value = true
            audioEngine.playSong(first)
        } else {
            val newPlaying = !_isPlaying.value
            _isPlaying.value = newPlaying
            if (newPlaying) {
                _currentSong.value?.let { audioEngine.resume() }
            } else {
                audioEngine.pause()
            }
        }
    }

    fun playNextTrack(autoTriggered: Boolean = false) {
        val current = _currentSong.value ?: return
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return

        if (_isShuffleEnabled.value) {
            val available = currentQueue.filter { it.id != current.id }
            val next = if (available.isNotEmpty()) available[Random.nextInt(available.size)] else current
            playSong(next, expandPlayer = !autoTriggered)
            return
        }

        val currentIndex = currentQueue.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % currentQueue.size else 0
        playSong(currentQueue[nextIndex], expandPlayer = !autoTriggered)
    }

    fun playPreviousTrack() {
        if (_playbackPositionMs.value > 3000L) {
            _playbackPositionMs.value = 0L
            return
        }

        val current = _currentSong.value ?: return
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return

        val currentIndex = currentQueue.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else currentQueue.size - 1
        playSong(currentQueue[prevIndex], expandPlayer = false)
    }

    fun seekTo(positionMs: Long) {
        val song = _currentSong.value ?: return
        val clamped = positionMs.coerceIn(0L, song.durationSeconds * 1000L)
        _playbackPositionMs.value = clamped
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val current = _playbackSpeed.value
        val currentIndex = speeds.indexOfFirst { kotlin.math.abs(it - current) < 0.05f }
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % speeds.size else 1
        _playbackSpeed.value = speeds[nextIndex]
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
        audioEngine.setEqualizer(preset)
    }

    fun setVolume(vol: Float) {
        _volume.value = vol.coerceIn(0f, 1f)
        audioEngine.setVolume(_volume.value)
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
                _isPlaying.value = false
                audioEngine.pause()
                _sleepTimerMinutes.value = null
            }
        }
    }

    fun toggleFavorite(songId: String) {
        _songs.update { list ->
            list.map { song ->
                if (song.id == songId) song.copy(isFavorite = !song.isFavorite) else song
            }
        }
        _queue.update { list ->
            list.map { song ->
                if (song.id == songId) song.copy(isFavorite = !song.isFavorite) else song
            }
        }
        _currentSong.update { current ->
            if (current?.id == songId) current.copy(isFavorite = !current.isFavorite) else current
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.update { !it }
    }

    fun cycleRepeatMode() {
        _repeatMode.update { current ->
            when (current) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
        }
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
        playbackJob?.cancel()
        sleepTimerJob?.cancel()
        audioEngine.release()
    }
}

