package com.example.voicejournal.ui.main.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.ui.main.mainScreen.VoiceNoteViewModel

@Composable
fun MediaScreen(navController: NavController) {
val viewModel:VoiceNoteViewModel = hiltViewModel()
val imageFiles = viewModel.state.value.notes.flatMap { item->
    item.imageUris?.filter { it.isNotEmpty() }?.map { uri->
        item.id?.let { JournalMedia(uri=uri, id = it) }
    } ?: emptyList()
}
    val journals = viewModel.state.value.notes


    LazyVerticalGrid(

        columns = GridCells.Fixed(3),
    ) {
        items(imageFiles.size) { index ->
            // Get the image file at the current index
            val imageFile = imageFiles[index]
          //  var journalIndex = 0
            val journal = journals.filter{  it ->
               // journalIndex=index2
                it.id == imageFile?.id
            }
            // Use a box to stack the image and the checkbox
            if (imageFile != null) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clickable {
                            navController.navigate(
                                "preview" + "?noteId=${journal[0].id}&noteColor=${journal[0].color}&noteIndex=${
                                   journals.indexOf(journal.first())
                                }"
                            )
                        }
                ) {
                    // Use an image composable to load the image from the URI

                    Image(
                        painter = rememberAsyncImagePainter(imageFile.uri),
                        contentDescription = imageFile.uri,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                }
            }
        }
    }
}
data class JournalMedia(val uri:String,val id:Int)