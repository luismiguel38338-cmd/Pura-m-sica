package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object DemoMusicProvider {

    private const val SAMPLE_RATE = 32000
    private const val DURATION_SEC = 35

    suspend fun getOrCreateDemoTracks(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val demoDir = File(context.filesDir, "pura_musica_samples").apply { mkdirs() }

        val tracks = listOf(
            DemoTrackSpec(
                fileName = "sueno_acustico.wav",
                title = "Sueño Acústico",
                artist = "Pura Música Studio",
                album = "Esencia Acústica",
                genre = "Acústico / Chill",
                colors = listOf(0xFF10B981, 0xFF3B82F6),
                type = TrackType.ACOUSTIC_CHILL
            ),
            DemoTrackSpec(
                fileName = "ondas_neon.wav",
                title = "Ondas Neón",
                artist = "Retro Pulse",
                album = "Vía Láctea",
                genre = "Synthwave / Retro",
                colors = listOf(0xFF8B5CF6, 0xFFEC4899),
                type = TrackType.SYNTHWAVE
            ),
            DemoTrackSpec(
                fileName = "ritmo_del_sol.wav",
                title = "Ritmo del Sol",
                artist = "Música Viva",
                album = "Tarde Tropical",
                genre = "Latino / Pop",
                colors = listOf(0xFFF97316, 0xFFF59E0B),
                type = TrackType.LATIN_GROOVE
            ),
            DemoTrackSpec(
                fileName = "claro_de_luna.wav",
                title = "Claro de Luna",
                artist = "Orquesta Armonía",
                album = "Noches Clásicas",
                genre = "Clásica / Piano",
                colors = listOf(0xFF3B82F6, 0xFF8B5CF6),
                type = TrackType.CLASSICAL_PIANO
            )
        )

        tracks.mapIndexed { index, spec ->
            val file = File(demoDir, spec.fileName)
            if (!file.exists() || file.length() < 1000L) {
                generateTrack(file, spec.type)
            }

            Song(
                id = "sample_track_${index + 1}",
                title = spec.title,
                artist = spec.artist,
                album = spec.album,
                durationSeconds = DURATION_SEC,
                contentUri = Uri.fromFile(file).toString(),
                albumArtUri = null,
                albumId = 99900L + index,
                filePath = file.absolutePath,
                folderPath = demoDir.absolutePath,
                folderName = "Pura Música",
                genre = spec.genre,
                sizeBytes = file.length(),
                gradientColors = spec.colors,
                isFavorite = false
            )
        }
    }

    private enum class TrackType {
        ACOUSTIC_CHILL,
        SYNTHWAVE,
        LATIN_GROOVE,
        CLASSICAL_PIANO
    }

    private data class DemoTrackSpec(
        val fileName: String,
        val title: String,
        val artist: String,
        val album: String,
        val genre: String,
        val colors: List<Long>,
        val type: TrackType
    )

    private fun generateTrack(targetFile: File, type: TrackType) {
        val totalSamples = SAMPLE_RATE * DURATION_SEC
        val samples = ShortArray(totalSamples)

        when (type) {
            TrackType.ACOUSTIC_CHILL -> generateAcousticChill(samples)
            TrackType.SYNTHWAVE -> generateSynthwave(samples)
            TrackType.LATIN_GROOVE -> generateLatinGroove(samples)
            TrackType.CLASSICAL_PIANO -> generateClassicalPiano(samples)
        }

        writeWavFile(targetFile, SAMPLE_RATE, samples)
    }

    // 1. Acoustic Chill (BPM: 90) - Plucked guitar chord arpeggios + lofi beat
    private fun generateAcousticChill(samples: ShortArray) {
        val bpm = 90.0
        val beatDuration = 60.0 / bpm
        val chordProg = listOf(
            doubleArrayOf(130.81, 196.00, 261.63, 329.63, 392.00, 493.88), // Cmaj7
            doubleArrayOf(110.00, 164.81, 220.00, 261.63, 329.63, 392.00), // Am7
            doubleArrayOf(87.31, 174.61, 220.00, 261.63, 349.23, 440.00),  // Fmaj7
            doubleArrayOf(98.00, 146.83, 196.00, 246.94, 293.66, 349.23)   // G7
        )

        val totalSamples = samples.size
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val beatTime = t % beatDuration
            val currentBeatIndex = (t / beatDuration).toInt()
            val chordIndex = (currentBeatIndex / 4) % chordProg.size
            val chord = chordProg[chordIndex]

            var mix = 0.0

            // Arpeggio notes in chord (plucked guitar simulation)
            val subBeat = (t / (beatDuration / 4.0)).toInt() % 4
            val noteFreq = chord[subBeat % chord.size]
            val noteTime = t % (beatDuration / 4.0)
            val pluckEnvelope = exp(-noteTime * 9.0)
            val guitarTone = sin(2.0 * PI * noteFreq * t) +
                    0.4 * sin(4.0 * PI * noteFreq * t) +
                    0.2 * sin(6.0 * PI * noteFreq * t)
            mix += guitarTone * pluckEnvelope * 0.45

            // Warm sub bass on 1st beat of measure
            val measureTime = t % (beatDuration * 4.0)
            val bassFreq = chord[0]
            val bassEnv = exp(-measureTime * 1.5)
            val bass = sin(2.0 * PI * bassFreq * t) + 0.3 * sin(4.0 * PI * bassFreq * t)
            mix += bass * bassEnv * 0.35

            // Gentle lofi drums: Kick on beat 1 & 3
            val beatInBar = currentBeatIndex % 4
            if (beatInBar == 0 || beatInBar == 2) {
                val kickEnv = exp(-beatTime * 18.0)
                val kickSweepFreq = 110.0 * exp(-beatTime * 35.0) + 45.0
                val kick = sin(2.0 * PI * kickSweepFreq * beatTime)
                mix += kick * kickEnv * 0.40
            }

            // Snare / rimshot on beat 2 & 4
            if (beatInBar == 1 || beatInBar == 3) {
                val snareEnv = exp(-beatTime * 16.0)
                val noise = (Random.nextDouble() * 2.0 - 1.0)
                val snareTone = sin(2.0 * PI * 180.0 * beatTime)
                mix += (noise * 0.6 + snareTone * 0.4) * snareEnv * 0.28
            }

            // Soft hi-hat on every 8th note
            val eighthTime = t % (beatDuration / 2.0)
            val hatEnv = exp(-eighthTime * 45.0)
            val hatNoise = (Random.nextDouble() * 2.0 - 1.0)
            mix += hatNoise * hatEnv * 0.12

            samples[i] = (mix.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
    }

    // 2. Synthwave / Retrowave (BPM: 116) - 80s analog bass, driving kick and neon arp
    private fun generateSynthwave(samples: ShortArray) {
        val bpm = 116.0
        val beatDuration = 60.0 / bpm
        val chordProg = listOf(
            doubleArrayOf(146.83, 220.00, 293.66, 349.23), // Dm
            doubleArrayOf(116.54, 174.61, 233.08, 293.66), // Bb
            doubleArrayOf(174.61, 220.00, 261.63, 349.23), // F
            doubleArrayOf(130.81, 196.00, 261.63, 329.63)  // C
        )

        val totalSamples = samples.size
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val beatTime = t % beatDuration
            val currentBeatIndex = (t / beatDuration).toInt()
            val chordIndex = (currentBeatIndex / 4) % chordProg.size
            val chord = chordProg[chordIndex]

            var mix = 0.0

            // 16th-note fast synth arp
            val sixteenthTime = t % (beatDuration / 4.0)
            val noteStep = (t / (beatDuration / 4.0)).toInt() % 4
            val arpFreq = chord[noteStep] * 2.0
            val arpEnv = exp(-sixteenthTime * 12.0)
            val arpTone = sin(2.0 * PI * arpFreq * t) + 0.5 * sin(4.0 * PI * arpFreq * t)
            mix += arpTone * arpEnv * 0.35

            // Punchy 80s Bassline (pumping 8th notes)
            val eighthTime = t % (beatDuration / 2.0)
            val bassFreq = chord[0] * 0.5
            val bassEnv = exp(-eighthTime * 8.0)
            val bassTone = sin(2.0 * PI * bassFreq * t) + 0.4 * sin(4.0 * PI * bassFreq * t)
            mix += bassTone * bassEnv * 0.40

            // Driving 4-on-the-floor Kick
            val kickEnv = exp(-beatTime * 20.0)
            val kickFreq = 140.0 * exp(-beatTime * 30.0) + 50.0
            val kick = sin(2.0 * PI * kickFreq * beatTime)
            mix += kick * kickEnv * 0.45

            // Crisp Snare on beat 2 & 4
            val beatInBar = currentBeatIndex % 4
            if (beatInBar == 1 || beatInBar == 3) {
                val snareEnv = exp(-beatTime * 14.0)
                val noise = (Random.nextDouble() * 2.0 - 1.0)
                mix += noise * snareEnv * 0.35
            }

            samples[i] = (mix.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
    }

    // 3. Latin Groove / Pop (BPM: 104) - Piano montuno & syncopated tropical rhythm
    private fun generateLatinGroove(samples: ShortArray) {
        val bpm = 104.0
        val beatDuration = 60.0 / bpm
        val chords = listOf(
            doubleArrayOf(220.00, 261.63, 329.63), // Am
            doubleArrayOf(146.83, 174.61, 220.00), // Dm
            doubleArrayOf(164.81, 207.65, 246.94), // E7
            doubleArrayOf(220.00, 261.63, 329.63)  // Am
        )

        val totalSamples = samples.size
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val beatTime = t % beatDuration
            val currentBeatIndex = (t / beatDuration).toInt()
            val chordIndex = (currentBeatIndex / 4) % chords.size
            val chord = chords[chordIndex]

            var mix = 0.0

            // Montuno syncopation
            val stepTime = t % (beatDuration / 2.0)
            val note = chord[(currentBeatIndex + (t / (beatDuration / 2.0)).toInt()) % chord.size]
            val pianoEnv = exp(-stepTime * 7.0)
            val piano = sin(2.0 * PI * note * t) + 0.3 * sin(4.0 * PI * note * t)
            mix += piano * pianoEnv * 0.40

            // Conga / Dembow syncopated beat
            val dembowTime = t % beatDuration
            val kick = sin(2.0 * PI * (120.0 * exp(-dembowTime * 25.0) + 40.0) * dembowTime) * exp(-dembowTime * 15.0)
            mix += kick * 0.35

            // Shaker pattern
            val sixteenthTime = t % (beatDuration / 4.0)
            val shaker = (Random.nextDouble() * 2.0 - 1.0) * exp(-sixteenthTime * 40.0)
            mix += shaker * 0.18

            samples[i] = (mix.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
    }

    // 4. Classical Piano Nocturne (BPM: 70) - Expressive nocturne waltz
    private fun generateClassicalPiano(samples: ShortArray) {
        val bpm = 70.0
        val beatDuration = 60.0 / bpm
        // Dbmaj7 - Bbm7 - Ebm7 - Ab7
        val leftHandNotes = listOf(138.59, 116.54, 155.56, 103.83)
        val melodyNotes = listOf(
            listOf(554.37, 622.25, 698.46, 554.37),
            listOf(466.16, 554.37, 622.25, 466.16),
            listOf(622.25, 698.46, 830.61, 622.25),
            listOf(415.30, 519.13, 622.25, 554.37)
        )

        val totalSamples = samples.size
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val currentBeat = (t / beatDuration).toInt()
            val bar = (currentBeat / 3) % 4
            val beatInBar = currentBeat % 3
            val beatTime = t % beatDuration

            var mix = 0.0

            // Bass root on 1st beat of waltz
            val bassEnv = exp(-beatTime * 3.5)
            val bass = sin(2.0 * PI * leftHandNotes[bar] * t) + 0.25 * sin(4.0 * PI * leftHandNotes[bar] * t)
            mix += bass * bassEnv * 0.35

            // Left hand accompaniment chords on beat 2 & 3
            if (beatInBar > 0) {
                val chordEnv = exp(-beatTime * 5.0)
                val chordTone = sin(2.0 * PI * (leftHandNotes[bar] * 2.0) * t) +
                        sin(2.0 * PI * (leftHandNotes[bar] * 2.5) * t)
                mix += chordTone * chordEnv * 0.22
            }

            // Right hand expressive melody
            val melodySet = melodyNotes[bar]
            val melodyNote = melodySet[currentBeat % melodySet.size]
            val melodyEnv = exp(-beatTime * 4.0)
            val melody = sin(2.0 * PI * melodyNote * t) +
                    0.35 * sin(4.0 * PI * melodyNote * t) +
                    0.15 * sin(6.0 * PI * melodyNote * t)
            mix += melody * melodyEnv * 0.45

            samples[i] = (mix.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
    }

    private fun writeWavFile(file: File, sampleRate: Int, samples: ShortArray) {
        val totalAudioLen = samples.size * 2L
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        FileOutputStream(file).use { fos ->
            BufferedOutputStream(fos, 64 * 1024).use { out ->
                val header = ByteArray(44)
                header[0] = 'R'.code.toByte()
                header[1] = 'I'.code.toByte()
                header[2] = 'F'.code.toByte()
                header[3] = 'F'.code.toByte()
                header[4] = (totalDataLen and 0xff).toByte()
                header[5] = ((totalDataLen shr 8) and 0xff).toByte()
                header[6] = ((totalDataLen shr 16) and 0xff).toByte()
                header[7] = ((totalDataLen shr 24) and 0xff).toByte()
                header[8] = 'W'.code.toByte()
                header[9] = 'A'.code.toByte()
                header[10] = 'V'.code.toByte()
                header[11] = 'E'.code.toByte()
                header[12] = 'f'.code.toByte()
                header[13] = 'm'.code.toByte()
                header[14] = 't'.code.toByte()
                header[15] = ' '.code.toByte()
                header[16] = 16
                header[17] = 0
                header[18] = 0
                header[19] = 0
                header[20] = 1
                header[21] = 0
                header[22] = channels.toByte()
                header[23] = 0
                header[24] = (sampleRate and 0xff).toByte()
                header[25] = ((sampleRate shr 8) and 0xff).toByte()
                header[26] = ((sampleRate shr 16) and 0xff).toByte()
                header[27] = ((sampleRate shr 24) and 0xff).toByte()
                header[28] = (byteRate and 0xff).toByte()
                header[29] = ((byteRate shr 8) and 0xff).toByte()
                header[30] = ((byteRate shr 16) and 0xff).toByte()
                header[31] = ((byteRate shr 24) and 0xff).toByte()
                header[32] = (channels * 2).toByte()
                header[33] = 0
                header[34] = 16
                header[35] = 0
                header[36] = 'd'.code.toByte()
                header[37] = 'a'.code.toByte()
                header[38] = 't'.code.toByte()
                header[39] = 'a'.code.toByte()
                header[40] = (totalAudioLen and 0xff).toByte()
                header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
                header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
                header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

                out.write(header)
                val buffer = ByteArray(4096)
                var bufIdx = 0
                for (sample in samples) {
                    buffer[bufIdx++] = (sample.toInt() and 0xFF).toByte()
                    buffer[bufIdx++] = ((sample.toInt() shr 8) and 0xFF).toByte()
                    if (bufIdx == buffer.size) {
                        out.write(buffer, 0, bufIdx)
                        bufIdx = 0
                    }
                }
                if (bufIdx > 0) {
                    out.write(buffer, 0, bufIdx)
                }
                out.flush()
            }
        }
    }
}
