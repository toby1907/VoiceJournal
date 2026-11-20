package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun NoteFolderDialog(
    initialFolder: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onClear: () -> Unit
) {
    var input by remember(initialFolder) { mutableStateOf(initialFolder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Folder") },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(40) },
                    label = { Text("Folder name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(input.trim())
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = {
                    onClear()
                    onDismiss()
                }) {
                    Text("Clear folder")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
