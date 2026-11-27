package com.example.voicejournal.ui.main.mainScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.imageUrisFromBlocks
import com.example.voicejournal.Data.model.isNoteLocked
import com.example.voicejournal.Data.model.primaryAudioPath
import com.example.voicejournal.R
import com.example.voicejournal.ui.main.calendar.extractCalendarCardText
import com.example.voicejournal.ui.theme.Variables
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AudioFileDurationBadge(
    audioPath: String,
    modifier: Modifier = Modifier,
    durationMsOverride: Long? = null,
) {
    val resolvedDurationMs = durationMsOverride ?: rememberAudioDurationMs(audioPath)
    val durationLabel = remember(resolvedDurationMs) {
        formatAudioDurationLabel(resolvedDurationMs)
    }

    Surface(
        modifier = modifier.testTag("note_audio_duration_badge"),
        shape = RoundedCornerShape(999.dp),
        color = Variables.SchemesPrimaryContainer,
        border = BorderStroke(0.75.dp, Variables.SchemesPrimary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.audio_file),
                contentDescription = "Audio file",
                tint = Variables.SchemesPrimary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = durationLabel.ifBlank { "Audio" },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Variables.SchemesOnPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun NewJournalItem(
    modifier: Modifier = Modifier,
    voiceJournal: VoiceJournal,
    audioDurationMsOverride: Long? = null,
) {
    val isLocked = voiceJournal.isNoteLocked()
    val (headline, snippet) = remember(voiceJournal) {
        extractCalendarCardText(voiceJournal)
    }
    val audioPath = remember(voiceJournal, isLocked) {
        voiceJournal.primaryAudioPath(revealLockedContent = !isLocked)
    }
    val imageUris = remember(voiceJournal, isLocked) {
        voiceJournal.imageUrisFromBlocks(revealLockedContent = !isLocked).filter { it.isNotBlank() }
    }
    val journalTime = remember(voiceJournal.created) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(voiceJournal.created))
    }
    val visibleTags = remember(voiceJournal.tags, isLocked) {
        if (isLocked) emptyList()
        else voiceJournal.tags.orEmpty()
            .filter { it.isChecked && it.name.isNotBlank() }
            .take(2)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            // Top metadata row: timestamp, folder pill, audio duration pill, favorite & lock icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = journalTime,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Variables.SchemesPrimary
                        ),
                        maxLines = 1
                    )
                    voiceJournal.folderName?.takeIf { it.isNotBlank() }?.let { folder ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Variables.SchemesPrimaryContainer.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Variables.SchemesOnPrimaryContainer,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = folder,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Variables.SchemesOnPrimaryContainer
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (audioPath.isNotBlank()) {
                        AudioFileDurationBadge(
                            audioPath = audioPath,
                            durationMsOverride = audioDurationMsOverride
                        )
                    }
                    if (voiceJournal.favourite == true) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Variables.SchemesError,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked note",
                            tint = Variables.SchemesOnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Headline
            Text(
                text = headline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Variables.SchemesOnSurface
                )
            )

            // Optional secondary snippet
            if (snippet.isNotBlank()) {
                Text(
                    text = snippet,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Variables.SchemesOnSurfaceVariant
                    )
                )
            }

            // Optional tag chips
            if (visibleTags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    visibleTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Variables.SchemesSurface,
                            border = BorderStroke(1.dp, Variables.SchemesPrimaryContainer)
                        ) {
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = Variables.SchemesOnSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Right-side visual preview: photo thumbnail or balanced monogram/media circle
        if (imageUris.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Variables.SchemesPrimaryContainer)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUris.last()),
                    contentDescription = "Note image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (imageUris.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.62f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "+${imageUris.size - 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isLocked) Variables.SchemesPrimaryContainer else Variables.SchemesPrimaryContainer
                    )
            ) {
                when {
                    isLocked -> Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Variables.SchemesPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    audioPath.isNotBlank() -> Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Variables.SchemesPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    else -> Text(
                        text = headline.firstOrNull()?.uppercaseChar()?.toString() ?: "J",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Variables.SchemesPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicDateRow(created: Long) {

    val formattedDate = formatDate(created)
    val formattedTime = formatTime(created)
    val dayOfMonth = getDayOfMonth(created)

    Row(
       // modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = dayOfMonth.toString(),
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(400),
                color = Variables.SchemesPrimary
            )
            ) // Day of the month
        Spacer(modifier = Modifier.padding(8.dp))
        Column {
            Text(text = formattedDate,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Variables.SchemesOnSurface
                )
            )
                        // Date ("March 16, 2024")
            Text(text = formattedTime,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Variables.SchemesOnSurface,

                )
                ) // Time
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val timeFormat = SimpleDateFormat("EEEE h:mm a", Locale.getDefault())
    return timeFormat.format(Date(timestamp))
}

private fun getDayOfMonth(timestamp: Long): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return calendar.get(Calendar.DAY_OF_MONTH)
}