package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.voicejournal.ui.theme.JournalDialogIconBadge
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.Variables

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
        shape = JournalModalDefaults.DialogShape,
        containerColor = JournalModalDefaults.ContainerColor,
        titleContentColor = JournalModalDefaults.TitleColor,
        textContentColor = JournalModalDefaults.TextColor,
        icon = {
            JournalDialogIconBadge(imageVector = Icons.Default.Lock)
        },
        title = {
            Text(
                text = when (mode) {
                    NotePinDialogMode.CREATE -> "Lock note with PIN"
                    NotePinDialogMode.UNLOCK -> "Unlock note"
                    NotePinDialogMode.CHANGE -> "Change note PIN"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Variables.SchemesOnPrimaryContainer
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (mode) {
                    NotePinDialogMode.CREATE -> {
                        PinField(
                            value = newPin,
                            onValueChange = { newPin = it; errorText = null },
                            label = "New PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                        PinField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it; errorText = null },
                            label = "Confirm PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                    }

                    NotePinDialogMode.UNLOCK -> {
                        PinField(
                            value = currentPin,
                            onValueChange = { currentPin = it; errorText = null },
                            label = "PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                    }

                    NotePinDialogMode.CHANGE -> {
                        PinField(
                            value = currentPin,
                            onValueChange = { currentPin = it; errorText = null },
                            label = "Current PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                        PinField(
                            value = newPin,
                            onValueChange = { newPin = it; errorText = null },
                            label = "New PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                        PinField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it; errorText = null },
                            label = "Confirm new PIN",
                            transformation = pinTransformation,
                            keyboardOptions = pinKeyboardOptions
                        )
                    }
                }

                errorText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Variables.SchemesError,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
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
                },
                shape = RoundedCornerShape(12.dp),
                colors = JournalModalDefaults.primaryButtonColors()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                onRemoveLock?.let {
                    TextButton(onClick = {
                        it()
                        onDismiss()
                    }) {
                        Text(
                            text = "Remove lock",
                            color = Variables.SchemesError
                        )
                    }
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
        shape = RoundedCornerShape(12.dp),
        colors = JournalModalDefaults.outlinedTextFieldColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        visualTransformation = transformation,
        keyboardOptions = keyboardOptions,
        singleLine = true
    )
}
