package com.example.voicejournal.ui.main.voiceJournalPreviewScreen.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.R
import com.example.voicejournal.convertIntToTimeFormat
import com.example.voicejournal.formatTime
import com.example.voicejournal.ui.main.AddVoiceNote.components.VoiceRecordAlertDialog
import com.example.voicejournal.ui.theme.Variables

@Composable
fun PlayRecordPanel(
    timerValue: Long,
    timerValue2: Int,
    onPlay: () -> Unit,
    onCancelRecord: () -> Unit,
    playingState: Boolean
) {
    //alertDialog
    val openDialog = remember { mutableStateOf(false) }
    val onPlayState = remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .width(200.dp)
            .height(45.dp)
            .background(color = Color.Transparent, shape = CircleShape),
        horizontalArrangement = Arrangement.End, // Arrange the children evenly
        verticalAlignment = Alignment.CenterVertically // Align the children vertically center
    ) {


        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {// Add an IconButton composable for the cancel icon
            // Add an IconButton composable for the record icon
            if (!onPlayState.value||!playingState){
                IconButton(
                    onClick = {
                        onPlay()
                        onPlayState.value = true
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Variables.SchemesSurfaceTint,
                        contentColor = Variables.SchemesSurface
                    )
                ) {

                    Icon(
                        tint = Variables.SchemesSurface,
                        painter = painterResource(id = R.drawable.play_button_24), // Use the built-in mic icon
                        contentDescription = "Record" // Accessibility label
                    )

                }
            }
            else{
                IconButton(
                    onClick = {

                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Variables.SchemesSurfaceTint,
                        contentColor = Variables.SchemesSurface
                    )
                ) {

                    Icon(
                        tint = Variables.SchemesSurface,
                        painter = painterResource(id = R.drawable.pause_button_24), // Use the built-in mic icon
                        contentDescription = "Record" // Accessibility label
                    )

                }
            }
           if(playingState) {
                IconButton(
                    onClick = {
                        onCancelRecord()
                        onPlayState.value = false
                        Log.d("PlayCanIconButton", "Clicked")
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Variables.SchemesSurfaceTint,
                        contentColor = Variables.SchemesOnPrimary
                    ),
                    enabled = true
                ) {

                    Icon(
                        tint = Variables.SchemesOnPrimary,
                        painter = painterResource(id = R.drawable.stop_button_36), // Use the built-in mic icon
                        contentDescription = "Record" // Accessibility label
                    )

                }
            }
            if (playingState) {
                Text(
                    text = timerValue.formatTime(), // Initial text
                    fontSize = 18.sp, // Text size
                    modifier = Modifier.padding(8.dp) // Padding around the text
                )
            }
            if (!playingState) {
                Text(
                    text = convertIntToTimeFormat(timerValue2), // Initial text
                    fontSize = 18.sp, // Text size
                    modifier = Modifier.padding(8.dp) // Padding around the text
                )
            }
        }



    }

}