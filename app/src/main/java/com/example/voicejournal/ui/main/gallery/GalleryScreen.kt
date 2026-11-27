package com.example.voicejournal.ui.main.gallery

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.voicejournal.Data.model.ImageFile

@Composable
fun GalleryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    galleryScreenViewModel: GalleryScreenViewModel = hiltViewModel(),
    noteColor: Int = -1
) {
    val uiState by galleryScreenViewModel.uiState.collectAsState()

    GalleryScreenContent(
        uiState = uiState,
        modifier = modifier,
        onClose = { navController.popBackStack() },
        onToggleImage = { uri -> galleryScreenViewModel.toggleImageSelection(uri) },
        onSelectAlbum = { album -> galleryScreenViewModel.selectAlbum(album) },
        onClearSelection = { galleryScreenViewModel.clearSelection() },
        onConfirmSelection = {
            galleryScreenViewModel.confirmSelection {
                navController.popBackStack()
            }
        },
        onRetry = { galleryScreenViewModel.refreshImages() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreenContent(
    uiState: GalleryUiState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onToggleImage: (Uri) -> Unit,
    onSelectAlbum: (String) -> Unit,
    onClearSelection: () -> Unit,
    onConfirmSelection: () -> Unit,
    onRetry: () -> Unit
) {
    val selectedCount = uiState.selectedCount
    val visibleImages = uiState.visibleImages

    com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor(true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = com.example.voicejournal.ui.theme.Variables.SchemesSurface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedCount > 0) {
                            "$selectedCount selected"
                        } else {
                            "Select Photos"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = com.example.voicejournal.ui.theme.Variables.SchemesOnPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("gallery_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Gallery",
                            tint = com.example.voicejournal.ui.theme.Variables.SchemesOnPrimaryContainer
                        )
                    }
                },
                actions = {
                    if (selectedCount > 0) {
                        TextButton(
                            onClick = onClearSelection,
                            modifier = Modifier.testTag("gallery_clear_button")
                        ) {
                            Text(
                                text = "Clear",
                                color = com.example.voicejournal.ui.theme.Variables.SchemesPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.voicejournal.ui.theme.Variables.SchemesPrimaryContainer
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                GallerySelectionBottomBar(
                    selectedUris = uiState.selectedUrisOrder,
                    isSaving = uiState.isSavingSelection,
                    onRemoveUri = onToggleImage,
                    onConfirm = onConfirmSelection
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Album filter chips (shown when photos span multiple albums)
            if (uiState.albums.size > 1 && !uiState.isLoading && uiState.errorMessage == null) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = uiState.albums, key = { it }) { album ->
                        val isSelected = album == uiState.selectedAlbum
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectAlbum(album) },
                            label = { Text(album) },
                            modifier = Modifier.testTag("gallery_album_$album")
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading photos...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRetry) {
                                Text("Retry")
                            }
                        }
                    }
                }

                visibleImages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No photos found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = visibleImages,
                            key = { it.uri.toString() }
                        ) { imageFile ->
                            val selectionNumber = uiState.selectionIndexOf(imageFile.uri)
                            GalleryPhotoTile(
                                imageFile = imageFile,
                                selectionNumber = selectionNumber,
                                onToggle = { onToggleImage(imageFile.uri) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryPhotoTile(
    imageFile: ImageFile,
    selectionNumber: Int?,
    onToggle: () -> Unit
) {
    val isSelected = selectionNumber != null
    val inset by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 0.dp,
        label = "galleryTileInset"
    )
    val cornerRadius = if (isSelected) 10.dp else 0.dp
    val tileShape = RoundedCornerShape(cornerRadius)
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageFile.uri)
            .size(320)
            .crossfade(true)
            .build()
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .testTag("gallery_item_${imageFile.uri}")
            .clickable(onClick = onToggle)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inset)
                .clip(tileShape)
                .then(
                    if (isSelected) {
                        Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, tileShape)
                    } else {
                        Modifier
                    }
                )
        ) {
            Image(
                painter = painter,
                contentDescription = imageFile.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.16f))
                )
            }
        }

        // Top-right numbered selection badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .testTag("gallery_badge_${imageFile.uri}")
                .semantics {
                    contentDescription = if (selectionNumber != null) {
                        "Selected $selectionNumber"
                    } else {
                        "Not selected"
                    }
                }
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Black.copy(alpha = 0.30f)
                    },
                    shape = CircleShape
                )
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selectionNumber != null) {
                Text(
                    text = selectionNumber.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GallerySelectionBottomBar(
    selectedUris: List<Uri>,
    isSaving: Boolean,
    onRemoveUri: (Uri) -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = selectedUris, key = { it.toString() }) { uri ->
                    val thumbPainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .size(120)
                            .crossfade(true)
                            .build()
                    )
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onRemoveUri(uri) }
                    ) {
                        Image(
                            painter = thumbPainter,
                            contentDescription = "Remove selected photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(16.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onConfirm,
                enabled = !isSaving && selectedUris.isNotEmpty(),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.testTag("gallery_confirm_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedUris.size == 1) {
                        "Add Photo"
                    } else {
                        "Add ${selectedUris.size}"
                    }
                )
            }
        }
    }
}
