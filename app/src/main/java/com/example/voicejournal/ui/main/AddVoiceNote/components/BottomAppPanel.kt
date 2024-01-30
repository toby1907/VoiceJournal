package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.voicejournal.R

@Composable
fun BottomAppPanel(cardVisibleState: () -> Unit) {

        // Define the icons and labels for the bottom panel
        val bottomMenuItems = listOf(
            BottomMenuItem(label = "Attachment", icon = painterResource(id = R.drawable.attach_file)),
            BottomMenuItem(label = "Tag", painterResource(id = R.drawable.tag)),
            BottomMenuItem(label = "Body", painterResource(id = R.drawable.format_align_left)),
            BottomMenuItem(label = "Bold", painterResource(id = R.drawable.format_bold)),
            BottomMenuItem(label = "Italic", painterResource(id = R.drawable.format_italic)),
            BottomMenuItem(label = "Strikethrough", painterResource(id = R.drawable.strikethrough)),
            BottomMenuItem(label = "Underline", icon = painterResource(id = R.drawable.format_underlined))
        )

        // Use a scrollable row to display the icons horizontally
    Row(
        modifier = Modifier
            .fillMaxWidth() // fill the entire width of the screen
            .height(48.dp)
            .background(color = Color(0xFFF3EDF7))
    ) {
        Row(
            modifier = Modifier // assign a weight to the child row
                .horizontalScroll(rememberScrollState())
                .weight(0.8f) // 80% of the available space
        ) {
            // Loop through the menu items and create an icon button for each one
            bottomMenuItems.forEachIndexed { index, menuItem ->
                IconButton(
                    onClick = {
                        if (index ==0){
                            cardVisibleState()
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = menuItem.icon,
                        contentDescription = menuItem.label
                    )
                }
            }
        }
        IconButton(
            onClick = { /*TODO*/ },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.double_arrow),
                contentDescription = "Double_arrow"
            )
        }
    }
    }

    // A data class to hold the icon and label information
    data class BottomMenuItem(val label: String, val icon: Painter)
