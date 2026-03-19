package com.enaven.compism.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface Event

class CompismHandler<S, E : Event>(
    initialState: S,
    private val reducer: (S, E, CompismAsync<E>) -> EventResult<S>,
    private val onExitRequest: suspend () -> Unit,
    private val onStateChanged: ((old: S, new: S) -> Unit)? = null,
) {
    // Separate writing to make sure no one outside can change the state
    private val _state = MutableStateFlow(initialState)

    // Create a read-only version of the same data (suitable for public use)
    val state: StateFlow<S> = _state

    private val eventQueue = Channel<E>(Channel.BUFFERED)

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val effectScope = CoroutineScope(SupervisorJob() + Dispatchers.Default) // TODO: Use .IO on Android

    init {
        mainScope.launch {
            for (event in eventQueue) {
                handleEvent(event)
            }
        }
    }

    fun send(event: E) {
        eventQueue.trySend(event)
    }

    private val async = object : CompismAsync<E> {
        override fun launch(block: suspend CoroutineScope.() -> Unit) {
            effectScope.launch {
                try {
                    block()
                } catch (e: Throwable) {
                    println("Compism async error: $e")
                }
            }
        }

        override fun send(event: E) {
            this@CompismHandler.send(event)
        }
    }

    private fun handleEvent(i: E) {
        val oldState = _state.value
        println("CompismHandler - Handling Interaction: $i on State: $oldState")

        val result = try {
            reducer(oldState, i, async)
        } catch (t: Throwable) {
            println("CompismHandler --- Exception in Interaction: $i on State: $oldState -> ${t::class.simpleName}: ${t.message}")
            t.printStackTrace()
            return
        }

        val newState = when (result) {
            is EventResult.NewState -> result.state
            EventResult.Unexpected -> {
                println("CompismHandler --- Unexpected Interaction: $i on State: $oldState")
                return
            }
            EventResult.Ignored -> {
                println("CompismHandler --- Skipped Interaction: $i on State: $oldState")
                return
            }

            EventResult.Exit -> {
                println("CompismHandler --- Closing App: $i on State: $oldState")
                effectScope.launch {
                    onExitRequest()
                }
                return
            }
        }

        if (newState != oldState) {
            _state.value = newState
            println("CompismHandler --- New State: $newState")

            onStateChanged?.invoke(oldState, newState)
        } else {
            println("CompismHandler --- Same State ($newState)")
        }
    }

    fun close() {
        mainScope.cancel()
        effectScope.cancel()
    }
}

interface CompismAsync<E> {
    fun launch(block: suspend CoroutineScope.() -> Unit)
    fun send(event: E)
}

sealed class EventResult<out S> {
    data class NewState<S>(val state: S) : EventResult<S>()
    object Ignored : EventResult<Nothing>()
    object Unexpected : EventResult<Nothing>()
    object Exit : EventResult<Nothing>()
}