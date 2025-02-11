package com.example.voicejournal.ui.main.voiceJournalPreviewScreen.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.R
import com.example.voicejournal.convertIntToTimeFormat
import com.example.voicejournal.formatTime
import com.example.voicejournal.ui.media_player.MediaPlayerViewModel
import com.example.voicejournal.ui.media_player.toTimeText
import com.example.voicejournal.ui.theme.Variables
@Composable
fun PlayRecordPanel(
    onPlay: () -> Unit,
    onCancelRecord: () -> Unit,
    onPaused: () -> Unit,
    state: MediaPlayerViewModel.State.Loaded
) {
    Row(
        modifier = Modifier
            .width(200.dp)
            .height(45.dp)
            .background(color = Color.Transparent, shape = CircleShape),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val onPlayState = remember { mutableStateOf(false) }

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (state.playingState) {
                MediaPlayerViewModel.PlayingState.Pause -> {
                    IconButton(
                        onClick = {
                            onPlay()
                            onPlayState.value = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Variables.SchemesSurfaceTint,
                            contentColor = Variables.SchemesSurface
                        )
                    ) {
                        Icon(
                            tint = Variables.SchemesSurface,
                            painter = painterResource(id = R.drawable.play_button_24),
                            contentDescription = "Play"
                        )
                    }
                }

                MediaPlayerViewModel.PlayingState.Playing -> {
                    IconButton(
                        onClick = onPaused,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Variables.SchemesSurfaceTint,
                            contentColor = Variables.SchemesSurface
                        )
                    ) {
                        Icon(
                            tint = Variables.SchemesSurface,
                            painter = painterResource(id = R.drawable.pause_button_24),
                            contentDescription = "Pause"
                        )
                    }

                    IconButton(
                        onClick = {
                            onCancelRecord()
                            onPlayState.value = false
                            Log.d("PlayCanIconButton", "Clicked")
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Variables.SchemesSurfaceTint,
                            contentColor = Variables.SchemesOnPrimary
                        )
                    ) {
                        Icon(
                            tint = Variables.SchemesOnPrimary,
                            painter = painterResource(id = R.drawable.stop_button_36),
                            contentDescription = "Cancel"
                        )
                    }
                }

                MediaPlayerViewModel.PlayingState.Cancel -> {

                }
            }

            // Display the time text
            if (state.mediaPlayerPositionText != 0.toLong().toTimeText()) {
                Text(
                    text = state.mediaPlayerPositionText,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Text(
                    text = state.mediaPlayerDurationText,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}