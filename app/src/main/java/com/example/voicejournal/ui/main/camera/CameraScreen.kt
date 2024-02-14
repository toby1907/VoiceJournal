package com.example.voicejournal.ui.main.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.R
import com.example.voicejournal.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavHostController, onClick: () -> Unit) {
    val applicationContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = androidx.compose.material3.rememberBottomSheetScaffoldState()
    val controller = remember {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )

        }
    }
    var cameraVisibleState by remember {
        mutableStateOf(false)
    }
    val viewModel: CameraScreenViewModel = hiltViewModel()
    val bitmaps by viewModel.bitmaps.collectAsState()
    var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    if (!cameraVisibleState) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContent = {
                /*   PhotoBottomSheetContent(
                       bitmaps = bitmaps,
                       modifier = Modifier
                           .fillMaxWidth()
                   )*/
            }, content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    CameraPreview(
                        controller = controller,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    IconButton(
                        onClick = {
                            controller.cameraSelector =
                                if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else CameraSelector.DEFAULT_BACK_CAMERA
                        },
                        modifier = Modifier
                            .offset(16.dp, 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch camera"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(
                            onClick = {/*
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }*/
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Open gallery"
                            )
                        }
                        IconButton(
                            onClick = {
                                takePhoto(
                                    controller = controller,
                                    onPhotoTaken = viewModel::onTakePhoto,
                                    applicationContext = applicationContext,
                                    compositionState = {
                                        cameraVisibleState = true
                                    },
                                    capturedImageBitmap = {
                                        capturedImageBitmap = it

                                    }
                                )

                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Take photo"
                            )
                        }
                    }
                }
            })
    } else {
        DisplayCapturedImage(capturedImageBitmap,
            saveImage =viewModel::saveCapturedImage,
            navigate = {
                navController.navigate(Screen.AddEditNoteScreen.route)
            }
        )
    }


}

private fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    applicationContext: Context,
    capturedImageBitmap: (Bitmap) -> Unit,
compositionState: () -> Unit
) {


    controller.takePicture(
        ContextCompat.getMainExecutor(applicationContext),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {

                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )
                val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 800, 600, false)

                onPhotoTaken(resizedBitmap)
                capturedImageBitmap(resizedBitmap)
                compositionState()
            }


            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayCapturedImage(capturedBitmap: Bitmap?,
                         saveImage:(Bitmap) -> Unit,
                         navigate:() -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val minScale = 0.5f
    val maxScale = 3f

    if (capturedBitmap != null) {
        Scaffold(topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = " Picture") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.check_24),
                        contentDescription = "",
                        modifier = Modifier.clickable {
                            saveImage(capturedBitmap)
                            navigate()
                        }
                    )
                },
                actions = {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription ="delete image",
                        modifier = Modifier.clickable {
                            navigate()
                        }

                        )
                }

                )
        }
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
            )  {
                Image(
                    painter = rememberAsyncImagePainter(capturedBitmap),
                    contentDescription = "Captured Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { _, pan, gestureZoom, _ ->
                                    scale = (scale * gestureZoom).coerceIn(minScale, maxScale)
                                    if (scale > 1) {
                                        offsetX += pan.x * gestureZoom
                                        offsetY += pan.y * gestureZoom
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            )
                        }
                        .fillMaxSize()
                )
            }
        }


    } else {
        Text("No image captured yet", modifier = Modifier.fillMaxSize())
    }
}




