package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.model.EqualizerPreset
import com.example.model.Song
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

class AudioSynthesisEngine(
    private val scope: CoroutineScope
) {
    private var audioTrack: AudioTrack? = null
    private var audioJob: Job? = null
    private var visualizerJob: Job? = null

    private val sampleRate = 22050
    private val bufferSize = maxOf(
        2048,
        try {
            AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
        } catch (_: Exception) {
            4096
        }
    )

    private val _visualizerBands = MutableStateFlow(listOf(0.15f, 0.25f, 0.2f, 0.3f, 0.2f, 0.25f, 0.15f))
    val visualizerBands: StateFlow<List<Float>> = _visualizerBands.asStateFlow()

    private var currentSong: Song? = null
    private var isPlaying: Boolean = false
    private var equalizerPreset: EqualizerPreset = EqualizerPreset.BALANCED
    private var volume: Float = 0.85f

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.w("AudioSynthesisEngine", "AudioTrack could not be initialized in this environment: ${e.message}")
            audioTrack = null
        }
    }

    fun playSong(song: Song) {
        currentSong = song
        isPlaying = true
        startAudioStream()
        startVisualizerLoop()
    }

    fun resume() {
        isPlaying = true
        startAudioStream()
        startVisualizerLoop()
    }

    fun pause() {
        isPlaying = false
        audioJob?.cancel()
        audioJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: Exception) {}
        _visualizerBands.value = listOf(0.08f, 0.08f, 0.08f, 0.08f, 0.08f, 0.08f, 0.08f)
    }

    fun setEqualizer(preset: EqualizerPreset) {
        equalizerPreset = preset
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        try {
            audioTrack?.setVolume(volume)
        } catch (_: Exception) {}
    }

    private fun startAudioStream() {
        audioJob?.cancel()
        val track = audioTrack ?: return

        audioJob = scope.launch(Dispatchers.Default) {
            try {
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }

                val baseFreq = currentSong?.baseFrequencyHz ?: 220f
                val shortBuffer = ShortArray(bufferSize / 2)
                var phase = 0.0
                var step = 0

                val chordIntervals = listOf(1.0, 1.25, 1.5, 1.875) // Major 7th harmony

                while (isActive && isPlaying) {
                    val root = baseFreq * chordIntervals[(step / 30) % chordIntervals.size]
                    val bassGain = equalizerPreset.bassMultiplier
                    val trebleGain = equalizerPreset.trebleMultiplier

                    for (i in shortBuffer.indices) {
                        val t = phase / sampleRate
                        // Soft analog synth pad formula: fundamental + sub-bass + harmonic overtone
                        val fundamental = sin(2.0 * Math.PI * root * t)
                        val subBass = sin(Math.PI * root * t) * (0.45 * bassGain)
                        val fifthHarmonic = sin(3.0 * Math.PI * root * t) * (0.25 * trebleGain)

                        // Amplitude envelope: soft, pleasing ambient level
                        val sample = (fundamental + subBass + fifthHarmonic) * 0.22 * volume
                        shortBuffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        phase += 1.0
                    }

                    track.write(shortBuffer, 0, shortBuffer.size)
                    step++
                }
            } catch (e: Exception) {
                Log.d("AudioSynthesisEngine", "Streaming ended: ${e.message}")
            }
        }
    }

    private fun startVisualizerLoop() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch(Dispatchers.Default) {
            var cycle = 0.0
            while (isActive && isPlaying) {
                cycle += 0.25
                val bass = (0.4f + 0.5f * sin(cycle * 0.9).toFloat() * equalizerPreset.bassMultiplier).coerceIn(0.15f, 0.95f)
                val lowMid = (0.35f + 0.45f * sin(cycle * 1.3 + 0.8).toFloat()).coerceIn(0.12f, 0.9f)
                val mid = (0.5f + 0.45f * sin(cycle * 1.7 + 1.6).toFloat()).coerceIn(0.18f, 0.98f)
                val highMid = (0.3f + 0.4f * sin(cycle * 2.1 + 2.4).toFloat()).coerceIn(0.15f, 0.88f)
                val treble = (0.25f + 0.45f * sin(cycle * 2.7 + 3.2).toFloat() * equalizerPreset.trebleMultiplier).coerceIn(0.1f, 0.92f)
                val ultraHigh = (0.2f + 0.35f * sin(cycle * 3.3 + 4.0).toFloat()).coerceIn(0.08f, 0.85f)
                val presence = (0.3f + 0.4f * sin(cycle * 1.5 + 4.8).toFloat()).coerceIn(0.12f, 0.88f)

                _visualizerBands.value = listOf(bass, lowMid, mid, highMid, treble, ultraHigh, presence)
                delay(60L)
            }
        }
    }

    fun release() {
        isPlaying = false
        audioJob?.cancel()
        visualizerJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
