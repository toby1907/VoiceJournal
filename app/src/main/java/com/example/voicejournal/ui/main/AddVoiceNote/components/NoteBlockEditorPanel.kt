package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.ui.theme.Variables

@Composable
fun NoteBlockEditorPanel(
    blocks: List<NoteBlock>,
    onSyncCurrent: () -> Unit,
    onAddTextSnapshot: () -> Unit,
    onAddAudioBlock: () -> Unit,
    onAddImageBlocks: () -> Unit,
    onAddChecklistBlock: () -> Unit,
    onTranscribeAudio: () -> Unit,
    onToggleAudioPlayback: (String?) -> Unit,
    isAudioPlaying: (String?) -> Boolean,
    onToggleChecklistChecked: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = Variables.SchemesSurface.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Blocks", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onSyncCurrent) { Text("Sync") }
            OutlinedButton(onClick = onAddTextSnapshot) { Text("+ Text") }
            OutlinedButton(onClick = onAddAudioBlock) { Text("+ Audio") }
            OutlinedButton(onClick = onAddImageBlocks) { Text("+ Images") }
            OutlinedButton(onClick = onAddChecklistBlock) { Text("+ Checklist") }
            OutlinedButton(onClick = onTranscribeAudio) { Text("Transcribe") }
        }

        if (blocks.isEmpty()) {
            Text(
                text = "No blocks yet. Use Sync to generate blocks from current note.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            blocks.forEachIndexed { index, block ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Variables.SchemesSurface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${index + 1}. ${blockTypeLabel(block, maxPreview = 36)}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row {
                       if (block.type == NoteBlockType.AUDIO.name) {
                           val playing = isAudioPlaying(block.mediaUri)
                           IconButton(onClick = { onToggleAudioPlayback(block.mediaUri) }) {
                               Icon(
                                   imageVector = if (playing) Icons.Default.Stop else Icons.Default.PlayArrow,
                                   contentDescription = if (playing) "Stop audio block" else "Play audio block"
                               )
                           }
                       }
                       if (block.type == NoteBlockType.CHECKLIST.name) {
                           val checked = block.isChecked == true
                           IconButton(onClick = { onToggleChecklistChecked(block.id) }) {
                               Icon(
                                   imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                   contentDescription = if (checked) "Uncheck checklist item" else "Check checklist item"
                               )
                           }
                       }
                       IconButton(onClick = { onMoveUp(block.id) }, enabled = index > 0) {
                           Icon(Icons.Default.ArrowUpward, contentDescription = "Move block up")
                       }
                        IconButton(onClick = { onMoveDown(block.id) }, enabled = index < blocks.lastIndex) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move block down")
                        }
                        IconButton(onClick = { onRemove(block.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove block")
                        }
                    }
                }
            }
        }
    }
}

private fun blockTypeLabel(block: NoteBlock, maxPreview: Int): String {
    val preview = block.transcript?.takeIf { it.isNotBlank() }?.let {
        if (it.length > maxPreview) "${it.take(maxPreview)}..." else it
    }
    return when (block.type) {
        NoteBlockType.TEXT.name -> "Text"
        NoteBlockType.AUDIO.name -> if (preview != null) "Audio ($preview)" else "Audio"
        NoteBlockType.IMAGE.name -> "Image"
        NoteBlockType.CHECKLIST.name -> {
            val status = if (block.isChecked == true) "[x]" else "[ ]"
            val text = block.text?.takeIf { it.isNotBlank() } ?: "Checklist item"
            "Checklist $status $text"
        }
        else -> block.type
    }
}
