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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun BottomAppPanel(
    cardVisibleState: () -> Unit,
    imageUri: List<Uri>?,
    imageSheetVisibleState: () -> Unit,
    tagVisiblestate: () -> Unit,
    boldButtonState: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    state: RichTextState,
    alignStyleSheetVisiblity: () -> Unit

) {
    var boldSelected by rememberSaveable { mutableStateOf(false) }
    var italicSelected by rememberSaveable { mutableStateOf(false) }
    var strikethroughSelected by rememberSaveable { mutableStateOf(false) }
    var underlineSelected by rememberSaveable { mutableStateOf(false) }

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

    Row(
        modifier = Modifier
            .fillMaxWidth() // fill the entire width of the screen
            .height(48.dp)
            .background(color = Variables.SchemesSurface)
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


                    }
                ) {

                    if (index == 0) {

                        if (imageUri?.isNotEmpty() == true) {
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
                        }

                        if(imageUri?.isEmpty() == true) {
                            Icon(
                                painter = menuItem.icon,
                                modifier = Modifier.clickable {

                                    cardVisibleState()


                                },
                                contentDescription = menuItem.label,

                            )
                        }

                    }
                    if (index == 3) {
                        val currentSpanStyle = state.currentSpanStyle
                        val isBold = currentSpanStyle.fontWeight == FontWeight.Bold

                        ControlWrapper(
                            selected = isBold
                            //boldSelected
                            ,
                            onChangeClick = { boldSelected = it },
                            onClick = onBoldClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatBold,
                                contentDescription = "Bold Control",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                     if(index==1) {
                         Icon(
                             painter = menuItem.icon,
                             modifier = Modifier.clickable {

                                     tagVisiblestate()

                             },
                             contentDescription = menuItem.label,
                            tint =  MaterialTheme.colorScheme.onPrimary
                         )
                     }

                    if (index==2){
                        Icon(
                            painter = menuItem.icon,
                            modifier = Modifier.clickable {
                                    alignStyleSheetVisiblity()
                            },
                            contentDescription = menuItem.label,
                            tint =  MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (index ==4){
                        val currentSpanStyle = state.currentSpanStyle
                        val isItalic = currentSpanStyle.fontStyle == FontStyle.Italic

                        ControlWrapper(
                            selected = isItalic,
                            onChangeClick = { italicSelected = it },
                            onClick = onItalicClick,
                            unselectedColor = Color.Transparent
                        ) {
                            Icon(
                                painter = menuItem.icon,
                                contentDescription = menuItem.label,
                                tint =  MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    if (index ==5){
                        val currentSpanStyle = state.currentSpanStyle
                        val isStrikethrough = currentSpanStyle.textDecoration == TextDecoration.LineThrough
                        ControlWrapper(
                            selected = isStrikethrough ,
                            onChangeClick = { strikethroughSelected = it },
                            onClick = onStrikethroughClick,
                            unselectedColor = Color.Transparent
                        ) {
                        Icon(
                            painter = menuItem.icon,
                            contentDescription = menuItem.label,
                            tint =  MaterialTheme.colorScheme.onPrimary
                        )
                        }
                    }
                    if (index ==6){
                        val currentSpanStyle = state.currentSpanStyle
                        val isUnderline = currentSpanStyle.textDecoration == TextDecoration.Underline
                        ControlWrapper(
                            selected = isUnderline,
                            onChangeClick = { underlineSelected = it },
                            onClick = onUnderlineClick,
                            unselectedColor = Color.Transparent
                        ) {
                            Icon(
                                painter = menuItem.icon,
                                contentDescription = menuItem.label,
                                tint =  MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }


                }
            }


        }
        IconButton(
            onClick = { /*TODO*/ },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.double_arrow),
                contentDescription = "Double_arrow",
                tint =  MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// A data class to hold the icon and label information
data class BottomMenuItem(val label: String, val icon: Painter)
