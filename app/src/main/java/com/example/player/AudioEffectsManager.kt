package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.EqualizerPreset

class AudioEffectsManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    private var currentPreset: EqualizerPreset = EqualizerPreset.BALANCED
    private var currentBassStrength: Int = 0
    private var currentVirtualizerStrength: Int = 0

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "No se pudo inicializar Equalizer: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "No se pudo inicializar BassBoost: ${e.message}")
        }

        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "No se pudo inicializar Virtualizer: ${e.message}")
        }

        applyPreset(currentPreset)
        setBassBoost(currentBassStrength)
        setVirtualizer(currentVirtualizerStrength)
    }

    fun applyPreset(preset: EqualizerPreset) {
        currentPreset = preset
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            if (numBands <= 0) return
            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]

            for (band in 0 until numBands) {
                val ratio = when (preset) {
                    EqualizerPreset.BALANCED -> 0f
                    EqualizerPreset.BASS_BOOST -> when {
                        band == 0 -> 0.75f
                        band == 1 -> 0.45f
                        else -> 0.05f
                    }
                    EqualizerPreset.VOCAL -> when {
                        band == numBands / 2 -> 0.65f
                        band > numBands / 2 -> 0.25f
                        else -> -0.15f
                    }
                    EqualizerPreset.ACOUSTIC -> when {
                        band == 0 -> 0.15f
                        band == 1 -> 0.20f
                        band >= numBands - 2 -> 0.30f
                        else -> 0.10f
                    }
                    EqualizerPreset.ROCK -> when {
                        band == 0 -> 0.60f
                        band == 1 -> 0.35f
                        band == numBands - 1 -> 0.65f
                        band == numBands - 2 -> 0.40f
                        else -> -0.20f
                    }
                    EqualizerPreset.POP -> when {
                        band >= numBands / 2 -> 0.45f
                        band == 0 -> 0.20f
                        else -> 0.10f
                    }
                    EqualizerPreset.JAZZ -> when {
                        band == 0 -> 0.35f
                        band == 1 -> 0.45f
                        band == 2 -> 0.25f
                        else -> 0.10f
                    }
                    EqualizerPreset.ELECTRONIC -> when {
                        band <= 1 -> 0.80f
                        band >= numBands - 2 -> 0.55f
                        else -> 0.05f
                    }
                    EqualizerPreset.CLASSICAL -> when {
                        band == 0 -> 0.35f
                        band == numBands - 1 -> 0.35f
                        else -> 0.05f
                    }
                }

                val targetLevel = if (ratio >= 0) {
                    (maxLevel * ratio).toInt().toShort()
                } else {
                    (minLevel * -ratio).toInt().toShort()
                }
                eq.setBandLevel(band.toShort(), targetLevel)
            }

            // Adjust bass boost accordingly if not overridden
            if (currentBassStrength == 0) {
                val autoBass: Short = when (preset) {
                    EqualizerPreset.BASS_BOOST -> 800
                    EqualizerPreset.ELECTRONIC -> 650
                    EqualizerPreset.ROCK -> 450
                    else -> 0
                }
                bassBoost?.setStrength(autoBass)
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Error al aplicar preset de ecualizador: ${e.message}")
        }
    }

    fun setBassBoost(strengthPercent: Int) {
        currentBassStrength = strengthPercent.coerceIn(0, 100)
        try {
            val strength = (currentBassStrength * 10).toShort()
            bassBoost?.setStrength(strength)
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Error al ajustar refuerzo de graves: ${e.message}")
        }
    }

    fun setVirtualizer(strengthPercent: Int) {
        currentVirtualizerStrength = strengthPercent.coerceIn(0, 100)
        try {
            val strength = (currentVirtualizerStrength * 10).toShort()
            virtualizer?.setStrength(strength)
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Error al ajustar virtualizador 3D: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            // Ignored
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        currentSessionId = 0
    }
}
