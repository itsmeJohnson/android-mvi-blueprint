package com.mvi.core.mvi

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Test implementations
    data class TestState(val count: Int = 0, val label: String = "init") : UiState
    sealed interface TestIntent : UiIntent {
        data object Increment : TestIntent
        data class SetLabel(val newLabel: String) : TestIntent
        data class TriggerSideEffect(val message: String) : TestIntent
    }
    sealed interface TestEffect : UiEffect {
        data class ShowToast(val text: String) : TestEffect
    }

    class TestMviViewModel : MviViewModel<TestState, TestIntent, TestEffect>(TestState()) {
        override fun onIntent(intent: TestIntent) {
            when (intent) {
                is TestIntent.Increment -> setState { copy(count = count + 1) }
                is TestIntent.SetLabel -> updateState { it.copy(label = intent.newLabel) }
                is TestIntent.TriggerSideEffect -> sendEffect(TestEffect.ShowToast(intent.message))
            }
        }
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is emitted immediately`() = runTest(testDispatcher) {
        val viewModel = TestMviViewModel()

        viewModel.uiState.test {
            assertEquals(TestState(0, "init"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `intents atomically reduce uiState`() = runTest(testDispatcher) {
        val viewModel = TestMviViewModel()

        viewModel.uiState.test {
            assertEquals(TestState(0, "init"), awaitItem())

            viewModel.onIntent(TestIntent.Increment)
            assertEquals(TestState(1, "init"), awaitItem())

            viewModel.onIntent(TestIntent.SetLabel("updated"))
            assertEquals(TestState(1, "updated"), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `side effects are emitted via buffered channel without modifying state`() = runTest(testDispatcher) {
        val viewModel = TestMviViewModel()

        viewModel.effect.test {
            viewModel.onIntent(TestIntent.TriggerSideEffect("Order Created"))
            assertEquals(TestEffect.ShowToast("Order Created"), awaitItem())

            // Verify state remained pristine
            assertEquals(TestState(0, "init"), viewModel.uiState.value)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
