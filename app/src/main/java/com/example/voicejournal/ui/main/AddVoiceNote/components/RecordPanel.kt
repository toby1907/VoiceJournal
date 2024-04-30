package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voicejournal.R
import com.example.voicejournal.formatTime
import com.example.voicejournal.ui.theme.Variables

@Composable
fun RecordPanelComponent(
    onTimerStart: () -> Unit,
    timerValue: Long,
    onCancelRecord: () -> Unit,
    onDoneClick: () -> Unit
) {

    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.lottie))
var isPlaying by remember{
    mutableStateOf(true)
}
    val progress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever)
    
    LaunchedEffect(key1 = Unit){
        // Start the play timer
        onTimerStart()

    }






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
            text = timerValue.formatTime(), // Initial text
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
                //containerColor = Variables.SchemesSurfaceTint,
                contentColor = Variables.SchemesSurface
            )
        ) {
            //here
           Box( contentAlignment = Alignment.Center){
                LottieAnimation(composition = composition, modifier = Modifier.size(96.dp),progress={progress})
                Icon(
                    tint = Variables.SchemesSurface,
                    painter = painterResource(id = R.drawable.baseline_mic_24), // Use the built-in mic icon
                    contentDescription = "Record" // Accessibility label
                )

            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {// Add an IconButton composable for the cancel icon
            IconButton(
                onClick = {
                    // Stop the media player
                    //    player?.stop()
                   onCancelRecord()

                },
                modifier = Modifier.padding(8.dp) // Padding around the button
            )
            {
                Icon(
                    painter = painterResource(id = com.google.android.material.R.drawable.ic_m3_chip_close), // Use the built-in cancel icon
                    contentDescription = "Cancel",
                    tint = Variables.SchemesOnPrimary
                // Accessibility label
                )
            }

            // Add a Text composable for the text done
            Text(
                text = "Done", // Text
                color = Variables.SchemesOnPrimary,
                fontSize = 18.sp, // Text size
                modifier = Modifier
                    .padding(8.dp) // Padding around the text
                    .clickable { // Make the text clickable
                        onDoneClick()
                    }
            )
        }
    }
}