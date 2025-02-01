package com.example.voicejournal

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.example.voicejournal.ui.MyAppNavHost
import com.example.voicejournal.ui.main.snackbar.ObserveAsEvents
import com.example.voicejournal.ui.main.snackbar.SnackbarController
import com.example.voicejournal.ui.theme.VoiceJournalTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

private const val USER_PREFERENCES_NAME = "user_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceJournalTheme {
                val snackbarHostState = remember {
                    SnackbarHostState()
                }
                val scope = rememberCoroutineScope()
                ObserveAsEvents(
                    flow = SnackbarController.events,
                    snackbarHostState
                ) { event ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()

                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.action?.name,
                            duration = SnackbarDuration.Short
                        )

                        if(result == SnackbarResult.ActionPerformed) {
                            event.action?.action?.invoke()
                        }
                    }
                }

                val permissionsList =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_MEDIA_AUDIO,
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                       // android.Manifest.permission.WRITE_MEDIA_AUDIO, // Replace WRITE_EXTERNAL_STORAGE
                    )
                } else {

                    listOf(
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                }
                val multiplePermissionsState = rememberMultiplePermissionsState(permissionsList)
                if (multiplePermissionsState.allPermissionsGranted) {
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->

                        MyAppNavHost(
                         //   Modifier.safeDrawingPadding()
                                Modifier.padding(innerPadding),
                        )
                    }

                }
                else {
                    Column {
                        Text(
                            getTextToShowGivenPermissions(
                                multiplePermissionsState.revokedPermissions,
                                multiplePermissionsState.shouldShowRationale
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
                            Text("Request permissions")
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

    }

    @OptIn(ExperimentalPermissionsApi::class)
    private fun getTextToShowGivenPermissions(
        permissions: List<PermissionState>,
        shouldShowRationale: Boolean
    ): String {
        val revokedPermissionsSize = permissions.size
        if (revokedPermissionsSize == 0) return ""

        val textToShow = StringBuilder().apply {
            append("The ")
        }

        for (i in permissions.indices) {
            textToShow.append(permissions[i].permission)
            when {
                revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                    textToShow.append(", and ")
                }
                i == revokedPermissionsSize - 1 -> {
                    textToShow.append(" ")
                }
                else -> {
                    textToShow.append(", ")
                }
            }
        }
        textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
        textToShow.append(
            if (shouldShowRationale) {
                " important. Please grant all of them for the app to function properly."
            } else {
                " denied. The app cannot function without them."
            }
        )
        return textToShow.toString()
    }

}
