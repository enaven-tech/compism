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
    private val onExitRequest: () -> Unit,
    private val onStateChanged: ((old: S, new: S) -> Unit)? = null,
    private val logger: CompismLogger? = DefaultCompismLogger,
    private val minLogLevel: LogLevel = LogLevel.INFO,
) {
    // Separate writing to make sure no one outside can change the state
    private val _state = MutableStateFlow(initialState)

    // Create a read-only version of the same data (suitable for public use)
    val state: StateFlow<S> = _state

    private val eventQueue = Channel<E>(Channel.BUFFERED)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default) // TODO: Use .IO on Android

    private fun log(level: LogLevel, message: String) {
        if (level.ordinal >= minLogLevel.ordinal) {
            logger?.log(level, message)
        }
    }

    init {
        scope.launch {
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
            scope.launch {
                try {
                    block()
                } catch (e: Throwable) {
                    log(LogLevel.ERROR, "Async error: ${e::class.simpleName}: ${e.message}")
                }
            }
        }

        override fun send(event: E) {
            this@CompismHandler.send(event)
        }
    }

    private fun handleEvent(event: E) {
        val oldState = _state.value

        val result = try {
            reducer(oldState, event, async)
        } catch (t: Throwable) {
            log(
                LogLevel.ERROR,
                "Exception during event: $event on state: $oldState -> ${t::class.simpleName}: ${t.message}"
            )
            return
        }

        val newState = when (result) {
            is EventResult.NewState -> {
                if (minLogLevel <= LogLevel.DEBUG) {
                    log(
                        LogLevel.DEBUG,
                        """
                        Event: $event
                           From:    $oldState
                           To:      ${result.state}
                           
                        """.trimIndent()
                    )
                } else {
                    log(LogLevel.INFO, "Handled Event: $event")
                }

                result.state
            }

            EventResult.Unexpected -> {
                log(LogLevel.WARN, "Unexpected event: $event on state: $oldState")
                return
            }

            EventResult.Ignored -> {
                log(LogLevel.DEBUG, "Ignored event: $event on state: $oldState")
                return
            }

            EventResult.Exit -> {
                log(LogLevel.INFO, "Exit requested from event: $event on state: $oldState")
                onExitRequest()
                return
            }
        }

        if (newState != oldState) {
            _state.value = newState
            onStateChanged?.invoke(oldState, newState)
        } else {
            log(LogLevel.DEBUG, "State unchanged: $newState")
        }
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