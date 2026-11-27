package com.example.voicejournal.ui.media_player

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor() : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.None)
    val state = _state.asStateFlow()

    private val jukeboxPlayer = JukeboxPlayer()

    private var refreshJob: Job? = null

    private var uri: Uri = Uri.EMPTY

    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    sealed interface Effect {
        object AudioListened : Effect
        object FileNotFound : Effect
    }

    sealed interface State {
        object None : State
        object Preparing : State
        object FileNotFound : State

        data class Loaded(
            val playingState: PlayingState = PlayingState.Pause,
            val mediaPlayerPosition: Int = 0,
            val mediaPlayerDuration: Int = 0,
            val playbackSpeed: Float = 1f,
        ) : State {
            val mediaPlayerPositionText: String = mediaPlayerPosition.toLong().toTimeText()
            val mediaPlayerDurationText: String = mediaPlayerDuration.toLong().toTimeText()
        }
    }

    sealed interface PlayingState {
        object Pause : PlayingState
        object Playing : PlayingState
        object Cancel : PlayingState
    }

    var uris = mutableListOf<Uri>()

    fun initUri(uri: Uri) {
        if (this.uri != Uri.EMPTY && this.uri != uri) {
            pause()
        }
        this.uri = uri
        if (uri !in uris) {
            uris.add(uri)
        }

        try {
            jukeboxPlayer.setDataSource(uri)
        } catch (e: Exception) {
            viewModelScope.launch { _effect.send(Effect.FileNotFound) }
            _state.value = State.FileNotFound
            return
        }

        jukeboxPlayer.prepare(uri) { player ->
            val currentSpeed = (_state.value as? State.Loaded)?.playbackSpeed ?: 1f
            _state.value = State.Loaded(
                playingState = PlayingState.Pause,
                mediaPlayerPosition = 0,
                mediaPlayerDuration = player.duration.coerceAtLeast(0),
                playbackSpeed = currentSpeed
            )
        }

        Log.d("JukeboxViewModel", "uri : $uri")
    }

    private suspend fun prepareMediaPlayer(startOnPrepared: Boolean) {
        jukeboxPlayer.reset(uri)
        _state.value = State.Preparing
        try {
            jukeboxPlayer.setDataSource(uri)
        } catch (e: FileNotFoundException) {
            _effect.send(Effect.FileNotFound)
            _state.value = State.FileNotFound
            return
        } catch (e: IOException) {
            _effect.send(Effect.FileNotFound)
            _state.value = State.FileNotFound
            return
        }

        jukeboxPlayer.prepare(uri) { player ->
            _state.value = State.Loaded(mediaPlayerDuration = player.duration.coerceAtLeast(0))
            if (startOnPrepared) {
                play()
            }
        }
        jukeboxPlayer.setOnCompletionListener(uri) {
            onPlaybackCompleted(it.duration)
        }
    }

    private fun onPlaybackCompleted(duration: Int) {
        refreshJob?.cancel()
        val currentSpeed = (_state.value as? State.Loaded)?.playbackSpeed ?: 1f
        jukeboxPlayer.seekTo(0, uri)
        _state.value = State.Loaded(
            playingState = PlayingState.Pause,
            mediaPlayerPosition = 0,
            mediaPlayerDuration = duration.coerceAtLeast(0),
            playbackSpeed = currentSpeed
        )
        viewModelScope.launch {
            _effect.send(Effect.AudioListened)
        }
    }

    fun togglePlayPause() {
        val loaded = _state.value as? State.Loaded
        if (loaded?.playingState is PlayingState.Playing) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        fun startAndRefreshPosition() {
            val loaded = _state.value as? State.Loaded ?: return
            refreshJob?.cancel()
            jukeboxPlayer.setPlaybackSpeed(loaded.playbackSpeed, uri)
            jukeboxPlayer.setOnCompletionListener(uri) {
                onPlaybackCompleted(it.duration)
            }
            jukeboxPlayer.start(uri)
            _state.value = loaded.copy(playingState = PlayingState.Playing)
            refreshJob = viewModelScope.launch {
                while (true) {
                    val current = _state.value as? State.Loaded ?: break
                    if (current.playingState !is PlayingState.Playing) break
                    val pos = jukeboxPlayer.currentPosition(uri)
                    _state.value = current.copy(
                        mediaPlayerPosition = pos.coerceIn(0, current.mediaPlayerDuration.coerceAtLeast(0))
                    )
                    delay(120)
                }
            }
        }

        if (_state.value is State.None) {
            viewModelScope.launch {
                prepareMediaPlayer(true)
            }
        } else if (_state.value is State.Loaded) {
            startAndRefreshPosition()
        }
    }

    fun pause() {
        val loaded = _state.value as? State.Loaded ?: return
        refreshJob?.cancel()
        try {
            jukeboxPlayer.pause(uri)
        } catch (e: IllegalStateException) {
            Log.e("ListenAudioViewModel", "pause $e")
        }
        _state.value = loaded.copy(
            playingState = PlayingState.Pause,
            mediaPlayerPosition = jukeboxPlayer.currentPosition(uri).coerceIn(0, loaded.mediaPlayerDuration.coerceAtLeast(0))
        )
    }

    fun resetToStart() {
        val loaded = _state.value as? State.Loaded ?: return
        refreshJob?.cancel()
        jukeboxPlayer.pause(uri)
        jukeboxPlayer.seekTo(0, uri)
        _state.value = loaded.copy(
            playingState = PlayingState.Pause,
            mediaPlayerPosition = 0,
        )
    }

    fun seekBy(deltaMs: Int) {
        val loaded = _state.value as? State.Loaded ?: return
        val target = (loaded.mediaPlayerPosition + deltaMs)
            .coerceIn(0, loaded.mediaPlayerDuration.coerceAtLeast(0))
        onSeek(target.toFloat())
    }

    fun cyclePlaybackSpeed() {
        val loaded = _state.value as? State.Loaded ?: return
        val speeds = listOf(1f, 1.25f, 1.5f, 2f)
        val currentIdx = speeds.indexOfFirst { kotlin.math.abs(it - loaded.playbackSpeed) < 0.05f }
        val nextSpeed = speeds[(currentIdx + 1) % speeds.size]
        jukeboxPlayer.setPlaybackSpeed(nextSpeed, uri)
        _state.value = loaded.copy(playbackSpeed = nextSpeed)
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        releaseMediaPlayer()
    }

    fun releaseMediaPlayer() {
        jukeboxPlayer.stopAndRelease(uri)
    }

    fun onSeek(sliderPosition: Float) {
        val loaded = _state.value as? State.Loaded ?: return
        val clamped = sliderPosition.toInt().coerceIn(0, loaded.mediaPlayerDuration.coerceAtLeast(0))
        jukeboxPlayer.seekTo(clamped, uri)
        _state.value = loaded.copy(
            mediaPlayerPosition = clamped,
        )
    }

    sealed interface Commands {
        object Play : Commands
        object Pause : Commands
        object Cancel : Commands
    }

    fun stopAndRelease() {
        jukeboxPlayer.prepare(uri) { player ->
            _state.value = State.Loaded(
                mediaPlayerDuration = player.duration.coerceAtLeast(0),
                mediaPlayerPosition = 0,
                playingState = PlayingState.Pause
            )
        }
    }
}
