package com.example.voicejournal.ui.main.mainScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.imageUrisFromBlocks
import com.example.voicejournal.Data.model.isNoteLocked
import com.example.voicejournal.Data.model.primaryAudioPath
import com.example.voicejournal.ui.main.calendar.buildPreviewRoute
import com.example.voicejournal.ui.main.calendar.formatEntryCountLabel
import com.example.voicejournal.ui.theme.Variables
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HomeQuickFilter(val label: String) {
    ALL("All"),
    VOICE("Voice"),
    PHOTOS("Photos"),
    FAVORITES("Favorites")
}

fun filterNotesByQuickFilter(
    notes: List<VoiceJournal>,
    filter: HomeQuickFilter
): List<VoiceJournal> {
    return when (filter) {
        HomeQuickFilter.ALL -> notes
        HomeQuickFilter.VOICE -> notes.filter { note ->
            !note.isNoteLocked() && note.primaryAudioPath(revealLockedContent = true).isNotBlank()
        }
        HomeQuickFilter.PHOTOS -> notes.filter { note ->
            !note.isNoteLocked() && note.imageUrisFromBlocks(revealLockedContent = true).any { it.isNotBlank() }
        }
        HomeQuickFilter.FAVORITES -> notes.filter { it.favourite == true }
    }
}

fun formatHomeDateGroupHeader(
    date: LocalDate,
    today: LocalDate = LocalDate.now(),
    locale: Locale = Locale.getDefault()
): String {
    val shortMonthDay = DateTimeFormatter.ofPattern("MMM d", locale)
    val fullFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy · EEE", locale)
    return when (date) {
        today -> "Today · ${date.format(shortMonthDay)}"
        today.minusDays(1) -> "Yesterday · ${date.format(shortMonthDay)}"
        else -> date.format(fullFormatter)
    }
}

