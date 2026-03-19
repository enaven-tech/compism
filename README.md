# Compism

**Compose Multiplatform Event State Machine**

Sequential, deterministic event processing for predictable and debuggable UI logic.

---

## Overview

Compism is a Kotlin Multiplatform library for building reliable applications using a **strict event state machine**.

All events are processed **sequentially** through a single reducer (a function that maps `State + Event → New State`), ensuring deterministic state updates.

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

Compism makes UI state changes explicit and safe:

- model UI as distinct states  
- move between them predictably  
- avoid invalid or inconsistent UI states

This fits naturally with Compose, where UI is derived from state.

---

## Example Flow

```
Button Click → Event → Reducer → New State
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
            // trigger async work
            async.launch {
                val data = loadSlowData()
                // result is fed back into the queue as an event
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
val kompis = CompismHandler(
    initialState = AppState.ScreenA,
    reducer = ::reducer,
    onCloseRequest = {}
)

// Send event
kompis.sendEvent(AppEvent.Open)
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