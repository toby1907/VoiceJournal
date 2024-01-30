package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.voicejournal.R
import kotlinx.coroutines.DisposableHandle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomAppPopUpScreen(cardVisible: Boolean) {
    if (cardVisible) {


        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFC)),
                modifier = Modifier
                    // Use a fixed size modifier with 375 x 291
                    .size(width = 375.dp, height = 291.dp)
                    .align(Alignment.Bottom)
            ) {
                Text(text = " Media")

                LazyColumn {

                    items(3) { index ->
                        // A list of icons and texts for each item
                        val icons = listOf(
                            R.drawable.image_icon,
                            R.drawable.media_file,
                            R.drawable.photo_camera
                        )
                        val texts = listOf(
                            "Select From Gallery",
                            "Select From File",
                            "Take a Photo",
                        )
                        val colors = listOf(
                            Color(0xFF1EBE71),
                            Color(0xFFF5D941),
                            Color(0xFF1E98BE),

                            )
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(shape = CircleShape, color = colors[index]),
                                onClick = {

                                }) {
                                Icon(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = icons[index]),
                                    tint = Color.White,
                                    contentDescription = texts[index]
                                )
                            }
                            Text(texts[index], Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    windowInsets: WindowInsets,
    onClick: () -> DisposableHandle,
    onImageClick: () ->Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
        windowInsets = windowInsets
    ) {
       Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFC)),
                modifier = Modifier
                    // Use a fixed size modifier with 375 x 291
                    .size(width = 375.dp, height = 291.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = " Media")

                LazyColumn {

                    items(3) { index ->
                        // A list of icons and texts for each item
                        val icons = listOf(
                            R.drawable.image_icon,
                            R.drawable.media_file,
                            R.drawable.photo_camera
                        )
                        val texts = listOf(
                            "Select From Gallery",
                            "Select From File",
                            "Take a Photo",
                        )
                        val colors = listOf(
                            Color(0xFF1EBE71),
                            Color(0xFFF5D941),
                            Color(0xFF1E98BE),

                            )
                       Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(shape = CircleShape, color = colors[index]),
                                onClick = {
                                    if(index==0){
                                        onImageClick()
                                    }
                                   else{ onClick()}
                                }) {
                                Icon(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = icons[index]),
                                    tint = Color.White,
                                    contentDescription = texts[index]
                                )
                            }
                            Text(texts[index], Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}