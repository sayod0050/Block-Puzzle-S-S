package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    var masterVolume = 0.8f
    var sfxVolume = 0.9f
    var musicVolume = 0.6f
    var isMuted = false
    var isMusicEnabled = true
    var isSfxEnabled = true

    private var musicJob: Job? = null
    private var isPlayingMusic = false

    private fun playTone(
        frequencies: List<Float>,
        durationMs: Int,
        volumeMultiplier: Float = 1.0f,
        envelopeType: String = "decay", // "decay", "pluck", "sparkle", "chirp"
        isMusic: Boolean = false
    ) {
        if (isMuted) return
        if (isMusic && !isMusicEnabled) return
        if (!isMusic && !isSfxEnabled) return
        val channelVol = if (isMusic) musicVolume else sfxVolume
        val finalVol = (masterVolume * channelVol * volumeMultiplier).coerceIn(0f, 1f)
        if (finalVol <= 0.01f) return

        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples.toFloat()
                    val envelope = when (envelopeType) {
                        "chirp" -> {
                            val attack = (progress / 0.15f).coerceAtMost(1f)
                            val release = ((1f - progress) / 0.85f).coerceAtLeast(0f)
                            attack * release
                        }
                        "pluck" -> {
                            (1f - progress) * (1f - progress)
                        }
                        "sparkle" -> {
                            sin(progress * PI.toFloat()).coerceAtLeast(0f)
                        }
                        else -> { // decay
                            val attack = (progress / 0.05f).coerceAtMost(1f)
                            attack * (1f - progress)
                        }
                    }

                    var sampleSum = 0f
                    for ((idx, baseFreq) in frequencies.withIndex()) {
                        val freq = if (envelopeType == "chirp") {
                            baseFreq * (1f + progress * 0.4f)
                        } else {
                            baseFreq
                        }
                        val wave = sin(2.0 * PI * freq * i / sampleRate).toFloat()
                        val harmonic = 0.25f * sin(4.0 * PI * freq * i / sampleRate).toFloat()
                        sampleSum += (wave + harmonic) * (1f / (idx + 1))
                    }

                    val sampleValue = (sampleSum * envelope * finalVol * Short.MAX_VALUE).toInt()
                    buffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }

    fun playButtonClick() {
        playTone(listOf(920f), 35, volumeMultiplier = 0.5f, envelopeType = "pluck")
    }

    fun playBlockPickup() {
        playTone(listOf(440f), 70, volumeMultiplier = 0.6f, envelopeType = "chirp")
    }

    fun playBlockPlace() {
        playTone(listOf(180f, 90f), 90, volumeMultiplier = 0.8f, envelopeType = "decay")
    }

    fun playLineClear(lines: Int) {
        val notes = when (lines) {
            1 -> listOf(523.25f, 659.25f) // C5, E5
            2 -> listOf(523.25f, 659.25f, 783.99f) // C5, E5, G5
            3 -> listOf(523.25f, 659.25f, 783.99f, 1046.50f) // C5, E5, G5, C6
            else -> listOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f) // C5, E5, G5, C6, E6
        }
        playTone(notes, 280, volumeMultiplier = 1.0f, envelopeType = "sparkle")
    }

    fun playCombo(combo: Int) {
        val baseMultiplier = 1f + (combo * 0.12f).coerceAtMost(0.8f)
        val notes = listOf(587.33f * baseMultiplier, 739.99f * baseMultiplier, 880.00f * baseMultiplier)
        playTone(notes, 340, volumeMultiplier = 1.0f, envelopeType = "sparkle")
    }

    fun playGameOver() {
        scope.launch {
            playTone(listOf(392.00f), 150, 0.7f, "decay") // G4
            delay(130)
            playTone(listOf(349.23f), 150, 0.7f, "decay") // F4
            delay(130)
            playTone(listOf(293.66f), 300, 0.7f, "decay") // D4
        }
    }

    fun playVictory() {
        scope.launch {
            playTone(listOf(523.25f), 120, 0.8f, "sparkle") // C5
            delay(110)
            playTone(listOf(659.25f), 120, 0.8f, "sparkle") // E5
            delay(110)
            playTone(listOf(783.99f), 120, 0.8f, "sparkle") // G5
            delay(110)
            playTone(listOf(1046.50f, 1318.51f), 450, 1.0f, "sparkle") // C6 + E6
        }
    }

    fun playBombTick() {
        playTone(listOf(1200f), 40, volumeMultiplier = 0.5f, envelopeType = "pluck")
    }

    fun playBombDefused() {
        playTone(listOf(600f, 900f, 1200f), 220, volumeMultiplier = 0.9f, envelopeType = "sparkle")
    }

    fun playRewardUnlocked() {
        playTone(listOf(587.33f, 880.00f, 1174.66f), 350, volumeMultiplier = 0.9f, envelopeType = "sparkle")
    }

    // Ambient loop music generator
    fun startBackgroundMusic(isZenMode: Boolean = false) {
        if (isPlayingMusic) return
        isPlayingMusic = true
        musicJob = scope.launch {
            val pentatonicScale = listOf(261.63f, 293.66f, 329.63f, 392.00f, 440.00f, 523.25f)
            var noteIndex = 0

            while (isActive && isPlayingMusic) {
                if (!isMuted && isMusicEnabled && masterVolume > 0.05f && musicVolume > 0.05f) {
                    val root = pentatonicScale[noteIndex % pentatonicScale.size]
                    val harmony = pentatonicScale[(noteIndex + 2) % pentatonicScale.size]
                    playTone(
                        frequencies = listOf(root * 0.5f, harmony * 0.5f),
                        durationMs = if (isZenMode) 1200 else 700,
                        volumeMultiplier = 0.35f,
                        envelopeType = "decay",
                        isMusic = true
                    )
                    noteIndex = (noteIndex + 1 + (if (isZenMode) 1 else 2)) % pentatonicScale.size
                }
                delay(if (isZenMode) 1800 else 1100)
            }
        }
    }

    fun stopBackgroundMusic() {
        isPlayingMusic = false
        musicJob?.cancel()
        musicJob = null
    }
}
