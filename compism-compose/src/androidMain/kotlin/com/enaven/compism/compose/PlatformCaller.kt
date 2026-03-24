package com.enaven.compism.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

actual object CompismPlatformCaller {

    @Composable
    actual fun includeBackHandler(
        onBack : () -> Unit
    ) {
        BackHandler(enabled = true) {
            onBack()
        }
    }
}