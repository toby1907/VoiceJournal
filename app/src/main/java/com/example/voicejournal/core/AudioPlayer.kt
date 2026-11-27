package com.example.voicejournal.core

import android.media.MediaPlayer

interface AudioPlayer {
    fun start(file: String)
    fun stop()
    fun pause()
    fun resume() {}
    fun seekTo(positionMs: Int) {}
    fun getCurrentPosition(): Int = 0
    fun setPlaybackSpeed(speed: Float) {}
    fun release()
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener)
    fun getDuration(): Int
    fun prepare()
}