package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.Data.model.NotePaperStyle
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.JournalSheetDragHandle
import com.example.voicejournal.ui.theme.JournalSheetHeader
import com.example.voicejournal.ui.theme.Variables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    currentPageIndex: Int,
    pageCount: Int,
    onPreviousPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    onPageBreakClick: () -> Unit,
    onRemovePageBreakClick: () -> Unit,
    canRemovePageBreak: Boolean,
    selectedPaperStyle: NotePaperStyle = NotePaperStyle.LINED,
    onSelectPaperStyle: (NotePaperStyle) -> Unit = {},
    onTemplateClick: () -> Unit,
    onFolderClick: () -> Unit,
    folderName: String,
    onLockClick: () -> Unit,
    isLocked: Boolean,
    onChecklistClick: () -> Unit,
    onTranscribeClick: () -> Unit,
    onSyncBlocksClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = JournalModalDefaults.SheetShape,
        containerColor = JournalModalDefaults.ContainerColor,
        contentColor = JournalModalDefaults.TextColor,
        scrimColor = JournalModalDefaults.ScrimColor,
        dragHandle = { JournalSheetDragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                JournalSheetHeader(
                    title = "Note & Page Tools",
                    subtitle = "Customize paper, pages, and note settings"
                )
            }

            // Paper Style Picker (Lined Notebook vs Plain Paper)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Paper Template",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Variables.SchemesOnSurfaceVariant
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    PaperStylePickerRow(
                        selectedPaperStyle = selectedPaperStyle,
                        onSelectPaperStyle = onSelectPaperStyle
                    )
                }
            }

            // Pages Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Variables.SchemesPrimaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, JournalModalDefaults.SubtleBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Page Navigation",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Variables.SchemesOnPrimaryContainer
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onPreviousPageClick,
                                enabled = currentPageIndex > 0
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous page",
                                    tint = if (currentPageIndex > 0) {
                                        Variables.SchemesOnPrimaryContainer
                                    } else {
                                        Variables.SchemesOnPrimaryContainer.copy(alpha = 0.35f)
                                    }
                                )
                            }

                            Text(
                                text = "Page ${currentPageIndex + 1} of $pageCount",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Variables.SchemesOnPrimaryContainer
                                )
                            )

                            IconButton(
                                onClick = onNextPageClick,
                                enabled = currentPageIndex < pageCount - 1
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next page",
                                    tint = if (currentPageIndex < pageCount - 1) {
                                        Variables.SchemesOnPrimaryContainer
                                    } else {
                                        Variables.SchemesOnPrimaryContainer.copy(alpha = 0.35f)
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPageBreakClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Variables.SchemesPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Variables.SchemesOnPrimaryContainer
                                )
                            ) {
                                Text(text = "+ Page Break", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = onRemovePageBreakClick,
                                enabled = canRemovePageBreak,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (canRemovePageBreak) {
                                        Variables.SchemesPrimary
                                    } else {
                                        Variables.SchemesPrimary.copy(alpha = 0.25f)
                                    }
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Variables.SchemesOnPrimaryContainer,
                                    disabledContentColor = Variables.SchemesOnPrimaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(text = "Remove Break", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Variables.SchemesOnSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = JournalModalDefaults.SoftCardColor,
                    border = BorderStroke(1.dp, JournalModalDefaults.SubtleBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        OptionRow(
                            title = "Templates",
                            subtitle = "Paper style & journal templates",
                            icon = painterResource(id = R.drawable.note),
                            onClick = onTemplateClick
                        )

                        HorizontalDivider(color = JournalModalDefaults.SubtleDividerColor)

                        OptionRow(
                            title = if (folderName.isNotBlank()) "Folder: $folderName" else "Folder",
                            subtitle = if (folderName.isNotBlank()) "Assigned folder" else "Assign note to a folder",
                            imageVector = Icons.Default.Folder,
                            onClick = onFolderClick
                        )

                        HorizontalDivider(color = JournalModalDefaults.SubtleDividerColor)

                        OptionRow(
                            title = if (isLocked) "Change Lock PIN" else "Lock Note",
                            subtitle = if (isLocked) "Note is protected with PIN" else "Protect this note with PIN",
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            onClick = onLockClick
                        )

                        HorizontalDivider(color = JournalModalDefaults.SubtleDividerColor)

                        OptionRow(
                            title = "Add Checklist Block",
                            subtitle = "Insert interactive to-do items",
                            imageVector = Icons.Default.CheckBox,
                            onClick = onChecklistClick
                        )

                        HorizontalDivider(color = JournalModalDefaults.SubtleDividerColor)

                        OptionRow(
                            title = "Transcribe Speech to Text",
                            subtitle = "Convert voice to text in note",
                            icon = painterResource(id = R.drawable.baseline_mic_24),
                            onClick = onTranscribeClick
                        )

                        HorizontalDivider(color = JournalModalDefaults.SubtleDividerColor)

                        OptionRow(
                            title = "Sync Blocks",
                            subtitle = "Synchronize blocks from editor text",
                            imageVector = Icons.Default.Sync,
                            onClick = onSyncBlocksClick
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OptionRow(
    title: String,
    subtitle: String,
    imageVector: ImageVector? = null,
    icon: androidx.compose.ui.graphics.painter.Painter? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Variables.SchemesPrimaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (imageVector != null) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = title,
                        modifier = Modifier.size(20.dp),
                        tint = Variables.SchemesPrimary
                    )
                } else if (icon != null) {
                    Icon(
                        painter = icon,
                        contentDescription = title,
                        modifier = Modifier.size(20.dp),
                        tint = Variables.SchemesPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Variables.SchemesOnSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Variables.SchemesOnSurfaceVariant
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Variables.SchemesOnSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.size(18.dp)
        )
    }
}
