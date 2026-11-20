package com.example.voicejournal.Data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteBlockListConverterTest {

    private val converter = NoteBlockListConverter()

    @Test
    fun converter_roundTripsBlocks() {
        val blocks = listOf(
            NoteBlock(
                id = "1",
                order = 0,
                type = NoteBlockType.TEXT.name,
                text = "Hello",
                richTextHtml = "<p><b>Hello</b></p>"
            ),
            NoteBlock(
                id = "2",
                order = 1,
                type = NoteBlockType.AUDIO.name,
                mediaUri = "/tmp/audio.3gp",
                transcript = "voice text"
            ),
            NoteBlock(
                id = "3",
                order = 2,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 1,
                drawingPath = "0.1,0.1;0.5,0.5",
                drawingColor = 0xFF1A73E8,
                drawingStrokeWidth = 8f,
                drawingAlpha = 0.35f,
                drawingTool = "HIGHLIGHTER"
            )
        )

        val payload = converter.fromNoteBlocks(blocks)
        val restored = converter.toNoteBlocks(payload)

        assertNotNull(restored)
        assertEquals(3, restored!!.size)
        assertEquals(NoteBlockType.TEXT.name, restored[0].type)
        assertEquals("Hello", restored[0].text)
        assertEquals(NoteBlockType.AUDIO.name, restored[1].type)
        assertEquals("/tmp/audio.3gp", restored[1].mediaUri)
        assertEquals(NoteBlockType.DRAWING.name, restored[2].type)
        assertEquals("0.1,0.1;0.5,0.5", restored[2].drawingPath)
        assertEquals(0.35f, restored[2].drawingAlpha)
        assertEquals("HIGHLIGHTER", restored[2].drawingTool)
    }

    @Test
    fun factory_createsAudioCenteredLegacyBlocks() {
        val blocks = NoteBlockFactory.createForNote(
            titleHtml = "<p><b>Title</b></p>",
            content = "Body",
            audioPath = "/tmp/recording.3gp",
            imageUris = listOf("content://image/1", "")
        )

        assertFalse(blocks.isEmpty())
        assertTrue(blocks.any { it.type == NoteBlockType.AUDIO.name && it.mediaUri == "/tmp/recording.3gp" })
        assertTrue(blocks.any { it.type == NoteBlockType.TEXT.name && it.text?.contains("Title") == true })
        assertTrue(blocks.any { it.type == NoteBlockType.IMAGE.name && it.mediaUri == "content://image/1" })
    }
}
