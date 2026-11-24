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

    private class FakeAudioPlayer : AudioPlayer {
        override fun start(file: String) = Unit
        override fun stop() = Unit
        override fun pause() = Unit
        override fun release() = Unit
        override fun isPlaying(): Boolean = false
        override fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) = Unit
        override fun getDuration(): Int = 0
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
}
