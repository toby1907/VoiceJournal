package com.example.voicejournal.Data.model

import androidx.core.text.HtmlCompat
import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

enum class NoteBlockType {
    TEXT,
    FLOATING_TEXT,
    CHECKLIST,
    IMAGE,
    AUDIO,
    DRAWING
}

data class NoteBlock(
    val id: String,
    val order: Int,
    val type: String,
    val text: String? = null,
    val richTextHtml: String? = null,
    val mediaUri: String? = null,
    val transcript: String? = null,
    val isChecked: Boolean? = null,
    val durationMs: Long? = null,
    val canvasPageIndex: Int? = null,
    val canvasX: Float? = null,
    val canvasY: Float? = null,
    val canvasWidth: Float? = null,
    val canvasHeight: Float? = null,
    val drawingPath: String? = null,
    val drawingColor: Long? = null,
    val drawingStrokeWidth: Float? = null,
    val drawingAlpha: Float? = null,
    val drawingTool: String? = null
)

class NoteBlockListConverter {
    @TypeConverter
    fun fromNoteBlocks(noteBlocks: List<NoteBlock>?): String? {
        if (noteBlocks == null) return null
        val array = JSONArray()
        noteBlocks.forEach { block ->
            val obj = JSONObject()
                .put("id", block.id)
                .put("order", block.order)
                .put("type", block.type)
                .put("text", block.text)
                .put("richTextHtml", block.richTextHtml)
                .put("mediaUri", block.mediaUri)
                .put("transcript", block.transcript)
                .put("isChecked", block.isChecked)
                .put("durationMs", block.durationMs)
                .put("canvasPageIndex", block.canvasPageIndex)
                .put("canvasX", block.canvasX)
                .put("canvasY", block.canvasY)
                .put("canvasWidth", block.canvasWidth)
                .put("canvasHeight", block.canvasHeight)
                .put("drawingPath", block.drawingPath)
                .put("drawingColor", block.drawingColor)
                .put("drawingStrokeWidth", block.drawingStrokeWidth)
                .put("drawingAlpha", block.drawingAlpha)
                .put("drawingTool", block.drawingTool)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toNoteBlocks(value: String?): List<NoteBlock>? {
        if (value.isNullOrBlank()) return null
        return try {
            val array = JSONArray(value)
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.getJSONObject(index)
                    add(
                        NoteBlock(
                            id = obj.optString("id"),
                            order = obj.optInt("order", index),
                            type = obj.optString("type"),
                            text = obj.optString("text").ifBlank { null },
                            richTextHtml = obj.optString("richTextHtml").ifBlank { null },
                            mediaUri = obj.optString("mediaUri").ifBlank { null },
                            transcript = obj.optString("transcript").ifBlank { null },
                            isChecked = obj.optNullableBoolean("isChecked"),
                            durationMs = obj.optNullableLong("durationMs"),
                            canvasPageIndex = obj.optNullableInt("canvasPageIndex"),
                            canvasX = obj.optNullableFloat("canvasX"),
                            canvasY = obj.optNullableFloat("canvasY"),
                            canvasWidth = obj.optNullableFloat("canvasWidth"),
                            canvasHeight = obj.optNullableFloat("canvasHeight"),
                            drawingPath = obj.optString("drawingPath").ifBlank { null },
                            drawingColor = obj.optNullableLong("drawingColor"),
                            drawingStrokeWidth = obj.optNullableFloat("drawingStrokeWidth"),
                            drawingAlpha = obj.optNullableFloat("drawingAlpha"),
                            drawingTool = obj.optString("drawingTool").ifBlank { null }
                        )
                    )
                }
            }
        } catch (e: JSONException) {
            null
        }
    }
}

