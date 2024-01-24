package com.example.voicejournal.ui.main.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun NewJournalItem(
    modifier: Modifier = Modifier,
    voiceJournal: VoiceJournal
) {


    Column(

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = modifier
                .background(color = Variables.SchemesSurface)
                .fillMaxWidth()
                .padding(start = 4.dp),
        )
        {
            Row(
                modifier = Modifier.clip(CircleShape)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(color = Color(voiceJournal.color))
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = voiceJournal.title.getOrNull(0).toString()
                            .toUpperCase(Locale.getDefault()),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight(500),
                            color = Variables.SchemesOnSurface,
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.padding(4.dp))
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
                        Icon(tint = Variables.SchemesError,
                            modifier= Modifier
                                .padding(1.dp)
                                .width(12.dp)
                                .height(12.dp),
                            painter = painterResource(id = R.drawable.favorite_fill),
                            contentDescription = "Favorite"
                        )
                        Icon(tint = Variables.SchemesOnSurface,
                            modifier= Modifier
                                .padding(1.dp)
                                .width(12.dp)
                                .height(12.dp),
                            painter = painterResource(id = R.drawable.cloud_off),
                            contentDescription = "Cloud Upload state"
                        )
                    }
                }

                Text(
                    text = voiceJournal.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                        color = Variables.SchemesOnSurface,
                    )
                )

                Row( modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    voiceJournal.content?.let {
                        Text(modifier = Modifier
                            .height(64.dp)
                            .width(259.dp),
                            text = it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(300),
                                color = Variables.SchemesOnSurface,
                            )
                        )
                    }

                    if (voiceJournal.fileName != "") Icon(
                        modifier= Modifier
                            .padding(1.dp)
                            .width(12.dp)
                            .height(12.dp),
                        painter = painterResource(id = R.drawable.audio_file),
                        contentDescription = ""
                    )
                }
            }
        }
        Divider()
    }


}