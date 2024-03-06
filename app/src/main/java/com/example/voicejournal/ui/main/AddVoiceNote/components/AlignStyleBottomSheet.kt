package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.DisposableHandle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlignStyleBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    windowInsets: WindowInsets,
    onClick: () -> DisposableHandle,
    state: RichTextState,
    onStartAlignClick: () -> Unit,
    onEndAlignClick: () -> Unit,
    onCenterAlignClick: () -> Unit,
    onOrderedListClick: () -> Unit,
    onUnorderedListClick: () -> Unit

    ) {

    var alignmentSelected by rememberSaveable { mutableIntStateOf(0) }
    var isOrderedListSelected by rememberSaveable { mutableStateOf(false) }
    var isUnorderedListSelected by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
        windowInsets = windowInsets,
        modifier = Modifier.padding(8.dp)

    ) {
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) { Text(text = " Align") }
        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            items(3) { index ->
                // A list of icons and texts for each item

                if (index == 0) {
                    ControlWrapper2(
                        selected = alignmentSelected == 0,
                        onChangeClick = { alignmentSelected = 0 },
                        onClick = onStartAlignClick,
                        text = com.example.voicejournal.ui.main.AddVoiceNote.components.Alignment.Left.name
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatAlignLeft,
                            contentDescription = "Start Align Control",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                if (index == 1) {
                    ControlWrapper2(
                        selected = alignmentSelected == 1,
                        onChangeClick = { alignmentSelected = 1 },
                        onClick = onCenterAlignClick,
                        text = com.example.voicejournal.ui.main.AddVoiceNote.components.Alignment.Center.name
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatAlignCenter,
                            contentDescription = "Center Align Control",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                }

                if (index == 2) {

                    ControlWrapper2(
                        selected = alignmentSelected == 2,
                        onChangeClick = { alignmentSelected = 2 },
                        onClick = onEndAlignClick,
                        text = com.example.voicejournal.ui.main.AddVoiceNote.components.Alignment.Right.name
                    ) {

                        Icon(
                            imageVector = Icons.Default.FormatAlignRight,
                            contentDescription = "End Align Control",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )

                    }


                }

            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) { Text(text = " List Type") }
        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            items(2) { index ->
                // A list of icons and texts for each item

                if (index == 0) {

                    val isOrderedList = state.isOrderedList
                    ControlWrapper2(
                        selected = isOrderedList,
                        onChangeClick = { isOrderedListSelected = it },
                        onClick = onOrderedListClick,
                        text = com.example.voicejournal.ui.main.AddVoiceNote.components.Alignment.Left.name
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = "Start Align Control",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                if (index == 1) {
                    val isUnorderedList = state.isUnorderedList
                    ControlWrapper2(
                        selected = isUnorderedList,
                        onChangeClick = { isUnorderedListSelected = it },
                        onClick = onUnorderedListClick,
                        text = com.example.voicejournal.ui.main.AddVoiceNote.components.Alignment.Center.name
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "Center Align Control",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                }



            }
        }
    }
}