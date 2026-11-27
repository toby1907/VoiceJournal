package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.ui.theme.Variables
import kotlin.math.roundToInt

@Composable
fun PlayRecordPanel(
    currentPositionMs: Int,
    durationMs: Int,
    isPlaying: Boolean,
    isPaused: Boolean,
    playbackSpeed: Float = 1f,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onSeekBy: (Int) -> Unit,
    onCycleSpeed: () -> Unit,
    onRemove: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val openDeleteDialog = remember { mutableStateOf(false) }

    val safeDuration = durationMs.coerceAtLeast(0)
    val safePosition = if (safeDuration > 0) {
        currentPositionMs.coerceIn(0, safeDuration)
    } else {
        currentPositionMs.coerceAtLeast(0)
    }

    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderPositionMs by remember { mutableFloatStateOf(safePosition.toFloat()) }

    LaunchedEffect(safePosition, isUserSeeking) {
        if (!isUserSeeking) {
            sliderPositionMs = safePosition.toFloat()
        }
    }

    val displayedPositionMs = if (isUserSeeking) sliderPositionMs.roundToInt() else safePosition
    val statusText = when {
        isPlaying -> "Playing Voice Note"
        isPaused -> "Paused"
        else -> "Voice Recording"
    }
    val speedLabel = formatSpeedLabel(playbackSpeed)
    val canStop = isPlaying || isPaused || safePosition > 0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("voice_media_panel"),
        shape = RoundedCornerShape(18.dp),
        color = Variables.SchemesSurface,
        shadowElevation = 8.dp,
        tonalElevation = 3.dp,
        border = BorderStroke(1.dp, Variables.SchemesPrimary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Header Row: Waveform badge + Status/Time + Delete + Collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isPlaying) {
                                Variables.SchemesPrimary
                            } else {
                                Variables.SchemesPrimaryContainer
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isPlaying) {
                            Variables.SchemesOnPrimary
                        } else {
                            Variables.SchemesOnPrimaryContainer
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Variables.SchemesOnSurface
                    )
                    Text(
                        text = "${formatDurationMs(displayedPositionMs)} / ${formatDurationMs(safeDuration)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Variables.SchemesOnSurface.copy(alpha = 0.72f),
                        modifier = Modifier.testTag("media_panel_time")
                    )
                }

                IconButton(
                    onClick = { openDeleteDialog.value = true },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("media_panel_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Recording",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("media_panel_collapse")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Hide Media Panel",
                        tint = Variables.SchemesOnSurface.copy(alpha = 0.78f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Seek Slider
            Slider(
                value = sliderPositionMs.coerceIn(0f, safeDuration.coerceAtLeast(1).toFloat()),
                onValueChange = { newPos ->
                    isUserSeeking = true
                    sliderPositionMs = newPos
                },
                onValueChangeFinished = {
                    isUserSeeking = false
                    onSeekTo(sliderPositionMs.roundToInt())
                },
                valueRange = 0f..safeDuration.coerceAtLeast(1).toFloat(),
                enabled = safeDuration > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .testTag("media_panel_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = Variables.SchemesPrimary,
                    activeTrackColor = Variables.SchemesPrimary,
                    inactiveTrackColor = Variables.SchemesPrimaryContainer
                )
            )

            // Transport Controls Row: Speed, Rewind 5s, Play/Pause, Forward 5s, Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playback Speed Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Variables.SchemesPrimaryContainer.copy(alpha = 0.7f))
                        .clickable(onClick = onCycleSpeed)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("media_panel_speed"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = speedLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Variables.SchemesOnPrimaryContainer
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onSeekBy(-5000) },
                        enabled = safeDuration > 0,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("media_panel_rewind")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay5,
                            contentDescription = "Rewind 5 seconds",
                            tint = Variables.SchemesOnSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("media_panel_play_pause"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Variables.SchemesPrimary,
                            contentColor = Variables.SchemesOnPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Recording" else "Play Recording",
                            tint = Variables.SchemesOnPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSeekBy(5000) },
                        enabled = safeDuration > 0,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("media_panel_forward")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward5,
                            contentDescription = "Forward 5 seconds",
                            tint = Variables.SchemesOnSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onStop,
                    enabled = canStop,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("media_panel_stop")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Playback",
                        tint = if (canStop) {
                            Variables.SchemesOnSurface
                        } else {
                            Variables.SchemesOnSurface.copy(alpha = 0.32f)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    VoiceRecordAlertDialog(
        openDialog = openDeleteDialog,
        onDismissRequest = { openDeleteDialog.value = false },
        confirmRequest = {
            openDeleteDialog.value = false
            onRemove()
        },
        dismissRequest = {
            openDeleteDialog.value = false
        }
    )
}

internal fun formatDurationMs(durationMs: Int): String {
    val totalSeconds = (durationMs.coerceAtLeast(0)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatSpeedLabel(speed: Float): String {
    return when {
        kotlin.math.abs(speed - 1f) < 0.05f -> "1x"
        kotlin.math.abs(speed - 1.25f) < 0.05f -> "1.25x"
        kotlin.math.abs(speed - 1.5f) < 0.05f -> "1.5x"
        kotlin.math.abs(speed - 2f) < 0.05f -> "2x"
        else -> "${speed}x"
    }
}