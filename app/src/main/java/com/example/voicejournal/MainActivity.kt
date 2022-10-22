package com.example.voicejournal

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.voicejournal.ui.MyAppNavHost
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel
import com.example.voicejournal.ui.theme.VoiceJournalTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
/*

    private val voiceNoteViewModel: VoiceNoteViewModel by viewModels { DataViewModelFactory(applicationContext)}
private val addVoiceNoteViewModel:AddVoiceNoteViewModel by viewModels { AddVoiceNoteViewModelFactory(applicationContext)  }
*/
    val viewModel by viewModels<AddVoiceNoteViewModel>()
    private var fileName: String = ""
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null


    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VoiceJournalTheme {
                // A surface container using the 'background' color from the theme

                val LOG_TAG = "AudioRecordTest"
             /*   val scope = rememberCoroutineScope()
                LaunchedEffect(key1 = true){
                   viewModel.eventFlow.collectLatest {event ->
                        when(event){
                            is AddVoiceNoteViewModel.UiEvent.Recording -> {
                                startRecording()
                            }
                            is AddVoiceNoteViewModel.UiEvent.StopRecord -> {
                              stopRecording()
                            }
                            is AddVoiceNoteViewModel.UiEvent.PlayNote -> {
                                startPlaying()
                            }
                            is AddVoiceNoteViewModel.UiEvent.StopPlay -> {
                                stopPlaying()

                            }
                            is AddVoiceNoteViewModel.UiEvent.SaveNote -> {
                                TODO()
                            }
                            is AddVoiceNoteViewModel.UiEvent.ShowSnackbar -> {
                               TODO()
                            }


                        }
                    }
                }
*/

//  MediaPlayer.OnCompletionListener {  }

                // Record to the external cache directory for visibility
              //  fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQUEST_RECORD_AUDIO_PERMISSION
                )

                MyAppNavHost(
                )


            }
        }


    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }


    override fun onStop() {
        super.onStop()
      /*  recorder?.release()
        recorder = null
        player?.release()
        player = null*/
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VoiceJournalTheme {
        Greeting("Android")
    }
}