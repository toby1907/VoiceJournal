package com.example.voicejournal.ui.main.gallery

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.R
import com.example.voicejournal.Screen


// Gallery screen composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    galleryScreenViewModel: GalleryScreenViewModel = hiltViewModel(),
    noteColor: Int,
    ) {
    val imageFiles by galleryScreenViewModel.imageFiles.collectAsState()
    var imageFileIsSelected by remember { mutableStateOf(false) }
    val selectedFiles = imageFiles.filter { it.isSelected }
    Log.d("ImageSelected","$selectedFiles")
    val numberOfSelectedFiles = selectedFiles.size
    val selectedUriss = selectedFiles.map { it.uri }
    val selectedStrings = selectedUriss.map { it.toString() }
    val noteId = galleryScreenViewModel.currentNoteId
    Log.d("UriofImageSelected","$selectedUriss")
    //val selectedUris by galleryScreenViewModel.selectedUris.collectAsState(initial = emptySet())
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(imageFileIsSelected||selectedFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(8.dp))
                       Text(text = numberOfSelectedFiles.toString() )
                    }
                    else{
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Select Picture")
                    }

                        },
                navigationIcon = {
                  if(imageFileIsSelected||selectedFiles.isNotEmpty())
                  {
                      Icon(
                          painter = painterResource(id = R.drawable.check_24),
                          contentDescription = "",
                          modifier = Modifier.clickable {
                              val uriListString = selectedUriss.joinToString(",") { it.toString() }

                           galleryScreenViewModel.saveSelectedUris(selectedStrings)
                           Log.d("uriListString", "$selectedStrings")
                            /*  navController.navigate(
                                  Screen.AddEditNoteScreen.route +
                                          "?noteId=${noteId}&noteColor=${noteColor}"
                              ){
                                  popUpTo("gallery") {
                                      inclusive = true
                                  }
                              }*/
                              navController.navigateUp()

                          }

                      )
                      Spacer(modifier = Modifier.size(8.dp))

                  }
                    else{
                      Icon(
                          painter = painterResource(id = com.google.android.material.R.drawable.ic_m3_chip_close),
                          contentDescription = "",
                          modifier = Modifier.clickable {
                              navController.popBackStack()
                          }
                      )
                      Spacer(modifier = Modifier.size(8.dp))
                  }
                }
                )
        },
        floatingActionButton = {
            // Perform some action with the selected images
            FloatingActionButton(onClick = { onClick() }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    ) { it ->


        // Use a lazy vertical grid to display the images in a grid layout
        LazyVerticalGrid(

            columns = GridCells.Fixed(3),
            contentPadding = it
        ) {
            items(imageFiles.size) { index ->
                // Get the image file at the current index
                val imageFile = imageFiles[index]
                // Use a box to stack the image and the checkbox
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clickable {
                            // Toggle the selection status of the image file
                            imageFileIsSelected = !imageFileIsSelected
                            galleryScreenViewModel.updateImageFile(
                                imageFile.uri,
                                imageFileIsSelected
                            )

                        }
                ) {
                    // Use an image composable to load the image from the URI
                    Image(
                        painter = rememberAsyncImagePainter(imageFile.uri),
                        contentDescription = imageFile.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Use a checkbox composable to indicate the selection status
                    Checkbox(
                        checked = imageFile.isSelected,
                        onCheckedChange = null, // handled by the clickable modifier
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}
