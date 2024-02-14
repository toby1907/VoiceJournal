package com.example.voicejournal.ui.main.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CameraScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"


    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap



    }


    private fun saveImageBitmapAndGetUri(bitmap: Bitmap): List<Uri?> {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Jorie-Image")
            }
        }

        // Insert the image into the MediaStore
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        // Save the bitmap data to the specified URI
        uri?.let { imageUri ->
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            }
        }

        val uriList: List<Uri?> = listOf(uri)
        return uriList
    }

    fun saveCapturedImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val selectedStrings = saveImageBitmapAndGetUri(bitmap).map { it.toString() }
            settingsRepository.saveSelectedUris(selectedStrings)
            Log.d("Camera ViewModel","savedImage")
        }
    }

}