package com.example.voicejournal.ui.main.AddVoiceNote.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.R
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.theme.Variables
import kotlinx.coroutines.DisposableHandle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    windowInsets: WindowInsets,
    onClick: () -> DisposableHandle,
    onImageClick: () -> Unit,
    showFileChooser: () -> Unit,
    onCameraClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
        windowInsets = windowInsets,

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFC)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                ) { Text(text = "Attach Media") }

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

                LazyColumn {
                    items(3) { index ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    when (index) {
                                        0 -> onImageClick()
                                        1 -> showFileChooser()
                                        2 -> {
                                            onCameraClick()
                                            onClick()
                                        }
                                    }
                                }
                                .padding(6.dp)
                                .width(320.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(shape = CircleShape, color = colors[index]),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = icons[index]),
                                    tint = Color.White,
                                    contentDescription = texts[index]
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(texts[index])
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    windowInsets: WindowInsets,
    onClick: () -> DisposableHandle,
    onImageClick: () -> Unit,
    imageUris: List<Uri>
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize()
            ) {
                items(count = imageUris.size + 1, itemContent = { index ->
                    when (index) {
                        0 -> {
                            Box(modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .clickable {

                                }
                            )
                            {
                                Image(
                                    painter = painterResource(id = R.drawable.add_image),
                                    contentDescription = "Add more images",
                                    alignment = Alignment.Center,
                                    modifier = Modifier
                                        .width(184.dp)
                                        .height(205.dp)
                                        .clickable {
                                            onClick()
                                        },
                                    contentScale = ContentScale.Fit,
                                )
                            }

                        }

                        else -> {
                            // Show the Image for the other items
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        color = Variables.SchemesSurface,
                                        shape = RoundedCornerShape(28.dp)
                                    )
                                    .aspectRatio(1f)
                            )
                            {

                                    Image(
                                        painter = rememberAsyncImagePainter(imageUris[index - 1]),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .width(184.dp)
                                            .height(205.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(color = Variables.SchemesSurface)
                                            .clickable {
                                                onImageClick()
                                            },

                                        contentScale = ContentScale.Crop,
                                    )

                            }
                        }
                    }
                })
            }
        }

    }
}
