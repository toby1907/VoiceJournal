package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel

@Composable
fun TagDialog(
   addVoiceNoteViewModel:AddVoiceNoteViewModel,
    onTagChecked: (String, Boolean) -> Unit, // The function to call when a tag is checked or unchecked
    onCancel: () -> Unit, // The function to call when the cancel button is clicked
    onOk: () -> Unit // The function to call when the ok button is clicked
) {
    val tags = addVoiceNoteViewModel.tags.value
    // A state variable to store the text entered in the edit text field
    val tagInput = remember { mutableStateOf("") }

        val filteredTags =  filterTags(tagInput.value, tags)


    // A function to add a new tag to the list
    fun addTag() {
        val newTag = tagInput.value.trim()
        if (newTag.isNotEmpty()) {
            onTagChecked(newTag, true) // Add the new tag and check it
            tagInput.value = "" // Clear the input field
        }
    }


    // A dialog with a custom content
    Dialog(onDismissRequest = onCancel) {
        // Use a card as the content of the dialog
        Card(
            modifier = Modifier
                .padding(16.dp)
                .size(width = 280.dp, height = 434.dp)

        ) {
            // Add your UI elements here
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // A row to hold the edit text field, the vertical divider, and the icon button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // An edit text field with a hint 'Tags'

                    OutlinedTextField(
                        value = tagInput.value,
                        onValueChange = { tagInput.value = it },
                        label = { Text("Tags") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )

                    // A small vertical divider
                    Spacer(modifier = Modifier.width(8.dp))
                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // An icon button with an add icon
                    IconButton(onClick = { addTag() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tag"
                        )
                    }
                }


                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {

                            items(filteredTags!!) { tag ->
                                // A row to hold the checkbox and the tag text
                                val (checkedState, onStateChange) = remember { mutableStateOf(true) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .toggleable(
                                            value = tag.isChecked
                                            //  checkedState
                                            ,
                                            onValueChange = {

                                                onStateChange(!checkedState)
                                                onTagChecked(tag.name, !tag.isChecked)
                                                //onTagChecked(tag.name, it)

                                            },
                                            role = Role.Checkbox
                                        ) // Toggle the checkbox when the row is clicked
                                ) {
                                    // A checkbox to indicate if the tag is selected or not
                                    Checkbox(
                                        checked = tag.isChecked,
                                        onCheckedChange = null
                                    )

                                    // A spacer to create some gap between the checkbox and the text
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // A text to show the tag name
                                    Text(text = tag.name)
                                }
                            }



                }

                // A horizontal divider to separate the content and the footer
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )

                // A row to hold the action buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // A button to cancel the dialog
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }

                    // A button to confirm the dialog
                    TextButton(onClick = onOk) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}

// A function that takes the input value and the list of tags as parameters
fun filterTags(input: String, tags: List<Tag>?): List<Tag>? {
    // If the input is empty, return the original list of tags
    if (input.isEmpty()) return tags
    // Otherwise, return a list of tags that contain the input as a substring, ignoring case

    return tags?.filter { tag -> tag.name.contains(input, ignoreCase = true) }
}

data class Tag(
    val name: String,
    var isChecked: Boolean
)
