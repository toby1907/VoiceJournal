package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.Data.model.decodeDrawingPath
import kotlin.math.max
import kotlin.math.roundToInt

enum class CanvasDrawingTool {
    PEN,
    HIGHLIGHTER,
    ERASER
}

@Composable
fun DrawingCanvasOverlay(
    drawingBlocks: List<NoteBlock>,
    drawingEnabled: Boolean,
    drawingTool: CanvasDrawingTool,
    drawingColorArgb: Long,
    drawingStrokeWidthDp: Float,
    drawingAlpha: Float,
    pageIndex: Int = 0,
    modifier: Modifier = Modifier,
    onStrokeDrawn: (
        pageIndex: Int,
        normalizedPoints: List<Pair<Float, Float>>,
        colorArgb: Long,
        strokeWidthPx: Float,
        alpha: Float,
        tool: String
    ) -> Unit,
    onEraseStroke: (pageIndex: Int, normalizedX: Float, normalizedY: Float, normalizedRadius: Float) -> Unit
) {
    val density = LocalDensity.current
    val densityScale = density.density
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val defaultBaseHeight = (screenHeightPx - with(density) { 180.dp.toPx() }).coerceAtLeast(400f)

    var maxCanvasHeight by remember(pageIndex) {
        mutableFloatStateOf(defaultBaseHeight)
    }

    val drawingColor = when (drawingTool) {
        CanvasDrawingTool.PEN -> Color(drawingColorArgb.toInt())
        CanvasDrawingTool.HIGHLIGHTER -> Color(drawingColorArgb.toInt())
        CanvasDrawingTool.ERASER -> Color.Transparent
    }
    val drawingStrokeWidth = when (drawingTool) {
        CanvasDrawingTool.PEN -> drawingStrokeWidthDp * densityScale
        CanvasDrawingTool.HIGHLIGHTER -> (drawingStrokeWidthDp * 1.75f) * densityScale
        CanvasDrawingTool.ERASER -> 0f
    }
    val activeAlpha = when (drawingTool) {
        CanvasDrawingTool.PEN -> drawingAlpha.coerceIn(0.1f, 1f)
        CanvasDrawingTool.HIGHLIGHTER -> drawingAlpha.coerceIn(0.1f, 0.75f)
        CanvasDrawingTool.ERASER -> 0f
    }

    var activeStroke by remember(pageIndex, drawingTool, drawingEnabled) {
        mutableStateOf<List<Pair<Float, Float>>>(emptyList())
    }
    var canvasSize by remember(pageIndex) {
        mutableStateOf(IntSize(1, 1))
    }
    val eraserRadiusNormalized = remember(canvasSize, maxCanvasHeight, densityScale, drawingStrokeWidthDp) {
        val minDimension = minOf(canvasSize.width.coerceAtLeast(1).toFloat(), maxCanvasHeight)
        ((drawingStrokeWidthDp * 2.5f * densityScale) / minDimension).coerceAtLeast(0.012f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val inputModifier = if (drawingEnabled) {
            Modifier.pointerInput(pageIndex, drawingEnabled, drawingTool, canvasSize, maxCanvasHeight) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val effectiveHeight = maxCanvasHeight.coerceAtLeast(1f)
                        if (drawingTool == CanvasDrawingTool.ERASER) {
                            onEraseStroke(
                                pageIndex,
                                (offset.x / canvasSize.width.toFloat()).coerceIn(0f, 1f),
                                (offset.y / effectiveHeight).coerceIn(0f, 1f),
                                eraserRadiusNormalized
                            )
                            activeStroke = emptyList()
                            return@detectDragGestures
                        }
                        val nx = (offset.x / canvasSize.width.toFloat()).coerceIn(0f, 1f)
                        val ny = (offset.y / effectiveHeight).coerceIn(0f, 1f)
                        activeStroke = listOf(nx to ny)
                    },
                    onDrag = { change, _ ->
                        val effectiveHeight = maxCanvasHeight.coerceAtLeast(1f)
                        if (drawingTool == CanvasDrawingTool.ERASER) {
                            onEraseStroke(
                                pageIndex,
                                (change.position.x / canvasSize.width.toFloat()).coerceIn(0f, 1f),
                                (change.position.y / effectiveHeight).coerceIn(0f, 1f),
                                eraserRadiusNormalized
                            )
                            return@detectDragGestures
                        }
                        val nx = (change.position.x / canvasSize.width.toFloat()).coerceIn(0f, 1f)
                        val ny = (change.position.y / effectiveHeight).coerceIn(0f, 1f)
                        activeStroke = activeStroke + (nx to ny)
                    },
                    onDragEnd = {
                        if (drawingTool != CanvasDrawingTool.ERASER && activeStroke.size > 1) {
                            onStrokeDrawn(
                                pageIndex,
                                activeStroke,
                                drawingColor.toArgb().toLong(),
                                drawingStrokeWidth,
                                activeAlpha,
                                drawingTool.name
                            )
                        }
                        activeStroke = emptyList()
                    },
                    onDragCancel = {
                        activeStroke = emptyList()
                    }
                )
            }
        } else {
            Modifier
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(inputModifier),
            onDraw = {
                canvasSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
                if (size.height > maxCanvasHeight) {
                    maxCanvasHeight = size.height
                }
                val effectiveHeight = maxCanvasHeight.coerceAtLeast(1f)
                drawDrawingBlocks(size, effectiveHeight, drawingBlocks)
                drawActiveStroke(
                    size = size,
                    effectiveHeight = effectiveHeight,
                    normalizedPoints = activeStroke,
                    color = drawingColor,
                    strokeWidth = drawingStrokeWidth,
                    alpha = activeAlpha
                )
            }
        )

        if (drawingEnabled) {
            Surface(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp),
                color = Color(0xE6FFF8E1),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (drawingTool == CanvasDrawingTool.ERASER) "Erasing Mode" else "Drawing Mode",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF5F4300)
                )
            }
        }
    }
}

