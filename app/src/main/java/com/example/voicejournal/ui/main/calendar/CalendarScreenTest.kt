package com.example.voicejournal.ui.main.calendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.R
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
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun Example8Page(horizontal: Boolean = true,
                 journals: List<VoiceJournal>,
                 navController:NavController
                 ) {

    val groupedVoiceJournals = journals.sortedByDescending { it.created }.groupBy {
       val instant = Instant.ofEpochMilli(it.created)
         instant.atZone(ZoneId.systemDefault()).toLocalDate()


    }




    val today = remember { LocalDate.now() }
    val currentMonth = remember(today) { today.yearMonth }
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    //StatusBarColorUpdateEffect(color = colorResource(id = R.color.example_1_bg_light))
    Column(
        modifier = Modifier
            .fillMaxSize()
            //    .background(colorResource(id = R.color.example_1_bg_light))
            .padding(top = 16.dp),
    ) {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = daysOfWeek.first(),
            outDateStyle = OutDateStyle.EndOfGrid,
        )
        val coroutineScope = rememberCoroutineScope()
        val visibleMonth = rememberFirstVisibleMonthAfterScroll(state)
        // Draw light content on dark background.
        CompositionLocalProvider(LocalContentColor provides darkColors().onSurface) {
            SimpleCalendarTitle(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                currentMonth = visibleMonth.yearMonth,
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
            FullScreenCalendar(
                modifier = Modifier
                    .fillMaxSize()
                    //    .background(Variables.SchemesPrimaryContainer)
                    .testTag("Calendar"),
                state = state,
                horizontal = horizontal,
                dayContent = { day ->
                    Day(
                        day = day,
                        isSelected = selections.contains(day),
                        isToday = day.position == DayPosition.MonthDate && day.date == today,
                        groupedVoiceJournals = groupedVoiceJournals,
                        onClick = { clicked ->
                            if (selections.contains(clicked)) {
                                selections.remove(clicked)
                            } else {
                                selections.add(clicked)
                            }
                        },
                        navController = navController
                    )
                },
                // The month body is only needed for ui test tag.
                monthBody = { _, content ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("MonthBody"),
                    ) {
                        content()
                    }
                },
                monthHeader = {
                    MonthHeader(daysOfWeek = daysOfWeek)
                },
                monthFooter = { month ->
                    val count = month.weekDays.flatten()
                        .count { selections.contains(it) }
                    MonthFooter(selectionCount = count)
                },
            )
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
            contentHeightMode = ContentHeightMode.Fill,
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
        Modifier
            .fillMaxWidth()
            .testTag("MonthHeader")
            .background(Variables.SchemesPrimary)
            .padding(vertical = 8.dp),
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                text = dayOfWeek.name.substring(0, minOf(dayOfWeek.name.length, 3)),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MonthFooter(selectionCount: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .testTag("MonthFooter")
            .background(Variables.SchemesPrimary)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        val text = if (selectionCount == 0) {
            stringResource(R.string.example_8_zero_selection)
        } else {
            pluralStringResource(R.plurals.example_8_selection, selectionCount, selectionCount)
        }
        Text(text = text)
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (CalendarDay) -> Unit,
    groupedVoiceJournals:  Map<LocalDate, List<VoiceJournal>>,
    navController: NavController,

) {



    /*Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RectangleShape)
            .background(
                color = when {
                    isSelected -> Variables.SchemesSecondary
                    isToday -> Color.Transparent
                    else -> Color.Transparent
                },
            )
            // Disable clicks on inDates/outDates
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                showRipple = !isSelected,
                onClick = { onClick(day) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        groupedVoiceJournals.forEach { (date, groupJournals) ->
            if (day.date==date){
                Image(
                    painter = rememberAsyncImagePainter(groupJournals.get(0).imageUris?.get(0)),
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RectangleShape)
                        .size(32.dp)

                )
            }

        }

        val textColor = when (day.position) {
            // Color.Unspecified will use the default text color from the current theme
            DayPosition.MonthDate -> if (isSelected) Variables.SchemesOnSecondary else Variables.SchemesPrimary
            DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
        }
       Column {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 15.sp,
            )
           if (isToday) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Variables.SchemesSecondary)
                )
            }
        }
    }*/
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RectangleShape)
            .background(
                color = when {
                    isSelected -> Variables.SchemesSecondary
                    isToday -> Color.Transparent
                    else -> Color.Transparent
                },
            )
            // Disable clicks on inDates/outDates
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                showRipple = !isSelected,
                onClick = {
                    onClick(day)

                          },
            ),
        contentAlignment = Alignment.Center
    ) {
        val textColor = when (day.position) {
            // Color.Unspecified will use the default text color from the current theme
            DayPosition.MonthDate -> if (isSelected) Variables.SchemesOnSecondary else Variables.SchemesPrimary
            DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
        }
        groupedVoiceJournals.forEach { (date, groupJournals) ->
         //   val listOfIds = groupJournals.filter {  }
            if (day.date == date) {
                val filterImages = groupJournals.get(0).imageUris?.filter { it.isNotEmpty() }

                if (groupJournals.get(0).imageUris?.isEmpty()==false&& day.position!= DayPosition.OutDate&& day.position!=DayPosition.InDate) {
                    Image(
                        painter = rememberAsyncImagePainter(groupJournals[0].imageUris?.get(0)),
                        contentDescription = "Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RectangleShape)
                            .size(32.dp)
                            .clickable {
                                navController.navigate(
                                    "JournalPreviewScreen/${
                                        groupJournals
                                            .map { it.id }
                                            .joinToString()
                                    }"
                                )
                            }
                    )
                }
                if (filterImages?.isEmpty()==true && day.position!= DayPosition.OutDate&& day.position!=DayPosition.InDate){
                    Box(modifier =Modifier
                        .background(color = Variables.SchemesSecondary)
                        .clip(RectangleShape)
                        .size(32.dp)
                        .clickable {
                            navController.navigate(
                                "JournalPreviewScreen/${
                                    groupJournals
                                        .map { it.id }
                                        .joinToString()
                                }"
                            )
                        }
                            )
                }
            }
        }

        Column {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 15.sp,
            )
            if (isToday) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Variables.SchemesSecondary)
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
) {
    Row(
        modifier = modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarNavigationIcon(
            icon = Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "Previous",
            onClick = goToPrevious,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .testTag("MonthTitle"),
            text = currentMonth.month.name,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = Variables.SchemesPrimary
        )
        CalendarNavigationIcon(
            icon = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Next",
            onClick = goToNext,
        )
    }
}

@Composable
private fun CalendarNavigationIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(1f)
        .clip(shape = CircleShape)
        .clickable(role = Role.Button, onClick = onClick),
) {
    Icon(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .align(Alignment.Center),
        imageVector = icon,
        contentDescription = contentDescription,
        tint = Variables.SchemesPrimary
    )
}
