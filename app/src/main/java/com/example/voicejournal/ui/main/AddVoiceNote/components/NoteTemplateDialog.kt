package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.Data.model.NotePaperStyle
import com.example.voicejournal.ui.theme.JournalDialogIconBadge
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.Variables

data class NoteTemplatePreset(
    val name: String,
    val titleHtml: String,
    val body: String,
    val folder: String? = null
)

val DefaultNoteTemplates = listOf(
    NoteTemplatePreset(
        name = "Daily Journal",
        titleHtml = "<b>Daily Journal</b><br/>Today I am feeling...<br/><br/><b>What happened today:</b><br/>• <br/><br/><b>What I learned:</b><br/>• ",
        body = "Daily Journal",
        folder = "Personal"
    ),
    NoteTemplatePreset(
        name = "Meeting Notes",
        titleHtml = "<b>Meeting Notes</b><br/>Date: <br/>Attendees: <br/><br/><b>Agenda:</b><br/>1. <br/><br/><b>Decisions:</b><br/>• <br/><br/><b>Action items:</b><br/>☐ ",
        body = "Meeting Notes",
        folder = "Work"
    ),
    NoteTemplatePreset(
        name = "Project Retrospective",
        titleHtml = "<b>Project Retrospective</b><br/><br/><b>What went well:</b><br/>• <br/><br/><b>What did not go well:</b><br/>• <br/><br/><b>Next steps:</b><br/>☐ ",
        body = "Project Retrospective",
        folder = "Work"
    ),
    NoteTemplatePreset(
        name = "Gratitude",
        titleHtml = "<b>Gratitude</b><br/>Three things I am grateful for today:<br/>1. <br/>2. <br/>3. ",
        body = "Gratitude",
        folder = "Personal"
    )
)

@Composable
fun PaperStylePickerRow(
    selectedPaperStyle: NotePaperStyle,
    onSelectPaperStyle: (NotePaperStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NotePaperStyle.values().forEach { style ->
            val isSelected = style == selectedPaperStyle
            val borderColor = if (isSelected) {
                Variables.SchemesPrimary
            } else {
                JournalModalDefaults.SubtleBorderColor
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectPaperStyle(style) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    JournalModalDefaults.ActiveCardColor
                } else {
                    JournalModalDefaults.SoftCardColor
                },
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (style == NotePaperStyle.LINED) Color(0xFFFFFDF7) else Color(0xFFFFFFFF)
                            )
                            .border(1.dp, Color(0x26000000), RoundedCornerShape(8.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (style == NotePaperStyle.LINED) {
                                val headerY = size.height * 0.20f
                                drawLine(
                                    color = Color(0x555C85B8),
                                    start = Offset(0f, headerY),
                                    end = Offset(size.width, headerY),
                                    strokeWidth = 1.5f
                                )
                                val rowCount = 4
                                val step = (size.height - headerY - 8f) / rowCount
                                for (i in 1..rowCount) {
                                    val y = headerY + i * step
                                    drawLine(
                                        color = Color(0x455C85B8),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 1.2f
                                    )
                                }
                                val marginX = size.width * 0.22f
                                drawLine(
                                    color = Color(0x66D9534F),
                                    start = Offset(marginX, 0f),
                                    end = Offset(marginX, size.height),
                                    strokeWidth = 1.5f
                                )
                            }
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(5.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Variables.SchemesPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "${style.displayName} selected",
                                    tint = Variables.SchemesOnPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Variables.SchemesOnPrimaryContainer else Variables.SchemesOnSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = style.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Variables.SchemesOnSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NoteTemplateDialog(
    selectedPaperStyle: NotePaperStyle = NotePaperStyle.LINED,
    onSelectPaperStyle: (NotePaperStyle) -> Unit = {},
    onDismiss: () -> Unit,
    onApply: (NoteTemplatePreset) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = JournalModalDefaults.DialogShape,
        containerColor = JournalModalDefaults.ContainerColor,
        titleContentColor = JournalModalDefaults.TitleColor,
        textContentColor = JournalModalDefaults.TextColor,
        icon = {
            JournalDialogIconBadge(imageVector = Icons.Default.Description)
        },
        title = {
            Text(
                text = "Note Template & Paper",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Variables.SchemesOnPrimaryContainer
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paper Style",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Variables.SchemesOnSurfaceVariant
                    )
                )

                PaperStylePickerRow(
                    selectedPaperStyle = selectedPaperStyle,
                    onSelectPaperStyle = onSelectPaperStyle
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = JournalModalDefaults.SubtleDividerColor
                )

                Text(
                    text = "Journal Templates",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Variables.SchemesOnSurfaceVariant
                    )
                )

                DefaultNoteTemplates.forEach { template ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onApply(template) },
                        shape = RoundedCornerShape(12.dp),
                        color = JournalModalDefaults.SoftCardColor,
                        border = BorderStroke(1.dp, JournalModalDefaults.SubtleBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = template.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Variables.SchemesOnSurface
                                )
                            )
                            template.folder?.let { folder ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Variables.SchemesPrimaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = folder,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = Variables.SchemesOnPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = JournalModalDefaults.primaryButtonColors()
            ) {
                Text("Done")
            }
        }
    )
}
