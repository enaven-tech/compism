package com.enaven.compism.compose

import androidx.compose.runtime.Composable

interface CompismPlatformCallerInterface {
    @Composable
    fun includeBackHandler(onBack : () -> Unit)
}

expect object CompismPlatformCaller: CompismPlatformCallerInterface