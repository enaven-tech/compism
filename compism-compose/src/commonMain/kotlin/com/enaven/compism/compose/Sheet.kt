package com.enaven.compism.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <D> CompismSheet(
    data: D?,
    onDismissRequest: () -> Unit,
    depth : Int = 0,
    scrimColor : Color = Color.Black.copy(alpha = 0.5f),
    dragHandle : @Composable () -> Unit = { SheetDragHandle() },
    content: @Composable ColumnScope.(D) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Holds the last non-null data so content survives during hide animation
    var currentData by remember { mutableStateOf<D?>(null) }

    LaunchedEffect(data) {
        if (data != null) {
            currentData = data

            // Wait a frame so the sheet first is laid out in the hidden state
                // So we get the showing animation when running who
            withFrameNanos { }

            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    // Clear displayed data only after the sheet is fully hidden
    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == SheetValue.Hidden) {
            currentData = null
        }
    }

    // Stay composed only while we have valid data
    if (currentData == null) return

    // ------------------------------------------------------------------
    // Manual scrim for correct insets
    // ------------------------------------------------------------------
    val scrimColorAnimated by animateColorAsState(
        targetValue = if (sheetState.targetValue != SheetValue.Hidden) {
            scrimColor
        } else {
            Color.Transparent
        },
        label = "sheet_scrim"
    )

    // ------------------------------------------------------------------
    // Place layered sheets further down from the top
    // ------------------------------------------------------------------
    val depthInset = when (depth) {
        0 -> 0.dp
        1 -> 20.dp
        2 -> 20.dp+24.dp
        else -> 20.dp+24.dp+28.dp
    }

    // ------------------------------------------------------------------

    Box(
        Modifier
            .fillMaxSize()
            .background(scrimColorAnimated)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onDismissRequest()
            }
    ) {
        ModalBottomSheet(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = depthInset),
            sheetState = sheetState,
            onDismissRequest = onDismissRequest,
            dragHandle = { dragHandle() },
            scrimColor = Color.Transparent
        ) {
            currentData?.let { content(it) }
        }
    }
}

@Composable
fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )
    }
}