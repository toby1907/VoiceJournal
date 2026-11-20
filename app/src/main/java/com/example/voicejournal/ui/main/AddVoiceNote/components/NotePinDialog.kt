package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

enum class NotePinDialogMode {
    CREATE,
    UNLOCK,
    CHANGE
}

@Composable
fun NotePinDialog(
    mode: NotePinDialogMode,
    onDismiss: () -> Unit,
    onConfirmCreate: (String) -> Unit = {},
    onConfirmUnlock: (String) -> Boolean = { false },
    onConfirmChange: (String, String) -> Boolean = { _, _ -> false },
    onRemoveLock: (() -> Unit)? = null
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val pinTransformation = PasswordVisualTransformation()
    val pinKeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (mode) {
                    NotePinDialogMode.CREATE -> "Lock note with PIN"
                    NotePinDialogMode.UNLOCK -> "Unlock note"
                    NotePinDialogMode.CHANGE -> "Change note PIN"
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (mode) {
                    NotePinDialogMode.CREATE -> {
                        PinField(
                            value = newPin,
                            onValueChange = { newPin = it },
                            label = "New PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                        PinField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it },
                            label = "Confirm PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                    }

                    NotePinDialogMode.UNLOCK -> {
                        PinField(
                            value = currentPin,
                            onValueChange = { currentPin = it },
                            label = "PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                    }

                    NotePinDialogMode.CHANGE -> {
                        PinField(
                            value = currentPin,
                            onValueChange = { currentPin = it },
                            label = "Current PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                        PinField(
                            value = newPin,
                            onValueChange = { newPin = it },
                            label = "New PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                        PinField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it },
                            label = "Confirm new PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                    }
                }

                errorText?.let {
                    Text(text = it)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (mode) {
                        NotePinDialogMode.CREATE -> {
                            if (newPin.length < 4) {
                                errorText = "PIN must be at least 4 digits."
                            } else if (newPin != confirmPin) {
                                errorText = "PIN confirmation does not match."
                            } else {
                                onConfirmCreate(newPin)
                                onDismiss()
                            }
                        }

                        NotePinDialogMode.UNLOCK -> {
                            if (onConfirmUnlock(currentPin)) {
                                onDismiss()
                            } else {
                                errorText = "Incorrect PIN."
                            }
                        }

                        NotePinDialogMode.CHANGE -> {
                            if (newPin.length < 4) {
                                errorText = "PIN must be at least 4 digits."
                            } else if (newPin != confirmPin) {
                                errorText = "PIN confirmation does not match."
                            } else if (onConfirmChange(currentPin, newPin)) {
                                onDismiss()
                            } else {
                                errorText = "Current PIN is incorrect."
                            }
                        }
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Column {
                onRemoveLock?.let {
                    TextButton(onClick = {
                        it()
                        onDismiss()
                    }) {
                        Text("Remove lock")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun PinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    transformation: VisualTransformation,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }.take(8)) },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        visualTransformation = transformation,
        keyboardOptions = keyboardOptions,
        singleLine = true
    )
}
