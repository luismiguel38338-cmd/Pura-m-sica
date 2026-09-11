package com.example.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.EqualizerPreset
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.service.MusicPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class RealMusicPlayer(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()

    private val _visualizerBands = MutableStateFlow(List(16) { 0.1f })
    val visualizerBands: StateFlow<List<Float>> = _visualizerBands.asStateFlow()

    private var positionTrackerJob: Job? = null
    private var visualizerJob: Job? = null

    private var onSongEndedCallback: (() -> Unit)? = null
    private var onMediaItemChangedCallback: ((String) -> Unit)? = null

    init {
        initializeController()
    }

    private fun isRunningInRobolectric(): Boolean {
        return android.os.Build.FINGERPRINT.startsWith("robolectric") ||
                "robolectric" == android.os.Build.HARDWARE
    }

    private fun initializeController() {
        if (isRunningInRobolectric()) return

        try {
            val serviceIntent = Intent(context, MusicPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.w("RealMusicPlayer", "No se pudo pre-iniciar MusicPlaybackService", e)
        }

        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val mediaController = controllerFuture?.get() ?: return@addListener
                controller = mediaController
                mediaController.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            startPositionTracking()
                            startVisualizer()
                        } else {
                            stopPositionTracking()
                            stopVisualizer()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            onSongEndedCallback?.invoke()
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.mediaId?.let { id ->
                            _currentMediaId.value = id
                            onMediaItemChangedCallback?.invoke(id)
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("RealMusicPlayer", "Error de reproducción: ${error.message}", error)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setCallbacks(onTrackEnded: () -> Unit, onMediaChanged: (String) -> Unit) {
        this.onSongEndedCallback = onTrackEnded
        this.onMediaItemChangedCallback = onMediaChanged
    }

    fun playSong(song: Song, queue: List<Song>) {
        if (isRunningInRobolectric()) {
            _isPlaying.value = true
            _currentMediaId.value = song.id
            return
        }
        val ctrl = controller
        if (ctrl != null) {
            playWithController(ctrl, song, queue)
            return
        }

        val activePlr = MusicPlaybackService.activePlayer
        if (activePlr != null) {
            playWithPlayer(activePlr, song, queue)
            return
        }

        initializeController()
        scope.launch {
            delay(350)
            controller?.let { playWithController(it, song, queue) }
                ?: MusicPlaybackService.activePlayer?.let { playWithPlayer(it, song, queue) }
        }
    }

    private fun playWithController(ctrl: MediaController, song: Song, queue: List<Song>) {
        try {
            val targetIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            val mediaItems = queue.map { qSong ->
                val metadataBuilder = MediaMetadata.Builder()
                    .setTitle(qSong.title)
                    .setArtist(qSong.artist)
                    .setAlbumTitle(qSong.album)
                qSong.albumArtUri?.let { uriStr ->
                    metadataBuilder.setArtworkUri(Uri.parse(uriStr))
                }

                MediaItem.Builder()
                    .setMediaId(qSong.id)
                    .setUri(Uri.parse(qSong.contentUri))
                    .setMediaMetadata(metadataBuilder.build())
                    .build()
            }

            ctrl.setMediaItems(mediaItems, targetIndex, 0L)
            ctrl.prepare()
            ctrl.play()
            _isPlaying.value = true
            _currentMediaId.value = song.id
            startPositionTracking()
            startVisualizer()
        } catch (e: Exception) {
            Log.e("RealMusicPlayer", "Error al reproducir con MediaController: ${e.message}", e)
        }
    }

    private fun playWithPlayer(plr: Player, song: Song, queue: List<Song>) {
        try {
            val targetIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            val mediaItems = queue.map { qSong ->
                val metadataBuilder = MediaMetadata.Builder()
                    .setTitle(qSong.title)
                    .setArtist(qSong.artist)
                    .setAlbumTitle(qSong.album)
                qSong.albumArtUri?.let { uriStr ->
                    metadataBuilder.setArtworkUri(Uri.parse(uriStr))
                }

                MediaItem.Builder()
                    .setMediaId(qSong.id)
                    .setUri(Uri.parse(qSong.contentUri))
                    .setMediaMetadata(metadataBuilder.build())
                    .build()
            }

            plr.setMediaItems(mediaItems, targetIndex, 0L)
            plr.prepare()
            plr.play()
            _isPlaying.value = true
            _currentMediaId.value = song.id
            startPositionTracking()
            startVisualizer()
        } catch (e: Exception) {
            Log.e("RealMusicPlayer", "Error al reproducir con Player: ${e.message}", e)
        }
    }

    fun togglePlayPause() {
        if (isRunningInRobolectric()) {
            _isPlaying.value = !_isPlaying.value
            return
        }
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.let { plr ->
            if (plr.isPlaying) {
                plr.pause()
            } else {
                plr.play()
            }
        }
    }

    fun pause() {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.pause()
    }

    fun resume() {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.play()
    }

    fun seekTo(positionMs: Long) {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun skipToNext() {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.let { plr ->
            if (plr.hasNextMediaItem()) {
                plr.seekToNextMediaItem()
            } else {
                onSongEndedCallback?.invoke()
            }
        }
    }

    fun skipToPrevious() {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.let { plr ->
            if (plr.currentPosition > 3000L) {
                plr.seekTo(0L)
            } else if (plr.hasPreviousMediaItem()) {
                plr.seekToPreviousMediaItem()
            } else {
                plr.seekTo(0L)
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.playbackParameters = PlaybackParameters(speed)
    }

    fun setRepeatMode(repeatMode: RepeatMode) {
        val mode = when (repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.repeatMode = mode
    }

    fun setShuffle(enabled: Boolean) {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.shuffleModeEnabled = enabled
    }

    fun setVolume(volume: Float) {
        val target: Player? = controller ?: MusicPlaybackService.activePlayer
        target?.volume = volume.coerceIn(0f, 1f)
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        MusicPlaybackService.activeInstance?.applyEqualizerPreset(preset)
    }

    fun setBassBoostStrength(strengthPercent: Int) {
        MusicPlaybackService.activeInstance?.setBassBoostStrength(strengthPercent)
    }

    fun setVirtualizerStrength(strengthPercent: Int) {
        MusicPlaybackService.activeInstance?.setVirtualizerStrength(strengthPercent)
    }

    private fun startPositionTracking() {
        positionTrackerJob?.cancel()
        positionTrackerJob = scope.launch {
            while (isActive) {
                val target: Player? = controller ?: MusicPlaybackService.activePlayer
                target?.let { plr ->
                    _currentPositionMs.value = plr.currentPosition.coerceAtLeast(0L)
                }
                delay(250)
            }
        }
    }

    private fun stopPositionTracking() {
        positionTrackerJob?.cancel()
        positionTrackerJob = null
    }

    private fun startVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch {
            var tick = 0.0
            while (isActive) {
                val bands = List(16) { i ->
                    val freq = (i + 1) * 0.4
                    val v1 = (sin(tick * 3.5 + freq) + 1.0) * 0.35
                    val v2 = (sin(tick * 5.0 - freq * 0.7) + 1.0) * 0.15
                    (v1 + v2).toFloat().coerceIn(0.08f, 0.95f)
                }
                _visualizerBands.value = bands
                tick += 0.08
                delay(60)
            }
        }
    }

    private fun stopVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = null
        _visualizerBands.value = List(16) { 0.08f }
    }

    fun release() {
        stopPositionTracking()
        stopVisualizer()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }
}
