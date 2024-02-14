package com.example.voicejournal.ui.main.AddVoiceNote.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.R

@Composable
fun BottomAppPanel(
    cardVisibleState: () -> Unit,
    imageUri: List<Uri>?,
    imageSheetVisibleState: () -> Unit,
    tagVisiblestate: () -> Unit,

    ) {
    Log.d("ImageUri", "$imageUri")
    // Define the icons and labels for the bottom panel
    val bottomMenuItems = listOf(
        BottomMenuItem(label = "Attachment", icon = painterResource(id = R.drawable.attach_file)),
        BottomMenuItem(label = "Tag", painterResource(id = R.drawable.tag)),
        BottomMenuItem(label = "Body", painterResource(id = R.drawable.format_align_left)),
        BottomMenuItem(label = "Bold", painterResource(id = R.drawable.format_bold)),
        BottomMenuItem(label = "Italic", painterResource(id = R.drawable.format_italic)),
        BottomMenuItem(label = "Strikethrough", painterResource(id = R.drawable.strikethrough)),
        BottomMenuItem(
            label = "Underline",
            icon = painterResource(id = R.drawable.format_underlined)
        )
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
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        if (index == 0) {
                            cardVisibleState()
                        }
                    }
                ) {
                    if (imageUri?.isEmpty() == true) {
                        Icon(
                            painter = menuItem.icon,
                            contentDescription = menuItem.label,

                        )
                    }
                    if (imageUri?.isNotEmpty() == true && index == 0) {
                        // Show the box with the image for the first index
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri[0]),
                                contentDescription = "Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(32.dp)
                                    .clickable {
                                        imageSheetVisibleState()
                                    }
                            )
                        }
                    } else {
                        // Show the icons for the other indices
                        Icon(
                            painter = menuItem.icon,
                            modifier = Modifier.clickable {
                                if (index == 0) {
                                    cardVisibleState()
                                }
                                if (index == 1) {
                                    tagVisiblestate()
                                }
                            },
                            contentDescription = menuItem.label
                        )
                    }
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
