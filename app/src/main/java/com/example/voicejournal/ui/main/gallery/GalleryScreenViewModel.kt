package com.example.voicejournal.ui.main.gallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.Data.model.ImageFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // State flow to store the image files
    private val _imageFiles = MutableStateFlow(listOf<ImageFile>())
    val imageFiles: StateFlow<List<ImageFile>> = _imageFiles
    val selectedUris: Flow<Set<String>> = settingsRepository.getSelectedUris()


    init {
        // Launch a coroutine to get the image files
        viewModelScope.launch {
            _imageFiles.value = getImageFiles()
        }
    }

    // Query the image files from the external storage
    private fun getImageFiles(): List<ImageFile> {
        // Query the image files from the external storage
        val contentResolver = context.contentResolver
            val imageFiles = mutableListOf<ImageFile>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val imageFile = ImageFile(uri, name)
                    imageFiles.add(imageFile)
                }
            }
            return imageFiles

    }


    // A function to update the isSelected property of an imageFile
    fun updateImageFileSelection(imageFile: ImageFile, isSelected: Boolean): ImageFile {
        // Create a copy of the imageFile with the new isSelected value
        return imageFile.copy(isSelected = isSelected)
    }

    // A function to update the imageFiles list with a new copy of an imageFile
    fun updateImageFilesList(imageFiles: List<ImageFile>, imageUri: Uri, isSelected: Boolean): List<ImageFile> {
        // Find the index of the imageFile with the given URI
        val index = imageFiles.indexOfFirst { it.uri == imageUri }
        // Check if the index is valid
        if (index != -1) {
            // Create a new list with the updated element
            return imageFiles.mapIndexed { i, imageFile ->
                if (i == index) {
                    // Create a new copy of the imageFile with the new isSelected value
                    updateImageFileSelection(imageFile, isSelected)
                } else {
                    // Keep the original value
                    imageFile
                }
            }
        }
        // Return the original list
        return imageFiles
    }

    fun updateImageFile(imageUri: Uri, isSelected: Boolean){
        viewModelScope.launch {
            _imageFiles.value = updateImageFilesList(_imageFiles.value,imageUri,isSelected)
        }

    }

    fun saveSelectedUris(selectedUris: List<String>) {
        viewModelScope.launch {
            settingsRepository.saveSelectedUris(selectedUris)
        }
    }

}