object NoteBlockFactory {
    fun createForNote(
        titleHtml: String?,
        content: String?,
        audioPath: String?,
        imageUris: List<String>?
    ): List<NoteBlock> {
        val blocks = mutableListOf<NoteBlock>()
        var order = 0

        val safeTitleHtml = titleHtml.orEmpty().trim()
        if (safeTitleHtml.isNotEmpty()) {
            val titleText = extractPlainTextFromHtml(safeTitleHtml)
            blocks.add(
                NoteBlock(
                    id = UUID.randomUUID().toString(),
                    order = order++,
                    type = NoteBlockType.TEXT.name,
                    text = titleText.ifBlank { null },
                    richTextHtml = safeTitleHtml,
                    canvasPageIndex = 0,
                    canvasX = 24f,
                    canvasY = 24f,
                    canvasWidth = 240f,
                    canvasHeight = 120f
                )
            )
        }

        val safeContent = content.orEmpty().trim()
        if (safeContent.isNotEmpty()) {
            blocks.add(
                NoteBlock(
                    id = UUID.randomUUID().toString(),
                    order = order++,
                    type = NoteBlockType.TEXT.name,
                    text = safeContent,
                    canvasPageIndex = 0,
                    canvasX = 24f,
                    canvasY = 160f,
                    canvasWidth = 240f,
                    canvasHeight = 120f
                )
            )
        }

        val safeAudio = audioPath.orEmpty().trim()
        if (safeAudio.isNotEmpty()) {
            blocks.add(
                NoteBlock(
                    id = UUID.randomUUID().toString(),
                    order = order++,
                    type = NoteBlockType.AUDIO.name,
                    mediaUri = safeAudio,
                    canvasPageIndex = 0,
                    canvasX = 24f,
                    canvasY = 300f,
                    canvasWidth = 280f,
                    canvasHeight = 96f
                )
            )
        }

        imageUris.orEmpty()
            .filter { it.isNotBlank() }
            .forEach { uri ->
                blocks.add(
                    NoteBlock(
                        id = UUID.randomUUID().toString(),
                        order = order++,
                        type = NoteBlockType.IMAGE.name,
                        mediaUri = uri,
                        canvasPageIndex = 0,
                        canvasX = 24f,
                        canvasY = 420f,
                        canvasWidth = 280f,
                        canvasHeight = 180f
                    )
                )
            }

        if (blocks.isEmpty()) {
            blocks.add(
                NoteBlock(
                    id = UUID.randomUUID().toString(),
                    order = 0,
                    type = NoteBlockType.TEXT.name,
                    text = "",
                    canvasPageIndex = 0,
                    canvasX = 24f,
                    canvasY = 24f,
                    canvasWidth = 240f,
                    canvasHeight = 120f
                )
            )
        }
        return blocks
    }
}

object NoteBlockOps {
    private const val DEFAULT_CANVAS_WIDTH = 240f
    private const val DEFAULT_CANVAS_HEIGHT = 120f
    private const val MIN_CANVAS_WIDTH = 72f
    private const val MIN_CANVAS_HEIGHT = 48f
    private const val DEFAULT_CANVAS_X = 24f
    private const val DEFAULT_CANVAS_Y = 24f

    fun normalized(blocks: List<NoteBlock>): List<NoteBlock> {
        return blocks.mapIndexed { index, block -> block.copy(order = index) }
    }

    fun moveUp(blocks: List<NoteBlock>, blockId: String): List<NoteBlock> {
        val mutable = blocks.toMutableList()
        val index = mutable.indexOfFirst { it.id == blockId }
        if (index <= 0) return normalized(mutable)
        val current = mutable[index]
        mutable[index] = mutable[index - 1]
        mutable[index - 1] = current
        return normalized(mutable)
    }

    fun moveDown(blocks: List<NoteBlock>, blockId: String): List<NoteBlock> {
        val mutable = blocks.toMutableList()
        val index = mutable.indexOfFirst { it.id == blockId }
        if (index == -1 || index >= mutable.lastIndex) return normalized(mutable)
        val current = mutable[index]
        mutable[index] = mutable[index + 1]
        mutable[index + 1] = current
        return normalized(mutable)
    }

    fun remove(blocks: List<NoteBlock>, blockId: String): List<NoteBlock> {
        return normalized(blocks.filterNot { it.id == blockId })
    }