@Composable
fun StickyHeader(
    text: String,
    count: Int? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Variables.SchemesPrimary)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Variables.SchemesOnPrimaryContainer,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (count != null) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Variables.SchemesPrimaryContainer.copy(alpha = 0.75f)
            ) {
                Text(
                    text = formatEntryCountLabel(count),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Variables.SchemesOnPrimaryContainer
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NoteList(
    journals: List<VoiceJournal>,
    navController: NavController,
    modifier: Modifier = Modifier,
    allNotes: List<VoiceJournal> = journals,
    selectedFolder: String? = null,
    onClearFolder: (() -> Unit)? = null,
    onCreateNote: (() -> Unit)? = null,
) {
    NoteListContent(
        journals = journals,
        modifier = modifier,
        selectedFolder = selectedFolder,
        onClearFolder = onClearFolder,
        onCreateNote = onCreateNote,
        onOpenJournal = { journal ->
            navController.navigate(buildPreviewRoute(journal, allNotes))
        }
    )
}

@Composable
fun NoteListContent(
    journals: List<VoiceJournal>,
    onOpenJournal: (VoiceJournal) -> Unit,
    modifier: Modifier = Modifier,
    selectedFolder: String? = null,
    onClearFolder: (() -> Unit)? = null,
    onCreateNote: (() -> Unit)? = null,
    today: LocalDate = remember { LocalDate.now() },
    zoneId: ZoneId = remember { ZoneId.systemDefault() },
) {
    var activeFilter by remember { mutableStateOf(HomeQuickFilter.ALL) }

    val filteredJournals = remember(journals, activeFilter) {
        filterNotesByQuickFilter(journals, activeFilter)
    }

    val groupedVoiceJournals = remember(filteredJournals, zoneId) {
        filteredJournals
            .sortedByDescending { it.created }
            .groupBy { note ->
                Instant.ofEpochMilli(note.created).atZone(zoneId).toLocalDate()
            }
    }

    val voiceCount = remember(journals) {
        filterNotesByQuickFilter(journals, HomeQuickFilter.VOICE).size
    }
    val photoCount = remember(journals) {
        filterNotesByQuickFilter(journals, HomeQuickFilter.PHOTOS).size
    }
    val favoriteCount = remember(journals) {
        filterNotesByQuickFilter(journals, HomeQuickFilter.FAVORITES).size
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .testTag("home_note_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick filter & active folder strip
        item(key = "home_filter_bar") {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeFilterChip(
                        label = "All",
                        count = journals.size,
                        selected = activeFilter == HomeQuickFilter.ALL,
                        onClick = { activeFilter = HomeQuickFilter.ALL },
                        modifier = Modifier.testTag("home_filter_all")
                    )
                    HomeFilterChip(
                        label = "Voice",
                        count = voiceCount,
                        icon = Icons.Default.GraphicEq,
                        selected = activeFilter == HomeQuickFilter.VOICE,
                        onClick = { activeFilter = HomeQuickFilter.VOICE },
                        modifier = Modifier.testTag("home_filter_voice")
                    )
                    HomeFilterChip(
                        label = "Photos",
                        count = photoCount,
                        icon = Icons.Default.Image,
                        selected = activeFilter == HomeQuickFilter.PHOTOS,
                        onClick = { activeFilter = HomeQuickFilter.PHOTOS },
                        modifier = Modifier.testTag("home_filter_photos")
                    )
                    HomeFilterChip(
                        label = "Favorites",
                        count = favoriteCount,
                        icon = Icons.Default.Favorite,
                        selected = activeFilter == HomeQuickFilter.FAVORITES,
                        onClick = { activeFilter = HomeQuickFilter.FAVORITES },
                        modifier = Modifier.testTag("home_filter_favorites")
                    )
                }

                if (!selectedFolder.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Variables.SchemesPrimary,
                            modifier = Modifier.testTag("home_active_folder_chip")
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable(enabled = onClearFolder != null) { onClearFolder?.invoke() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Variables.SchemesOnPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Folder: $selectedFolder",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Variables.SchemesOnPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                if (onClearFolder != null) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear folder filter",
                                        tint = Variables.SchemesOnPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (groupedVoiceJournals.isEmpty()) {
            item(key = "home_empty_state") {
                val hasActiveConstraint = activeFilter != HomeQuickFilter.ALL || !selectedFolder.isNullOrBlank()
                HomeEmptyStateCard(
                    hasActiveFilter = hasActiveConstraint,
                    filterLabel = when {
                        activeFilter != HomeQuickFilter.ALL -> activeFilter.label.lowercase(Locale.getDefault())
                        !selectedFolder.isNullOrBlank() -> "folder \"$selectedFolder\""
                        else -> ""
                    },
                    onClearFilters = {
                        activeFilter = HomeQuickFilter.ALL
                        onClearFolder?.invoke()
                    },
                    onCreateNote = onCreateNote
                )
            }
        } else {
            groupedVoiceJournals.forEach { (date, groupJournals) ->
                item(key = "header_$date") {
                    StickyHeader(
                        text = formatHomeDateGroupHeader(date = date, today = today),
                        count = groupJournals.size
                    )
                }

                item(key = "group_$date") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = Variables.SchemesSurface.copy(alpha = 0.96f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, Variables.SchemesPrimaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            groupJournals.forEachIndexed { index, journal ->
                                NewJournalItem(
                                    voiceJournal = journal,
                                    modifier = Modifier
                                        .testTag("home_note_item_${journal.id ?: journal.created}")
                                        .clickable { onOpenJournal(journal) }
                                )
                                if (index < groupJournals.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.75.dp,
                                        color = Variables.SchemesPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Variables.SchemesPrimary else Variables.SchemesSurface.copy(alpha = 0.95f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Variables.SchemesPrimary else Variables.SchemesPrimaryContainer
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Variables.SchemesOnPrimary else Variables.SchemesPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Variables.SchemesOnPrimary else Variables.SchemesOnPrimaryContainer
                )
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selected) {
                            Variables.SchemesOnPrimary.copy(alpha = 0.85f)
                        } else {
                            Variables.SchemesPrimary
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun HomeEmptyStateCard(
    hasActiveFilter: Boolean,
    filterLabel: String,
    onClearFilters: () -> Unit,
    onCreateNote: (() -> Unit)?,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .testTag("home_empty_state_card"),
        shape = RoundedCornerShape(24.dp),
        color = Variables.SchemesSurface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, Variables.SchemesPrimaryContainer),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Variables.SchemesPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Variables.SchemesPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = if (hasActiveFilter) "No matching entries" else "Your journal awaits",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Variables.SchemesOnPrimaryContainer
                )
            )
            Text(
                text = if (hasActiveFilter) {
                    "There are no entries under $filterLabel right now."
                } else {
                    "Capture your thoughts, voice memos, and photos in one calm place."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Variables.SchemesOnSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (hasActiveFilter) {
                Button(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Variables.SchemesPrimary,
                        contentColor = Variables.SchemesOnPrimary
                    )
                ) {
                    Text(
                        text = "Show all entries",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (onCreateNote != null) {
                Button(
                    onClick = onCreateNote,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Variables.SchemesPrimary,
                        contentColor = Variables.SchemesOnPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create first entry",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
