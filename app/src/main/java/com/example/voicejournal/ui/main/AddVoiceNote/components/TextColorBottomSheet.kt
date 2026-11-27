package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.ui.theme.JournalModalDefaults
import com.example.voicejournal.ui.theme.JournalSheetDragHandle
import com.example.voicejournal.ui.theme.JournalSheetHeader
import com.example.voicejournal.ui.theme.Variables

data class TextColorOption(
    val name: String,
    val color: Color
)

val DefaultTextColorPalette: List<TextColorOption> = listOf(
    TextColorOption("Charcoal", Color(0xFF1F1F1F)),
    TextColorOption("Slate Gray", Color(0xFF616161)),
    TextColorOption("Crimson Red", Color(0xFFD32F2F)),
    TextColorOption("Burnt Orange", Color(0xFFE65100)),
    TextColorOption("Amber Gold", Color(0xFFC68400)),
    TextColorOption("Warm Brown", Color(0xFF6D4C41)),
    TextColorOption("Forest Green", Color(0xFF2E7D32)),
    TextColorOption("Teal", Color(0xFF00796B)),
    TextColorOption("Royal Blue", Color(0xFF1565C0)),
    TextColorOption("Indigo", Color(0xFF3949AB)),
    TextColorOption("Deep Purple", Color(0xFF7B1FA2)),
    TextColorOption("Magenta Rose", Color(0xFFC2185B))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextColorBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    selectedColor: Color,
    onColorSelected: (Color?) -> Unit
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
        TextColorPaletteContent(
            selectedColor = selectedColor,
            onColorSelected = onColorSelected
        )
    }
}

@Composable
fun TextColorPaletteContent(
    selectedColor: Color,
    onColorSelected: (Color?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDefaultSelected = selectedColor == Color.Unspecified
    val selectedArgb = if (isDefaultSelected) null else selectedColor.toArgb()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        JournalSheetHeader(
            title = "Text Color",
            subtitle = "Applies to highlighted or next typed words",
            trailing = {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onColorSelected(null) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDefaultSelected) {
                        Variables.SchemesPrimaryContainer
                    } else {
                        JournalModalDefaults.SoftCardColor
                    },
                    border = BorderStroke(
                        width = if (isDefaultSelected) 1.5.dp else 1.dp,
                        color = if (isDefaultSelected) {
                            Variables.SchemesPrimary
                        } else {
                            JournalModalDefaults.SubtleBorderColor
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isDefaultSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Default color selected",
                                tint = Variables.SchemesPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Default",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDefaultSelected) {
                                    Variables.SchemesOnPrimaryContainer
                                } else {
                                    Variables.SchemesOnSurface
                                }
                            )
                        )
                    }
                }
            }
        )

        // Live color preview chip styled like warm notebook paper
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = JournalModalDefaults.CardShape,
            color = JournalModalDefaults.SoftCardColor,
            border = BorderStroke(1.dp, JournalModalDefaults.SubtleBorderColor)
        ) {
            Text(
                text = "Sample notebook text preview",
                color = if (isDefaultSelected) Variables.SchemesOnSurface else selectedColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }

        // 2 rows x 6 color swatches
        val rows = DefaultTextColorPalette.chunked(6)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            rows.forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowColors.forEach { option ->
                        val isSelected = selectedArgb != null && option.color.toArgb() == selectedArgb
                        val checkTint = if (option.color.luminance() > 0.5f) Color.Black else Color.White
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(option.color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) {
                                        Variables.SchemesPrimary
                                    } else {
                                        Color(0x33000000)
                                    },
                                    shape = CircleShape
                                )
                                .semantics { contentDescription = "Text Color ${option.name}" }
                                .clickable { onColorSelected(option.color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "${option.name} selected",
                                    tint = checkTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
