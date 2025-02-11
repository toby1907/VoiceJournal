package com.example.voicejournal.ui.media_player.default_views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.voicejournal.ui.media_player.MediaPlayerViewModel

@Composable
fun DefaultAudioSliderView(
    state: MediaPlayerViewModel.State.Loaded,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(state.mediaPlayerPositionText)
        Spacer(Modifier.width(4.dp))
        Slider(
            value = state.mediaPlayerPosition.toFloat(),
            onValueChange = onSeek,
            valueRange = 0.toFloat()..state.mediaPlayerDuration.toFloat(),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(4.dp))
        Text(state.mediaPlayerDurationText)
    }
}
