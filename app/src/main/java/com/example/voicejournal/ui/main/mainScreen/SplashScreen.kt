package com.example.voicejournal.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicejournal.R
import kotlinx.coroutines.delay

private const val SplashWaitTime: Long = 2000
@Composable
fun SplashScreen(
    modifier: Modifier=Modifier,
    onTimeout: () -> Unit
){

        Box(
          modifier = modifier
              .fillMaxSize()
              .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center

        ) {
           val currentOnTimeout by rememberUpdatedState(newValue = onTimeout)
            LaunchedEffect(onTimeout){
                delay(SplashWaitTime)
                currentOnTimeout()
            }
          Column(
              horizontalAlignment = Alignment.CenterHorizontally

          ) {
                Image(painter = painterResource(id = R.drawable.baseline_graphic_eq_black_48), contentDescription = null
                )
                Spacer(Modifier.height(16.dp))
                Text(text = "Voice Journal")
            }
        }
    }

@Composable
@Preview
fun SplashScreenPreview(
){
    SplashScreen(onTimeout = {})
}