    fun upsertAudioTranscript(
        blocks: List<NoteBlock>,
        audioUri: String?,
        transcript: String
    ): List<NoteBlock> {
        val cleanTranscript = transcript.trim().ifBlank { null }
        val cleanAudioUri = audioUri?.trim().orEmpty()
        val targetIndex = if (cleanAudioUri.isNotBlank()) {
            blocks.indexOfFirst {
                it.type == NoteBlockType.AUDIO.name && it.mediaUri == cleanAudioUri
            }
        } else {
            -1
        }
        val fallbackIndex = blocks.indexOfFirst { it.type == NoteBlockType.AUDIO.name }
        val indexToUpdate = if (targetIndex >= 0) targetIndex else fallbackIndex
        if (indexToUpdate < 0) return normalized(blocks)

        val mutable = blocks.toMutableList()
        mutable[indexToUpdate] = mutable[indexToUpdate].copy(transcript = cleanTranscript)
        return normalized(mutable)
    }

    fun toggleChecklistChecked(blocks: List<NoteBlock>, blockId: String): List<NoteBlock> {
        val mutable = blocks.toMutableList()
        val index = mutable.indexOfFirst { it.id == blockId && it.type == NoteBlockType.CHECKLIST.name }
        if (index < 0) return normalized(mutable)
        val current = mutable[index]
        mutable[index] = current.copy(isChecked = !(current.isChecked ?: false))
        return normalized(mutable)
    }

    fun updateCanvasLayout(
        blocks: List<NoteBlock>,
        blockId: String,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float? = null,
        height: Float? = null
    ): List<NoteBlock> {
        val mutable = blocks.toMutableList()
        val index = mutable.indexOfFirst { it.id == blockId }
        if (index < 0) return normalized(mutable)
        mutable[index] = mutable[index].copy(
            canvasPageIndex = pageIndex.coerceAtLeast(0),
            canvasX = x.coerceAtLeast(0f),
            canvasY = y.coerceAtLeast(0f),
            canvasWidth = width?.coerceAtLeast(1f),
            canvasHeight = height?.coerceAtLeast(1f)
        )
        return normalized(mutable)
    }

    fun resizeCanvasBlock(
        blocks: List<NoteBlock>,
        blockId: String,
        width: Float?,
        height: Float?
    ): List<NoteBlock> {
        val mutable = blocks.toMutableList()
        val index = mutable.indexOfFirst { it.id == blockId }
        if (index < 0) return normalized(mutable)
        val current = mutable[index]
        mutable[index] = current.copy(
            canvasWidth = (width ?: current.canvasWidth ?: DEFAULT_CANVAS_WIDTH).coerceAtLeast(MIN_CANVAS_WIDTH),
            canvasHeight = (height ?: current.canvasHeight ?: DEFAULT_CANVAS_HEIGHT).coerceAtLeast(MIN_CANVAS_HEIGHT)
        )
        return normalized(mutable)
    }

    fun moveToFrontOnPage(blocks: List<NoteBlock>, blockId: String): List<NoteBlock> {
        val mutable = blocks.toMutableList()
        val targetIndex = mutable.indexOfFirst { it.id == blockId }
        if (targetIndex < 0) return normalized(mutable)
        val target = mutable[targetIndex]
        val targetPage = target.canvasPageIndex ?: 0
        val lastIndexOnPage = mutable.indexOfLast { (it.canvasPageIndex ?: 0) == targetPage }
        if (lastIndexOnPage <= targetIndex) return normalized(mutable)

        mutable.removeAt(targetIndex)
        val newLastIndexOnPage = mutable.indexOfLast { (it.canvasPageIndex ?: 0) == targetPage }
        val insertIndex = if (newLastIndexOnPage < 0) mutable.size else newLastIndexOnPage + 1
        mutable.add(insertIndex, target)
        return normalized(mutable)
    }

