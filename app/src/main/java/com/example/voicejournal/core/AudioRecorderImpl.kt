package com.example.voicejournal.core

import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.IOException

class AudioRecorderImpl : AudioRecorder {
    private var recorder: MediaRecorder? = null

    override fun start(outputFile: String) {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                // Handle prepare() failure
                throw e
            }
            start()
        }
    }

    override fun stop() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun pause() {
        recorder?.pause()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun resume() {
        recorder?.resume()
    }

    override fun release() {
        recorder?.release()
        recorder = null
    }
    override fun prepare() {
        recorder?.prepare()
    }
}