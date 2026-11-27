package com.example.voicejournal.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object JournalModalDefaults {
    val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val DialogShape = RoundedCornerShape(24.dp)
    val CardShape = RoundedCornerShape(14.dp)

    val ContainerColor: Color = Variables.SchemesSurface
    val TitleColor: Color = Variables.SchemesOnPrimaryContainer
    val TextColor: Color = Variables.SchemesOnSurface
    val MutedTextColor: Color = Variables.SchemesOnSurfaceVariant
    val SoftCardColor: Color = Variables.SchemesPrimaryContainer.copy(alpha = 0.28f)
    val ActiveCardColor: Color = Variables.SchemesPrimaryContainer.copy(alpha = 0.72f)
    val SubtleBorderColor: Color = Variables.SchemesPrimary.copy(alpha = 0.18f)
    val SubtleDividerColor: Color = Variables.SchemesPrimary.copy(alpha = 0.14f)
    val ScrimColor: Color = Color(0x66221919)

    @Composable
    fun primaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Variables.SchemesPrimary,
        contentColor = Variables.SchemesOnPrimary,
        disabledContainerColor = Variables.SchemesPrimary.copy(alpha = 0.35f),
        disabledContentColor = Variables.SchemesOnPrimary.copy(alpha = 0.75f)
    )

    @Composable
    fun destructiveButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Variables.SchemesError,
        contentColor = Variables.SchemesOnPrimary
    )

    @Composable
    fun outlinedTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Variables.SchemesPrimary,
        unfocusedBorderColor = Variables.SchemesPrimary.copy(alpha = 0.35f),
        focusedLabelColor = Variables.SchemesPrimary,
        unfocusedLabelColor = Variables.SchemesOnSurfaceVariant,
        cursorColor = Variables.SchemesPrimary,
        focusedTextColor = Variables.SchemesOnSurface,
        unfocusedTextColor = Variables.SchemesOnSurface,
        focusedContainerColor = Variables.SchemesSurface,
        unfocusedContainerColor = Variables.SchemesSurface
    )
}

@Composable
fun JournalSheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    color = Variables.SchemesPrimary.copy(alpha = 0.32f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun JournalSheetHeader(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Variables.SchemesOnPrimaryContainer
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Variables.SchemesOnSurfaceVariant
                    )
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun JournalDialogIconBadge(
    imageVector: ImageVector,
    contentDescription: String? = null,
    isDestructive: Boolean = false
) {
    val bg = if (isDestructive) {
        Variables.SchemesError.copy(alpha = 0.14f)
    } else {
        Variables.SchemesPrimaryContainer
    }
    val tint = if (isDestructive) {
        Variables.SchemesError
    } else {
        Variables.SchemesPrimary
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(color = bg, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