@Composable
fun CanvasNoteBoard(
    blocks: List<NoteBlock>,
    pageCount: Int,
    pageHeight: Dp = 780.dp,
    pageGap: Dp = 24.dp,
    onBlockPositionChange: (blockId: String, pageIndex: Int, x: Float, y: Float, width: Float?, height: Float?) -> Unit = { _, _, _, _, _, _ -> },
    onBringToFront: (blockId: String) -> Unit = {},
    onSendToBack: (blockId: String) -> Unit = {},
    onResizeBlock: (blockId: String, width: Float?, height: Float?) -> Unit = { _, _, _ -> },
    drawingEnabled: Boolean,
    drawingTool: CanvasDrawingTool,
    drawingColorArgb: Long,
    drawingStrokeWidthDp: Float,
    drawingAlpha: Float,
    onStrokeDrawn: (
        pageIndex: Int,
        normalizedPoints: List<Pair<Float, Float>>,
        colorArgb: Long,
        strokeWidthPx: Float,
        alpha: Float,
        tool: String
    ) -> Unit,
    onEraseStroke: (pageIndex: Int, normalizedX: Float, normalizedY: Float, normalizedRadius: Float) -> Unit
) {
    val totalPages = max(pageCount, 1)
    val density = LocalDensity.current
    val densityScale = density.density
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        repeat(totalPages) { pageIndex ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pageHeight)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x33000000), RoundedCornerShape(16.dp))
            ) {
                val pageWidthDp = maxWidth.value.coerceAtLeast(1f)
                val pageHeightDp = maxHeight.value.coerceAtLeast(1f)
                val pageGapDp = pageGap.value
                Text(
                    text = "Page ${pageIndex + 1}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0x99000000),
                    fontWeight = FontWeight.Medium
                )
                blocks
                    .filter { (it.canvasPageIndex ?: 0) == pageIndex }
                    .filterNot { it.type == NoteBlockType.DRAWING.name }
                    .sortedBy { it.order }
                    .forEach { block ->
                        val blockX = block.canvasX ?: 24f
                        val blockY = block.canvasY ?: 48f
                        val blockWidth = block.canvasWidth ?: 240f
                        val blockHeight = block.canvasHeight ?: 120f
                        val blockColor = when (block.type) {
                            NoteBlockType.TEXT.name -> Color(0xFFF6F7FB)
                            NoteBlockType.CHECKLIST.name -> Color(0xFFF1F7F1)
                            NoteBlockType.IMAGE.name -> Color(0xFFF7F2EB)
                            NoteBlockType.AUDIO.name -> Color(0xFFEEF2FF)
                            else -> Color(0xFFF6F7FB)
                        }
                        Card(
                            modifier = Modifier
                                .offset { IntOffset(blockX.roundToInt(), blockY.roundToInt()) }
                                .size(blockWidth.dp, blockHeight.dp)
                                .pointerInput(block.id, pageIndex, blockX, blockY) {
                                    if (drawingEnabled) return@pointerInput
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val dragXDp = dragAmount.x / densityScale
                                        val dragYDp = dragAmount.y / densityScale
                                        val absoluteY = (pageIndex * (pageHeightDp + pageGapDp)) + blockY + dragYDp
                                        val virtualPageHeight = pageHeightDp + pageGapDp
                                        val nextPage = (absoluteY / virtualPageHeight)
                                            .toInt()
                                            .coerceIn(0, totalPages - 1)
                                        val nextPageStart = nextPage * virtualPageHeight
                                        val nextY = (absoluteY - nextPageStart).coerceIn(
                                            0f,
                                            (pageHeightDp - blockHeight).coerceAtLeast(0f)
                                        )
                                        val nextX = (blockX + dragXDp).coerceIn(
                                            0f,
                                            (pageWidthDp - blockWidth).coerceAtLeast(0f)
                                        )
                                        onBlockPositionChange(
                                            block.id,
                                            nextPage,
                                            nextX,
                                            nextY,
                                            blockWidth,
                                            blockHeight
                                        )
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (drawingEnabled) blockColor.copy(alpha = 0.72f) else blockColor
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                if (!drawingEnabled) {
                                    Row {
                                        IconButton(
                                            onClick = { onSendToBack(block.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VerticalAlignTop,
                                                contentDescription = "Send block back",
                                                tint = Color(0xCC202124)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onBringToFront(block.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VerticalAlignBottom,
                                                contentDescription = "Bring block front",
                                                tint = Color(0xCC202124)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                onResizeBlock(
                                                    block.id,
                                                    blockWidth - 20f,
                                                    blockHeight - 12f
                                                )
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Shrink block",
                                                tint = Color(0xCC202124)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                onResizeBlock(
                                                    block.id,
                                                    blockWidth + 20f,
                                                    blockHeight + 12f
                                                )
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Enlarge block",
                                                tint = Color(0xCC202124)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = canvasLabel(block),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF202124),
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
                                )
                            }
                        }
                    }

                val drawingBlocks = blocks
                    .filter { (it.canvasPageIndex ?: 0) == pageIndex && it.type == NoteBlockType.DRAWING.name }
                    .sortedBy { it.order }

                DrawingCanvasOverlay(
                    drawingBlocks = drawingBlocks,
                    drawingEnabled = drawingEnabled,
                    drawingTool = drawingTool,
                    drawingColorArgb = drawingColorArgb,
                    drawingStrokeWidthDp = drawingStrokeWidthDp,
                    drawingAlpha = drawingAlpha,
                    pageIndex = pageIndex,
                    onStrokeDrawn = onStrokeDrawn,
                    onEraseStroke = onEraseStroke
                )
            }
            if (pageIndex < totalPages - 1) {
                Spacer(modifier = Modifier.height(pageGap))
            }
        }
    }
}

private fun canvasLabel(block: NoteBlock): String {
    return when (block.type) {
        NoteBlockType.TEXT.name -> block.text?.takeIf { it.isNotBlank() } ?: "Text block"
        NoteBlockType.AUDIO.name -> "Audio block"
        NoteBlockType.IMAGE.name -> "Image block"
        NoteBlockType.CHECKLIST.name -> block.text?.takeIf { it.isNotBlank() } ?: "Checklist item"
        NoteBlockType.DRAWING.name -> "Drawing stroke"
        else -> block.type
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDrawingBlocks(
    size: Size,
    effectiveHeight: Float,
    drawingBlocks: List<NoteBlock>
) {
    drawingBlocks.forEach { block ->
        val points = decodeDrawingPath(block.drawingPath)
        if (points.size < 2) return@forEach
        val strokeColorInt = block.drawingColor?.toInt() ?: 0xFF1A73E8.toInt()
        val strokeColor = Color(strokeColorInt)
        val strokeWidth = (block.drawingStrokeWidth ?: 4f).coerceAtLeast(1f)
        val alpha = (block.drawingAlpha ?: 1f).coerceIn(0.05f, 1f)
        drawStrokePoints(size, effectiveHeight, points, strokeColor, strokeWidth, alpha)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawActiveStroke(
    size: Size,
    effectiveHeight: Float,
    normalizedPoints: List<Pair<Float, Float>>,
    color: Color,
    strokeWidth: Float,
    alpha: Float
) {
    if (normalizedPoints.size < 2) return
    drawStrokePoints(size, effectiveHeight, normalizedPoints, color, strokeWidth, alpha)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrokePoints(
    size: Size,
    effectiveHeight: Float,
    normalizedPoints: List<Pair<Float, Float>>,
    color: Color,
    strokeWidth: Float,
    alpha: Float
) {
    for (i in 1 until normalizedPoints.size) {
        val start = normalizedPoints[i - 1]
        val end = normalizedPoints[i]
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(
                x = start.first * size.width,
                y = start.second * effectiveHeight
            ),
            end = androidx.compose.ui.geometry.Offset(
                x = end.first * size.width,
                y = end.second * effectiveHeight
            ),
            strokeWidth = strokeWidth,
            alpha = alpha,
            cap = StrokeCap.Round
        )
    }
}
