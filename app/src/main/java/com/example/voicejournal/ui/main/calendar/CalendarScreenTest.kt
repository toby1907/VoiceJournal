package com.example.voicejournal.ui.main.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.imageUrisFromBlocks
import com.example.voicejournal.Data.model.isNoteLocked
import com.example.voicejournal.Data.model.primaryAudioPath
import com.example.voicejournal.ui.theme.Variables
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.ContentHeightMode
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun Example8Page(
    horizontal: Boolean = true,
    journals: List<VoiceJournal>,
    navController: NavController
) {
    CalendarScreenContent(
        horizontal = horizontal,
        journals = journals,
        onOpenJournal = { journal ->
            navController.navigate(buildPreviewRoute(journal, journals))
        },
        onOpenDayPreview = { dayJournals ->
            buildJournalPreviewRoute(dayJournals)?.let { route ->
                navController.navigate(route)
            }
        },
        onCreateJournalForDate = { selectedDate ->
            navController.navigate(buildNewNoteForDateRoute(selectedDate))
        }
    )
}

@Composable
fun CalendarScreenContent(
    horizontal: Boolean = true,
    journals: List<VoiceJournal>,
    today: LocalDate = remember { LocalDate.now() },
    zoneId: ZoneId = remember { ZoneId.systemDefault() },
    onOpenJournal: (VoiceJournal) -> Unit,
    onOpenDayPreview: (List<VoiceJournal>) -> Unit,
    onCreateJournalForDate: (LocalDate) -> Unit,
) {
    val groupedVoiceJournals = remember(journals, zoneId) {
        groupJournalsByLocalDate(journals, zoneId)
    }

    val currentMonth = remember(today) { today.yearMonth }
    val startMonth = remember(currentMonth) { currentMonth.minusMonths(500) }
    val endMonth = remember(currentMonth) { currentMonth.plusMonths(500) }
    val daysOfWeek = remember { daysOfWeek() }
    var selectedDate by remember(today) { mutableStateOf(today) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfGrid,
    )
    val coroutineScope = rememberCoroutineScope()
    val visibleMonth = rememberFirstVisibleMonthAfterScroll(state)
    val visibleMonthEntryCount = remember(groupedVoiceJournals, visibleMonth.yearMonth) {
        countEntriesInMonth(groupedVoiceJournals, visibleMonth.yearMonth)
    }
    val selectedDayJournals = groupedVoiceJournals[selectedDate].orEmpty()
    val showTodayJump = visibleMonth.yearMonth != currentMonth || selectedDate != today

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("CalendarRoot"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "calendar_card") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Variables.SchemesSurface.copy(alpha = 0.96f),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Variables.SchemesPrimaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SimpleCalendarTitle(
                        modifier = Modifier.fillMaxWidth(),
                        currentMonth = visibleMonth.yearMonth,
                        monthEntryCount = visibleMonthEntryCount,
                        showTodayButton = showTodayJump,
                        onJumpToToday = {
                            selectedDate = today
                            coroutineScope.launch {
                                state.animateScrollToMonth(currentMonth)
                            }
                        },
                        goToPrevious = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                            }
                        },
                        goToNext = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                            }
                        },
                    )

                    MonthHeader(daysOfWeek = daysOfWeek)

                    FullScreenCalendar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let { mod -> if (horizontal) mod else mod.height(312.dp) }
                            .testTag("Calendar"),
                        state = state,
                        horizontal = horizontal,
                        dayContent = { day ->
                            val dayJournals = groupedVoiceJournals[day.date].orEmpty()
                            Day(
                                day = day,
                                isSelected = day.position == DayPosition.MonthDate && day.date == selectedDate,
                                isToday = day.position == DayPosition.MonthDate && day.date == today,
                                dayJournals = dayJournals,
                                onClick = { clicked ->
                                    if (selectedDate == clicked.date && dayJournals.isNotEmpty()) {
                                        onOpenDayPreview(dayJournals)
                                    } else {
                                        selectedDate = clicked.date
                                    }
                                },
                            )
                        },
                        monthBody = { _, content ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("MonthBody"),
                            ) {
                                content()
                            }
                        },
                        monthHeader = {},
                        monthFooter = {},
                    )
                }
            }
        }

        item(key = "selected_day_header") {
            SelectedDayAgendaHeader(
                selectedDate = selectedDate,
                isToday = selectedDate == today,
                entryCount = selectedDayJournals.size,
                onViewAll = { onOpenDayPreview(selectedDayJournals) },
                onAddForDate = { onCreateJournalForDate(selectedDate) }
            )
        }

        if (selectedDayJournals.isEmpty()) {
            item(key = "empty_day_state") {
                EmptyDayAgendaCard(
                    selectedDate = selectedDate,
                    onAddForDate = { onCreateJournalForDate(selectedDate) }
                )
            }
        } else {
            items(
                items = selectedDayJournals,
                key = { journal -> journal.id ?: journal.created }
            ) { journal ->
                CalendarAgendaNoteCard(
                    voiceJournal = journal,
                    zoneId = zoneId,
                    onClick = { onOpenJournal(journal) },
                    modifier = Modifier.testTag("calendar_agenda_item_${journal.id ?: journal.created}")
                )
            }
        }
    }
}

