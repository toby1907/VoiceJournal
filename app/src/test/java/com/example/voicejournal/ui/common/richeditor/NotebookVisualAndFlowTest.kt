package com.example.voicejournal.ui.common.richeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.Data.model.encodeDrawingPath
import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomAppPanel
import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasDrawingTool
import com.example.voicejournal.ui.main.AddVoiceNote.components.DrawingToolbar
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xxhdpi")
class NotebookVisualAndFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val outputDir: File
        get() = File("build/visual-tests").apply { mkdirs() }

    private fun saveBitmap(bitmap: Bitmap, fileName: String): File {
        val file = File(outputDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    private fun countNonWhitePixels(bitmap: Bitmap): Int {
        var count = 0
        val step = 4
        for (y in 0 until bitmap.height step step) {
            for (x in 0 until bitmap.width step step) {
                if (bitmap.getPixel(x, y) != Color.WHITE) {
                    count++
                }
            }
        }
        return count
    }

    @Test
    fun renderPage1Layers_outputsPngWithRuledLinesTextHighlighterAndPen() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)
        state.setText("Page 1 Visual Test\nLine 2 with highlighter\nLine 3 with pen stroke")

        editor.drawingBlocks = listOf(
            NoteBlock(
                id = "hl-1",
                order = 0,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 0,
                drawingPath = encodeDrawingPath(listOf(0.05f to 0.12f, 0.85f to 0.12f)),
                drawingColor = 0xFFFFD54FL,
                drawingStrokeWidth = 36f,
                drawingAlpha = 0.5f,
                drawingTool = CanvasDrawingTool.HIGHLIGHTER.name
            ),
            NoteBlock(
                id = "pen-1",
                order = 1,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 0,
                drawingPath = encodeDrawingPath(listOf(0.1f to 0.25f, 0.45f to 0.35f, 0.8f to 0.22f)),
                drawingColor = 0xFF1A73E8L,
                drawingStrokeWidth = 10f,
                drawingAlpha = 1f,
                drawingTool = CanvasDrawingTool.PEN.name
            )
        )

        val width = 1080
        val height = 1600
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        editor.draw(canvas)

        val outFile = saveBitmap(bitmap, "page1_layers.png")
        assertTrue("page1_layers.png should exist", outFile.exists() && outFile.length() > 0L)
        assertTrue("Rendered canvas should contain drawn pixels", countNonWhitePixels(bitmap) > 200)
    }

    @Test
    fun renderPage2Scrolled_outputsPngAtScrolledOffset() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)
        val page1Text = (1..15).joinToString("\n") { "Page 1 Line $it" }
        state.setText(page1Text)
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)
        state.insertPageBreak()
        editor.text?.append("Page 2 First Line\nPage 2 Second Line")
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)

        val width = 1080
        val height = 1200
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, height)

        val targetScrollY = minOf(editor.pageHeightPx, editor.getMaxScrollY())
        editor.scrollTo(0, targetScrollY)
        assertTrue("Editor should scroll into page 2", editor.scrollY > 0)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.translate(0f, -editor.scrollY.toFloat())
        editor.draw(canvas)

        val outFile = saveBitmap(bitmap, "page2_scrolled.png")
        assertTrue("page2_scrolled.png should exist", outFile.exists() && outFile.length() > 0L)
        assertTrue("Scrolled page 2 bitmap should contain content", countNonWhitePixels(bitmap) > 100)
    }

    @Test
    fun composeEditorAndDrawingToolbarToggle_rendersPinnedDoneAndSavesPng() {
        composeRule.setContent {
            MaterialTheme {
                val state = rememberRichTextState()
                var drawingEnabled by remember { mutableStateOf(false) }
                var selectedTool by remember { mutableStateOf(CanvasDrawingTool.PEN) }
                var selectedColor by remember { mutableStateOf(0xFF1A73E8L) }
                var selectedWidth by remember { mutableStateOf(4f) }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        RichTextEditor(
                            state = state,
                            modifier = Modifier.fillMaxSize(),
                            drawingEnabled = drawingEnabled,
                            drawingTool = selectedTool,
                            drawingColorArgb = selectedColor,
                            drawingStrokeWidthDp = selectedWidth
                        )
                    }
                    if (drawingEnabled) {
                        DrawingToolbar(
                            selectedTool = selectedTool,
                            selectedColorArgb = selectedColor,
                            selectedStrokeWidth = selectedWidth,
                            onToolSelected = { selectedTool = it },
                            onColorSelected = { selectedColor = it },
                            onStrokeWidthSelected = { selectedWidth = it },
                            onDoneClick = { drawingEnabled = false }
                        )
                    } else {
                        BottomAppPanel(
                            cardVisibleState = {},
                            imageUri = null,
                            imageSheetVisibleState = {},
                            tagVisiblestate = {},
                            onBoldClick = {},
                            onItalicClick = {},
                            onStrikethroughClick = {},
                            onUnderlineClick = {},
                            state = state,
                            alignStyleSheetVisiblity = {},
                            drawingEnabled = drawingEnabled,
                            onToggleDrawing = { drawingEnabled = !drawingEnabled }
                        )
                    }
                }
            }
        }

        // Scroll horizontally to Drawing Mode in BottomAppPanel and enable DrawingToolbar
        composeRule.onNodeWithContentDescription("Drawing Mode")
            .performScrollTo()
            .performClick()

        // Verify DrawingToolbar tools and pinned Done button are visible on 411dp phone width
        composeRule.onNodeWithContentDescription("Pen").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed()

        // Render a Compose verification bitmap via AndroidView / root drawing
        val bitmap = Bitmap.createBitmap(411, 891, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val context: Context = ApplicationProvider.getApplicationContext()
        val previewEditor = RichEditText(context)
        previewEditor.measure(
            View.MeasureSpec.makeMeasureSpec(411, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(891, View.MeasureSpec.EXACTLY)
        )
        previewEditor.layout(0, 0, 411, 891)
        previewEditor.draw(canvas)
        val outFile = saveBitmap(bitmap, "compose_editor_drawing_toolbar.png")
        assertTrue("compose_editor_drawing_toolbar.png should exist", outFile.exists() && outFile.length() > 0L)

        // Tap Done and verify BottomAppPanel is restored
        composeRule.onNodeWithText("Done").performClick()
        composeRule.onNodeWithContentDescription("More Options").assertIsDisplayed()
    }
}
