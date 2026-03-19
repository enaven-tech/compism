package com.enaven.compism

import com.enaven.compism.compose.HasScreen
import com.enaven.compism.core.CompismAsync
import com.enaven.compism.core.Event
import com.enaven.compism.core.EventResult
import kotlinx.coroutines.delay

sealed interface AppEvent : Event {
    data object Open : AppEvent
    data object Back : AppEvent
    data class DataLoaded(val data: String) : AppEvent
}

sealed interface AppState : HasScreen<AppScreen> {
    data object ScreenA : AppState
    data class ScreenB(val data: String? = null) : AppState

    override fun getScreen(): AppScreen = when (this) {
        is ScreenA -> AppScreen.ScreenA
        is ScreenB -> AppScreen.ScreenB
    }
}

enum class AppScreen {
    ScreenA,
    ScreenB
}

// -----------------------------------------------------------------------

fun reducer(
    state: AppState,
    event: AppEvent,
    async: CompismAsync<AppEvent>
): EventResult<AppState> = when (state) {

    is AppState.ScreenA -> when (event) {
        AppEvent.Open -> {
            // Trigger async work
            async.launch {
                // Doing something slow and heavy
                delay(1000)

                // Result is fed back into the queue as an event
                async.send(AppEvent.DataLoaded("Some data"))
            }

            EventResult.NewState(AppState.ScreenB())
        }

        AppEvent.Back,
        is AppEvent.DataLoaded ->
            EventResult.Ignored
    }

    is AppState.ScreenB -> when (event) {
        is AppEvent.DataLoaded ->
            EventResult.NewState(state.copy(data = event.data))

        AppEvent.Back ->
            EventResult.NewState(AppState.ScreenA)

        AppEvent.Open ->
            EventResult.Ignored
    }
}