package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.model.EqualizerPreset
import com.example.player.AudioEffectsManager

class MusicPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private val audioEffectsManager = AudioEffectsManager()

    companion object {
        const val CHANNEL_ID = "pura_musica_playback_channel"
        const val NOTIFICATION_ID = 1001

        @Volatile
        var activeInstance: MusicPlaybackService? = null
            private set

        @Volatile
        var activePlayer: ExoPlayer? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("MusicPlaybackService", "Error en reproducción: ${error.errorCodeName} - ${error.message}", error)
            }
        })

        player = exoPlayer
        activePlayer = exoPlayer
        activeInstance = this

        try {
            audioEffectsManager.attachToSession(exoPlayer.audioSessionId)
        } catch (e: Exception) {
            Log.w("MusicPlaybackService", "No se pudo vincular efectos de audio: ${e.message}")
        }

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val callback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailablePlayerCommands(
                        MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                            .add(Player.COMMAND_PLAY_PAUSE)
                            .add(Player.COMMAND_PREPARE)
                            .add(Player.COMMAND_STOP)
                            .add(Player.COMMAND_SEEK_TO_NEXT)
                            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                            .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                            .add(Player.COMMAND_SET_SPEED_AND_PITCH)
                            .add(Player.COMMAND_SET_REPEAT_MODE)
                            .add(Player.COMMAND_SET_SHUFFLE_MODE)
                            .add(Player.COMMAND_SET_MEDIA_ITEM)
                            .add(Player.COMMAND_CHANGE_MEDIA_ITEMS)
                            .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
                            .add(Player.COMMAND_GET_TIMELINE)
                            .build()
                    )
                    .setAvailableSessionCommands(
                        MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                    )
                    .build()
            }
        }

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(callback)
            .build()

        try {
            val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setNotificationId(NOTIFICATION_ID)
                .build()
            setMediaNotificationProvider(notificationProvider)
        } catch (e: Exception) {
            Log.w("MusicPlaybackService", "No se pudo configurar el proveedor de notificaciones predeterminado", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación multimedia y controles de reproducción de Pura Música"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun applyEqualizerPreset(preset: EqualizerPreset) {
        player?.audioSessionId?.let { audioEffectsManager.attachToSession(it) }
        audioEffectsManager.applyPreset(preset)
    }

    fun setBassBoostStrength(strengthPercent: Int) {
        player?.audioSessionId?.let { audioEffectsManager.attachToSession(it) }
        audioEffectsManager.setBassBoost(strengthPercent)
    }

    fun setVirtualizerStrength(strengthPercent: Int) {
        player?.audioSessionId?.let { audioEffectsManager.attachToSession(it) }
        audioEffectsManager.setVirtualizer(strengthPercent)
    }

    override fun onDestroy() {
        audioEffectsManager.release()
        if (activeInstance == this) {
            activeInstance = null
            activePlayer = null
        }
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
