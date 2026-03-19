package com.enaven.compism.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.enaven.compism.core.CompismHandler
import com.enaven.compism.core.Event

@Composable
fun <S, E : Event> CompismHandler<S, E>.SystemInteractions(
    backEvent: E
) {
    val currentHandler by rememberUpdatedState(this)

    CompismPlatformCaller.includeBackHandler {
        currentHandler.send(backEvent)
    }
}