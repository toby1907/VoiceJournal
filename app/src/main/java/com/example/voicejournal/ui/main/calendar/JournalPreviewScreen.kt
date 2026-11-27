package com.example.voicejournal.ui.main.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor
import com.example.voicejournal.ui.theme.Variables
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun JournalPreviewScreen(
    journalIds: List<Int>?,
    navController: NavController,
) {
    SetStatusBarContentColor(false)

    val viewModel: CalendarViewModel = hiltViewModel()
    val state = viewModel.state.value
    val allJournalsList = state.notes
    val targetIds = remember(journalIds) { journalIds.orEmpty().toSet() }
    val matchingJournals = remember(allJournalsList, targetIds) {
        allJournalsList
            .filter { journal -> journal.id != null && targetIds.contains(journal.id) }
            .sortedByDescending { it.created }
    }

    JournalPreviewScreenContent(
        journals = matchingJournals,
        isLoading = state.isLoading,
        onBack = { navController.popBackStack() },
        onOpenJournal = { journal ->
            navController.navigate(buildPreviewRoute(journal, allJournalsList))
        },
        onAddForDate = { date ->
            navController.navigate(buildNewNoteForDateRoute(date))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalPreviewScreenContent(
    journals: List<VoiceJournal>,
    isLoading: Boolean = false,
    zoneId: ZoneId = remember { ZoneId.systemDefault() },
    onBack: () -> Unit,
    onOpenJournal: (VoiceJournal) -> Unit,
    onAddForDate: ((LocalDate) -> Unit)? = null,
) {
    val distinctDates = remember(journals, zoneId) {
        journals.map { journal ->
            Instant.ofEpochMilli(journal.created).atZone(zoneId).toLocalDate()
        }.distinct()
    }
    val singleDate = distinctDates.singleOrNull()
    val headerTitle = remember(singleDate) {
        singleDate?.let { formatSelectedDateHeader(it) } ?: "Journal Entries"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Variables.SchemesSurface,
        topBar = {
            Surface(
                color = Variables.SchemesPrimary,
                shadowElevation = 2.dp
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Variables.SchemesPrimary,
                        titleContentColor = Variables.SchemesOnPrimary,
                        navigationIconContentColor = Variables.SchemesOnPrimary,
                        actionIconContentColor = Variables.SchemesOnPrimary,
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("journal_preview_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Variables.SchemesOnPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("journal_preview_title")
                            )
                            if (!isLoading) {
                                Text(
                                    text = formatEntryCountLabel(journals.size),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Variables.SchemesOnPrimary.copy(alpha = 0.85f)
                                    ),
                                    modifier = Modifier.testTag("journal_preview_subtitle")
                                )
                            }
                        }
                    },
                    actions = {
                        if (singleDate != null && onAddForDate != null) {
                            IconButton(
                                onClick = { onAddForDate(singleDate) },
                                modifier = Modifier.testTag("journal_preview_add_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add entry for date"
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Variables.SchemesSurface)
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("journal_preview_loading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Variables.SchemesPrimary)
                    }
                }

                journals.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("journal_preview_empty"),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Variables.SchemesSurface,
                            border = BorderStroke(1.dp, Variables.SchemesPrimaryContainer),
                            tonalElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Variables.SchemesPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Variables.SchemesPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "No entries found",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Variables.SchemesOnPrimaryContainer
                                    )
                                )
                                Text(
                                    text = "Entries for this date may have been moved or deleted.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Variables.SchemesOnSurfaceVariant
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = onBack,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Variables.SchemesPrimary,
                                        contentColor = Variables.SchemesOnPrimary
                                    )
                                ) {
                                    Text(
                                        text = "Back to Calendar",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("journal_preview_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = journals,
                            key = { journal -> journal.id ?: journal.created }
                        ) { journal ->
                            CalendarAgendaNoteCard(
                                voiceJournal = journal,
                                zoneId = zoneId,
                                onClick = { onOpenJournal(journal) },
                                modifier = Modifier.testTag("journal_preview_item_${journal.id ?: journal.created}")
                            )
                        }
                    }
                }
            }
        }
    }
}