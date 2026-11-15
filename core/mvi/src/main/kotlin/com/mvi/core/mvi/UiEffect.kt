package com.mvi.core.mvi

/**
 * Marker interface for all One-Off Side Effects (navigation, snackbars, haptic feedback, dialogs).
 * Handled via buffered channels so effects are delivered exactly once and never replayed upon recomposition.
 */
interface UiEffect
