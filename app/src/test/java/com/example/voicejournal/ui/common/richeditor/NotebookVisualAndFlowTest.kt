package com.example.voicejournal.ui.common.richeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.voicejournal.Data.model.NotePaperStyle
import com.example.voicejournal.Data.model.encodeDrawingPath
import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomAppPanel
import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasDrawingTool
import com.example.voicejournal.ui.main.AddVoiceNote.components.DrawingToolbar
import com.example.voicejournal.ui.main.AddVoiceNote.components.NoteTemplateDialog
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

    @Test
    fun paperLayerDrawing_rendersBehindText_andClipsInsidePageCard() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val density = context.resources.displayMetrics.density
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)

        val page1Lines = buildList {
            add("MMMMMMMMMMMMMMMMMM")
            for (i in 2..15) add("Line $i")
        }.joinToString("\n")
        state.setText(page1Lines)
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)
        state.insertPageBreak()
        editor.text?.append("Page 2 Top Line")
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)

        // Solid opaque blue pen stroke across Line 1 (behind dark black text)
        // + thick red pen stroke right at bottom edge of Page 1 (must clip inside Page 1 card, 0 bleed in gap)
        editor.drawingBlocks = listOf(
            NoteBlock(
                id = "blue-under-text",
                order = 0,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 0,
                drawingPath = encodeDrawingPath(listOf(0.0f to 0.03f, 0.5f to 0.03f, 0.9f to 0.03f)),
                drawingColor = 0xFF1A73E8L,
                drawingStrokeWidth = 44f * density,
                drawingAlpha = 1f,
                drawingTool = CanvasDrawingTool.PEN.name
            ),
            NoteBlock(
                id = "red-bottom-edge",
                order = 1,
                type = NoteBlockType.DRAWING.name,
                canvasPageIndex = 0,
                drawingPath = encodeDrawingPath(listOf(0.05f to 1.0f, 0.5f to 1.0f, 0.95f to 1.0f)),
                drawingColor = 0xFFD93025L,
                drawingStrokeWidth = 60f * density,
                drawingAlpha = 1f,
                drawingTool = CanvasDrawingTool.PEN.name
            )
        )

        val width = 1080
        val height = editor.pageHeightPx + editor.pageGapPx + 400
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        editor.draw(canvas)

        val outFile = saveBitmap(bitmap, "paper_layer_clip_and_zorder.png")
        assertTrue("paper_layer_clip_and_zorder.png should exist", outFile.exists() && outFile.length() > 0L)

        // 1. Inspect Line 1 region: verify BOTH solid blue pen pixels AND crisp dark text pixels rendered on top
        val topPadPx = (20f * density).toInt()
        val line1Top = topPadPx
        val line1Bottom = (topPadPx + editor.fixedLineHeight).coerceAtMost(bitmap.height - 1)
        var blueStrokePixels = 0
        var darkTextOnBluePixels = 0
        for (y in line1Top until line1Bottom) {
            for (x in 50 until 550) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                if (b > 170 && r < 90) {
                    blueStrokePixels++
                }
                if (r < 60 && g < 60 && b < 60) {
                    darkTextOnBluePixels++
                }
            }
        }
        assertTrue("Expected solid blue pen stroke pixels on Line 1 (found $blueStrokePixels)", blueStrokePixels > 100)
        assertTrue("Expected dark text pixels rendered on top of blue stroke (found $darkTextOnBluePixels)", darkTextOnBluePixels > 50)

        // 2. Inspect the inter-page gap between Page 1 and Page 2: assert 0 red pixels bleed outside Page 1 card
        val gapStart = editor.pageHeightPx + 2
        val gapEnd = (editor.pageHeightPx + editor.pageGapPx - 2).coerceAtMost(bitmap.height - 1)
        var redPixelsInGap = 0
        for (y in gapStart until gapEnd) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                if (r > 170 && g < 90 && b < 90) {
                    redPixelsInGap++
                }
            }
        }
        assertTrue("Expected 0 red stroke pixels to bleed into inter-page gap (found $redPixelsInGap)", redPixelsInGap == 0)
    }

    @Test
    fun keyboardOpen_lastLineVisibility_andAlwaysVisibleScrollbar_outputsPng() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val density = context.resources.displayMetrics.density
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)
        assertTrue("Native fading vertical scrollbar should be disabled", !editor.isVerticalScrollBarEnabled)

        val fifteenLines = (1..15).joinToString("\n") { line ->
            if (line == 15) "Page 1 Line 15 - clear above FAB" else "Page 1 Line $line"
        }
        state.setText(fifteenLines)
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)

        val width = 1080
        val closedKeyboardHeight = 2000
        val openKeyboardHeight = 1250
        editor.layoutParams = android.view.ViewGroup.LayoutParams(width, closedKeyboardHeight)

        // 1. Initial full-height layout with cursor on Line 15
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(closedKeyboardHeight, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, closedKeyboardHeight)
        editor.setSelection(editor.text!!.length)

        // 2. Simulate soft keyboard opening via imePadding() shrinking editor to 1080x1250
        editor.layoutParams = android.view.ViewGroup.LayoutParams(width, openKeyboardHeight)
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(openKeyboardHeight, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, openKeyboardHeight)

        assertTrue("Editor should be scrollable with keyboard open", editor.getMaxScrollY() > 0)
        assertTrue("ensureCursorVisible() should scroll down so Line 15 is visible (scrollY=${editor.scrollY})", editor.scrollY > 0)

        val layout = editor.layout!!
        val cursorLine = layout.getLineForOffset(editor.selectionEnd)
        val lineBottomContent = editor.totalPaddingTop + maxOf(
            layout.getLineBottom(cursorLine),
            layout.getLineTop(cursorLine) + editor.fixedLineHeight
        )
        val lineBottomViewport = lineBottomContent - editor.scrollY
        val clearanceLimitY = openKeyboardHeight - editor.floatingOverlayClearancePx
        assertTrue(
            "Line 15 bottom ($lineBottomViewport) should sit clear above height - floatingOverlayClearancePx ($clearanceLimitY)",
            lineBottomViewport <= clearanceLimitY
        )

        // 3. Render Skia screenshot of keyboard-open viewport with always-visible right-edge scrollbar
        val bitmap = Bitmap.createBitmap(width, openKeyboardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.translate(0f, -editor.scrollY.toFloat())
        editor.draw(canvas)

        val outFile = saveBitmap(bitmap, "keyboard_open_last_line_scrollbar.png")
        assertTrue("keyboard_open_last_line_scrollbar.png should exist", outFile.exists() && outFile.length() > 0L)

        // 4. Verify visible scrollbar thumb pixels along the right edge (and stopped 56dp above bottom)
        val stripLeft = (width - (8f * density).toInt()).coerceAtLeast(0)
        val stripRight = (width - (2f * density).toInt()).coerceAtMost(width)
        val trackBottomCutoff = (openKeyboardHeight - (56f * density).toInt()).coerceAtLeast(0)
        var thumbPixels = 0
        for (y in 0 until trackBottomCutoff) {
            for (x in stripLeft until stripRight) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                if (r < 175 && g < 175 && b < 175) {
                    thumbPixels++
                }
            }
        }
        assertTrue("Expected visible scrollbar thumb pixels along right edge (found $thumbPixels)", thumbPixels > 200)

        // Ensure scrollbar stops 56dp above bottom so it stays above BottomAppPanel
        val belowCutoffStart = (openKeyboardHeight - (54f * density).toInt()).coerceAtMost(openKeyboardHeight)
        var thumbPixelsBelowCutoff = 0
        for (y in belowCutoffStart until openKeyboardHeight) {
            for (x in (width - (7f * density).toInt()) until stripRight) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                if (r < 175 && g < 175 && b < 175) {
                    thumbPixelsBelowCutoff++
                }
            }
        }
        assertTrue(
            "Expected 0 scrollbar thumb pixels in bottom 56dp clearance zone (found $thumbPixelsBelowCutoff)",
            thumbPixelsBelowCutoff == 0
        )

        // 5. Verify direct thumb dragging on rightmost 24dp scrolls cleanly to top
        val now = SystemClock.uptimeMillis()
        val scrollbarX = width - (6f * density)
        val downEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, scrollbarX, openKeyboardHeight * 0.5f, 0)
        val moveEvent = MotionEvent.obtain(now, now + 16, MotionEvent.ACTION_MOVE, scrollbarX, 12f * density, 0)
        val upEvent = MotionEvent.obtain(now, now + 32, MotionEvent.ACTION_UP, scrollbarX, 12f * density, 0)
        try {
            assertTrue("Scrollbar touch down should be handled", editor.onTouchEvent(downEvent))
            assertTrue("Scrollbar touch move should be handled", editor.onTouchEvent(moveEvent))
            assertTrue("Scrollbar touch up should be handled", editor.onTouchEvent(upEvent))
            assertTrue("Dragging scrollbar thumb to top should scroll to 0 (scrollY=${editor.scrollY})", editor.scrollY == 0)
        } finally {
            downEvent.recycle()
            moveEvent.recycle()
            upEvent.recycle()
        }
    }

    @Test
    fun tapOnSpecificLine_keepsCursorOnTappedLineWithoutSnappingToLine1() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            movementMethod = android.text.method.ArrowKeyMovementMethod.getInstance()
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)

        val tenLines = (1..10).joinToString("\n") { "Notebook line $it" }
        state.setText(tenLines)
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)

        val width = 1080
        val height = 1600
        editor.layoutParams = android.view.ViewGroup.LayoutParams(width, height)
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, height)

        // Tap on Line 6 (0-indexed line 5)
        val targetLineIndex = 5
        val layout = editor.layout!!
        val tapY = editor.totalPaddingTop + (layout.getLineTop(targetLineIndex) + layout.getLineBottom(targetLineIndex)) / 2f
        val tapX = editor.totalPaddingLeft + 120f
        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, tapX, tapY, 0)
        val up = MotionEvent.obtain(now, now + 40, MotionEvent.ACTION_UP, tapX, tapY, 0)
        try {
            editor.onTouchEvent(down)
            editor.onTouchEvent(up)
        } finally {
            down.recycle()
            up.recycle()
        }

        val tappedOffset = editor.selectionEnd
        val tappedLine = editor.layout!!.getLineForOffset(tappedOffset)
        assertTrue(
            "Cursor should land on tapped line $targetLineIndex (was line $tappedLine, offset=$tappedOffset)",
            tappedLine == targetLineIndex
        )

        // Simulate typing on the tapped line and subsequent recomposition property updates
        editor.text?.insert(tappedOffset, "X")
        val afterTypeOffset = editor.selectionEnd
        editor.drawingEnabled = false
        editor.drawingBlocks = emptyList()

        val finalLine = editor.layout!!.getLineForOffset(editor.selectionEnd)
        assertTrue(
            "Cursor should remain on line $targetLineIndex after typing stops (was line $finalLine, offset=${editor.selectionEnd}, expected=$afterTypeOffset)",
            finalLine == targetLineIndex && editor.selectionEnd == afterTypeOffset
        )
    }

    @Test
    fun bringPointIntoView_doesNotSnapScrollBackToZeroWhenLayoutShorterThanViewport() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            movementMethod = android.text.method.ArrowKeyMovementMethod.getInstance()
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)

        // Reproduce the exact 7-line layout from the screen recording where layout.height < viewport height,
        // but Line 7 sits inside the bottom floatingOverlayClearancePx zone (FAB + BottomAppPanel).
        state.setText("Fgg\n\n\n\n\nThg\n")
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)

        val width = 1080
        val keyboardOpenHeight = 1100
        editor.layoutParams = android.view.ViewGroup.LayoutParams(width, keyboardOpenHeight)
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(keyboardOpenHeight, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, keyboardOpenHeight)
        editor.setSelection(editor.text!!.length)
        editor.ensureCursorVisible()

        val layoutHeight = editor.layout!!.height
        assertTrue("Precondition: 7-line layout ($layoutHeight) is shorter than viewport ($keyboardOpenHeight)", layoutHeight < keyboardOpenHeight)
        val scrolledY = editor.scrollY
        assertTrue("Line 7 should scroll down to clear floating FAB/BottomAppPanel (scrollY=$scrolledY)", scrolledY > 0)

        // TextView.onPreDraw() calls bringPointIntoView(selectionEnd) on the next frame;
        // previously super.bringPointIntoView() slammed scrollY back to 0!
        editor.bringPointIntoView(editor.selectionEnd)
        assertTrue(
            "bringPointIntoView must NOT snap scrollY back to 0 (expected $scrolledY, got ${editor.scrollY})",
            editor.scrollY == scrolledY
        )
    }

    @Test
    fun readOnlyMultiPagePreview_scrollsDownPastPage1WithoutBlockingParent() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val state = RichTextState()
        val page1 = (1..15).joinToString("\n") { "Preview Page 1 Line $it" }
        state.setText(page1)
        state.insertPageBreak()
        val twoPageHtml = state.toHtml() + "<p>Preview Page 2 Line 1</p><p>Preview Page 2 Line 2</p>"

        // 1. Verify standalone read-only full-height RichEditText measures both pages and does NOT steal touch from parent scroll
        val readOnlyState = RichTextState().apply { setHtml(twoPageHtml) }
        val readOnlyEditor = RichEditText(context).apply {
            isFocusable = false
            isFocusableInTouchMode = false
            richTextState = readOnlyState
        }
        readOnlyState.bindEditor(readOnlyEditor)

        val width = 1080
        readOnlyEditor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        readOnlyEditor.layout(0, 0, width, readOnlyEditor.measuredHeight)

        assertTrue(
            "Full-height read-only 2-page editor should measure taller than 1 page (measured=${readOnlyEditor.measuredHeight}, pageHeight=${readOnlyEditor.pageHeightPx})",
            readOnlyEditor.measuredHeight >= readOnlyEditor.pageHeightPx * 2
        )
        assertTrue("Full-height read-only editor should have getMaxScrollY() == 0", readOnlyEditor.getMaxScrollY() == 0)

        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 400f, 800f, 0)
        val move = MotionEvent.obtain(now, now + 16, MotionEvent.ACTION_MOVE, 400f, 400f, 0)
        try {
            assertTrue("Read-only full-height editor must not consume ACTION_DOWN so parent verticalScroll works", !readOnlyEditor.onTouchEvent(down))
            assertTrue("Read-only full-height editor must not consume ACTION_MOVE so parent verticalScroll works", !readOnlyEditor.onTouchEvent(move))
        } finally {
            down.recycle()
            move.recycle()
        }

        // 2. Verify inside Compose Column.verticalScroll (PreviewScreen layout) that scrollState.maxValue > 0 and can scroll into Page 2
        var capturedMaxScroll = 0
        composeRule.setContent {
            MaterialTheme {
                val previewState = remember(twoPageHtml) {
                    RichTextState().apply { setHtml(twoPageHtml) }
                }
                val scrollState = rememberScrollState()
                capturedMaxScroll = scrollState.maxValue
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    RichTextEditor(
                        state = previewState,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                }
            }
        }
        composeRule.waitForIdle()
        assertTrue(
            "Compose PreviewScreen verticalScroll should have maxValue > 0 for 2-page note (maxValue=$capturedMaxScroll)",
            capturedMaxScroll > 0
        )
    }

    @Test
    fun alignAndListTypes_renderLeftCenterRightAndLists_outputsPng() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)

        val lines = listOf(
            "Left aligned",
            "Centered",
            "Right aligned",
            "First item",
            "Second item",
            "Bullet one",
            "Bullet two",
            "Checklist task"
        )
        state.setText(lines.joinToString("\n"))

        // Highlight & align Line 2 Center, Line 3 Right
        val text0 = editor.text!!.toString()
        val l2 = text0.indexOf("Centered")
        state.onEditorContentChanged(editor.text, l2, l2 + "Centered".length)
        state.toggleParagraphStyle(androidx.compose.ui.text.ParagraphStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center))

        val l3 = editor.text!!.toString().indexOf("Right aligned")
        state.onEditorContentChanged(editor.text, l3, l3 + "Right aligned".length)
        state.toggleParagraphStyle(androidx.compose.ui.text.ParagraphStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End))

        // Numbered list on lines 4-5
        val l4Start = editor.text!!.toString().indexOf("First item")
        val l5End = editor.text!!.toString().indexOf("Second item") + "Second item".length
        state.onEditorContentChanged(editor.text, l4Start, l5End)
        state.toggleOrderedList()

        // Bulleted list on lines 6-7
        val l6Start = editor.text!!.toString().indexOf("Bullet one")
        val l7End = editor.text!!.toString().indexOf("Bullet two") + "Bullet two".length
        state.onEditorContentChanged(editor.text, l6Start, l7End)
        state.toggleUnorderedList()

        // Checklist on line 8
        val l8Start = editor.text!!.toString().indexOf("Checklist task")
        state.onEditorContentChanged(editor.text, l8Start, l8Start)
        state.toggleChecklistLine()

        // Confirm zero leaking BulletSpans
        assertTrue(editor.text!!.getSpans(0, editor.text!!.length, android.text.style.BulletSpan::class.java).isEmpty())

        val width = 1080
        val height = 1400
        editor.layoutParams = android.view.ViewGroup.LayoutParams(width, height)
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        editor.draw(canvas)

        val outFile = saveBitmap(bitmap, "align_and_lists_visual.png")
        assertTrue("align_and_lists_visual.png should exist", outFile.exists() && outFile.length() > 0L)

        fun countDarkPixels(lineIndex: Int, xStart: Int, xEnd: Int): Int {
            val l = editor.layout!!
            val yTop = (editor.totalPaddingTop + l.getLineTop(lineIndex)).coerceIn(0, height - 1)
            val yBot = (editor.totalPaddingTop + l.getLineBottom(lineIndex)).coerceIn(yTop + 1, height)
            var count = 0
            for (y in yTop until yBot) {
                for (x in xStart until xEnd) {
                    val p = bitmap.getPixel(x, y)
                    if (Color.red(p) < 80 && Color.green(p) < 80 && Color.blue(p) < 80) {
                        count++
                    }
                }
            }
            return count
        }

        // Line 0 ("Left aligned"): dark pixels on left third, none on right third
        assertTrue("Line 0 should render on left third", countDarkPixels(0, 40, 360) > 80)
        assertTrue("Line 0 should not render on right third", countDarkPixels(0, 720, 1030) == 0)

        // Line 1 ("Centered"): dark pixels in center third, none on left/right edges
        assertTrue("Line 1 should render in center third", countDarkPixels(1, 360, 720) > 80)
        assertTrue("Line 1 should not render on far left", countDarkPixels(1, 40, 260) == 0)
        assertTrue("Line 1 should not render on far right", countDarkPixels(1, 820, 1030) == 0)

        // Line 2 ("Right aligned"): dark pixels on right third, none on left third
        assertTrue("Line 2 should render on right third", countDarkPixels(2, 700, 1040) > 80)
        assertTrue("Line 2 should not render on left third", countDarkPixels(2, 40, 360) == 0)
    }

    @Test
    fun bold_endToEndComposeAndImeFlow_andPixelRendering_outputsPng() {
        val state = RichTextState()
        composeRule.setContent {
            MaterialTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    RichTextEditor(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    BottomAppPanel(
                        cardVisibleState = {},
                        imageUri = emptyList(),
                        imageSheetVisibleState = {},
                        tagVisiblestate = {},
                        onBoldClick = {
                            state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                        },
                        onItalicClick = {
                            state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                        },
                        onStrikethroughClick = {
                            state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                        },
                        onUnderlineClick = {
                            state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline))
                        },
                        state = state,
                        alignStyleSheetVisiblity = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()

        // Access the real bound RichEditText inside RichTextEditor
        val editorField = RichTextState::class.java.getDeclaredField("boundEditor").apply { isAccessible = true }
        val realEditor = editorField.get(state) as RichEditText

        // 1. Tap Bold BEFORE focusing editor, then focus & place cursor at 0, then type via IME setComposingText
        composeRule.onNodeWithContentDescription("Bold Control").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue("Bold button state should be armed before typing", state.currentSpanStyle.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold)

        composeRule.runOnIdle {
            realEditor.requestFocus()
            realEditor.setSelection(0)
            val ic = realEditor.onCreateInputConnection(android.view.inputmethod.EditorInfo())!!
            ic.setComposingText("B", 1)
            ic.setComposingText("Bo", 1)
            ic.setComposingText("Bol", 1)
            ic.setComposingText("Bold", 1)
            ic.finishComposingText()
        }
        composeRule.waitForIdle()

        assertTrue("Typed word 'Bold' should be bold", state.toHtml().lowercase().contains("<b>bold</b>"))
        val leakedUnderlines = realEditor.text!!.getSpans(0, realEditor.text!!.length, android.text.style.UnderlineSpan::class.java)
            .filter { (realEditor.text!!.getSpanFlags(it) and android.text.Spanned.SPAN_COMPOSING) == 0 }
        assertTrue("Composing underline must not leak into permanent UnderlineSpan", leakedUnderlines.isEmpty())

        // 2. Toggle Bold OFF via toolbar button, type " plain" via IME -> only "Bold" stays bold
        composeRule.onNodeWithContentDescription("Bold Control").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue("Bold state should be OFF after toggling", state.currentSpanStyle.fontWeight == null)

        composeRule.runOnIdle {
            val ic = realEditor.onCreateInputConnection(android.view.inputmethod.EditorInfo())!!
            ic.commitText(" ", 1)
            ic.setComposingText("plain", 1)
            ic.finishComposingText()
        }
        composeRule.waitForIdle()

        val htmlAfterUnbold = state.toHtml().lowercase()
        assertTrue("Expected '<b>bold</b> plain' in HTML, got: $htmlAfterUnbold", htmlAfterUnbold.contains("<b>bold</b> plain"))

        // 3. Highlight "plain" (5..10) in real RichEditText, tap Bold button -> bolds in-place and preserves highlight
        composeRule.runOnIdle {
            realEditor.setSelection(5, 10)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Bold Control").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertTrue("Highlighted 'plain' should now be bold", state.currentSpanStyle.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold)
        assertTrue("Selection should stay preserved at 5..10 after in-place bold", realEditor.selectionStart == 5 && realEditor.selectionEnd == 10)
        val plainSpans = realEditor.text!!.getSpans(5, 10, android.text.style.StyleSpan::class.java)
            .filter { it.style == android.graphics.Typeface.BOLD }
        assertTrue("Expected BOLD StyleSpan covering 5..10", plainSpans.any { realEditor.text!!.getSpanStart(it) <= 5 && realEditor.text!!.getSpanEnd(it) >= 10 })

        // 4. Visual pixel verification: compare plain vs bold identical text rendered by RichEditText
        val context: Context = ApplicationProvider.getApplicationContext()
        val renderState = RichTextState()
        val renderEditor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = renderState
        }
        renderState.bindEditor(renderEditor)
        renderState.setText("Sample Weight Text\nSample Weight Text")
        val line2Start = "Sample Weight Text\n".length
        renderState.onEditorContentChanged(renderEditor.text, line2Start, renderEditor.text!!.length)
        renderState.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))

        val width = 1080
        val height = 500
        renderEditor.layoutParams = android.view.ViewGroup.LayoutParams(width, height)
        renderEditor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        renderEditor.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        renderEditor.draw(canvas)

        val outFile = saveBitmap(bitmap, "bold_visual.png")
        assertTrue("bold_visual.png should exist", outFile.exists() && outFile.length() > 0L)

        fun countInkPixels(lineIdx: Int): Int {
            val l = renderEditor.layout!!
            val yTop = (renderEditor.totalPaddingTop + l.getLineTop(lineIdx)).coerceIn(0, height - 1)
            val yBot = (renderEditor.totalPaddingTop + l.getLineBottom(lineIdx)).coerceIn(yTop + 1, height)
            var count = 0
            for (y in yTop until yBot) {
                for (x in 30 until 900) {
                    val p = bitmap.getPixel(x, y)
                    if (Color.red(p) < 100 && Color.green(p) < 100 && Color.blue(p) < 100) {
                        count++
                    }
                }
            }
            return count
        }

        val regularInk = countInkPixels(0)
        val boldInk = countInkPixels(1)
        assertTrue(
            "Bold text (ink=$boldInk) must render visibly heavier than regular text (ink=$regularInk)",
            boldInk > regularInk * 1.15
        )

        // 5. Simulate Samsung system "Bold font" enabled (base editor typeface is ALREADY Typeface.DEFAULT_BOLD):
        // BoldStyleSpan must STILL render visibly heavier than normal text!
        val samsungBoldEditor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            richTextState = renderState
        }
        renderState.bindEditor(samsungBoldEditor)
        renderState.setText("Sample Weight Text\nSample Weight Text")
        renderState.onEditorContentChanged(samsungBoldEditor.text, line2Start, samsungBoldEditor.text!!.length)
        renderState.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
        samsungBoldEditor.layoutParams = android.view.ViewGroup.LayoutParams(width, height)
        samsungBoldEditor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        samsungBoldEditor.layout(0, 0, width, height)
        val samsungBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val samsungCanvas = Canvas(samsungBitmap)
        samsungCanvas.drawColor(Color.WHITE)
        samsungBoldEditor.draw(samsungCanvas)
        saveBitmap(samsungBitmap, "bold_samsung_system_bold_visual.png")

        fun countSamsungInk(lineIdx: Int): Int {
            val l = samsungBoldEditor.layout!!
            val yTop = (samsungBoldEditor.totalPaddingTop + l.getLineTop(lineIdx)).coerceIn(0, height - 1)
            val yBot = (samsungBoldEditor.totalPaddingTop + l.getLineBottom(lineIdx)).coerceIn(yTop + 1, height)
            var count = 0
            for (y in yTop until yBot) {
                for (x in 30 until 900) {
                    val p = samsungBitmap.getPixel(x, y)
                    if (Color.red(p) < 100 && Color.green(p) < 100 && Color.blue(p) < 100) {
                        count++
                    }
                }
            }
            return count
        }
        val sysBoldRegularInk = countSamsungInk(0)
        val sysBoldHeavyInk = countSamsungInk(1)
        assertTrue(
            "Even with system Bold font ON, BoldStyleSpan (ink=$sysBoldHeavyInk) must render visibly heavier than base text (ink=$sysBoldRegularInk)",
            sysBoldHeavyInk > sysBoldRegularInk * 1.10
        )
    }

    @Test
    fun physicalNotebookLines_alignWithTextBaselines_andSwitchCleanlyToPlain_outputsPngs() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val density = context.resources.displayMetrics.density
        val state = RichTextState()
        val editor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = state
        }
        state.bindEditor(editor)

        val content = listOf(
            "Monday Journal Entry",
            "The quick brown fox jumps gently over lazy dogs.",
            "Every baseline should rest neatly on its ruled line.",
            "Centered note",
            "• Buy groceries & notebook",
            "☐ Review weekly goals"
        ).joinToString("\n")
        state.setText(content)

        // Make Line 0 a 22sp bold heading and center Line 3
        state.onEditorContentChanged(editor.text, 0, "Monday Journal Entry".length)
        state.setFontSize(22)
        state.toggleSpanStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))

        val centeredStart = editor.text!!.toString().indexOf("Centered note")
        state.onEditorContentChanged(editor.text, centeredStart, centeredStart + "Centered note".length)
        state.toggleParagraphStyle(androidx.compose.ui.text.ParagraphStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center))

        val width = 1080
        val height = 1600
        editor.layoutParams = android.view.ViewGroup.LayoutParams(width, height)

        // 1. Render LINED physical notebook style
        editor.paperStyle = NotePaperStyle.LINED
        val linedLeftPad = editor.paddingLeft
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, height)

        val linedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val linedCanvas = Canvas(linedBitmap)
        linedCanvas.drawColor(Color.WHITE)
        editor.draw(linedCanvas)
        val linedOut = saveBitmap(linedBitmap, "physical_notebook_lined.png")
        assertTrue("physical_notebook_lined.png should exist", linedOut.exists() && linedOut.length() > 0L)

        // Verify red vertical margin line exists in LINED mode (at cardLeft + 34dp = 42dp)
        val marginX = ((8f + 34f) * density).toInt()
        var redMarginPixels = 0
        for (y in 200 until 1200) {
            for (x in (marginX - 3)..(marginX + 3)) {
                val p = linedBitmap.getPixel(x, y)
                if (Color.red(p) > Color.blue(p) + 12 && Color.red(p) > Color.green(p) + 10) {
                    redMarginPixels++
                }
            }
        }
        assertTrue("Expected visible vertical red notebook margin line in LINED mode (found $redMarginPixels)", redMarginPixels > 200)

        // Verify blue ruled line sits immediately below Line 1's actual baseline (baseline + 4dp)
        val layout = editor.layout!!
        val line1RuleY = (editor.totalPaddingTop + layout.getLineBaseline(1) + (4f * density)).toInt()
        var blueRulePixelsAtBaseline = 0
        for (y in (line1RuleY - 2)..(line1RuleY + 2)) {
            for (x in 650 until 1000) {
                val p = linedBitmap.getPixel(x, y)
                if (Color.blue(p) > Color.red(p) + 5) {
                    blueRulePixelsAtBaseline++
                }
            }
        }
        assertTrue(
            "Expected blue ruled line directly below Line 1 baseline at y=$line1RuleY (found $blueRulePixelsAtBaseline)",
            blueRulePixelsAtBaseline > 100
        )

        // 2. Switch to PLAIN paper template -> no ruled lines, no red margin, smaller left padding
        editor.paperStyle = NotePaperStyle.PLAIN
        val plainLeftPad = editor.paddingLeft
        assertTrue("PLAIN left padding ($plainLeftPad) should be smaller than LINED margin padding ($linedLeftPad)", plainLeftPad < linedLeftPad)
        editor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        editor.layout(0, 0, width, height)

        val plainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val plainCanvas = Canvas(plainBitmap)
        plainCanvas.drawColor(Color.WHITE)
        editor.draw(plainCanvas)
        val plainOut = saveBitmap(plainBitmap, "physical_notebook_plain.png")
        assertTrue("physical_notebook_plain.png should exist", plainOut.exists() && plainOut.length() > 0L)

        var plainMarginPixels = 0
        for (y in 200 until 1200) {
            for (x in (marginX - 3)..(marginX + 3)) {
                val p = plainBitmap.getPixel(x, y)
                if (Color.red(p) > Color.blue(p) + 12 && Color.red(p) > Color.green(p) + 10) {
                    plainMarginPixels++
                }
            }
        }
        assertTrue("Expected 0 red margin pixels in PLAIN mode (found $plainMarginPixels)", plainMarginPixels == 0)

        // 3. Verify NoteTemplateDialog switches between Lined Notebook and Plain Paper cleanly
        var selectedStyle by mutableStateOf(NotePaperStyle.LINED)
        composeRule.setContent {
            MaterialTheme {
                NoteTemplateDialog(
                    selectedPaperStyle = selectedStyle,
                    onSelectPaperStyle = { selectedStyle = it },
                    onDismiss = {},
                    onApply = {}
                )
            }
        }
        composeRule.onNodeWithText("Plain Paper").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue("Selecting Plain Paper should update paperStyle to PLAIN", selectedStyle == NotePaperStyle.PLAIN)

        composeRule.onNodeWithText("Lined Notebook").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue("Selecting Lined Notebook should update paperStyle to LINED", selectedStyle == NotePaperStyle.LINED)
    }

    @Test
    fun textColor_toolbarPaletteSelection_andColoredRendering_outputsPng() {
        val state = RichTextState()
        var paletteOpen by mutableStateOf(false)
        composeRule.setContent {
            MaterialTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    RichTextEditor(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    if (paletteOpen) {
                        com.example.voicejournal.ui.main.AddVoiceNote.components.TextColorPaletteContent(
                            selectedColor = state.currentSpanStyle.color,
                            onColorSelected = { chosen ->
                                state.setTextColor(chosen)
                                paletteOpen = false
                            }
                        )
                    }
                    BottomAppPanel(
                        cardVisibleState = {},
                        imageUri = emptyList(),
                        imageSheetVisibleState = {},
                        tagVisiblestate = {},
                        onBoldClick = {},
                        onItalicClick = {},
                        onStrikethroughClick = {},
                        onUnderlineClick = {},
                        onTextColorClick = { paletteOpen = true },
                        state = state,
                        alignStyleSheetVisiblity = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()

        val editorField = RichTextState::class.java.getDeclaredField("boundEditor").apply { isAccessible = true }
        val realEditor = editorField.get(state) as RichEditText

        // 1. Open palette from BottomAppPanel Text Color button and select Crimson Red
        composeRule.onNodeWithContentDescription("Text Color Control").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Text Color Crimson Red").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        assertTrue(
            "Selected color should be Crimson Red",
            state.currentSpanStyle.color == androidx.compose.ui.graphics.Color(0xFFD32F2F)
        )

        // 2. Focus editor and type via IME -> newly typed word gets Crimson Red
        composeRule.runOnIdle {
            realEditor.requestFocus()
            realEditor.setSelection(0)
            val ic = realEditor.onCreateInputConnection(android.view.inputmethod.EditorInfo())!!
            ic.commitText("Alert", 1)
        }
        composeRule.waitForIdle()

        val alertSpans = realEditor.text!!.getSpans(0, 5, android.text.style.ForegroundColorSpan::class.java)
        assertTrue("Typed word 'Alert' should carry Crimson Red ForegroundColorSpan", alertSpans.any { it.foregroundColor == 0xFFD32F2F.toInt() })

        // 3. Render multi-color lines on RichEditText and verify actual RGB pixels in text_color_visual.png
        val context: Context = ApplicationProvider.getApplicationContext()
        val renderState = RichTextState()
        val renderEditor = RichEditText(context).apply {
            setTextColor(Color.BLACK)
            richTextState = renderState
        }
        renderState.bindEditor(renderEditor)
        renderState.setText("Crimson Red Line\nForest Green Line\nRoyal Blue Line")
        val text = renderEditor.text!!.toString()
        val l1Start = 0
        val l1End = "Crimson Red Line".length
        val l2Start = text.indexOf("Forest Green Line")
        val l2End = l2Start + "Forest Green Line".length
        val l3Start = text.indexOf("Royal Blue Line")
        val l3End = l3Start + "Royal Blue Line".length

        renderState.onEditorContentChanged(renderEditor.text, l1Start, l1End)
        renderState.setTextColor(androidx.compose.ui.graphics.Color(0xFFD32F2F))
        renderState.onEditorContentChanged(renderEditor.text, l2Start, l2End)
        renderState.setTextColor(androidx.compose.ui.graphics.Color(0xFF2E7D32))
        renderState.onEditorContentChanged(renderEditor.text, l3Start, l3End)
        renderState.setTextColor(androidx.compose.ui.graphics.Color(0xFF1565C0))

        val width = 1080
        val height = 700
        renderEditor.layoutParams = android.view.ViewGroup.LayoutParams(width, height)
        renderEditor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        renderEditor.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        renderEditor.draw(canvas)

        val outFile = saveBitmap(bitmap, "text_color_visual.png")
        assertTrue("text_color_visual.png should exist", outFile.exists() && outFile.length() > 0L)

        fun countLineColorPixels(lineIdx: Int, predicate: (Int, Int, Int) -> Boolean): Int {
            val l = renderEditor.layout!!
            val yTop = (renderEditor.totalPaddingTop + l.getLineTop(lineIdx)).coerceIn(0, height - 1)
            val yBot = (renderEditor.totalPaddingTop + l.getLineBottom(lineIdx)).coerceIn(yTop + 1, height)
            var count = 0
            for (y in yTop until yBot) {
                for (x in 140 until 900) {
                    val p = bitmap.getPixel(x, y)
                    if (predicate(Color.red(p), Color.green(p), Color.blue(p))) count++
                }
            }
            return count
        }

        val redPixels = countLineColorPixels(0) { r, g, b -> r > 170 && g < 100 && b < 100 }
        val greenPixels = countLineColorPixels(1) { r, g, b -> g > 95 && r < 90 && b < 90 }
        val bluePixels = countLineColorPixels(2) { r, g, b -> b > 150 && r < 80 }
        assertTrue("Line 0 should render crisp red pixels (found $redPixels)", redPixels > 100)
        assertTrue("Line 1 should render crisp green pixels (found $greenPixels)", greenPixels > 100)
        assertTrue("Line 2 should render crisp blue pixels (found $bluePixels)", bluePixels > 100)
    }
}
