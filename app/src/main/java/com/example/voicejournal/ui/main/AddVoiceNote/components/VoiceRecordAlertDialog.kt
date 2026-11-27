package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voicejournal.ui.theme.JournalDialogIconBadge
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.Variables

@Composable
fun VoiceRecordAlertDialog(
    openDialog: MutableState<Boolean>,
    onDismissRequest: () -> Unit,
    confirmRequest: () -> Unit,
    dismissRequest: () -> Unit
) {
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                onDismissRequest()
            },
            shape = JournalModalDefaults.DialogShape,
            containerColor = JournalModalDefaults.ContainerColor,
            titleContentColor = JournalModalDefaults.TitleColor,
            textContentColor = JournalModalDefaults.TextColor,
            icon = {
                JournalDialogIconBadge(
                    imageVector = Icons.Default.DeleteOutline,
                    isDestructive = true
                )
            },
            title = {
                Text(
                    text = "Remove Voice Recording?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Variables.SchemesOnPrimaryContainer
                    )
                )
            },
            text = {
                Text(
                    text = "You are about to remove this audio recording permanently.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Variables.SchemesOnSurfaceVariant
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmRequest()
                        openDialog.value = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = JournalModalDefaults.destructiveButtonColors()
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dismissRequest()
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = Variables.SchemesOnSurfaceVariant
                    )
                }
            }
        )
    }
}
