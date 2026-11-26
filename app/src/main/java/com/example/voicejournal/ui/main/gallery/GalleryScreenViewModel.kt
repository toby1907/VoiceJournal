package com.example.voicejournal.ui.main.gallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.Data.model.ImageFile
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel.NoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GalleryUiState(
    val images: List<ImageFile> = emptyList(),
    val albums: List<String> = listOf(GalleryScreenViewModel.ALL_ALBUMS),
    val selectedAlbum: String = GalleryScreenViewModel.ALL_ALBUMS,
    val selectedUrisOrder: List<Uri> = emptyList(),
    val isLoading: Boolean = true,
    val isSavingSelection: Boolean = false,
    val errorMessage: String? = null
) {
    val visibleImages: List<ImageFile>
        get() = if (selectedAlbum == GalleryScreenViewModel.ALL_ALBUMS) {
            images
        } else {
            images.filter { it.bucketName == selectedAlbum }
        }

    val selectedCount: Int
        get() = selectedUrisOrder.size

    fun selectionIndexOf(uri: Uri): Int? {
        val idx = selectedUrisOrder.indexOf(uri)
        return if (idx >= 0) idx + 1 else null
    }
}

@HiltViewModel
class GalleryScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private var loadImagesJob: Job? = null

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    // Kept for backwards compatibility with existing callers/tests
    private val _imageFiles = MutableStateFlow<List<ImageFile>>(emptyList())
    val imageFiles: StateFlow<List<ImageFile>> = _imageFiles.asStateFlow()

    val selectedUris: Flow<Set<String>> = settingsRepository.getSelectedUris()
    var currentNoteId: Int? = null
    private val _noteState = mutableStateOf(NoteState())
    val noteState: State<NoteState> = _noteState

    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                viewModelScope.launch {
                    currentNoteId = noteId
                    voiceJournalRepository.getNote(noteId).collect { note ->
                        _noteState.value = noteState.value.copy(voiceJournal = note)
                    }
                }
            }
        }
        refreshImages()
    }

    fun refreshImages() {
        loadImagesJob?.cancel()
        loadImagesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val loaded = withContext(ioDispatcher) { queryDeviceImages() }
                applyLoadedImages(loaded)
            } catch (ce: kotlinx.coroutines.CancellationException) {
                throw ce
            } catch (se: SecurityException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Photo library permission is required to view your images."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unable to load photos from gallery."
                    )
                }
            }
        }
    }

    internal fun setImagesForTest(images: List<ImageFile>) {
        loadImagesJob?.cancel()
        applyLoadedImages(images)
    }

    private fun applyLoadedImages(loaded: List<ImageFile>) {
        val currentSelectedSet = _uiState.value.selectedUrisOrder.toSet()
        val synced = loaded.map { image ->
            image.copy(isSelected = image.uri in currentSelectedSet)
        }
        val availableUris = synced.map { it.uri }.toSet()
        val retainedOrder = _uiState.value.selectedUrisOrder.filter { it in availableUris }

        val bucketCounts = synced.groupingBy { it.bucketName }.eachCount()
        val sortedAlbums = listOf(ALL_ALBUMS) + bucketCounts.entries
            .sortedByDescending { it.value }
            .map { it.key }
        val activeAlbum = if (_uiState.value.selectedAlbum in sortedAlbums) {
            _uiState.value.selectedAlbum
        } else {
            ALL_ALBUMS
        }

        _imageFiles.value = synced
        _uiState.update {
            it.copy(
                images = synced,
                albums = sortedAlbums,
                selectedAlbum = activeAlbum,
                selectedUrisOrder = retainedOrder,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    // Query images from MediaStore off the main thread
    private fun queryDeviceImages(): List<ImageFile> {
        val contentResolver = context.contentResolver
        val imageFiles = mutableListOf<ImageFile>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC, ${MediaStore.Images.Media._ID} DESC"
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = if (nameColumn >= 0) cursor.getString(nameColumn).orEmpty().ifBlank { "Image" } else "Image"
                val bucket = if (bucketColumn >= 0) cursor.getString(bucketColumn).orEmpty().ifBlank { "Photos" } else "Photos"
                val dateAdded = if (dateColumn >= 0) cursor.getLong(dateColumn) else 0L
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                imageFiles.add(
                    ImageFile(
                        uri = uri,
                        name = name,
                        isSelected = false,
                        bucketName = bucket,
                        dateAddedSeconds = dateAdded
                    )
                )
            }
        }
        return imageFiles
    }

    fun selectAlbum(albumName: String) {
        _uiState.update { state ->
            if (albumName in state.albums) {
                state.copy(selectedAlbum = albumName)
            } else {
                state
            }
        }
    }

    fun toggleImageSelection(imageUri: Uri) {
        _uiState.update { state ->
            val isCurrentlySelected = imageUri in state.selectedUrisOrder
            val updatedOrder = if (isCurrentlySelected) {
                state.selectedUrisOrder - imageUri
            } else {
                state.selectedUrisOrder + imageUri
            }
            val updatedImages = state.images.map { image ->
                if (image.uri == imageUri) {
                    image.copy(isSelected = !isCurrentlySelected)
                } else {
                    image
                }
            }
            _imageFiles.value = updatedImages
            state.copy(
                images = updatedImages,
                selectedUrisOrder = updatedOrder
            )
        }
    }

    fun clearSelection() {
        _uiState.update { state ->
            if (state.selectedUrisOrder.isEmpty()) return@update state
            val updatedImages = state.images.map { if (it.isSelected) it.copy(isSelected = false) else it }
            _imageFiles.value = updatedImages
            state.copy(
                images = updatedImages,
                selectedUrisOrder = emptyList()
            )
        }
    }

    fun updateImageFileSelection(imageFile: ImageFile, isSelected: Boolean): ImageFile {
        return imageFile.copy(isSelected = isSelected)
    }

    fun updateImageFilesList(imageFiles: List<ImageFile>, imageUri: Uri, isSelected: Boolean): List<ImageFile> {
        val index = imageFiles.indexOfFirst { it.uri == imageUri }
        if (index != -1) {
            return imageFiles.mapIndexed { i, imageFile ->
                if (i == index) {
                    updateImageFileSelection(imageFile, isSelected)
                } else {
                    imageFile
                }
            }
        }
        return imageFiles
    }

    fun updateImageFile(imageUri: Uri, isSelected: Boolean) {
        _uiState.update { state ->
            val updatedImages = updateImageFilesList(state.images, imageUri, isSelected)
            val updatedOrder = when {
                isSelected && imageUri !in state.selectedUrisOrder -> state.selectedUrisOrder + imageUri
                !isSelected -> state.selectedUrisOrder - imageUri
                else -> state.selectedUrisOrder
            }
            _imageFiles.value = updatedImages
            state.copy(
                images = updatedImages,
                selectedUrisOrder = updatedOrder
            )
        }
    }

    fun saveSelectedUris(selectedUris: List<String>) {
        viewModelScope.launch {
            settingsRepository.saveSelectedUris(selectedUris)
        }
    }

    fun confirmSelection(onSaved: () -> Unit) {
        val urisToSave = _uiState.value.selectedUrisOrder.map { it.toString() }
        if (urisToSave.isEmpty() || _uiState.value.isSavingSelection) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSelection = true) }
            try {
                settingsRepository.saveSelectedUris(urisToSave)
                onSaved()
            } finally {
                _uiState.update { it.copy(isSavingSelection = false) }
            }
        }
    }

    companion object {
        const val ALL_ALBUMS = "All Photos"
    }
}
