package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.R
import com.example.voicejournal.ui.main.AddVoiceNote.AddEditNoteEvent
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel
import com.example.voicejournal.ui.theme.Variables
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RecordPanelComponent(
    addVoiceNoteViewModel: AddVoiceNoteViewModel
) {

    // Get the coroutine scope of the composable
    val scope = rememberCoroutineScope()
    var playTimerText by remember {
        mutableStateOf("0:00")
    }
    // Initialize a variable to store the elapsed time
    var elapsedTime by remember { mutableStateOf(0L) }

    // Define a function to update the play timer
    fun updatePlayTimer() {
        // Convert the elapsed time to minutes and seconds
        val minutes = (elapsedTime / 1000) / 60
        val seconds = (elapsedTime / 1000) % 60

        // Format the play timer text
        val playTimerTxt = String.format("%02d:%02d", minutes, seconds)

        // Set the text to the play timer view
        playTimerText = playTimerTxt
    }

    // Define a function to start the play timer
    fun startPlayTimer() {
        // Launch a coroutine in the scope
        scope.launch {
            // Schedule a task to run every second
            while (true) {
                // Increment the elapsed time by one second
                elapsedTime += 1000

                // Update the play timer
                updatePlayTimer()

                // Delay for one second
                delay(1000)
            }
        }
    }

    // Define a function to stop the play timer
    fun stopPlayTimer() {
        // Cancel the coroutine in the scope
        scope.cancel()
    }

    // Call your startPlaying function
    addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Recording(addVoiceNoteViewModel.noteFileName.value.text))

    // Start the play timer
    startPlayTimer()

    // Create a row layout for the control panel button
    Row(
        modifier = Modifier
            .width(343.dp)
            .height(96.dp)
            .background(color = Variables.SchemesPrimary, shape = CircleShape),
        horizontalArrangement = Arrangement.SpaceEvenly, // Arrange the children evenly
        verticalAlignment = Alignment.CenterVertically // Align the children vertically center
    ) {
        // Add a Text composable for the play timer
        Text(
            text = playTimerText, // Initial text
            fontSize = 18.sp, // Text size
            modifier = Modifier.padding(8.dp) // Padding around the text
        )
        Spacer(modifier = Modifier.size(8.dp))
        // Add an IconButton composable for the record icon
        IconButton(
            onClick = {

            },
            modifier = Modifier
                .padding(8.dp) // Padding around the button

                .width(96.dp)
                .height(96.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Variables.SchemesSurfaceTint,
                contentColor = Variables.SchemesSurface
            )
        ) {

            Icon(
                tint = Variables.SchemesSurface,
                painter = painterResource(id = R.drawable.baseline_mic_24), // Use the built-in mic icon
                contentDescription = "Record" // Accessibility label
            )

        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {// Add an IconButton composable for the cancel icon
            IconButton(
                onClick = {
                    // Stop the media player
                    //    player?.stop()
                    addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)
                    // Stop the play timer
                    stopPlayTimer()
                    addVoiceNoteViewModel.changeRecordState(false)
                },
                modifier = Modifier.padding(8.dp) // Padding around the button
            )
            {
                Icon(
                    painter = painterResource(id = com.google.android.material.R.drawable.ic_m3_chip_close), // Use the built-in cancel icon
                    contentDescription = "Cancel" // Accessibility label
                )
            }

            // Add a Text composable for the text done
            Text(
                text = "Done", // Text
                fontSize = 18.sp, // Text size
                modifier = Modifier
                    .padding(8.dp) // Padding around the text
                    .clickable { // Make the text clickable
                        // Perform any action you want
                    }
            )
        }
    }
}