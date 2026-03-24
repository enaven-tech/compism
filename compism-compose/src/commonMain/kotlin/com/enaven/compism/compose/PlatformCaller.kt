package com.enaven.compism.compose

import androidx.compose.runtime.Composable

expect object CompismPlatformCaller {
    @Composable
    fun includeBackHandler(onBack: () -> Unit)
}