package com.mvi.blueprint.feature.orderdashboard.presentation

import app.cash.turbine.test
import com.mvi.blueprint.domain.model.Order
import com.mvi.blueprint.domain.model.OrderPriority
import com.mvi.blueprint.domain.model.OrderStatus
import com.mvi.blueprint.domain.model.OrderStatusFilter
import com.mvi.blueprint.domain.usecase.BatchApproveOrdersUseCase
import com.mvi.blueprint.domain.usecase.ObserveOrdersUseCase
import com.mvi.blueprint.domain.usecase.RefreshOrdersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeOrdersUseCase: ObserveOrdersUseCase = mockk()
    private val batchApproveOrdersUseCase: BatchApproveOrdersUseCase = mockk()
    private val refreshOrdersUseCase: RefreshOrdersUseCase = mockk()

    private val sampleOrders = listOf(
        Order(
            id = "ORD-001",
            customerName = "Apex Robotics",
            sku = "NV-H200",
            productName = "NVIDIA H200 GPU",
            itemCount = 8,
            totalAmount = 264_000.0,
            status = OrderStatus.PENDING,
            priority = OrderPriority.URGENT,
            createdAt = 1_700_000_000_000L,
            destinationCity = "San Jose, CA"
        ),
        Order(
            id = "ORD-002",
            customerName = "Starlight Systems",
            sku = "APL-MBP",
            productName = "MacBook Pro 16",
            itemCount = 10,
            totalAmount = 45_000.0,
            status = OrderStatus.APPROVED,
            priority = OrderPriority.HIGH,
            createdAt = 1_700_001_000_000L,
            destinationCity = "Seattle, WA"
        )
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeOrdersUseCase(any(), any()) } returns flowOf(sampleOrders)
        coEvery { refreshOrdersUseCase() } returns Result.success(Unit)
        coEvery { batchApproveOrdersUseCase(any()) } returns Result.success(2)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): OrderDashboardViewModel {
        return OrderDashboardViewModel(
            observeOrdersUseCase = observeOrdersUseCase,
            batchApproveOrdersUseCase = batchApproveOrdersUseCase,
            refreshOrdersUseCase = refreshOrdersUseCase
        )
    }

    @Test
    @DisplayName("Initial state emits loading then populates with mapped OrderUiModels")
    fun `initial state flow emission sequence loads orders successfully`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Initial state from constructor
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            assertTrue(initialState.orders.isEmpty())

            // Advance debounce and coroutines
            advanceTimeBy(350L)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(2, loadedState.orders.size)
            assertEquals("ORD-001", loadedState.orders[0].id)
            assertEquals("ORD-002", loadedState.orders[1].id)
            assertNull(loadedState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("SearchQueryChanged updates searchQuery instantly and debounces DB query")
    fun `search query intent updates state immediately and debounces observation`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(350L)

        viewModel.uiState.test {
            val current = awaitItem()
            assertEquals("", current.searchQuery)

            viewModel.onIntent(OrderDashboardIntent.SearchQueryChanged("NVIDIA"))

            val updatedQueryState = awaitItem()
            assertEquals("NVIDIA", updatedQueryState.searchQuery)

            // Verify before debounce finishes, query was not fired
            advanceTimeBy(200L)

            // Now advance past 300ms debounce
            advanceTimeBy(150L)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("FilterStatusSelected updates activeFilter in state")
    fun `filter status selection intent updates active filter`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(350L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(OrderStatusFilter.ALL, state.activeFilter)

            viewModel.onIntent(OrderDashboardIntent.FilterStatusSelected(OrderStatusFilter.PENDING))

            val filteredState = awaitItem()
            assertEquals(OrderStatusFilter.PENDING, filteredState.activeFilter)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("ToggleOrderSelection correctly updates selectedIds and order item isSelected flag")
    fun `selection toggle intent transitions selection state atomically`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(350L)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.hasSelection)

            // Select ORD-001
            viewModel.onIntent(OrderDashboardIntent.ToggleOrderSelection("ORD-001"))
            val selectedState = awaitItem()
            assertTrue(selectedState.selectedIds.contains("ORD-001"))
            assertEquals(1, selectedState.selectedCount)
            assertTrue(selectedState.orders.first { it.id == "ORD-001" }.isSelected)

            // Deselect ORD-001
            viewModel.onIntent(OrderDashboardIntent.ToggleOrderSelection("ORD-001"))
            val deselectedState = awaitItem()
            assertFalse(deselectedState.selectedIds.contains("ORD-001"))
            assertEquals(0, deselectedState.selectedCount)
            assertFalse(deselectedState.orders.first { it.id == "ORD-001" }.isSelected)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("SelectAllVisible and ClearSelection update all items in state atomically")
    fun `bulk selection intents transition state cleanly`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(350L)

        viewModel.uiState.test {
            awaitItem() // Current state

            viewModel.onIntent(OrderDashboardIntent.SelectAllVisible)
            val allSelectedState = awaitItem()
            assertEquals(2, allSelectedState.selectedCount)
            assertTrue(allSelectedState.orders.all { it.isSelected })

            viewModel.onIntent(OrderDashboardIntent.ClearSelection)
            val clearedState = awaitItem()
            assertEquals(0, clearedState.selectedCount)
            assertTrue(clearedState.orders.none { it.isSelected })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("BatchApproveSelected executes use case and triggers haptic + snackbar effects")
    fun `batch approval executes successfully and emits side effects`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(350L)

        // Select items first
        viewModel.onIntent(OrderDashboardIntent.ToggleOrderSelection("ORD-001"))
        viewModel.onIntent(OrderDashboardIntent.ToggleOrderSelection("ORD-002"))
        advanceTimeBy(100L)

        viewModel.effect.test {
            viewModel.onIntent(OrderDashboardIntent.BatchApproveSelected)
            advanceTimeBy(100L)

            // Verify one-off effects emitted in sequence
            assertEquals(OrderDashboardEffect.TriggerSuccessHaptic, awaitItem())

            val snackbarEffect = awaitItem() as OrderDashboardEffect.ShowSnackbar
            assertTrue(snackbarEffect.message.contains("Approved 2 orders"))

            // Verify use case was invoked with selected ids
            coVerify(exactly = 1) {
                batchApproveOrdersUseCase(setOf("ORD-001", "ORD-002"))
            }

            cancelAndIgnoreRemainingEvents()
        }

        // Selection should be cleared after approval
        assertTrue(viewModel.uiState.value.selectedIds.isEmpty())
    }

    @Test
    @DisplayName("OrderClicked emits NavigateToOrderDetail effect without polluting uiState")
    fun `order click intent emits navigation effect purely`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(350L)

        viewModel.effect.test {
            viewModel.onIntent(OrderDashboardIntent.OrderClicked("ORD-9481"))

            val effect = awaitItem()
            assertEquals(OrderDashboardEffect.NavigateToOrderDetail("ORD-9481"), effect)

            cancelAndIgnoreRemainingEvents()
        }

        // Verify uiState contains no lingering navigation flags
        assertEquals(2, viewModel.uiState.value.orders.size)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    @DisplayName("Error handling emits snackbar effect and updates errorMessage state")
    fun `database or network error updates error state and emits snackbar`() = runTest(testDispatcher) {
        val errorFlow = MutableSharedFlow<List<Order>>()
        every { observeOrdersUseCase(any(), any()) } returns errorFlow

        val viewModel = createViewModel()
        advanceTimeBy(350L)

        viewModel.effect.test {
            // Trigger failure
            viewModel.uiState.test {
                awaitItem() // Current state

                viewModel.onIntent(OrderDashboardIntent.SearchQueryChanged("faulty_query"))
                awaitItem() // searchQuery updated
                advanceTimeBy(350L)

                cancelAndIgnoreRemainingEvents()
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
