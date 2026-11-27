package com.example.voicejournal.ui.main.AddVoiceNote.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.JournalSheetDragHandle
import com.example.voicejournal.ui.theme.JournalSheetHeader
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
        shape = JournalModalDefaults.SheetShape,
        containerColor = JournalModalDefaults.ContainerColor,
        contentColor = JournalModalDefaults.TextColor,
        scrimColor = JournalModalDefaults.ScrimColor,
        dragHandle = { JournalSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JournalSheetHeader(
                title = "Attach Media",
                subtitle = "Add photos to place anywhere on your note"
            )

            val options = listOf(
                Triple(
                    R.drawable.image_icon,
                    "Select From Gallery" to "Choose photos from your library",
                    Variables.SchemesPrimary
                ),
                Triple(
                    R.drawable.media_file,
                    "Select From File" to "Browse device folders for images",
                    Variables.SchemesSecondary
                ),
                Triple(
                    R.drawable.photo_camera,
                    "Take a Photo" to "Capture a new image with camera",
                    Variables.SchemesSurfaceTint
                )
            )

            options.forEachIndexed { index, (iconRes, labels, accentColor) ->
                val (title, subtitle) = labels
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(JournalModalDefaults.CardShape)
                        .clickable {
                            when (index) {
                                0 -> onImageClick()
                                1 -> showFileChooser()
                                2 -> {
                                    onCameraClick()
                                    onClick()
                                }
                            }
                        },
                    shape = JournalModalDefaults.CardShape,
                    color = JournalModalDefaults.SoftCardColor,
                    border = BorderStroke(1.dp, JournalModalDefaults.SubtleBorderColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    shape = CircleShape,
                                    color = accentColor.copy(alpha = 0.16f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(id = iconRes),
                                tint = accentColor,
                                contentDescription = title
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Variables.SchemesOnSurface
                                )
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Variables.SchemesOnSurfaceVariant
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Variables.SchemesOnSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
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
        windowInsets = windowInsets,
        shape = JournalModalDefaults.SheetShape,
        containerColor = JournalModalDefaults.ContainerColor,
        contentColor = JournalModalDefaults.TextColor,
        scrimColor = JournalModalDefaults.ScrimColor,
        dragHandle = { JournalSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JournalSheetHeader(
                title = "Attached Photos",
                subtitle = if (imageUris.size == 1) "1 photo attached" else "${imageUris.size} photos attached"
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(count = imageUris.size + 1) { index ->
                    when (index) {
                        0 -> {
                            Surface(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onClick() },
                                shape = RoundedCornerShape(18.dp),
                                color = JournalModalDefaults.SoftCardColor,
                                border = BorderStroke(1.5.dp, Variables.SchemesPrimary.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Variables.SchemesPrimaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Add more images",
                                            tint = Variables.SchemesPrimary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Add More",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Variables.SchemesOnPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }

                        else -> {
                            Surface(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onImageClick() },
                                shape = RoundedCornerShape(18.dp),
                                color = JournalModalDefaults.SoftCardColor,
                                border = BorderStroke(1.dp, JournalModalDefaults.SubtleBorderColor)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUris[index - 1]),
                                    contentDescription = "Attached image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
