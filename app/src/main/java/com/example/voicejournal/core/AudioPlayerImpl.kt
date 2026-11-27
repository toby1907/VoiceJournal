package com.example.voicejournal.core

import android.media.MediaPlayer
import android.os.Build
import java.io.IOException

class AudioPlayerImpl : AudioPlayer {
    private var player: MediaPlayer? = null
    private var completionListener: MediaPlayer.OnCompletionListener? = null
    private var currentSpeed: Float = 1f

    override fun start(file: String) {
        stop()
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(file)
            mediaPlayer.prepare()
            completionListener?.let { mediaPlayer.setOnCompletionListener(it) }
            applySpeedIfSupported(mediaPlayer, currentSpeed)
            mediaPlayer.start()
            player = mediaPlayer
        } catch (e: IOException) {
            mediaPlayer.release()
            throw e
        } catch (e: IllegalStateException) {
            mediaPlayer.release()
            throw e
        }
    }

    override fun stop() {
        player?.apply {
            try {
                if (isPlaying) {
                    stop()
                }
            } catch (_: IllegalStateException) {
                // Ignore if already stopped
            }
            release()
        }
        player = null
    }

    override fun pause() {
        player?.apply {
            try {
                if (isPlaying) {
                    pause()
                }
            } catch (_: IllegalStateException) {
                // Ignore if not playing
            }
        }
    }

    override fun resume() {
        player?.apply {
            try {
                if (!isPlaying) {
                    applySpeedIfSupported(this, currentSpeed)
                    start()
                }
            } catch (_: IllegalStateException) {
                // Ignore if not prepared
            }
        }
    }

    override fun seekTo(positionMs: Int) {
        player?.apply {
            try {
                val safePos = positionMs.coerceIn(0, duration.coerceAtLeast(0))
                seekTo(safePos)
            } catch (_: IllegalStateException) {
                // Ignore if not prepared
            }
        }
    }

    override fun getCurrentPosition(): Int {
        return try {
            player?.currentPosition ?: 0
        } catch (_: IllegalStateException) {
            0
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.5f)
        player?.let { mediaPlayer ->
            applySpeedIfSupported(mediaPlayer, currentSpeed)
        }
    }

    private fun applySpeedIfSupported(mediaPlayer: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val wasPlaying = mediaPlayer.isPlaying
                val params = mediaPlayer.playbackParams
                params.speed = speed
                mediaPlayer.playbackParams = params
                if (!wasPlaying && mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            } catch (_: Exception) {
                // Some codecs/emulators do not support dynamic playback speed
            }
        }
    }

    override fun release() {
        player?.release()
        player = null
    }

    override fun isPlaying(): Boolean {
        return try {
            player?.isPlaying ?: false
        } catch (_: IllegalStateException) {
            false
        }
    }

    override fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) {
        completionListener = listener
        player?.setOnCompletionListener(listener)
    }

    override fun getDuration(): Int {
        return try {
            player?.duration ?: 0
        } catch (_: IllegalStateException) {
            0
        }
    }

    override fun prepare() {
        player?.prepare()
    }
}