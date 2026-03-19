package com.enaven.compism

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.enaven.compism.compose.CompismDisplay
import com.enaven.compism.compose.SystemInteractions
import com.enaven.compism.core.CompismHandler

// -------------------------------------------------------------------------------

object AppObject {
    val compism = CompismHandler(
        initialState = loadInitialState(),
        reducer = ::reducer,
        onExitRequest = {
            // Android: close app
            // iOS: no-op
        },
        onStateChanged = { _, _ ->
            // Persist state (e.g. save to disk for next app launch)
        }
    )
}

fun loadInitialState() : AppState {
    // Replace with persisted state if available
    return  AppState.ScreenA
}

// -------------------------------------------------------------------------------

@Composable
@Preview
fun App() {
    MaterialTheme {
        val compism = AppObject.compism

        compism.SystemInteractions(AppEvent.Back)

        val state = compism.state.collectAsState().value

        CompismDisplay(
            state,
            ::transitionFor,
            ::screensToReset
        ) { s ->
            when (s) {
                is AppState.ScreenA -> ScreenA(
                    onOpen = { compism.send(AppEvent.Open) }
                )
                is AppState.ScreenB -> ScreenB(
                    s.data,
                    onBack = { compism.send(AppEvent.Back) }
                )
            }
        }
    }
}