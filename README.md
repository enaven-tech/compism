# Compism

**Compose Multiplatform Event State Machine**

⚠️ This library is under active development and comes with no stability guarantees. Use with caution in production.

Sequential, deterministic event processing through a single, ordered pipeline.

Build apps that are:

- predictable in behavior  
- easy to debug and reason about  
- safe under asynchronous operations  

Built for Compose Multiplatform.

---

## Overview

Compism is a Kotlin Multiplatform library for building reliable applications using a **strict event state machine**.

All events are processed **sequentially** through a single reducer (a function that maps `state + event → new state`), ensuring deterministic state updates.

Events can come from:
- user actions (e.g. button clicks)
- async work (network, database, etc.)

Async work runs outside the system, but **all results are fed back into the same queue as events**, ensuring updates are always applied to the **current state**.

---

## Why Compism?

Compism enforces a simple rule:

> All events are processed sequentially through a single, ordered pipeline.

This makes state transitions predictable, easier to debug, and avoids issues from concurrent state updates.

---

## Core Principles

- **Single source of truth** — state is only updated through the reducer  
- **Sequential processing** — one event at a time  
- **Deterministic state updates** — same state and event → same resulting state
- **Async via events** — all async results re-enter as events  
- **State-aware results** — stale results can be safely ignored  

---

## UI Integration

Compose renders UI from state, which aligns naturally with Compism’s state machine. Navigation can then be expressed as transitions between states, where each state represents a screen or UI configuration.

This makes navigation deterministic: the current state fully defines the current screen. Returning to a previous screen is just another state transition, not a stack operation, so there is no implicit back stack to reason about. Any screen can be reached directly by transitioning to its corresponding state, making flows easier to test, reproduce, and initialize.

### CompismDisplay

`CompismDisplay` is an optional helper for rendering state transitions in Compose.

It provides:

- explicit mapping from state → screen  
- consistent transitions/animations between screens  
- explicit control over when composable state is kept or cleared during state transitions

This keeps UI transitions predictable and prevents lingering UI state.

See the demo app for usage examples.

---

## Example Flow

```
Button Click   → Event → Reducer → New State
Network Result → Event → Reducer → New State
```

---

## Example

```kotlin
// Events
sealed interface AppEvent : Event {
    data object Open : AppEvent
    data object Back : AppEvent
    data class DataLoaded(val value: String) : AppEvent
}

// State
sealed interface AppState {
    data object ScreenA : AppState
    data class ScreenB(val data: String? = null) : AppState
}

// Reducer
fun reducer(
    state: AppState,
    event: AppEvent,
    async: CompismAsync<AppEvent>
): EventResult<AppState> = when (state) {

    AppState.ScreenA -> when (event) {
        AppEvent.Open -> {
            // Trigger async work
            async.launch {
                val data = loadSlowData()
                // Result is fed back into the queue as an event
                async.send(AppEvent.DataLoaded(data))
            }
            EventResult.NewState(AppState.ScreenB())
        }
        AppEvent.Back -> EventResult.Ignored
        else -> EventResult.Ignored
    }

    is AppState.ScreenB -> when (event) {
        is AppEvent.DataLoaded ->
            EventResult.NewState(state.copy(data = event.value))

        AppEvent.Back ->
            EventResult.NewState(AppState.ScreenA)

        else -> EventResult.Ignored
    }
}

// Create handler
val compism = CompismHandler(
    initialState = AppState.ScreenA,
    reducer = ::reducer,
    onExitRequest = {}
)

// Send event
compism.send(AppEvent.Open)
```

See the demo app for a full working example, including Compose UI.

---

## When to Use

Use Compism when you want to build Compose Multiplatform apps that are:

- predictable in behavior  
- easy to debug and reason about  
- safe under asynchronous operations  
- structured around clear state transitions  

---

## License

Apache License 2.0