@Composable
private fun FullScreenCalendar(
    modifier: Modifier,
    state: CalendarState,
    horizontal: Boolean,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthBody: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit,
) {
    if (horizontal) {
        HorizontalCalendar(
            modifier = modifier,
            state = state,
            calendarScrollPaged = true,
            contentHeightMode = ContentHeightMode.Wrap,
            dayContent = dayContent,
            monthBody = monthBody,
            monthHeader = monthHeader,
            monthFooter = monthFooter,
        )
    } else {
        VerticalCalendar(
            modifier = modifier,
            state = state,
            calendarScrollPaged = true,
            contentHeightMode = ContentHeightMode.Fill,
            dayContent = dayContent,
            monthBody = monthBody,
            monthHeader = monthHeader,
            monthFooter = monthFooter,
        )
    }
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("MonthHeader")
            .clip(RoundedCornerShape(12.dp))
            .background(Variables.SchemesPrimaryContainer.copy(alpha = 0.55f))
            .padding(vertical = 8.dp),
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Variables.SchemesOnPrimaryContainer,
                text = formatWeekdayShort(dayOfWeek),
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    dayJournals: List<VoiceJournal>,
    onClick: (CalendarDay) -> Unit,
) {
    val isInMonth = day.position == DayPosition.MonthDate
    val hasEntries = isInMonth && dayJournals.isNotEmpty()
    val thumbnailUri = remember(dayJournals, isInMonth) {
        if (isInMonth) dayPreviewThumbnailUri(dayJournals) else null
    }
    val hasAudio = remember(dayJournals, isInMonth) {
        isInMonth && dayHasAudio(dayJournals)
    }
    val cellShape = RoundedCornerShape(12.dp)

    val containerColor = when {
        !isInMonth -> Color.Transparent
        thumbnailUri != null -> Variables.SchemesPrimaryContainer
        isSelected -> Variables.SchemesPrimary
        isToday -> Variables.SchemesPrimaryContainer
        hasEntries -> Variables.SchemesPrimaryContainer.copy(alpha = 0.55f)
        else -> Color.Transparent
    }

    val borderStroke = when {
        !isInMonth -> null
        isSelected && thumbnailUri != null -> BorderStroke(2.dp, Variables.SchemesPrimary)
        isToday && !isSelected -> BorderStroke(1.5.dp, Variables.SchemesPrimary)
        else -> null
    }

    val textColor = when {
        !isInMonth -> Variables.SchemesOnSurfaceVariant.copy(alpha = 0.28f)
        thumbnailUri != null -> Color.White
        isSelected -> Variables.SchemesOnPrimary
        isToday || hasEntries -> Variables.SchemesOnPrimaryContainer
        else -> Variables.SchemesOnSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(2.dp)
            .let { mod ->
                if (isInMonth) mod.testTag("calendar_day_${day.date}") else mod
            }
            .clip(cellShape)
            .background(containerColor)
            .let { mod ->
                if (borderStroke != null) mod.border(borderStroke, cellShape) else mod
            }
            .clickable(
                enabled = isInMonth,
                role = Role.Button,
                onClick = { onClick(day) },
            ),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailUri != null) {
            Image(
                painter = rememberAsyncImagePainter(thumbnailUri),
                contentDescription = "Entry thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (isSelected) 0.25f else 0.36f))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday || hasEntries) FontWeight.Bold else FontWeight.Medium,
            )

            if (hasEntries && thumbnailUri == null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Variables.SchemesOnPrimary else Variables.SchemesPrimary
                            )
                    )
                    if (hasAudio) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Variables.SchemesOnPrimary.copy(alpha = 0.75f)
                                    else Variables.SchemesSecondary
                                )
                        )
                    }
                }
            }
        }

        if (hasEntries && dayJournals.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected && thumbnailUri == null) Variables.SchemesOnPrimary
                        else Variables.SchemesPrimary
                    )
                    .padding(horizontal = 4.dp, vertical = 0.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (dayJournals.size > 9) "9+" else dayJournals.size.toString(),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected && thumbnailUri == null) Variables.SchemesPrimary else Variables.SchemesOnPrimary
                )
            }
        }
    }
}

