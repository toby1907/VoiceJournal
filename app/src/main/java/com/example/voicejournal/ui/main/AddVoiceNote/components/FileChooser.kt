package com.example.voicejournal.ui.main.AddVoiceNote.components

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FileChooser(
    onOk: () -> Unit,
    saveSelectedUris: (List<String>) -> Unit,
    fileChooserState: Boolean
) {
    // Get the current activity from the LocalContext
    val activity = LocalContext.current as Activity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            // Check the size of the list
            when (uris.size) {
                0 -> {
                    // Handle the empty case, such as showing a message
                    Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show()
                    onOk()
                }
                1 -> {
                    // Handle the single file case, such as displaying the image

                    onOk()
                    // Use the uri to load the image from Coil or other libraries
                    saveSelectedUris(uris.map { it.toString() })
                }
                else -> {
                    // Handle the multiple files case, such as displaying the images
                    val selectedStrings = uris.map { it.toString() }
                    saveSelectedUris(selectedStrings)
                    onOk()
                    // Use the uris to load the images from Coil or other libraries
                }
            }
        }
    )

    LaunchedEffect(key1 = true) {
       if(fileChooserState) { launcher.launch("image/*") }
    }


}
