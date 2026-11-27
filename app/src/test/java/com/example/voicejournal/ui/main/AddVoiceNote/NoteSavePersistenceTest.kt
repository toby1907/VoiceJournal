package com.example.voicejournal.ui.main.AddVoiceNote

import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournalDatabase
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.core.AudioPlayer
import com.example.voicejournal.core.AudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NoteSavePersistenceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var db: VoiceJournalDatabase
    private lateinit var repository: VoiceJournalRepositoryImpl
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var settingsRepository: SettingsRepository

    private class FakeAudioRecorder : AudioRecorder {
        override fun start(outputFile: String) = Unit
        override fun stop() = Unit
        override fun pause() = Unit
        override fun resume() = Unit
        override fun release() = Unit
        override fun prepare() = Unit
    }

    private class FakeAudioPlayer(
        var fakeDurationMs: Int = 65_000
    ) : AudioPlayer {
        var playing: Boolean = false
        var positionMs: Int = 0
        var speed: Float = 1f
        var startCount: Int = 0
        var pauseCount: Int = 0
        var resumeCount: Int = 0
        var stopCount: Int = 0
        var lastSeekMs: Int? = null
        var completionListener: MediaPlayer.OnCompletionListener? = null

        override fun start(file: String) {
            startCount++
            playing = true
        }
        override fun stop() {
            stopCount++
            playing = false
            positionMs = 0
        }
        override fun pause() {
            pauseCount++
            playing = false
        }
        override fun resume() {
            resumeCount++
            playing = true
        }
        override fun seekTo(positionMs: Int) {
            this.positionMs = positionMs
            lastSeekMs = positionMs
        }
        override fun getCurrentPosition(): Int = positionMs
        override fun setPlaybackSpeed(speed: Float) {
            this.speed = speed
        }
        override fun release() {
            playing = false
        }
        override fun isPlaying(): Boolean = playing
        override fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) {
            completionListener = listener
        }
        override fun getDuration(): Int = fakeDurationMs
        override fun prepare() = Unit
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, VoiceJournalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VoiceJournalRepositoryImpl(db.voiceJournalDao())
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(tempFolder.newFolder(), "settings.preferences_pb") }
        )
        settingsRepository = SettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        db.close()
        dataStoreScope.cancel()
    }

    private fun createViewModel(): AddVoiceNoteViewModel {
        return AddVoiceNoteViewModel(
            savedStateHandle = SavedStateHandle(),
            voiceJournalRepository = repository,
            settingsRepository = settingsRepository,
            context = context,
            audioRecorder = FakeAudioRecorder(),
            audioPlayer = FakeAudioPlayer()
        )
    }

    private fun waitUntil(timeoutMs: Long = 5000L, predicate: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            ShadowLooper.idleMainLooper()
            if (predicate()) return
            Thread.sleep(20)
        }
        ShadowLooper.idleMainLooper()
        assertTrue("Condition was not satisfied within ${timeoutMs}ms", predicate())
    }

    @Test
    fun saveNote_persistsTitleFontSizeDrawingAndFloatingText_andEmitsSaveEvent() = runBlocking {
        val viewModel = createViewModel()
        val events = mutableListOf<AddVoiceNoteViewModel.UiEvent>()
        val collectJob = launch(Dispatchers.Unconfined) {
            viewModel.eventFlow.collect { events.add(it) }
        }

        try {
            viewModel.onEvent(AddEditNoteEvent.EnteredContent("Sprint Retro"))
            viewModel.addDrawingStroke(
                pageIndex = 0,
                normalizedPoints = listOf(0.1f to 0.2f, 0.5f to 0.6f),
                colorArgb = 0xFF1A73E8L,
                strokeWidthPx = 8f,
                alpha = 1f,
                tool = "PEN"
            )
            viewModel.addFloatingTextBox(pageIndex = 0, initialText = "Action item")

            val richHtml = "<p><span style=\"font-size:24px;\" data-font-size=\"24\">Highlights</span> and notes</p>"
            viewModel.onEvent(AddEditNoteEvent.SaveNote(richHtml))

            waitUntil {
                events.any { it is AddVoiceNoteViewModel.UiEvent.SaveNote }
            }

            val savedNotes = db.voiceJournalDao().getAllLetters().first()
            assertEquals(1, savedNotes.size)
            val saved = savedNotes.first()
            assertEquals("Sprint Retro", saved.content)
            assertTrue(saved.title.contains("data-font-size=\"24\""))

            val blocks = saved.noteBlocks.orEmpty()
            assertTrue("Should persist DRAWING block", blocks.any { it.type == NoteBlockType.DRAWING.name })
            assertTrue(
                "Should persist FLOATING_TEXT block separately from TEXT",
                blocks.any { it.type == NoteBlockType.FLOATING_TEXT.name && it.text == "Action item" }
            )
        } finally {
            collectJob.cancel()
        }
    }

    @Test
    fun saveNoteAndSaveNoteOnly_doNotCreateDuplicateRows() = runBlocking {
        val viewModel = createViewModel()
        val events = mutableListOf<AddVoiceNoteViewModel.UiEvent>()
        val collectJob = launch(Dispatchers.Unconfined) {
            viewModel.eventFlow.collect { events.add(it) }
        }

        try {
            viewModel.onEvent(AddEditNoteEvent.EnteredContent("No Dupes"))
            viewModel.onEvent(AddEditNoteEvent.SaveNote("<p>First version</p>"))

            waitUntil {
                events.any { it is AddVoiceNoteViewModel.UiEvent.SaveNote }
            }

            // Simulate subsequent ON_PAUSE auto-save
            viewModel.onEvent(AddEditNoteEvent.SaveNoteOnly("<p>Updated on pause</p>"))
            waitUntil {
                runBlocking {
                    db.voiceJournalDao().getAllLetters().first()
                        .any { it.title.contains("Updated on pause") }
                }
            }

            val savedNotes = db.voiceJournalDao().getAllLetters().first()
            assertEquals("Subsequent SaveNoteOnly must update same row, not duplicate", 1, savedNotes.size)
            assertEquals("No Dupes", savedNotes.first().content)
            assertTrue(savedNotes.first().title.contains("Updated on pause"))
        } finally {
            collectJob.cancel()
        }
    }

    @Test
    fun saveNote_drawingOnlyNote_persistsAsUntitled() = runBlocking {
        val viewModel = createViewModel()
        val events = mutableListOf<AddVoiceNoteViewModel.UiEvent>()
        val collectJob = launch(Dispatchers.Unconfined) {
            viewModel.eventFlow.collect { events.add(it) }
        }

        try {
            // Blank title and empty text, only a drawing stroke
            viewModel.addDrawingStroke(
                pageIndex = 0,
                normalizedPoints = listOf(0.2f to 0.2f, 0.7f to 0.7f),
                colorArgb = 0xFFD93025L,
                strokeWidthPx = 6f,
                alpha = 1f,
                tool = "PEN"
            )
            viewModel.onEvent(AddEditNoteEvent.SaveNote(""))

            waitUntil {
                events.any { it is AddVoiceNoteViewModel.UiEvent.SaveNote }
            }

            val savedNotes = db.voiceJournalDao().getAllLetters().first()
            assertEquals(1, savedNotes.size)
            val saved = savedNotes.first()
            assertEquals("Untitled", saved.content)
            assertTrue(saved.noteBlocks.orEmpty().any { it.type == NoteBlockType.DRAWING.name })
        } finally {
            collectJob.cancel()
        }
    }

    @Test
    fun attachImage_dragResizePersistAndReload_preservesExactCanvasLayout() = runBlocking {
        val viewModel = createViewModel()
        val events = mutableListOf<AddVoiceNoteViewModel.UiEvent>()
        val collectJob = launch(Dispatchers.Unconfined) {
            viewModel.eventFlow.collect { events.add(it) }
        }

        try {
            viewModel.setActivePageIndex(0)
            viewModel.attachImageUris(listOf("content://media/external/images/media/42"))

            val attached = viewModel.noteBlocks.firstOrNull { it.type == NoteBlockType.IMAGE.name }
            assertTrue("Attaching an image should immediately create an IMAGE NoteBlock", attached != null)
            assertEquals(
                listOf("content://media/external/images/media/42"),
                viewModel.tempImageUris.value.imageFileUris
            )

            // Drag image to new position and resize it
            viewModel.updateBlockCanvasLayout(
                blockId = attached!!.id,
                pageIndex = 0,
                x = 96f,
                y = 210f,
                width = attached.canvasWidth,
                height = attached.canvasHeight
            )
            viewModel.resizeBlockCanvas(
                blockId = attached.id,
                width = 275f,
                height = 230f
            )

            // Image-only note (empty text) must save cleanly
            viewModel.onEvent(AddEditNoteEvent.SaveNote(""))
            waitUntil {
                events.any { it is AddVoiceNoteViewModel.UiEvent.SaveNote }
            }

            val savedNotes = db.voiceJournalDao().getAllLetters().first()
            assertEquals(1, savedNotes.size)
            val saved = savedNotes.first()
            assertEquals("Untitled", saved.content)

            // Reopen the note in a fresh ViewModel and verify exact coordinates/size reload
            val reopenedVm = AddVoiceNoteViewModel(
                savedStateHandle = SavedStateHandle(mapOf("noteId" to saved.id!!)),
                voiceJournalRepository = repository,
                settingsRepository = settingsRepository,
                context = context,
                audioRecorder = FakeAudioRecorder(),
                audioPlayer = FakeAudioPlayer()
            )
            waitUntil {
                reopenedVm.noteBlocks.any { it.type == NoteBlockType.IMAGE.name }
            }

            val reloadedImage = reopenedVm.noteBlocks.first { it.type == NoteBlockType.IMAGE.name }
            assertEquals("content://media/external/images/media/42", reloadedImage.mediaUri)
            assertEquals(0, reloadedImage.canvasPageIndex)
            assertEquals(96f, reloadedImage.canvasX)
            assertEquals(210f, reloadedImage.canvasY)
            assertEquals(275f, reloadedImage.canvasWidth)
            assertEquals(230f, reloadedImage.canvasHeight)
        } finally {
            collectJob.cancel()
        }
    }

    @Test
    fun removeImageBlock_deletesBlockAndUri_andDoesNotReappearOnSave() = runBlocking {
        val viewModel = createViewModel()
        val events = mutableListOf<AddVoiceNoteViewModel.UiEvent>()
        val collectJob = launch(Dispatchers.Unconfined) {
            viewModel.eventFlow.collect { events.add(it) }
        }

        try {
            viewModel.attachImageUris(listOf("content://img/first", "content://img/second"))
            val images = viewModel.noteBlocks.filter { it.type == NoteBlockType.IMAGE.name }
            assertEquals(2, images.size)

            val first = images[0]
            val second = images[1]
            viewModel.updateBlockCanvasLayout(second.id, 0, 115f, 185f, 240f, 195f)

            // Remove the first image box
            viewModel.removeBlock(first.id)

            assertEquals(
                listOf("content://img/second"),
                viewModel.noteBlocks.filter { it.type == NoteBlockType.IMAGE.name }.map { it.mediaUri }
            )
            assertEquals(
                listOf("content://img/second"),
                viewModel.tempImageUris.value.imageFileUris
            )

            viewModel.onEvent(AddEditNoteEvent.SaveNote("<p>Kept second photo</p>"))
            waitUntil {
                events.any { it is AddVoiceNoteViewModel.UiEvent.SaveNote }
            }

            val saved = db.voiceJournalDao().getAllLetters().first().first()
            val savedImages = saved.noteBlocks.orEmpty().filter { it.type == NoteBlockType.IMAGE.name }
            assertEquals(1, savedImages.size)
            assertEquals("content://img/second", savedImages.first().mediaUri)
            assertEquals(115f, savedImages.first().canvasX)
            assertEquals(185f, savedImages.first().canvasY)
            assertEquals(listOf("content://img/second"), saved.imageUris)
        } finally {
            collectJob.cancel()
        }
    }

    @Test
    fun gallerySelectionDuringExistingNoteHydration_appendsNewImageWithoutClobberingExisting() = runBlocking {
        val initialVm = createViewModel()
        val events = mutableListOf<AddVoiceNoteViewModel.UiEvent>()
        val collectJob = launch(Dispatchers.Unconfined) {
            initialVm.eventFlow.collect { events.add(it) }
        }

        val savedNoteId: Int
        try {
            initialVm.attachImageUris(listOf("content://img/existing"))
            val existingBlock = initialVm.noteBlocks.first { it.type == NoteBlockType.IMAGE.name }
            initialVm.updateBlockCanvasLayout(existingBlock.id, 0, 88f, 144f, 210f, 165f)
            initialVm.onEvent(AddEditNoteEvent.SaveNote("<p>Existing note</p>"))
            waitUntil { events.any { it is AddVoiceNoteViewModel.UiEvent.SaveNote } }
            savedNoteId = db.voiceJournalDao().getAllLetters().first().first().id!!
        } finally {
            collectJob.cancel()
        }

        // Simulate GalleryScreen writing a newly selected image into SettingsRepository before returning
        settingsRepository.saveSelectedUris(listOf("content://img/from_gallery"))

        val returningVm = AddVoiceNoteViewModel(
            savedStateHandle = SavedStateHandle(mapOf("noteId" to savedNoteId)),
            voiceJournalRepository = repository,
            settingsRepository = settingsRepository,
            context = context,
            audioRecorder = FakeAudioRecorder(),
            audioPlayer = FakeAudioPlayer()
        )

        waitUntil {
            returningVm.noteBlocks.count { it.type == NoteBlockType.IMAGE.name } == 2
        }

        val images = returningVm.noteBlocks.filter { it.type == NoteBlockType.IMAGE.name }
        val keptExisting = images.first { it.mediaUri == "content://img/existing" }
        val newlyAdded = images.first { it.mediaUri == "content://img/from_gallery" }

        assertEquals(88f, keptExisting.canvasX)
        assertEquals(144f, keptExisting.canvasY)
        assertEquals(210f, keptExisting.canvasWidth)
        assertEquals(165f, keptExisting.canvasHeight)
        assertTrue(newlyAdded.canvasWidth != null && newlyAdded.canvasWidth!! > 0f)
        assertEquals(2, returningVm.tempImageUris.value.imageFileUris?.size)
    }

    @Test
    fun voicePlaybackControls_playPauseResumeSeekSpeedStopAndRemove_workEndToEnd() = runBlocking {
        val fakePlayer = FakeAudioPlayer(fakeDurationMs = 65_000)
        val viewModel = AddVoiceNoteViewModel(
            savedStateHandle = SavedStateHandle(),
            voiceJournalRepository = repository,
            settingsRepository = settingsRepository,
            context = context,
            audioRecorder = FakeAudioRecorder(),
            audioPlayer = fakePlayer
        )

        // Start and finish recording
        viewModel.onEvent(AddEditNoteEvent.Recording(""))
        waitUntil { viewModel.noteFileName.value.text.isNotBlank() }
        assertTrue("Voice-only note should be persistable", viewModel.hasPersistableAttachments())

        viewModel.onEvent(AddEditNoteEvent.StopRecording)
        waitUntil { !viewModel.recordState.value }

        // Play
        viewModel.togglePlayPauseRecording()
        waitUntil { viewModel.voicePlaybackState.value.isPlaying }
        assertEquals(1, fakePlayer.startCount)
        assertEquals(65_000, viewModel.voicePlaybackState.value.durationMs)

        // Forward 5s
        viewModel.seekRecordingBy(5000)
        assertEquals(5000, fakePlayer.lastSeekMs)
        assertEquals(5000, viewModel.voicePlaybackState.value.currentPositionMs)

        // Pause
        viewModel.togglePlayPauseRecording()
        assertEquals(1, fakePlayer.pauseCount)
        assertEquals(false, viewModel.voicePlaybackState.value.isPlaying)
        assertEquals(true, viewModel.voicePlaybackState.value.isPaused)

        // Resume from paused position (does not restart from 0)
        viewModel.togglePlayPauseRecording()
        assertEquals(1, fakePlayer.resumeCount)
        assertEquals(1, fakePlayer.startCount)
        assertEquals(true, viewModel.voicePlaybackState.value.isPlaying)

        // Cycle playback speed: 1x -> 1.25x -> 1.5x -> 2x -> 1x
        viewModel.cyclePlaybackSpeed()
        assertEquals(1.25f, viewModel.voicePlaybackState.value.playbackSpeed)
        assertEquals(1.25f, fakePlayer.speed)
        viewModel.cyclePlaybackSpeed()
        assertEquals(1.5f, viewModel.voicePlaybackState.value.playbackSpeed)
        viewModel.cyclePlaybackSpeed()
        assertEquals(2f, viewModel.voicePlaybackState.value.playbackSpeed)
        viewModel.cyclePlaybackSpeed()
        assertEquals(1f, viewModel.voicePlaybackState.value.playbackSpeed)

        // Stop resets back to 00:00
        viewModel.stopRecordingPlayback()
        assertEquals(false, viewModel.voicePlaybackState.value.isPlaying)
        assertEquals(false, viewModel.voicePlaybackState.value.isPaused)
        assertEquals(0, viewModel.voicePlaybackState.value.currentPositionMs)

        // Remove recording clears file path and state
        viewModel.removeVoiceRecording()
        assertEquals("", viewModel.noteFileName.value.text)
        assertEquals(0, viewModel.voicePlaybackState.value.durationMs)
    }
}
