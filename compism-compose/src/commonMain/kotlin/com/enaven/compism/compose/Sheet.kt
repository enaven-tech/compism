package com.enaven.compism.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <D> CompismSheet(
    data: D?,
    onDismissRequest: (() -> Unit)? = null,
    depth: Int = 0,
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    dragHandle: @Composable () -> Unit = { SheetDragHandle() },
    content: @Composable ColumnScope.(D) -> Unit
) {
    val locked = onDismissRequest == null

    // Holds the last non-null data so content survives during hide animation
    var currentData by remember { mutableStateOf<D?>(null) }

    // Tracks measured sheet height for slide animation and drag dismissal
    var sheetHeightPx by remember { mutableFloatStateOf(0f) }
    val offsetY = remember { Animatable(0f) }

    // ------------------------------------------------------------------
    // Keep displayed content up to date while visible
    // ------------------------------------------------------------------
    LaunchedEffect(data) {
        if (data != null) {
            currentData = data
        }
    }

    // ------------------------------------------------------------------
    // Handle visibility transitions only
    // ------------------------------------------------------------------
    LaunchedEffect(data != null) {
        if (data != null) {
            currentData = data

            withFrameNanos { }

            offsetY.snapTo(sheetHeightPx)
            offsetY.animateTo(0f, spring())
        } else {
            try {
                offsetY.animateTo(
                    sheetHeightPx,
                    tween(220)
                )
            } finally {
                currentData = null
            }
        }
    }

    // ------------------------------------------------------------------
    // Do not include the sheet at all if we have no data to display
    // ------------------------------------------------------------------
    if (currentData == null) return

    // ------------------------------------------------------------------
    // Manual scrim for correct insets
    // ------------------------------------------------------------------
    val scrimColorAnimated by animateColorAsState(
        targetValue = if (data != null) scrimColor else Color.Transparent,
        label = "sheet_scrim"
    )

    // ------------------------------------------------------------------
    // Place layered sheets further down from the top
    // ------------------------------------------------------------------
    val depthInset = when (depth) {
        0 -> 0.dp
        1 -> 20.dp
        2 -> 20.dp + 24.dp
        else -> 20.dp + 24.dp + 28.dp
    }

    // ------------------------------------------------------------------

    val scope = rememberCoroutineScope()

    Box(
        Modifier.fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(scrimColorAnimated)
                .clickable(
                    indication = null,
                    interactionSource = null
                ) {
                    // Only allow closing if we have data set, to prevent multi clicks
                    if (!locked && data != null) {
                        onDismissRequest.invoke()
                    }
                }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = depthInset)
                .widthIn(max = sheetMaxWidth)
                .fillMaxWidth()
                .onSizeChanged {
                    sheetHeightPx = it.height.toFloat()
                }
                .alpha(if (sheetHeightPx < 1f) 0f else 1f) // Hide until correctly measured
                .offset {
                    IntOffset(
                        x = 0,
                        y = offsetY.value.roundToInt()
                    )
                }
                .then(
                    if (!locked) {
                        Modifier.pointerInput(sheetHeightPx) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()

                                    scope.launch {
                                        offsetY.snapTo(
                                            (offsetY.value + dragAmount)
                                                .coerceIn(0f, sheetHeightPx)
                                        )
                                    }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        if (offsetY.value > sheetHeightPx * 0.35f) {
                                            offsetY.animateTo(
                                                targetValue = sheetHeightPx,
                                                animationSpec = tween(durationMillis = 180)
                                            )

                                            onDismissRequest.invoke()
                                        } else {
                                            offsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            ),
            color = containerColor,
            contentColor = contentColor
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding()
            ) {
                dragHandle()

                currentData?.let { content(it) }
            }
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