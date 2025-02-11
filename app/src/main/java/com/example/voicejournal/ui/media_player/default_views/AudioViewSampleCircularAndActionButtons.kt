package com.example.voicejournal.ui.media_player.default_views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.voicejournal.ui.media_player.MediaPlayerViewModel

@Composable
fun AudioViewSampleCircularAndActionButtons(state: MediaPlayerViewModel.State.Loaded, onSeek: (Float) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val progress =
            (state.mediaPlayerPosition.toFloat() / state.mediaPlayerDuration.toFloat())

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onSeek((state.mediaPlayerPosition - 10000).toFloat()) }) {
                Text("-10")
            }
            androidx.compose.material.CircularProgressIndicator(
                progress = progress,
                color = Color.Magenta
            )
            TextButton(onClick = { onSeek((state.mediaPlayerPosition + 30000).toFloat()) }) {
                Text("+30")
            }
        }
    }
}