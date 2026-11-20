package com.example.voicejournal.Data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteBlockOpsTest {

    @Test
    fun moveUp_swapsWithPreviousAndRenormalizesOrder() {
        val blocks = listOf(
            NoteBlock(id = "a", order = 0, type = NoteBlockType.TEXT.name),
            NoteBlock(id = "b", order = 1, type = NoteBlockType.AUDIO.name),
            NoteBlock(id = "c", order = 2, type = NoteBlockType.IMAGE.name)
        )

        val moved = NoteBlockOps.moveUp(blocks, "c")

        assertEquals(listOf("a", "c", "b"), moved.map { it.id })
        assertEquals(listOf(0, 1, 2), moved.map { it.order })
    }

    @Test
    fun moveDown_swapsWithNextAndRenormalizesOrder() {
        val blocks = listOf(
            NoteBlock(id = "a", order = 0, type = NoteBlockType.TEXT.name),
            NoteBlock(id = "b", order = 1, type = NoteBlockType.AUDIO.name),
            NoteBlock(id = "c", order = 2, type = NoteBlockType.IMAGE.name)
        )

        val moved = NoteBlockOps.moveDown(blocks, "a")

        assertEquals(listOf("b", "a", "c"), moved.map { it.id })
        assertEquals(listOf(0, 1, 2), moved.map { it.order })
    }

    @Test
    fun remove_deletesBlockAndRenormalizesOrder() {
        val blocks = listOf(
            NoteBlock(id = "a", order = 0, type = NoteBlockType.TEXT.name),
            NoteBlock(id = "b", order = 1, type = NoteBlockType.AUDIO.name)
        )

        val removed = NoteBlockOps.remove(blocks, "a")

        assertEquals(listOf("b"), removed.map { it.id })
        assertEquals(listOf(0), removed.map { it.order })
    }

    @Test
    fun upsertAudioTranscript_updatesMatchingAudioBlock() {
        val blocks = listOf(
            NoteBlock(id = "t", order = 0, type = NoteBlockType.TEXT.name, text = "Body"),
            NoteBlock(id = "a1", order = 1, type = NoteBlockType.AUDIO.name, mediaUri = "one.3gp"),
            NoteBlock(id = "a2", order = 2, type = NoteBlockType.AUDIO.name, mediaUri = "two.3gp")
        )

        val updated = NoteBlockOps.upsertAudioTranscript(blocks, "two.3gp", "captured speech")

        assertEquals("captured speech", updated[2].transcript)
        assertEquals(null, updated[1].transcript)
    }

    @Test
    fun upsertAudioTranscript_fallsBackToFirstAudioWhenPathNotFound() {
        val blocks = listOf(
            NoteBlock(id = "a1", order = 0, type = NoteBlockType.AUDIO.name, mediaUri = "one.3gp"),
            NoteBlock(id = "a2", order = 1, type = NoteBlockType.AUDIO.name, mediaUri = "two.3gp")
        )

        val updated = NoteBlockOps.upsertAudioTranscript(blocks, "missing.3gp", "fallback transcript")

        assertEquals("fallback transcript", updated[0].transcript)
        assertEquals(null, updated[1].transcript)
    }

    @Test
    fun toggleChecklistChecked_flipsCheckState() {
        val blocks = listOf(
            NoteBlock(id = "c1", order = 0, type = NoteBlockType.CHECKLIST.name, text = "Task", isChecked = false)
        )

        val checked = NoteBlockOps.toggleChecklistChecked(blocks, "c1")
        val unchecked = NoteBlockOps.toggleChecklistChecked(checked, "c1")

        assertEquals(true, checked[0].isChecked)
        assertEquals(false, unchecked[0].isChecked)
    }

    @Test
    fun updateCanvasLayout_updatesPositionAndPage() {
        val blocks = listOf(
            NoteBlock(id = "t1", order = 0, type = NoteBlockType.TEXT.name, canvasPageIndex = 0, canvasX = 0f, canvasY = 0f)
        )

        val updated = NoteBlockOps.updateCanvasLayout(
            blocks = blocks,
            blockId = "t1",
            pageIndex = 2,
            x = 48f,
            y = 96f,
            width = 220f,
            height = 140f
        )

        assertEquals(2, updated[0].canvasPageIndex)
        assertEquals(48f, updated[0].canvasX)
        assertEquals(96f, updated[0].canvasY)
        assertEquals(220f, updated[0].canvasWidth)
        assertEquals(140f, updated[0].canvasHeight)
    }

    @Test
    fun moveToFrontOnPage_movesBlockToTopWithinSamePage() {
        val blocks = listOf(
            NoteBlock(id = "p0a", order = 0, type = NoteBlockType.TEXT.name, canvasPageIndex = 0),
            NoteBlock(id = "p1a", order = 1, type = NoteBlockType.TEXT.name, canvasPageIndex = 1),
            NoteBlock(id = "p0b", order = 2, type = NoteBlockType.TEXT.name, canvasPageIndex = 0),
            NoteBlock(id = "p0c", order = 3, type = NoteBlockType.TEXT.name, canvasPageIndex = 0)
        )

        val updated = NoteBlockOps.moveToFrontOnPage(blocks, "p0a")

        assertEquals(listOf("p1a", "p0b", "p0c", "p0a"), updated.map { it.id })
    }

    @Test
    fun moveToBackOnPage_movesBlockToBottomWithinSamePage() {
        val blocks = listOf(
            NoteBlock(id = "p0a", order = 0, type = NoteBlockType.TEXT.name, canvasPageIndex = 0),
            NoteBlock(id = "p1a", order = 1, type = NoteBlockType.TEXT.name, canvasPageIndex = 1),
            NoteBlock(id = "p0b", order = 2, type = NoteBlockType.TEXT.name, canvasPageIndex = 0),
            NoteBlock(id = "p0c", order = 3, type = NoteBlockType.TEXT.name, canvasPageIndex = 0)
        )

        val updated = NoteBlockOps.moveToBackOnPage(blocks, "p0c")

        assertEquals(listOf("p0c", "p0a", "p1a", "p0b"), updated.map { it.id })
    }

    @Test
    fun resizeCanvasBlock_appliesMinimumSizeClamp() {
        val blocks = listOf(
            NoteBlock(id = "r1", order = 0, type = NoteBlockType.TEXT.name, canvasWidth = 180f, canvasHeight = 90f)
        )

        val updated = NoteBlockOps.resizeCanvasBlock(
            blocks = blocks,
            blockId = "r1",
            width = 10f,
            height = 10f
        )

        assertEquals(72f, updated[0].canvasWidth)
        assertEquals(48f, updated[0].canvasHeight)
    }

    @Test
    fun syncImageBlocks_preservesExistingLayoutForMatchingUris() {
        val blocks = listOf(
            NoteBlock(id = "t1", order = 0, type = NoteBlockType.TEXT.name),
            NoteBlock(
                id = "img-keep",
                order = 1,
                type = NoteBlockType.IMAGE.name,
                mediaUri = "one.jpg",
                canvasPageIndex = 2,
                canvasX = 55f,
                canvasY = 75f,
                canvasWidth = 310f,
                canvasHeight = 190f
            ),
            NoteBlock(id = "img-drop", order = 2, type = NoteBlockType.IMAGE.name, mediaUri = "old.jpg")
        )

        val updated = NoteBlockOps.syncImageBlocks(blocks, listOf("one.jpg", "two.jpg"))
        val kept = updated.first { it.mediaUri == "one.jpg" }
        val added = updated.first { it.mediaUri == "two.jpg" }

        assertEquals("img-keep", kept.id)
        assertEquals(2, kept.canvasPageIndex)
        assertEquals(55f, kept.canvasX)
        assertEquals(75f, kept.canvasY)
        assertEquals(310f, kept.canvasWidth)
        assertEquals(190f, kept.canvasHeight)
        assertEquals(NoteBlockType.IMAGE.name, added.type)
    }

    @Test
    fun ensureCanvasLayout_fillsMissingCanvasFields() {
        val blocks = listOf(
            NoteBlock(id = "a", order = 0, type = NoteBlockType.TEXT.name),
            NoteBlock(id = "b", order = 1, type = NoteBlockType.AUDIO.name),
            NoteBlock(id = "c", order = 2, type = NoteBlockType.IMAGE.name)
        )

        val updated = NoteBlockOps.ensureCanvasLayout(blocks)

        assertEquals(0, updated[0].canvasPageIndex)
        assertEquals(24f, updated[0].canvasX)
        assertEquals(24f, updated[0].canvasY)
        assertEquals(240f, updated[0].canvasWidth)
        assertEquals(120f, updated[0].canvasHeight)

        assertEquals(280f, updated[1].canvasWidth)
        assertEquals(96f, updated[1].canvasHeight)
        assertEquals(280f, updated[2].canvasWidth)
        assertEquals(180f, updated[2].canvasHeight)
    }

    @Test
    fun addDrawingStroke_appendsDrawingBlockOnPage() {
        val blocks = listOf(
            NoteBlock(id = "t1", order = 0, type = NoteBlockType.TEXT.name, canvasPageIndex = 0)
        )

        val updated = NoteBlockOps.addDrawingStroke(
            blocks = blocks,
            pageIndex = 1,
            normalizedPath = "0.1,0.2;0.3,0.4",
            color = 0xFF1A73E8.toLong(),
            strokeWidth = 8f,
            alpha = 0.7f,
            tool = "PEN"
        )

        val drawing = updated.last()
        assertEquals(NoteBlockType.DRAWING.name, drawing.type)
        assertEquals(1, drawing.canvasPageIndex)
        assertEquals("0.1,0.2;0.3,0.4", drawing.drawingPath)
        assertEquals("PEN", drawing.drawingTool)
    }

    @Test
    fun eraseLastDrawingStroke_removesLatestStrokeOnlyOnTargetPage() {
        val blocks = listOf(
            NoteBlock(id = "d1", order = 0, type = NoteBlockType.DRAWING.name, canvasPageIndex = 0),
            NoteBlock(id = "d2", order = 1, type = NoteBlockType.DRAWING.name, canvasPageIndex = 1),
            NoteBlock(id = "d3", order = 2, type = NoteBlockType.DRAWING.name, canvasPageIndex = 1)
        )

        val updated = NoteBlockOps.eraseLastDrawingStroke(blocks, pageIndex = 1)

        assertEquals(listOf("d1", "d2"), updated.map { it.id })
    }

    @Test
    fun eraseDrawingStrokeAtPoint_removesTopmostIntersectingStroke() {
        val blocks = listOf(
            NoteBlock(
                id = "d1",
                order = 0,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 1,
                drawingPath = "0.1,0.1;0.5,0.5",
                drawingStrokeWidth = 8f
            ),
            NoteBlock(
                id = "d2",
                order = 1,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 1,
                drawingPath = "0.15,0.1;0.55,0.5",
                drawingStrokeWidth = 8f
            )
        )

        val updated = NoteBlockOps.eraseDrawingStrokeAtPoint(
            blocks = blocks,
            pageIndex = 1,
            normalizedX = 0.2f,
            normalizedY = 0.16f,
            normalizedRadius = 0.04f
        )

        assertEquals(listOf("d1"), updated.map { it.id })
    }

    @Test
    fun eraseDrawingStrokeAtPoint_keepsStrokesWhenPointMisses() {
        val blocks = listOf(
            NoteBlock(
                id = "d1",
                order = 0,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 1,
                drawingPath = "0.1,0.1;0.2,0.2",
                drawingStrokeWidth = 8f
            )
        )

        val updated = NoteBlockOps.eraseDrawingStrokeAtPoint(
            blocks = blocks,
            pageIndex = 1,
            normalizedX = 0.9f,
            normalizedY = 0.9f,
            normalizedRadius = 0.02f
        )

        assertEquals(listOf("d1"), updated.map { it.id })
    }

    @Test
    fun drawingPath_roundTripsNormalizedPoints() {
        val encoded = encodeDrawingPath(listOf(0.1f to 0.2f, 0.8f to 0.9f))
        val decoded = decodeDrawingPath(encoded)

        assertEquals(2, decoded.size)
        assertEquals(0.1f, decoded[0].first)
        assertEquals(0.2f, decoded[0].second)
        assertEquals(0.8f, decoded[1].first)
        assertEquals(0.9f, decoded[1].second)
    }
}
