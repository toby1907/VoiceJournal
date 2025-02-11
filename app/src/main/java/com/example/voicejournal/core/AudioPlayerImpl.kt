package com.example.voicejournal.core

import android.media.MediaPlayer
import java.io.IOException

class AudioPlayerImpl : AudioPlayer {
    private var player: MediaPlayer? = null

    override fun start(file: String) {
        player = MediaPlayer().apply {
            try {
                setDataSource(file)
                prepare()
                start()
            } catch (e: IOException) {
                // Handle prepare() or start() failure
                throw e
            }
        }
    }

    override fun stop() {
        player?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        player = null
    }

    override fun pause() {
        player?.pause()
    }

    override fun release() {
        player?.release()
        player = null
    }

    override fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    override fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) {
        player?.setOnCompletionListener(listener)
    }
    override fun getDuration(): Int {
        return player?.duration ?: 0
    }
    override fun prepare() {
        player?.prepare()
    }
}