package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.ui.common.richeditor.RichEditText
import com.example.voicejournal.ui.theme.Variables
import kotlin.math.roundToInt

private const val DEFAULT_IMAGE_X_DP = 44f
private const val DEFAULT_IMAGE_Y_DP = 56f
private const val DEFAULT_IMAGE_WIDTH_DP = 220f
private const val DEFAULT_IMAGE_HEIGHT_DP = 180f
private const val MIN_IMAGE_WIDTH_DP = 96f
private const val MAX_IMAGE_WIDTH_DP = 360f
private const val MIN_IMAGE_HEIGHT_DP = 80f
private const val MAX_IMAGE_HEIGHT_DP = 440f

@Composable
fun FloatingImageBoxOverlay(
    blocks: List<NoteBlock>,
    drawingEnabled: Boolean,
    modifier: Modifier = Modifier.fillMaxSize(),
    scrollY: Int = 0,
    pageCount: Int = 1,
    pageStridePx: Int = 0,
    readOnly: Boolean = false,
    onPositionChange: (blockId: String, pageIndex: Int, x: Float, y: Float, width: Float, height: Float) -> Unit = { _, _, _, _, _, _ -> },
    onResize: (blockId: String, width: Float, height: Float) -> Unit = { _, _, _ -> },
    onBringToFront: (blockId: String) -> Unit = {},
    onDelete: (blockId: String) -> Unit = {}
) {
    if (drawingEnabled) return

    val imageBlocks = remember(blocks) {
        blocks.filter {
            it.type == NoteBlockType.IMAGE.name && !it.mediaUri.isNullOrBlank()
        }
    }
    if (imageBlocks.isEmpty()) return

    val density = LocalDensity.current
    val densityScale = density.density
    val effectivePageStridePx = remember(pageStridePx, densityScale) {
        if (pageStridePx > 0) {
            pageStridePx
        } else {
            val fixedLineHeightPx = (38f * densityScale).roundToInt()
            RichEditText.PAGE_LINE_STRIDE * fixedLineHeightPx
        }
    }

    var selectedBlockId by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
    ) {
        val containerWidthDp = maxWidth.value.takeIf { it > 0f } ?: 360f
        val containerHeightPx = constraints.maxHeight.takeIf { it > 0 } ?: (effectivePageStridePx * pageCount.coerceAtLeast(1))

        imageBlocks.forEach { block ->
            key(block.id) {
                val blockPage = (block.canvasPageIndex ?: 0).coerceAtLeast(0)
                val initialX = (block.canvasX ?: DEFAULT_IMAGE_X_DP).coerceAtLeast(0f)
                val initialY = (block.canvasY ?: DEFAULT_IMAGE_Y_DP).coerceAtLeast(0f)
                val initialW = (block.canvasWidth ?: DEFAULT_IMAGE_WIDTH_DP).coerceIn(MIN_IMAGE_WIDTH_DP, MAX_IMAGE_WIDTH_DP)
                val initialH = (block.canvasHeight ?: DEFAULT_IMAGE_HEIGHT_DP).coerceIn(MIN_IMAGE_HEIGHT_DP, MAX_IMAGE_HEIGHT_DP)

                var currentX by remember { mutableFloatStateOf(initialX) }
                var currentAbsYPx by remember { mutableFloatStateOf(blockPage * effectivePageStridePx + initialY * densityScale) }
                var currentW by remember { mutableFloatStateOf(initialW) }
                var currentH by remember { mutableFloatStateOf(initialH) }
                var isDragging by remember { mutableStateOf(false) }
                var isResizing by remember { mutableStateOf(false) }

                LaunchedEffect(block.canvasPageIndex, block.canvasX, block.canvasY, block.canvasWidth, block.canvasHeight, effectivePageStridePx) {
                    if (!isDragging && !isResizing) {
                        currentX = initialX
                        currentAbsYPx = blockPage * effectivePageStridePx + initialY * densityScale
                        currentW = initialW
                        currentH = initialH
                    }
                }

                val latestContainerWidthDp by rememberUpdatedState(containerWidthDp)
                val latestContainerHeightPx by rememberUpdatedState(containerHeightPx)
                val latestPageCount by rememberUpdatedState(pageCount.coerceAtLeast(1))
                val latestPageStridePx by rememberUpdatedState(effectivePageStridePx)
                val latestOnPositionChange by rememberUpdatedState(onPositionChange)
                val latestOnResize by rememberUpdatedState(onResize)
                val latestOnBringToFront by rememberUpdatedState(onBringToFront)
                val latestOnDelete by rememberUpdatedState(onDelete)

                val isActive = !readOnly && (isDragging || isResizing || selectedBlockId == block.id)
                val cornerShape = RoundedCornerShape(12.dp)
                val borderColor = if (readOnly) {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                } else if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Variables.SchemesPrimaryContainer
                }
                val borderWidth = if (readOnly) 0.75.dp else if (isActive) 2.dp else 1.5.dp
                val elevation = if (readOnly) 2.dp else if (isActive) 8.dp else 4.dp

                val bodyModifier = if (readOnly) {
                    Modifier
                } else {
                    Modifier.pointerInput(block.id) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                selectedBlockId = block.id
                                latestOnBringToFront(block.id)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val maxX = (latestContainerWidthDp - currentW).coerceAtLeast(0f)
                                val totalCanvasHeightPx = maxOf(
                                    latestContainerHeightPx.toFloat(),
                                    latestPageCount * latestPageStridePx.toFloat()
                                )
                                val maxY = (totalCanvasHeightPx - currentH * densityScale).coerceAtLeast(0f)
                                currentX = (currentX + dragAmount.x / densityScale).coerceIn(0f, maxX)
                                currentAbsYPx = (currentAbsYPx + dragAmount.y).coerceIn(0f, maxY)
                            },
                            onDragEnd = {
                                isDragging = false
                                val maxPage = (latestPageCount - 1).coerceAtLeast(0)
                                val targetPage = if (latestPageStridePx > 0) {
                                    (currentAbsYPx / latestPageStridePx).toInt().coerceIn(0, maxPage)
                                } else {
                                    0
                                }
                                val localYDp = ((currentAbsYPx - targetPage * latestPageStridePx) / densityScale)
                                    .coerceAtLeast(0f)
                                latestOnPositionChange(
                                    block.id,
                                    targetPage,
                                    currentX,
                                    localYDp,
                                    currentW,
                                    currentH
                                )
                            },
                            onDragCancel = {
                                isDragging = false
                                val maxPage = (latestPageCount - 1).coerceAtLeast(0)
                                val targetPage = if (latestPageStridePx > 0) {
                                    (currentAbsYPx / latestPageStridePx).toInt().coerceIn(0, maxPage)
                                } else {
                                    0
                                }
                                val localYDp = ((currentAbsYPx - targetPage * latestPageStridePx) / densityScale)
                                    .coerceAtLeast(0f)
                                latestOnPositionChange(
                                    block.id,
                                    targetPage,
                                    currentX,
                                    localYDp,
                                    currentW,
                                    currentH
                                )
                            }
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .zIndex(if (isDragging || isResizing) 2f else if (isActive) 1f else 0f)
                        .offset {
                            IntOffset(
                                x = (currentX * densityScale).roundToInt(),
                                y = (currentAbsYPx - scrollY).roundToInt()
                            )
                        }
                        .size(currentW.dp, currentH.dp)
                        .testTag("floating_image_box_${block.id}")
                        .shadow(elevation, cornerShape)
                        .clip(cornerShape)
                        .border(borderWidth, borderColor, cornerShape),
                    color = Color(0xFFF3F1EC),
                    shape = cornerShape
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = block.mediaUri),
                            contentDescription = "Attached Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(bodyModifier)
                        )

                        if (!readOnly) {
                            // Subtle drag affordance indicator (top-left)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.52f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenWith,
                                    contentDescription = "Move Image",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Remove button (top-right)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(34.dp)
                                    .testTag("floating_image_remove_${block.id}")
                                    .semantics { contentDescription = "Remove Image" }
                                    .clickable {
                                        if (selectedBlockId == block.id) {
                                            selectedBlockId = null
                                        }
                                        latestOnDelete(block.id)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(Color(0xCC1C1B1F), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            // Resize handle (bottom-right)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp)
                                    .size(36.dp)
                                    .testTag("floating_image_resize_${block.id}")
                                    .semantics { contentDescription = "Resize Image" }
                                    .pointerInput(block.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                isResizing = true
                                                selectedBlockId = block.id
                                                latestOnBringToFront(block.id)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val maxAllowedW = (latestContainerWidthDp - currentX)
                                                    .coerceAtLeast(MIN_IMAGE_WIDTH_DP)
                                                    .coerceAtMost(MAX_IMAGE_WIDTH_DP)
                                                currentW = (currentW + dragAmount.x / densityScale)
                                                    .coerceIn(MIN_IMAGE_WIDTH_DP, maxAllowedW)
                                                currentH = (currentH + dragAmount.y / densityScale)
                                                    .coerceIn(MIN_IMAGE_HEIGHT_DP, MAX_IMAGE_HEIGHT_DP)
                                            },
                                            onDragEnd = {
                                                isResizing = false
                                                latestOnResize(block.id, currentW, currentH)
                                            },
                                            onDragCancel = {
                                                isResizing = false
                                                latestOnResize(block.id, currentW, currentH)
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .rotate(90f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
