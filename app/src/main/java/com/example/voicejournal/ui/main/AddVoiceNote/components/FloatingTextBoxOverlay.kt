package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.ui.theme.Variables
import kotlin.math.roundToInt

@Composable
fun FloatingTextBoxOverlay(
    blocks: List<NoteBlock>,
    currentPageIndex: Int,
    drawingEnabled: Boolean,
    scrollY: Int = 0,
    onPositionChange: (blockId: String, pageIndex: Int, x: Float, y: Float, width: Float?, height: Float?) -> Unit,
    onResize: (blockId: String, width: Float?, height: Float?) -> Unit,
    onTextChange: (blockId: String, newText: String) -> Unit,
    onDelete: (blockId: String) -> Unit
) {
    if (drawingEnabled) return

    val density = LocalDensity.current
    val densityScale = density.density

    val textBoxBlocks = remember(blocks, currentPageIndex) {
        blocks.filter {
            it.type == NoteBlockType.FLOATING_TEXT.name &&
                    (it.canvasPageIndex ?: 0) == currentPageIndex &&
                    it.canvasX != null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        textBoxBlocks.forEach { block ->
            var currentX by remember(block.id, block.canvasX) { mutableFloatStateOf(block.canvasX ?: 40f) }
            var currentY by remember(block.id, block.canvasY) { mutableFloatStateOf(block.canvasY ?: 80f) }
            var currentW by remember(block.id, block.canvasWidth) { mutableFloatStateOf(block.canvasWidth ?: 200f) }
            var currentH by remember(block.id, block.canvasHeight) { mutableFloatStateOf(block.canvasHeight ?: 90f) }

            Surface(
                modifier = Modifier
                    .offset { IntOffset(currentX.roundToInt(), (currentY - scrollY).roundToInt()) }
                    .size(currentW.dp, currentH.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, Variables.SchemesPrimaryContainer, RoundedCornerShape(12.dp)),
                color = Variables.SchemesSurface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Drag header bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(Variables.SchemesPrimaryContainer.copy(alpha = 0.35f))
                                .pointerInput(block.id) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            currentX = (currentX + dragAmount.x / densityScale).coerceAtLeast(0f)
                                            currentY = (currentY + dragAmount.y / densityScale).coerceAtLeast(0f)
                                        },
                                        onDragEnd = {
                                            onPositionChange(block.id, currentPageIndex, currentX, currentY, currentW, currentH)
                                        }
                                    )
                                }
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenWith,
                                contentDescription = "Move Text Box",
                                modifier = Modifier.size(14.dp),
                                tint = Variables.SchemesOnPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { onDelete(block.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Text Box",
                                    modifier = Modifier.size(14.dp),
                                    tint = Variables.SchemesOnPrimaryContainer
                                )
                            }
                        }

                        // Content text input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            val text = block.text ?: ""
                            BasicTextField(
                                value = text,
                                onValueChange = { onTextChange(block.id, it) },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = Variables.SchemesOnSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (text.isEmpty()) {
                                        Text(
                                            text = "Tap to type...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Variables.SchemesOnSurface.copy(alpha = 0.45f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    // Resize handle in bottom-right corner
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .pointerInput(block.id) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        currentW = (currentW + dragAmount.x / densityScale).coerceIn(120f, 500f)
                                        currentH = (currentH + dragAmount.y / densityScale).coerceIn(60f, 500f)
                                    },
                                    onDragEnd = {
                                        onResize(block.id, currentW, currentH)
                                    }
                                )
                            }
                            .padding(2.dp)
                            .background(Variables.SchemesPrimaryContainer, CircleShape)
                    )
                }
            }
        }
    }
}
