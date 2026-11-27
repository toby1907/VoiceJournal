package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel
import com.example.voicejournal.ui.theme.JournalDialogIconBadge
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.Variables

@Composable
fun TagDialog(
    addVoiceNoteViewModel: AddVoiceNoteViewModel,
    onTagChecked: (String, Boolean) -> Unit,
    onCancel: () -> Unit,
    onOk: () -> Unit
) {
    val tags = addVoiceNoteViewModel.tags.value
    val tagInput = remember { mutableStateOf("") }
    val filteredTags = filterTags(tagInput.value, tags).orEmpty()

    fun addTag() {
        val newTag = tagInput.value.trim()
        if (newTag.isNotEmpty()) {
            onTagChecked(newTag, true)
            tagInput.value = ""
        }
    }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = JournalModalDefaults.DialogShape,
            color = JournalModalDefaults.ContainerColor,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, JournalModalDefaults.SubtleBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JournalDialogIconBadge(imageVector = Icons.Default.Label)
                    Column {
                        Text(
                            text = "Manage Tags",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Variables.SchemesOnPrimaryContainer
                            )
                        )
                        Text(
                            text = "Search or add tags for this note",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Variables.SchemesOnSurfaceVariant
                            )
                        )
                    }
                }

                // Input row to search or add a tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tagInput.value,
                        onValueChange = { tagInput.value = it },
                        label = { Text("Tags") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = JournalModalDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { addTag() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Variables.SchemesPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tag",
                            tint = Variables.SchemesOnPrimary
                        )
                    }
                }

                if (filteredTags.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tagInput.value.isBlank()) {
                                "No tags yet. Type a name and tap + to add."
                            } else {
                                "Tap + to create \"${tagInput.value.trim()}\""
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Variables.SchemesOnSurfaceVariant
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredTags) { tag ->
                            val (checkedState, onStateChange) = remember { mutableStateOf(true) }
                            val rowBg = if (tag.isChecked) {
                                JournalModalDefaults.ActiveCardColor
                            } else {
                                JournalModalDefaults.SoftCardColor
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(rowBg)
                                    .toggleable(
                                        value = tag.isChecked,
                                        onValueChange = {
                                            onStateChange(!checkedState)
                                            onTagChecked(tag.name, !tag.isChecked)
                                        },
                                        role = Role.Checkbox
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = tag.isChecked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Variables.SchemesPrimary,
                                        uncheckedColor = Variables.SchemesPrimary.copy(alpha = 0.6f),
                                        checkmarkColor = Variables.SchemesOnPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (tag.isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (tag.isChecked) Variables.SchemesOnPrimaryContainer else Variables.SchemesOnSurface
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = JournalModalDefaults.SubtleDividerColor)

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = "Cancel",
                            color = Variables.SchemesOnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOk,
                        shape = RoundedCornerShape(12.dp),
                        colors = JournalModalDefaults.primaryButtonColors()
                    ) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}

// A function that takes the input value and the list of tags as parameters
fun filterTags(input: String, tags: List<Tag>?): List<Tag>? {
    if (input.isEmpty()) return tags
    return tags?.filter { tag -> tag.name.contains(input, ignoreCase = true) }
}

data class Tag(
    val name: String,
    var isChecked: Boolean
)