    fun moveToBackOnPage(blocks: List<NoteBlock>, blockId: String): List<NoteBlock> {
        val mutable = blocks.toMutableList()
        val targetIndex = mutable.indexOfFirst { it.id == blockId }
        if (targetIndex < 0) return normalized(mutable)
        val target = mutable[targetIndex]
        val targetPage = target.canvasPageIndex ?: 0
        val firstIndexOnPage = mutable.indexOfFirst { (it.canvasPageIndex ?: 0) == targetPage }
        if (firstIndexOnPage < 0 || firstIndexOnPage >= targetIndex) return normalized(mutable)

        mutable.removeAt(targetIndex)
        val newFirstIndexOnPage = mutable.indexOfFirst { (it.canvasPageIndex ?: 0) == targetPage }
        val insertIndex = if (newFirstIndexOnPage < 0) 0 else newFirstIndexOnPage
        mutable.add(insertIndex, target)
        return normalized(mutable)
    }

    fun syncImageBlocks(blocks: List<NoteBlock>, latestImageUris: List<String>): List<NoteBlock> {
        val nonImageBlocks = blocks.filterNot { it.type == NoteBlockType.IMAGE.name }
        val existingByUri = blocks
            .filter { it.type == NoteBlockType.IMAGE.name }
            .groupBy { it.mediaUri.orEmpty() }
            .mapValues { (_, value) -> value.toMutableList() }
            .toMutableMap()

        val imageBlocks = latestImageUris
            .filter { it.isNotBlank() }
            .map { uri ->
                val queue = existingByUri[uri]
                if (queue != null && queue.isNotEmpty()) {
                    queue.removeAt(0).copy(mediaUri = uri)
                } else {
                    NoteBlock(
                        id = UUID.randomUUID().toString(),
                        order = 0,
                        type = NoteBlockType.IMAGE.name,
                        mediaUri = uri
                    )
                }
            }

        return normalized(nonImageBlocks + imageBlocks)
    }

    fun ensureCanvasLayout(blocks: List<NoteBlock>): List<NoteBlock> {
        return blocks.mapIndexed { index, block ->
            block.copy(
                canvasPageIndex = block.canvasPageIndex ?: 0,
                canvasX = block.canvasX ?: DEFAULT_CANVAS_X,
                canvasY = block.canvasY ?: (DEFAULT_CANVAS_Y + (index * 120f)),
                canvasWidth = block.canvasWidth ?: defaultWidthForType(block.type),
                canvasHeight = block.canvasHeight ?: defaultHeightForType(block.type)
            )
        }
    }

    fun addDrawingStroke(
        blocks: List<NoteBlock>,
        pageIndex: Int,
        normalizedPath: String,
        color: Long,
        strokeWidth: Float,
        alpha: Float,
        tool: String
    ): List<NoteBlock> {
        if (normalizedPath.isBlank()) return normalized(blocks)
        val drawingBlock = NoteBlock(
            id = UUID.randomUUID().toString(),
            order = blocks.size,
            type = NoteBlockType.DRAWING.name,
            canvasPageIndex = pageIndex.coerceAtLeast(0),
            drawingPath = normalizedPath,
            drawingColor = color,
            drawingStrokeWidth = strokeWidth.coerceAtLeast(1f),
            drawingAlpha = alpha.coerceIn(0.05f, 1f),
            drawingTool = tool
        )
        return normalized(blocks + drawingBlock)
    }

    fun eraseDrawingStrokeAtPoint(
        blocks: List<NoteBlock>,
        pageIndex: Int,
        normalizedX: Float,
        normalizedY: Float,
        normalizedRadius: Float
    ): List<NoteBlock> {
        val normalizedPage = pageIndex.coerceAtLeast(0)
        val point = normalizedX.coerceIn(0f, 1f) to normalizedY.coerceIn(0f, 1f)
        val radius = normalizedRadius.coerceAtLeast(0.005f)
        val targetIndex = blocks.indexOfLast { block ->
            block.type == NoteBlockType.DRAWING.name &&
                (block.canvasPageIndex ?: 0) == normalizedPage &&
                drawingStrokeContainsPoint(block, point, radius)
        }
        if (targetIndex < 0) return normalized(blocks)
        val mutable = blocks.toMutableList()
        mutable.removeAt(targetIndex)
        return normalized(mutable)
    }

