# Android MVI Blueprint

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-8.14.6-blue.svg)](https://gradle.org)
[![AGP](https://img.shields.io/badge/AGP-8.7.2-green.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.10.01%20BOM-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Latest-orange.svg)](https://m3.material.io)
[![Hilt](https://img.shields.io/badge/Hilt-2.52-brightgreen.svg)](https://dagger.dev/hilt/)
[![Room](https://img.shields.io/badge/Room%20DB-2.6.1-red.svg)](https://developer.android.com/training/data-storage/room)
[![Turbine](https://img.shields.io/badge/Turbine-1.2.0-yellow.svg)](https://github.com/cashapp/turbine)

A production-grade, multi-module Android reference architecture demonstrating a **strict, type-safe implementation of Model-View-Intent (MVI)** with Jetpack Compose, Kotlin Coroutines, Flow, Hilt Dependency Injection, and Room DB.

---

## 🏛️ Unidirectional Data Flow (UDF) Architecture

```
   ┌─────────────────────────────────────────────────────────────┐
   │                       COMPOSE UI                            │
   │  OrderDashboardRoute (Stateful)                             │
   │    │  collectAsStateWithLifecycle()                         │
   │    ▼                                                        │
   │  OrderDashboardScreen (100% Stateless & Previewable)        │
   └───────────────┬─────────────────────────────▲───────────────┘
                   │                             │
       UiIntent    │                             │  Immutable UiState
  (User Action /   │                             │  (@Immutable StateFlow)
 Lifecycle Event)  │                             │
                   ▼                             │
   ┌─────────────────────────────┐               │
   │    OrderDashboardViewModel  │               │
   │         (MviViewModel)      │               │
   │  ┌───────────────────────┐  │               │
   │  │ onIntent(intent: I)   │  │               │
   │  └───────────┬───────────┘  │               │
   │              ▼              │               │
   │  ┌───────────────────────┐  │               │
   │  │ Asynchronous Use Case │  │               │
   │  │ or Debounced Search   │  │               │
   │  └───────────┬───────────┘  │               │
   │              ▼              │               │
   │  ┌───────────────────────┐  │               │
   │  │ Pure State Reducer    ├──┴───────────────┘
   │  │ .update { state -> }  │
   │  └───────────┬───────────┘
   │              ▼
   │  ┌───────────────────────┐
   │  │ sendEffect(effect: E) ├──────────────────┐
   │  └───────────────────────┘                  │
   └─────────────────────────────┘                  │  One-Off UiEffect
                                                 │  (Channel.BUFFERED -> receiveAsFlow())
                                                 ▼
                                     ┌───────────────────────┐
                                     │  LaunchedEffect UI    │
                                     │  • Snackbar           │
                                     │  • Navigation         │
                                     │  • Haptic Feedback    │
                                     └───────────────────────┘
```

---

## 📦 Multi-Module Organization

```
android-mvi-blueprint/
├── :app                         # App orchestrator, Hilt Application, Navigation host
├── :core:mvi                    # Core MVI Contracts (UiState, UiIntent, UiEffect, MviViewModel)
├── :domain                      # Pure Kotlin JVM (Entities, Repository contracts, Use Cases)
├── :data                        # Room DB, Entity mappers, Pre-populated Data Seeder, Repository Impl
└── :feature:order-dashboard     # Enterprise Order Tracker UI, MVI ViewModel, State & Reducer
```

| Module | Android SDK | Exact Responsibility |
| :--- | :--- | :--- |
| **`:core:mvi`** | AndroidX Lifecycle & Compose Runtime | Reusable, type-safe MVI base contracts (`UiState`, `UiIntent`, `UiEffect`, `MviViewModel`). |
| **`:domain`** | **ZERO (Pure Kotlin JVM)** | Enterprise business rules, domain entities (`Order`, `OrderStatus`, `OrderPriority`), and pure Use Cases. |
| **`:data`** | Room DB, KSP, Hilt | Single Source of Truth persistence with SQLite indices, mock seeder, and repository implementations. |
| **`:feature:order-dashboard`** | Compose BOM, Material 3, Lifecycle | Interactive Enterprise Order & Inventory Tracker dashboard, MVI ViewModel, debounced search, and stateless composables. |
| **`:app`** | Android Application | Root `@HiltAndroidApp`, edge-to-edge `MainActivity`, and Material 3 theme wiring. |

---

## 🧠 Core Engineering Principles

### 1. Why `Channel` is used for `UiEffect` instead of `SharedFlow` or `StateFlow`

| Observable Primitive | Behavior on Recomposition | Behavior on Configuration Change | Verdict for Side Effects |
| :--- | :--- | :--- | :--- |
| **`StateFlow`** | Conflates values; replays the last value to new collectors. | Replays the last emitted effect (e.g., re-triggers navigation or re-shows a snackbar on screen rotation). | ❌ **Unsuitable** (State tearing & duplicate triggers) |
| **`SharedFlow` (replay=0)** | Hot broadcast; drops emissions if no collector is actively listening. | If an effect is emitted while the Activity/Fragment is rotating (between `STOPPED` and `STARTED`), the event is permanently lost. | ❌ **Unsafe** (Dropped navigation / snackbars) |
| **`Channel(Channel.BUFFERED)`** | Queue-based point-to-point delivery; holds unconsumed events until a collector arrives. | Buffers the event during rotation and delivers it **exactly once** when `receiveAsFlow()` resumes. Never replays upon recomposition. | ✅ **Strict MVI Standard** |

### 2. Atomic Thread-Safe State Mutations

State mutations in `MviViewModel` are guarded with `MutableStateFlow.update`:

```kotlin
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState: MutableStateFlow<S> = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect: Channel<E> = Channel(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected fun setState(reducer: S.() -> S) {
        _uiState.update { current -> current.reducer() }
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
```

### 3. Jetpack Compose 60 FPS Recomposition Optimizations

* **`@Immutable` Contract**: `UiState` is annotated with `@Immutable`. All collections use `kotlinx.collections.immutable.ImmutableList` and `ImmutableSet`, guaranteeing parameter stability so `LazyColumn` items are completely skipped during unrelated state changes.
* **`derivedStateOf` Isolation**:
  ```kotlin
  val showBatchActionBar by remember {
      derivedStateOf { uiState.hasSelection }
  }
  val showScrollToTop by remember {
      derivedStateOf { listState.firstVisibleItemIndex > 2 }
  }
  ```
  Isolates high-frequency scroll and collection mutations so the parent Composable only recomposes when the boolean evaluation flips.
* **Stable Keys and Content Types**:
  ```kotlin
  items(
      items = uiState.orders,
      key = { order -> order.id },
      contentType = { "order_card" }
  )
  ```
* **Clean State Hoisting**:
  * `OrderDashboardRoute`: Handles ViewModel lifecycle, Hilt injection, and side-effect observation.
  * `OrderDashboardScreen`: 100% Stateless Composable taking only `(state, onIntent)`. Enables instant `@Preview` inspection for Light, Dark, Loading, and Empty states without mocking Android runtime dependencies.

---

## ⚡ Reactive Search & Asynchronous Use Cases

The search and filter pipeline uses Kotlin Coroutines `Flow` operators:
```
Text Input -> SearchQueryChanged Intent -> Immediate State Update (0ms) 
  -> searchAndFilterParams Flow 
  -> debounce(300ms) 
  -> distinctUntilChanged() 
  -> flatMapLatest { (query, filter) -> observeOrdersUseCase(query, filter) }
  -> SQLite Query on Dispatchers.IO 
  -> Atomic UiState Update
```

---

## 🧪 Comprehensive Unit Testing Suite

All MVI behaviors are verified with **JUnit 5**, **MockK**, and **Cash App Turbine**:

1. **StateFlow Emission Verification**: Asserts initial loading emission followed by populated state upon debounce resolution.
2. **Side Effect Observation**: Verifies `OrderClicked(id)` emits `NavigateToOrderDetail(id)` without leaving lingering flags in `uiState`.
3. **Pure Reducer State Transitions**: Tests `ToggleOrderSelection`, `SelectAllVisible`, and `ClearSelection` for deterministic set operations.
4. **Batch Approval**: Tests batch use case execution, `TriggerSuccessHaptic`, and `ShowSnackbar` emission.

### Running Unit Tests
```bash
./gradlew test
```

### Building Debug APK
```bash
./gradlew assembleDebug
```

---

## 📄 License
```
Copyright 2026 Android MVI Blueprint Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