@Composable
fun SimpleCalendarTitle(
    modifier: Modifier,
    currentMonth: YearMonth,
    goToPrevious: () -> Unit,
    goToNext: () -> Unit,
    monthEntryCount: Int? = null,
    showTodayButton: Boolean = false,
    onJumpToToday: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarNavigationIcon(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous",
                onClick = goToPrevious,
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.testTag("MonthTitle"),
                    text = formatCalendarMonthTitle(currentMonth),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Variables.SchemesOnPrimaryContainer
                    ),
                    textAlign = TextAlign.Center,
                )
                if (monthEntryCount != null) {
                    Text(
                        modifier = Modifier.testTag("calendar_month_count"),
                        text = "${formatEntryCountLabel(monthEntryCount)} this month",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Variables.SchemesOnSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            CalendarNavigationIcon(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                onClick = goToNext,
            )
        }

        if (showTodayButton && onJumpToToday != null) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = onJumpToToday,
                    shape = RoundedCornerShape(999.dp),
                    color = Variables.SchemesPrimaryContainer,
                    modifier = Modifier.testTag("calendar_today_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = Variables.SchemesPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Jump to Today",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Variables.SchemesOnPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarNavigationIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Variables.SchemesPrimaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Variables.SchemesPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SelectedDayAgendaHeader(
    selectedDate: LocalDate,
    isToday: Boolean,
    entryCount: Int,
    onViewAll: () -> Unit,
    onAddForDate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatSelectedDateHeader(selectedDate),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Variables.SchemesOnPrimaryContainer
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("calendar_selected_date_header")
                )
                if (isToday) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Variables.SchemesPrimary
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Variables.SchemesOnPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = formatEntryCountLabel(entryCount),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Variables.SchemesOnSurfaceVariant
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (entryCount > 0) {
                TextButton(
                    onClick = onViewAll,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("calendar_view_all_button")
                ) {
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Variables.SchemesPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Surface(
                onClick = onAddForDate,
                shape = RoundedCornerShape(999.dp),
                color = Variables.SchemesPrimary,
                modifier = Modifier.testTag("calendar_add_for_date_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add entry for date",
                        tint = Variables.SchemesOnPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Variables.SchemesOnPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDayAgendaCard(
    selectedDate: LocalDate,
    onAddForDate: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_empty_day_card"),
        shape = RoundedCornerShape(20.dp),
        color = Variables.SchemesSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, Variables.SchemesPrimaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Variables.SchemesPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Variables.SchemesPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "No entries on this day",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Variables.SchemesOnPrimaryContainer
                )
            )
            Text(
                text = "Capture a thought, voice note, or photo for ${formatSelectedDateHeader(selectedDate)}.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Variables.SchemesOnSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Button(
                onClick = onAddForDate,
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
                    text = "Write for this day",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CalendarAgendaNoteCard(
    voiceJournal: VoiceJournal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zoneId: ZoneId = ZoneId.systemDefault(),
) {
    val isLocked = voiceJournal.isNoteLocked()
    val (headline, snippet) = remember(voiceJournal) {
        extractCalendarCardText(voiceJournal)
    }
    val audioPath = remember(voiceJournal, isLocked) {
        voiceJournal.primaryAudioPath(revealLockedContent = !isLocked)
    }
    val imageUris = remember(voiceJournal, isLocked) {
        voiceJournal.imageUrisFromBlocks(revealLockedContent = !isLocked)
    }
    val previewImage = imageUris.firstOrNull { it.isNotBlank() }
    val timeLabel = remember(voiceJournal.created, zoneId) {
        formatEntryTime(voiceJournal.created, zoneId)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Variables.SchemesSurface.copy(alpha = 0.96f),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Variables.SchemesPrimaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Variables.SchemesPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (voiceJournal.favourite == true) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Variables.SchemesError,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (audioPath.isNotBlank()) {
                        com.example.voicejournal.ui.main.mainScreen.AudioFileDurationBadge(
                            audioPath = audioPath
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

                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Variables.SchemesOnSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (snippet.isNotBlank()) {
                    Text(
                        text = snippet,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Variables.SchemesOnSurfaceVariant
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                voiceJournal.folderName?.takeIf { it.isNotBlank() }?.let { folder ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Variables.SchemesOnSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = folder,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Variables.SchemesOnSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (previewImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(previewImage),
                    contentDescription = "Entry image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Variables.SchemesPrimaryContainer)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Variables.SchemesPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Variables.SchemesPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (audioPath.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Variables.SchemesPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = headline.firstOrNull()?.uppercaseChar()?.toString() ?: "J",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Variables.SchemesPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}
