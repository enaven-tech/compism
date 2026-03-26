package com.enaven.compism.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <D> CompismSheet(
    data: D?,
    onDismissRequest: () -> Unit,
    depth : Int = 0,
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
    // Prevent the sheet from going in under the status bar
    // ------------------------------------------------------------------
    val topInset = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val screenHeight = LocalWindowInfo.current.containerDpSize.height

    var handleHeightPx by remember { mutableStateOf(0) }
    val handleHeightDp = with(LocalDensity.current) { handleHeightPx.toDp() }

    // And also let overlapping sheets stop a bit below
    val depthInset = when (depth) {
        0 -> 0.dp
        1 -> 20.dp
        2 -> 20.dp+24.dp
        else -> 20.dp+24.dp+28.dp
    }

    val maxHeight = screenHeight - topInset - bottomInset - handleHeightDp - depthInset - 12.dp // A little extra safety padding
    // ------------------------------------------------------------------

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Box(
                modifier = Modifier.onGloballyPositioned { coords ->
                    handleHeightPx = coords.size.height
                }
            ) {
                dragHandle()
            }
        }
    ) {
        Column(
            modifier = Modifier.heightIn(max = maxHeight)
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
                .size(width = 36.dp, height = 44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )
    }
}