package com.enaven.compism

import com.enaven.compism.AppEvent.*
import com.enaven.compism.compose.HasScreen
import com.enaven.compism.core.CompismAsync
import com.enaven.compism.core.Event
import com.enaven.compism.core.EventResult
import com.enaven.compism.core.EventResult.*
import kotlinx.coroutines.delay

sealed interface AppEvent : Event {
    data object Open : AppEvent
    data object Back : AppEvent
    data object Next : AppEvent
    data object Increment : AppEvent
    data class DataLoaded(val data: String) : AppEvent
}

sealed interface AppState : HasScreen<AppScreen> {
    data object ScreenA : AppState
    data class ScreenB(val data: String? = null) : AppState
    data class ScreenC(val data: String? = null, val sheetData : Int?) : AppState

    override fun getScreen(): AppScreen = when (this) {
        is ScreenA -> AppScreen.ScreenA
        is ScreenB -> AppScreen.ScreenB
        is ScreenC -> AppScreen.ScreenC
    }
}

enum class AppScreen {
    ScreenA,
    ScreenB,
    ScreenC,
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
                async.send(DataLoaded("Some data"))
            }

            EventResult.NewState(AppState.ScreenB(null))
        }

        AppEvent.Back ->
            EventResult.Exit

        AppEvent.Next,
        AppEvent.Increment,
        is AppEvent.DataLoaded ->
            EventResult.Ignored
    }

    is AppState.ScreenB -> when (event) {
        is AppEvent.DataLoaded ->
            EventResult.NewState(state.copy(data = event.data))

        AppEvent.Back ->
            EventResult.NewState(AppState.ScreenA)

        AppEvent.Next ->
            EventResult.NewState(AppState.ScreenC(state.data, null))

        AppEvent.Increment,
        AppEvent.Open ->
            EventResult.Ignored
    }

    is AppState.ScreenC -> when (event) {
        Back -> if (state.sheetData != null) {
            NewState(state.copy(sheetData = null))
        } else {
            NewState(AppState.ScreenB(state.data))
        }
        Increment ->
            NewState(state.copy(sheetData = state.sheetData?.inc()))
        is DataLoaded ->
            NewState(state.copy(data = event.data))
        Open ->
            NewState(state.copy(sheetData = 0))

        Next,
        Open ->
            Ignored
    }
}