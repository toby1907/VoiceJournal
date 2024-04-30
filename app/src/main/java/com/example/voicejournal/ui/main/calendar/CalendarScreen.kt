package com.example.voicejournal.ui.main.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.voicejournal.ui.theme.Variables
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
   // val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        //firstDayOfWeek = firstDayOfWeek
        firstDayOfWeek = daysOfWeek.first()
    )

  /*  HorizontalCalendar(
        state = state,
        dayContent = { Day(it) }
    )*/

    /*   Column {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek) // Use the title here
        HorizontalCalendar(
            state = state,
            dayContent = { Day(it) }
        )
    }*/

   Column {
       DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        VerticalCalendar(
            state = state,
            dayContent = { Day(it) },
            monthHeader = { month ->

               Row {
                    Spacer(modifier = Modifier.padding(8.dp))
                   Row(verticalAlignment = Alignment.CenterVertically,
                       horizontalArrangement = Arrangement.SpaceBetween){
                        Text(text = month.yearMonth.month.name)
                       Spacer(modifier = Modifier.padding(4.dp))
                        Text(text = month.yearMonth.year.toString())
                    }
                }
            }
        )
    }
}

@Composable
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Variables.SchemesOnSurface else Color.Gray
        )
    }
}
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}
