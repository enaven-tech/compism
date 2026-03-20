package com.enaven.compism.compose

import androidx.compose.runtime.Composable

actual object CompismPlatformCaller : CompismPlatformCallerInterface {

    @Composable
    override fun includeBackHandler(
        onBack : () -> Unit
    ) {
        // TODO: Support browser backing
    }
}