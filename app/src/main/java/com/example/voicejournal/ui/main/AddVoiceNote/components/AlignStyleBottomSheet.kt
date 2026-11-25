package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.voicejournal.ui.common.richeditor.RichTextState
import com.example.voicejournal.ui.theme.Variables
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
    val currentAlign = state.currentTextAlign
    val isLeftSelected = currentAlign == TextAlign.Start || currentAlign == TextAlign.Left
    val isCenterSelected = currentAlign == TextAlign.Center
    val isRightSelected = currentAlign == TextAlign.End || currentAlign == TextAlign.Right
    val isOrderedList = state.isOrderedList
    val isUnorderedList = state.isUnorderedList

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
        windowInsets = windowInsets,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                Text(
                    text = "Align",
                    style = MaterialTheme.typography.titleSmall,
                    color = Variables.SchemesOnSurface
                )
            }

            ControlWrapper2(
                selected = isLeftSelected,
                onChangeClick = {},
                onClick = onStartAlignClick,
                text = Alignment.Left.name
            ) {
                Icon(
                    imageVector = Icons.Default.FormatAlignLeft,
                    contentDescription = "Start Align Control",
                    tint = if (isLeftSelected) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                )
            }

            ControlWrapper2(
                selected = isCenterSelected,
                onChangeClick = {},
                onClick = onCenterAlignClick,
                text = Alignment.Center.name
            ) {
                Icon(
                    imageVector = Icons.Default.FormatAlignCenter,
                    contentDescription = "Center Align Control",
                    tint = if (isCenterSelected) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                )
            }

            ControlWrapper2(
                selected = isRightSelected,
                onChangeClick = {},
                onClick = onEndAlignClick,
                text = Alignment.Right.name
            ) {
                Icon(
                    imageVector = Icons.Default.FormatAlignRight,
                    contentDescription = "End Align Control",
                    tint = if (isRightSelected) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                Text(
                    text = "List Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = Variables.SchemesOnSurface
                )
            }

            ControlWrapper2(
                selected = isOrderedList,
                onChangeClick = {},
                onClick = onOrderedListClick,
                text = "Numbered List"
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = "Numbered List",
                    tint = if (isOrderedList) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                )
            }

            ControlWrapper2(
                selected = isUnorderedList,
                onChangeClick = {},
                onClick = onUnorderedListClick,
                text = "Bulleted List"
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Bulleted List",
                    tint = if (isUnorderedList) MaterialTheme.colorScheme.onPrimary else Variables.SchemesOnSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}