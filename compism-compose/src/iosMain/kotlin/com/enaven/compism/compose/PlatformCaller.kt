package com.enaven.compism.compose

import androidx.compose.runtime.Composable

actual object CompismPlatformCaller {

    @Composable
    actual fun includeBackHandler(
        onBack : () -> Unit
    ) {
        // No system backing on iOS
    }
}