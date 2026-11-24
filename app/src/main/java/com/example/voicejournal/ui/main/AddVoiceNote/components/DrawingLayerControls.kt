package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.ui.theme.Variables

@Composable
fun DrawingToolbar(
    selectedTool: CanvasDrawingTool,
    selectedColorArgb: Long,
    selectedStrokeWidth: Float,
    onToolSelected: (CanvasDrawingTool) -> Unit,
    onColorSelected: (Long) -> Unit,
    onStrokeWidthSelected: (Float) -> Unit,
    onDoneClick: () -> Unit,
    onUndoStroke: () -> Unit = {}
) {
    val colorPresets = listOf(
        0xFF1A73E8L, // Blue
        0xFF202124L, // Black
        0xFFD93025L, // Red
        0xFF188038L, // Green
        0xFFFFD54FL  // Yellow
    )

    val strokePresets = listOf(
        4f to "Thin",
        8f to "Med",
        14f to "Bold"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        color = Variables.SchemesSurface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tools: Pen
                ToolIcon(
                    selected = selectedTool == CanvasDrawingTool.PEN,
                    onClick = { onToolSelected(CanvasDrawingTool.PEN) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = "Pen",
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedTool == CanvasDrawingTool.PEN) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                        )
                    }
                )

                // Highlighter
                ToolIcon(
                    selected = selectedTool == CanvasDrawingTool.HIGHLIGHTER,
                    onClick = { onToolSelected(CanvasDrawingTool.HIGHLIGHTER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BorderColor,
                            contentDescription = "Highlighter",
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedTool == CanvasDrawingTool.HIGHLIGHTER) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                        )
                    }
                )

                // Eraser
                ToolIcon(
                    selected = selectedTool == CanvasDrawingTool.ERASER,
                    onClick = { onToolSelected(CanvasDrawingTool.ERASER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoFixNormal,
                            contentDescription = "Eraser",
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedTool == CanvasDrawingTool.ERASER) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                        )
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color.Gray.copy(alpha = 0.35f))
                )
                Spacer(modifier = Modifier.width(4.dp))

                // Colors
                colorPresets.forEach { color ->
                    val isSelected = selectedColorArgb == color
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(color.toInt()))
                            .clickable { onColorSelected(color) }
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, Variables.SchemesPrimary, CircleShape)
                                } else {
                                    Modifier.border(1.dp, Color(0x33000000), CircleShape)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color.Gray.copy(alpha = 0.35f))
                )
                Spacer(modifier = Modifier.width(4.dp))

                // Stroke widths
                strokePresets.forEach { (width, label) ->
                    val isSelected = selectedStrokeWidth == width
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Variables.SchemesPrimaryContainer else Color.Transparent,
                        modifier = Modifier
                            .clickable { onStrokeWidthSelected(width) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Variables.SchemesOnPrimaryContainer else Variables.SchemesOnSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // Undo stroke
                IconButton(
                    onClick = onUndoStroke,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo stroke",
                        tint = Variables.SchemesOnSurface
                    )
                }
            }

            // Done button pinned outside scroll area on the right
            TextButton(
                onClick = onDoneClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done drawing",
                    tint = Variables.SchemesPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Done",
                    color = Variables.SchemesPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ToolIcon(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Variables.SchemesPrimary else Color.Transparent,
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

// Backward-compatible wrapper
@Composable
fun DrawingLayerControls(
    drawingEnabled: Boolean,
    selectedTool: CanvasDrawingTool,
    selectedColorArgb: Long,
    selectedStrokeWidth: Float,
    selectedAlpha: Float,
    onToggleDrawing: () -> Unit,
    onToolSelected: (CanvasDrawingTool) -> Unit,
    onColorSelected: (Long) -> Unit,
    onStrokeWidthSelected: (Float) -> Unit,
    onAlphaSelected: (Float) -> Unit
) {
    if (drawingEnabled) {
        DrawingToolbar(
            selectedTool = selectedTool,
            selectedColorArgb = selectedColorArgb,
            selectedStrokeWidth = selectedStrokeWidth,
            onToolSelected = onToolSelected,
            onColorSelected = onColorSelected,
            onStrokeWidthSelected = onStrokeWidthSelected,
            onDoneClick = onToggleDrawing
        )
    }
}
