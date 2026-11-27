package com.example.voicejournal.ui.main.AddVoiceNote.components

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
    val onContainer = Variables.SchemesOnPrimaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Variables.SchemesPrimaryContainer)
            .testTag("voice_media_panel")
    ) {
        // Subtle hairline divider separating TopAppBar header from extended media controls
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.75.dp,
            color = onContainer.copy(alpha = 0.14f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 10.dp)
        ) {
            // Compact Status & Time Row + Delete + Collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (isPlaying) {
                                Variables.SchemesPrimary
                            } else {
                                onContainer.copy(alpha = 0.12f)
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
                            onContainer
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = onContainer
                    )
                    Text(
                        text = "${formatDurationMs(displayedPositionMs)} / ${formatDurationMs(safeDuration)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = onContainer.copy(alpha = 0.75f),
                        modifier = Modifier.testTag("media_panel_time")
                    )
                }

                IconButton(
                    onClick = { openDeleteDialog.value = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("media_panel_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Recording",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("media_panel_collapse")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Hide Media Panel",
                        tint = onContainer.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Sleek Seek Slider integrated into TopBar surface
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
                    .height(28.dp)
                    .testTag("media_panel_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = Variables.SchemesPrimary,
                    activeTrackColor = Variables.SchemesPrimary,
                    inactiveTrackColor = onContainer.copy(alpha = 0.22f)
                )
            )

            // Transport Controls Row: Speed, Rewind 5s, Play/Pause, Forward 5s, Stop
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playback Speed Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(onContainer.copy(alpha = 0.12f))
                        .clickable(onClick = onCycleSpeed)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("media_panel_speed"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = speedLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onContainer
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onSeekBy(-5000) },
                        enabled = safeDuration > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("media_panel_rewind")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay5,
                            contentDescription = "Rewind 5 seconds",
                            tint = onContainer,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(40.dp)
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
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSeekBy(5000) },
                        enabled = safeDuration > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("media_panel_forward")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward5,
                            contentDescription = "Forward 5 seconds",
                            tint = onContainer,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onStop,
                    enabled = canStop,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("media_panel_stop")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Playback",
                        tint = if (canStop) {
                            onContainer
                        } else {
                            onContainer.copy(alpha = 0.32f)
                        },
                        modifier = Modifier.size(21.dp)
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