    fun eraseLastDrawingStroke(blocks: List<NoteBlock>, pageIndex: Int): List<NoteBlock> {
        val normalizedPage = pageIndex.coerceAtLeast(0)
        val targetIndex = blocks.indexOfLast {
            it.type == NoteBlockType.DRAWING.name && (it.canvasPageIndex ?: 0) == normalizedPage
        }
        if (targetIndex < 0) return normalized(blocks)
        val mutable = blocks.toMutableList()
        mutable.removeAt(targetIndex)
        return normalized(mutable)
    }

    private fun defaultWidthForType(type: String): Float {
        return when (type) {
            NoteBlockType.AUDIO.name, NoteBlockType.IMAGE.name -> 280f
            else -> DEFAULT_CANVAS_WIDTH
        }
    }

    private fun defaultHeightForType(type: String): Float {
        return when (type) {
            NoteBlockType.AUDIO.name -> 96f
            NoteBlockType.IMAGE.name -> 180f
            else -> DEFAULT_CANVAS_HEIGHT
        }
    }
}

private fun drawingStrokeContainsPoint(
    block: NoteBlock,
    point: Pair<Float, Float>,
    radius: Float
): Boolean {
    val points = decodeDrawingPath(block.drawingPath)
    if (points.isEmpty()) return false
    if (points.size == 1) return distanceBetween(points[0], point) <= radius

    val strokeRadius = ((block.drawingStrokeWidth ?: 1f) / 1000f).coerceAtLeast(0.0025f)
    val hitRadius = radius + strokeRadius
    for (index in 1 until points.size) {
        if (distancePointToSegment(point, points[index - 1], points[index]) <= hitRadius) {
            return true
        }
    }
    return false
}

private fun distanceBetween(a: Pair<Float, Float>, b: Pair<Float, Float>): Float {
    val dx = a.first - b.first
    val dy = a.second - b.second
    return kotlin.math.sqrt((dx * dx) + (dy * dy))
}

private fun distancePointToSegment(
    point: Pair<Float, Float>,
    start: Pair<Float, Float>,
    end: Pair<Float, Float>
): Float {
    val dx = end.first - start.first
    val dy = end.second - start.second
    if (dx == 0f && dy == 0f) return distanceBetween(point, start)

    val projection = (((point.first - start.first) * dx) + ((point.second - start.second) * dy)) /
        ((dx * dx) + (dy * dy))
    val clamped = projection.coerceIn(0f, 1f)
    val closestX = start.first + (clamped * dx)
    val closestY = start.second + (clamped * dy)
    return distanceBetween(point, closestX to closestY)
}

fun encodeDrawingPath(points: List<Pair<Float, Float>>): String {
    return points
        .map { (x, y) -> "${x.coerceIn(0f, 1f)},${y.coerceIn(0f, 1f)}" }
        .joinToString(";")
}

fun decodeDrawingPath(path: String?): List<Pair<Float, Float>> {
    if (path.isNullOrBlank()) return emptyList()
    return path.split(";")
        .mapNotNull { pair ->
            val split = pair.split(",")
            if (split.size != 2) return@mapNotNull null
            val x = split[0].toFloatOrNull() ?: return@mapNotNull null
            val y = split[1].toFloatOrNull() ?: return@mapNotNull null
            x.coerceIn(0f, 1f) to y.coerceIn(0f, 1f)
        }
}

private fun JSONObject.optNullableBoolean(key: String): Boolean? {
    return if (isNull(key)) null else optBoolean(key)
}

private fun JSONObject.optNullableLong(key: String): Long? {
    return if (isNull(key)) null else optLong(key)
}

private fun JSONObject.optNullableInt(key: String): Int? {
    return if (isNull(key)) null else optInt(key)
}

private fun JSONObject.optNullableFloat(key: String): Float? {
    return if (isNull(key)) null else optDouble(key).toFloat()
}

fun extractPlainTextFromHtml(html: String): String {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
}
