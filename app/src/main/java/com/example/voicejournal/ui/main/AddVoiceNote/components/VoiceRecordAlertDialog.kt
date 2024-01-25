package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun VoiceRecordAlertDialog(
    openDialog: MutableState<Boolean>,
    onDismissRequest: () -> Unit,
    confirmRequest: () -> Unit,
    dismissRequest: ()  -> Unit

){

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                onDismissRequest()

            },
            text = {
                Text(text = "You are About to remove this audio file permanently")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRequest()

                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dismissRequest()

                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

}
