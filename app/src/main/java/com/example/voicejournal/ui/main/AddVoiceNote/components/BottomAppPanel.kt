package com.example.voicejournal.ui.main.AddVoiceNote.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.R
import com.example.voicejournal.ui.theme.Variables
import com.example.voicejournal.ui.common.richeditor.HeadingStyle
import com.example.voicejournal.ui.common.richeditor.RichTextState

@Composable
fun BottomAppPanel(
    cardVisibleState: () -> Unit,
    imageUri: List<Uri>?,
    imageSheetVisibleState: () -> Unit,
    tagVisiblestate: () -> Unit,
    boldButtonState: () -> Unit = {},
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onTextColorClick: () -> Unit = {},
    onChecklistClick: () -> Unit = {},
    onAddTextBoxClick: () -> Unit = {},
    onPageBreakClick: () -> Unit = {},
    onPreviousPageClick: () -> Unit = {},
    onNextPageClick: () -> Unit = {},
    state: RichTextState,
    alignStyleSheetVisiblity: () -> Unit,
    drawingEnabled: Boolean = false,
    onToggleDrawing: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {}
) {
    var boldSelected by rememberSaveable { mutableStateOf(false) }
    var italicSelected by rememberSaveable { mutableStateOf(false) }
    var strikethroughSelected by rememberSaveable { mutableStateOf(false) }
    var underlineSelected by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = Variables.SchemesSurface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 0: Attachment / Image
            IconButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = {}
            ) {
                if (imageUri?.isNotEmpty() == true) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri[0]),
                            contentDescription = "Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(32.dp)
                                .clickable {
                                    imageSheetVisibleState()
                                }
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.attach_file),
                        modifier = Modifier.clickable {
                            cardVisibleState()
                        },
                        contentDescription = "Attachment",
                        tint = Variables.SchemesOnSurface
                    )
                }
            }

            // 1: Tag
            IconButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = tagVisiblestate
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.tag),
                    contentDescription = "Tag",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 2: Heading Style Selector (Aa ▼)
            var headingDropdownExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Variables.SchemesPrimaryContainer.copy(alpha = 0.5f))
                        .clickable { headingDropdownExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.currentHeadingStyle.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Variables.SchemesOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Heading Dropdown",
                        tint = Variables.SchemesOnSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = headingDropdownExpanded,
                    onDismissRequest = { headingDropdownExpanded = false }
                ) {
                    HeadingStyle.values().forEach { heading ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = heading.label,
                                    fontSize = heading.sizeSp.sp,
                                    fontWeight = if (heading.isBold) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                state.setHeadingStyle(heading)
                                headingDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 3: Font Size Selector (16 ▼)
            var fontSizeDropdownExpanded by remember { mutableStateOf(false) }
            val fontSizes = listOf(12, 14, 15, 16, 17, 18, 20, 24, 28, 32)
            Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Variables.SchemesPrimaryContainer.copy(alpha = 0.5f))
                        .clickable { fontSizeDropdownExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.currentFontSizeSp}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Variables.SchemesOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Font Size Dropdown",
                        tint = Variables.SchemesOnSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = fontSizeDropdownExpanded,
                    onDismissRequest = { fontSizeDropdownExpanded = false }
                ) {
                    fontSizes.forEach { size ->
                        DropdownMenuItem(
                            text = { Text("$size sp", fontSize = 14.sp) },
                            onClick = {
                                state.setFontSize(size)
                                fontSizeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 4: Checklist button
            IconButton(
                modifier = Modifier.padding(horizontal = 2.dp),
                onClick = onChecklistClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_checklist_24),
                    contentDescription = "Checklist",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 5: Text Box Tool (T)
            IconButton(
                modifier = Modifier.padding(horizontal = 2.dp),
                onClick = onAddTextBoxClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_textbox_24),
                    contentDescription = "Floating Text Box",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 6: Alignment / Body style
            IconButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = alignStyleSheetVisiblity
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.format_align_left),
                    contentDescription = "Body",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 3: Bold
            val currentSpanStyle = state.currentSpanStyle
            val isBold = currentSpanStyle.fontWeight == FontWeight.Bold
            ControlWrapper(
                selected = isBold,
                onChangeClick = { boldSelected = it },
                onClick = onBoldClick,
                unselectedColor = Color.Transparent
            ) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Bold Control",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 4: Italic
            val isItalic = currentSpanStyle.fontStyle == FontStyle.Italic
            ControlWrapper(
                selected = isItalic,
                onChangeClick = { italicSelected = it },
                onClick = onItalicClick,
                unselectedColor = Color.Transparent
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.format_italic),
                    contentDescription = "Italic Control",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 5: Strikethrough
            val isStrikethrough = currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true
            ControlWrapper(
                selected = isStrikethrough,
                onChangeClick = { strikethroughSelected = it },
                onClick = onStrikethroughClick,
                unselectedColor = Color.Transparent
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.strikethrough),
                    contentDescription = "Strikethrough Control",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 6: Underline
            val isUnderline = currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true
            ControlWrapper(
                selected = isUnderline,
                onChangeClick = { underlineSelected = it },
                onClick = onUnderlineClick,
                unselectedColor = Color.Transparent
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.format_underlined),
                    contentDescription = "Underline Control",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 6b: Text Color (with live color indicator bar)
            val activeTextColor = currentSpanStyle.color
            val hasCustomTextColor = activeTextColor != Color.Unspecified
            val indicatorColor = if (hasCustomTextColor) activeTextColor else Variables.SchemesOnSurface
            ControlWrapper(
                selected = hasCustomTextColor,
                selectedColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                onChangeClick = { },
                onClick = onTextColorClick,
                unselectedColor = Color.Transparent
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics { contentDescription = "Text Color Control" }
                ) {
                    Text(
                        text = "A",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Variables.SchemesOnSurface
                    )
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(indicatorColor)
                    )
                }
            }

            // 7: Drawing / Pen Mode Toggle
            ControlWrapper(
                selected = drawingEnabled,
                onChangeClick = { },
                onClick = onToggleDrawing,
                unselectedColor = Color.Transparent
            ) {
                Icon(
                    imageVector = Icons.Default.Draw,
                    contentDescription = "Drawing Mode",
                    tint = Variables.SchemesOnSurface
                )
            }

            // 8: Undo
            IconButton(
                onClick = { state.undo() },
                enabled = state.canUndo
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Undo",
                    tint = if (state.canUndo) Variables.SchemesOnSurface else Variables.SchemesOnSurface.copy(alpha = 0.38f)
                )
            }

            // 9: Redo
            IconButton(
                onClick = { state.redo() },
                enabled = state.canRedo
            ) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "Redo",
                    tint = if (state.canRedo) Variables.SchemesOnSurface else Variables.SchemesOnSurface.copy(alpha = 0.38f)
                )
            }
        }

        // Double Arrow (>>): Opens More Options
        IconButton(
            onClick = onMoreOptionsClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.double_arrow),
                contentDescription = "More Options",
                tint = Variables.SchemesOnSurface
            )
        }
    }
}

// A data class to hold the icon and label information
data class BottomMenuItem(val label: String, val icon: Painter)
