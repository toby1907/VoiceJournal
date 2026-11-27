package com.example.voicejournal.ui.media_player

import android.media.MediaPlayer
import android.net.Uri
import android.os.Build

class JukeboxPlayer {

    /**
     * We keep track of datasourceSet and playerPrepared to have a better management of orientation change
     */
    data class MediaPlayerWithDataSet(
        val mediaPlayer: MediaPlayer,
        val datasourceSet: Boolean,
        val playerPrepared: Boolean,
    )

    private val players: MutableMap<Uri, MediaPlayerWithDataSet> = mutableMapOf()

    fun reset(uri: Uri) {
        try {
            players[uri]?.mediaPlayer?.reset()
            players[uri]?.let {
                players[uri] = it.copy(datasourceSet = false, playerPrepared = false)
            }
        } catch (_: IllegalStateException) {
            // Ignore reset on invalid state
        }
    }

    fun setDataSource(uri: Uri) {
        if (players[uri] == null) {
            players[uri] = MediaPlayerWithDataSet(MediaPlayer(), datasourceSet = false, playerPrepared = false)
        }

        if (!players[uri]!!.datasourceSet) {
            players[uri]?.mediaPlayer?.setDataSource(uri.toString())
            players[uri] = players[uri]!!.copy(datasourceSet = true)
        }
    }

    fun prepare(uri: Uri, onPrepared: (MediaPlayer) -> Unit) {
        val player = players[uri] ?: return
        player.mediaPlayer.setOnPreparedListener(onPrepared)
        if (!player.playerPrepared) {
            player.mediaPlayer.prepareAsync()
            players[uri] = player.copy(playerPrepared = true)
        } else {
            onPrepared(player.mediaPlayer)
        }
    }

    fun setOnCompletionListener(uri: Uri, callback: (MediaPlayer) -> Unit) {
        players[uri]?.mediaPlayer?.setOnCompletionListener {
            callback(it)
        }
    }

    fun start(uri: Uri) {
        try {
            players[uri]?.mediaPlayer?.start()
        } catch (_: IllegalStateException) {
            // Ignore if not prepared
        }
    }

    fun currentPosition(uri: Uri): Int {
        return try {
            players[uri]?.mediaPlayer?.currentPosition ?: 0
        } catch (_: IllegalStateException) {
            0
        }
    }

    fun pause(uri: Uri) {
        try {
            players[uri]?.mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                }
            }
        } catch (_: IllegalStateException) {
            // Ignore if not playing
        }
    }

    fun seekTo(positionMs: Int, uri: Uri) {
        try {
            players[uri]?.mediaPlayer?.let { mp ->
                val clamped = positionMs.coerceIn(0, mp.duration.coerceAtLeast(0))
                mp.seekTo(clamped)
            }
        } catch (_: IllegalStateException) {
            // Ignore if not prepared
        }
    }

    fun setPlaybackSpeed(speed: Float, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mp = players[uri]?.mediaPlayer ?: return
            try {
                val wasPlaying = mp.isPlaying
                val params = mp.playbackParams
                params.speed = speed.coerceIn(0.5f, 2.5f)
                mp.playbackParams = params
                if (!wasPlaying && mp.isPlaying) {
                    mp.pause()
                }
            } catch (_: Exception) {
                // Some devices/codecs do not support dynamic speed changes
            }
        }
    }

    fun stopAndRelease(uri: Uri) {
        players.remove(uri)?.mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
            } catch (_: IllegalStateException) {
                // Ignore
            }
            mp.release()
        }
    }
}
