package com.mvi.core.mvi

import androidx.compose.runtime.Immutable

/**
 * Marker interface for all UI State models in the MVI architecture.
 * Marked with [Immutable] so the Jetpack Compose compiler guarantees that all public properties
 * will never change after construction, allowing Composables to safely skip recomposition.
 */
@Immutable
interface UiState
