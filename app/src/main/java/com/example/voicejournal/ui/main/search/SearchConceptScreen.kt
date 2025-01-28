package com.example.voicejournal.ui.main.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction


@Composable
private fun SearchAppBAr() {
    var isSearch by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current
    var searchQuery by remember {
        mutableStateOf("")
    }
    if (isSearch) {
        androidx.compose.material3.TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
               // viewModel.searchFor(searchQuery.trim())
            },
            label = {
                Text(text = "Search")
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (!it.isFocused) {
                        focusManager.clearFocus()
                        isSearch = false
                    }
                },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                focusManager.clearFocus()
                isSearch = false
            },
            colors = TextFieldDefaults.colors(),
            leadingIcon = {
                IconButton(onClick = {
                    isSearch = false
                }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        )
    } else {
        TopAppBar(
            title = {
                Text(text = "Voice Journal")
            },
            actions = {
                IconButton(onClick = {
                    isSearch = true
                }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        )
    }
}