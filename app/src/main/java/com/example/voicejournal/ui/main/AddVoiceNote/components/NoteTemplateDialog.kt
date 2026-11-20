package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class NoteTemplatePreset(
    val name: String,
    val titleHtml: String,
    val body: String,
    val folder: String? = null
)

val DefaultNoteTemplates = listOf(
    NoteTemplatePreset(
        name = "Daily Journal",
        titleHtml = "<h2>Daily Journal</h2>",
        body = "Today I am feeling...\n\nWhat happened today:\n- \n\nWhat I learned:\n- ",
        folder = "Personal"
    ),
    NoteTemplatePreset(
        name = "Meeting Notes",
        titleHtml = "<h2>Meeting Notes</h2>",
        body = "Date:\nAttendees:\n\nAgenda:\n1. \n\nDecisions:\n- \n\nAction items:\n- ",
        folder = "Work"
    ),
    NoteTemplatePreset(
        name = "Project Retrospective",
        titleHtml = "<h2>Project Retrospective</h2>",
        body = "What went well:\n- \n\nWhat did not go well:\n- \n\nNext steps:\n- ",
        folder = "Work"
    ),
    NoteTemplatePreset(
        name = "Gratitude",
        titleHtml = "<h2>Gratitude</h2>",
        body = "Three things I am grateful for today:\n1. \n2. \n3. ",
        folder = "Personal"
    )
)

@Composable
fun NoteTemplateDialog(
    onDismiss: () -> Unit,
    onApply: (NoteTemplatePreset) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose template") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DefaultNoteTemplates.forEach { template ->
                    TextButton(
                        onClick = { onApply(template) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(template.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
