package com.example.voicejournal.ui.media_player

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voicejournal.ui.media_player.default_views.AudioLoading
import com.example.voicejournal.ui.media_player.default_views.DefaultAudioSliderView
import com.example.voicejournal.ui.media_player.default_views.FileNotFoundText

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach


@Composable
fun Jukebox(
    uri: Uri,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = false,
    onAudioListened: () -> Unit = { Log.d("AudioSlider", "onAudioListened") },
    onFileNotFound: () -> Unit = { Log.d("AudioSlider", "onFileNotFound") },
    commands: Flow<MediaPlayerViewModel.Commands>,
    loadingView: @Composable () -> Unit = { AudioLoading() },
    errorView: @Composable () -> Unit = { FileNotFoundText() },
    audioView: @Composable (MediaPlayerViewModel.State.Loaded, (Float) -> Unit) -> Unit =
        { state, onSeek -> DefaultAudioSliderView(state, onSeek) },
) {
    val viewModel: MediaPlayerViewModel = hiltViewModel()

    LaunchedEffect(uri) { viewModel.initUri(uri) }

    LaunchedEffect(uri) {
        viewModel.effect.onEach {
            when (it) {
                MediaPlayerViewModel.Effect.AudioListened -> onAudioListened()
                MediaPlayerViewModel.Effect.FileNotFound -> onFileNotFound()
            }
        }.collect()
    }

    LaunchedEffect(uri) {
        commands.onEach {
            when (it) {
                MediaPlayerViewModel.Commands.Pause -> viewModel.pause()
                MediaPlayerViewModel.Commands.Play -> viewModel.play()
                MediaPlayerViewModel.Commands.Cancel -> viewModel.resetToStart()
            }
        }.collect()
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    JukeboxContent(
        state = state,
        onSeek = viewModel::onSeek,
        modifier = modifier,
        loadingView = loadingView,
        errorView = errorView,
        audioView = audioView,
    )
}

@Composable
internal fun JukeboxContent(
    state: MediaPlayerViewModel.State,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    loadingView: @Composable () -> Unit,
    errorView: @Composable () -> Unit,
    audioView: @Composable (MediaPlayerViewModel.State.Loaded, (Float) -> Unit) -> Unit
) {
    Box(modifier) {
        when (state) {
            is MediaPlayerViewModel.State.Loaded -> audioView(state, onSeek)
            MediaPlayerViewModel.State.Preparing -> loadingView()
            MediaPlayerViewModel.State.FileNotFound -> errorView()
            MediaPlayerViewModel.State.None -> loadingView()
        }
    }
}
