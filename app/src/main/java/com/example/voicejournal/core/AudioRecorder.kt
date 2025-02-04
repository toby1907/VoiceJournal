package com.example.voicejournal.core

interface AudioRecorder{
    fun start(outputFile: String)
    fun stop()
    fun pause()
    fun resume()
    fun release()
    fun prepare()
}