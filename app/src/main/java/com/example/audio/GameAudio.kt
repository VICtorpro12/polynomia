package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class GameAudio(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isMuted = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }

    fun isMuted(): Boolean = isMuted

    fun vibrate(durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun playTap() {
        if (isMuted) return
        scope.launch {
            playTone(880.0, 35)
        }
    }

    fun playSuccess() {
        vibrate(60)
        if (isMuted) return
        scope.launch {
            playChord(listOf(523.25, 659.25, 783.99, 1046.50), 90) // C5, E5, G5, C6 arpeggio
        }
    }

    fun playError() {
        vibrate(100)
        if (isMuted) return
        scope.launch {
            playTone(220.0, 120, waveType = 1) // square buzz
        }
    }

    fun playHit() {
        vibrate(80)
        if (isMuted) return
        scope.launch {
            playSweep(700.0, 150.0, 140)
        }
    }

    fun playVictory() {
        vibrate(120)
        if (isMuted) return
        scope.launch {
            // Fanfare notes
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            for (note in notes) {
                playTone(note, 80)
            }
        }
    }

    fun playTransform() {
        vibrate(50)
        if (isMuted) return
        scope.launch {
            playSweep(300.0, 950.0, 160)
        }
    }

    private fun playTone(freq: Double, durationMs: Int, waveType: Int = 0) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            val twoPi = 2.0 * Math.PI

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val raw = if (waveType == 1) {
                    // Square wave with decay
                    if (sin(twoPi * freq * t) > 0) 1.0 else -1.0
                } else {
                    sin(twoPi * freq * t)
                }
                // Apply subtle envelope (fade in and out to prevent click)
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                    else -> 1.0
                }
                buffer[i] = (raw * envelope * 24000.0).toInt().coerceIn(-32768, 32767).toShort()
            }

            val track = AudioTrack.Builder()
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

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (_: Exception) {}
    }

    private fun playSweep(startFreq: Double, endFreq: Double, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            val twoPi = 2.0 * Math.PI
            var phase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                phase += twoPi * currentFreq / sampleRate
                val envelope = 1.0 - progress * 0.7
                val raw = sin(phase)
                buffer[i] = (raw * envelope * 24000.0).toInt().coerceIn(-32768, 32767).toShort()
            }

            val track = AudioTrack.Builder()
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

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20)
            track.release()
        } catch (_: Exception) {}
    }

    private fun playChord(freqs: List<Double>, noteDurationMs: Int) {
        for (freq in freqs) {
            playTone(freq, noteDurationMs)
        }
    }
}
