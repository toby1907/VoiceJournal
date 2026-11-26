package com.example.voicejournal.ui.main.gallery

import android.content.Context
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournalDatabase
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.Data.model.ImageFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
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
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xxhdpi")
class GalleryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var db: VoiceJournalDatabase
    private lateinit var repository: VoiceJournalRepositoryImpl
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var settingsRepository: SettingsRepository

    private val uri1 = Uri.parse("content://media/external/images/media/1")
    private val uri2 = Uri.parse("content://media/external/images/media/2")
    private val uri3 = Uri.parse("content://media/external/images/media/3")

    private val sampleImages = listOf(
        ImageFile(uri = uri1, name = "IMG_001.jpg", bucketName = "Camera", dateAddedSeconds = 300L),
        ImageFile(uri = uri2, name = "IMG_002.jpg", bucketName = "Camera", dateAddedSeconds = 200L),
        ImageFile(uri = uri3, name = "Screenshot_1.png", bucketName = "Screenshots", dateAddedSeconds = 100L)
    )

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
            produceFile = { File(tempFolder.newFolder(), "gallery_settings.preferences_pb") }
        )
        settingsRepository = SettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        db.close()
        dataStoreScope.cancel()
    }

    private fun createViewModel(): GalleryScreenViewModel {
        val vm = GalleryScreenViewModel(
            savedStateHandle = SavedStateHandle(),
            voiceJournalRepository = repository,
            context = context,
            settingsRepository = settingsRepository
        )
        vm.setImagesForTest(sampleImages)
        return vm
    }

    private fun waitUntil(timeoutMs: Long = 5000L, predicate: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            ShadowLooper.idleMainLooper()
            if (predicate()) return
            Thread.sleep(20)
        }
        ShadowLooper.idleMainLooper()
        assertTrue("Condition not met within ${timeoutMs}ms", predicate())
    }

    @Test
    fun multiSelection_togglesEachPhotoIndependentlyAndMaintainsNumberedOrder() {
        val vm = createViewModel()
        var closed = false

        composeRule.setContent {
            MaterialTheme {
                val state by vm.uiState.collectAsState()
                GalleryScreenContent(
                    uiState = state,
                    onClose = { closed = true },
                    onToggleImage = vm::toggleImageSelection,
                    onSelectAlbum = vm::selectAlbum,
                    onClearSelection = vm::clearSelection,
                    onConfirmSelection = {},
                    onRetry = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Select Photos").assertIsDisplayed()
        composeRule.onNode(hasTestTag("gallery_confirm_button")).assertDoesNotExist()

        // Tap 1st, 2nd, and 3rd photos consecutively — each must select on the very first tap
        composeRule.onNode(hasTestTag("gallery_item_$uri1")).performClick()
        composeRule.onNode(hasTestTag("gallery_item_$uri2")).performClick()
        composeRule.onNode(hasTestTag("gallery_item_$uri3")).performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(uri1, uri2, uri3), vm.uiState.value.selectedUrisOrder)
        composeRule.onNodeWithText("3 selected").assertIsDisplayed()
        composeRule.onNodeWithText("Add 3").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Selected 1").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Selected 2").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Selected 3").assertIsDisplayed()

        // Deselect 1st photo -> 2nd becomes #1 and 3rd becomes #2
        composeRule.onNode(hasTestTag("gallery_item_$uri1")).performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(uri2, uri3), vm.uiState.value.selectedUrisOrder)
        assertEquals(1, vm.uiState.value.selectionIndexOf(uri2))
        assertEquals(2, vm.uiState.value.selectionIndexOf(uri3))
        composeRule.onNodeWithText("2 selected").assertIsDisplayed()

        // Close button remains available even while items are selected
        composeRule.onNode(hasTestTag("gallery_close_button")).performClick()
        assertTrue("Close button should always work", closed)
    }

    @Test
    fun albumFilteringAndClearSelection_preserveSelectionAcrossAlbumsAndClearCleanly() {
        val vm = createViewModel()

        composeRule.setContent {
            MaterialTheme {
                val state by vm.uiState.collectAsState()
                GalleryScreenContent(
                    uiState = state,
                    onClose = {},
                    onToggleImage = vm::toggleImageSelection,
                    onSelectAlbum = vm::selectAlbum,
                    onClearSelection = vm::clearSelection,
                    onConfirmSelection = {},
                    onRetry = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Select a Camera photo
        composeRule.onNode(hasTestTag("gallery_item_$uri1")).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add Photo").assertIsDisplayed()

        // Filter to Screenshots album
        composeRule.onNode(hasTestTag("gallery_album_Screenshots")).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasTestTag("gallery_item_$uri3")).assertIsDisplayed()
        composeRule.onNode(hasTestTag("gallery_item_$uri1")).assertDoesNotExist()

        // Select screenshot too -> selection from Camera is preserved
        composeRule.onNode(hasTestTag("gallery_item_$uri3")).performClick()
        composeRule.waitForIdle()
        assertEquals(listOf(uri1, uri3), vm.uiState.value.selectedUrisOrder)
        composeRule.onNodeWithText("2 selected").assertIsDisplayed()

        // Tap Clear -> resets selection and hides confirm bar
        composeRule.onNode(hasTestTag("gallery_clear_button")).performClick()
        composeRule.waitForIdle()
        assertTrue(vm.uiState.value.selectedUrisOrder.isEmpty())
        composeRule.onNodeWithText("Select Photos").assertIsDisplayed()
        composeRule.onNode(hasTestTag("gallery_confirm_button")).assertDoesNotExist()
    }

    @Test
    fun confirmSelection_persistsUrisBeforeRunningOnSavedCallback() = runBlocking {
        val vm = createViewModel()
        vm.toggleImageSelection(uri1)
        vm.toggleImageSelection(uri3)

        var savedCallbackRan = false
        var urisAtCallbackTime: Set<String> = emptySet()

        vm.confirmSelection {
            urisAtCallbackTime = runBlocking { settingsRepository.getSelectedUris().first() }
            savedCallbackRan = true
        }

        waitUntil { savedCallbackRan }

        assertEquals(
            setOf(uri1.toString(), uri3.toString()),
            urisAtCallbackTime
        )
    }

    @Test
    fun emptyAndErrorStates_displayFriendlyFeedbackAndRetry() {
        var retried = false
        composeRule.setContent {
            MaterialTheme {
                GalleryScreenContent(
                    uiState = GalleryUiState(
                        images = emptyList(),
                        isLoading = false,
                        errorMessage = "Photo library permission is required to view your images."
                    ),
                    onClose = {},
                    onToggleImage = {},
                    onSelectAlbum = {},
                    onClearSelection = {},
                    onConfirmSelection = {},
                    onRetry = { retried = true }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Photo library permission is required to view your images.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        assertTrue("Retry button should trigger onRetry", retried)
    }
}
