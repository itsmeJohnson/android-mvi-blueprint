package com.mvi.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Production-grade reference implementation of an MVI (Model-View-Intent) ViewModel.
 *
 * Core Architectural Guarantees:
 * 1. Single Source of Truth: UI state is represented as a single immutable [S : UiState] exposed via [uiState].
 * 2. Thread-Safe Atomic Reductions: State mutations use atomic `.update { currentState -> reduce(...) }`
 *    transformations so concurrent updates never cause race conditions or state tearing.
 * 3. Lossless One-Off Side Effects: Ephemeral events (navigation, snackbar, haptics) are emitted through a
 *    buffered [Channel] exposed via [receiveAsFlow], ensuring zero dropped events during configuration
 *    changes and zero accidental replays like StateFlow or SharedFlow(replay=1).
 * 4. Unidirectional Data Flow: Composables send [I : UiIntent] through [onIntent], the single entry point.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState: MutableStateFlow<S> = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect: Channel<E> = Channel(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    /**
     * Snapshot of the current UI state.
     */
    protected val currentState: S
        get() = _uiState.value

    /**
     * Single entry point for all intents originating from the UI or lifecycle observers.
     */
    abstract fun onIntent(intent: I)

    /**
     * Atomically mutates the UI state using [MutableStateFlow.update].
     * The [reducer] lambda takes the latest snapshot of state and returns the newly reduced state.
     */
    protected fun updateState(reducer: (currentState: S) -> S) {
        _uiState.update(reducer)
    }

    /**
     * Convenience extension to mutate state with the current state as receiver `this`.
     */
    protected fun setState(reducer: S.() -> S) {
        _uiState.update { current -> current.reducer() }
    }

    /**
     * Emits a one-off side effect into the buffered channel.
     * Delivered asynchronously on [viewModelScope].
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
