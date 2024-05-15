package com.example.voicejournal.ui.main.mainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun NewJournalItem(
    modifier: Modifier = Modifier,
    voiceJournal: VoiceJournal,

) {
    val titleState = rememberRichTextState()
    val iconState = rememberRichTextState()

    titleState.setHtml(voiceJournal.title)
    iconState.setHtml(voiceJournal.title)
    iconState.setText(iconState.annotatedString.text.getOrNull(0).toString()
        .toUpperCase(Locale.getDefault()))

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = modifier
                .background(color = Color.Transparent)
                .fillMaxWidth()
                .padding(8.dp),
        ) {


            Column(

                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
                
            ) {
                Spacer(modifier = Modifier.padding(4.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val formatTime = SimpleDateFormat("hh:mm a", Locale.getDefault())

                    val journalTime = formatTime.format(voiceJournal.created)
                  /*  val simpleDate = SimpleDateFormat("d/MMM/yy", Locale.getDefault())
                    val journalDate = simpleDate.format(voiceJournal.created)*/
                    Text(
                        text = journalTime,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight(400),
                            color = Variables.SchemesOnSurface
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {

                       if(voiceJournal.favourite!=false) {
                            Icon(
                                tint = Variables.SchemesError,
                                modifier = Modifier
                                    .padding(1.dp)
                                    .width(12.dp)
                                    .height(12.dp),
                                painter = painterResource(id = R.drawable.favorite_fill),
                                contentDescription = "Favorite"
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Variables.SchemesSecondary)
                        )
                        Icon(tint = Variables.SchemesOnSurface,
                            modifier= Modifier
                                .padding(1.dp)
                                .width(12.dp)
                                .height(12.dp),
                            painter = painterResource(id = R.drawable.cloud_off),
                            contentDescription = "Cloud Upload state"
                        )
                        if (voiceJournal.fileName != "") {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Variables.SchemesSecondary)
                            )
                            Icon(
                                modifier = Modifier
                                    .padding(1.dp)
                                    .width(12.dp)
                                    .height(12.dp),
                                painter = painterResource(id = R.drawable.audio_file),
                                contentDescription = ""
                            )
                        }
                    }
                }



                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                )  {
                    Column(verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxHeight()
                    ) {

                        voiceJournal.content?.let {
                            Text(
                                text = it,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight(300),
                                    color = Variables.SchemesOnSurface,
                                )
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                        }

                        RichText(

                            state = titleState,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(500),
                                color = Variables.SchemesOnSurface,
                            ),
                           modifier = modifier.size(width = 200.dp, height = 48.dp),
                        )
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    val filterImages = voiceJournal.imageUris?.filter { it.isNotEmpty() }
                    if (filterImages?.isNotEmpty()==true) {
                        Image(
                            painter = rememberAsyncImagePainter(voiceJournal.imageUris?.last()),
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(color = Variables.SchemesSurface),

                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(color = Variables.SchemesPrimary, shape = CircleShape)
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 3.dp,
                                    color = Color.Transparent,
                                    shape = CircleShape
                                )

                        ) {
                            RichText(
                                state = iconState,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight(500),
                                    color = Variables.SchemesOnPrimary,
                                )
                            )
                        }
                    }
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
        Text(text = dayOfMonth.toString()) // Day of the month
        Spacer(modifier = Modifier.padding(8.dp))
        Column {
            Text(text = formattedDate) // Date ("March 16, 2024")
            Text(text = formattedTime) // Time
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