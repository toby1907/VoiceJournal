package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.voicejournal.ui.theme.JournalDialogIconBadge
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.Variables

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
        shape = JournalModalDefaults.DialogShape,
        containerColor = JournalModalDefaults.ContainerColor,
        titleContentColor = JournalModalDefaults.TitleColor,
        textContentColor = JournalModalDefaults.TextColor,
        icon = {
            JournalDialogIconBadge(imageVector = Icons.Default.Folder)
        },
        title = {
            Text(
                text = "Folder",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Variables.SchemesOnPrimaryContainer
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Organize this note into a folder.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Variables.SchemesOnSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(40) },
                    label = { Text("Folder name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = JournalModalDefaults.outlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(input.trim())
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = JournalModalDefaults.primaryButtonColors()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = {
                    onClear()
                    onDismiss()
                }) {
                    Text(
                        text = "Clear folder",
                        color = Variables.SchemesError
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = Variables.SchemesOnSurfaceVariant
                    )
                }
            }
        }
    )
}
