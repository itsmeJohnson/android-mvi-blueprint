package com.mvi.blueprint.feature.orderdashboard.presentation

import androidx.lifecycle.viewModelScope
import com.mvi.blueprint.domain.model.OrderStatusFilter
import com.mvi.blueprint.domain.usecase.BatchApproveOrdersUseCase
import com.mvi.blueprint.domain.usecase.ObserveOrdersUseCase
import com.mvi.blueprint.domain.usecase.RefreshOrdersUseCase
import com.mvi.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderDashboardViewModel @Inject constructor(
    private val observeOrdersUseCase: ObserveOrdersUseCase,
    private val batchApproveOrdersUseCase: BatchApproveOrdersUseCase,
    private val refreshOrdersUseCase: RefreshOrdersUseCase
) : MviViewModel<OrderDashboardState, OrderDashboardIntent, OrderDashboardEffect>(
    initialState = OrderDashboardState(isLoading = true)
) {

    private val searchAndFilterParams = MutableStateFlow(
        Pair(currentState.searchQuery, currentState.activeFilter)
    )

    init {
        setupReactiveSearchPipeline()
        onIntent(OrderDashboardIntent.LoadOrders)
    }

    override fun onIntent(intent: OrderDashboardIntent) {
        when (intent) {
            is OrderDashboardIntent.LoadOrders -> handleLoadOrders()
            is OrderDashboardIntent.SearchQueryChanged -> handleSearchQueryChanged(intent.query)
            is OrderDashboardIntent.FilterStatusSelected -> handleFilterSelected(intent.status)
            is OrderDashboardIntent.ToggleOrderSelection -> handleToggleSelection(intent.orderId)
            is OrderDashboardIntent.ClearSelection -> handleClearSelection()
            is OrderDashboardIntent.SelectAllVisible -> handleSelectAllVisible()
            is OrderDashboardIntent.BatchApproveSelected -> handleBatchApprove()
            is OrderDashboardIntent.RetryFailedSync -> handleRetry()
            is OrderDashboardIntent.OrderClicked -> handleOrderClicked(intent.orderId)
            is OrderDashboardIntent.DismissError -> setState { copy(errorMessage = null) }
        }
    }

    private fun setupReactiveSearchPipeline() {
        searchAndFilterParams
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { (query, filter) ->
                observeOrdersUseCase(query, filter)
                    .map { domainOrders -> Result.success(domainOrders) }
                    .catch { emit(Result.failure(it)) }
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { domainList ->
                        setState {
                            val uiModels = domainList.map { order ->
                                order.toUiModel(isSelected = selectedIds.contains(order.id))
                            }.toImmutableList()

                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                orders = uiModels,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        val message = error.message ?: "Failed to query orders database."
                        setState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = message
                            )
                        }
                        sendEffect(OrderDashboardEffect.ShowSnackbar(message))
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun handleLoadOrders() {
        viewModelScope.launch {
            if (!currentState.isLoading) {
                setState { copy(isRefreshing = true) }
            }
            val refreshResult = refreshOrdersUseCase()
            refreshResult.fold(
                onSuccess = {
                    setState { copy(isRefreshing = false) }
                },
                onFailure = { error ->
                    val message = error.message ?: "Sync error occurred."
                    setState { copy(errorMessage = message, isRefreshing = false, isLoading = false) }
                    sendEffect(OrderDashboardEffect.ShowSnackbar(message))
                }
            )
        }
    }

    private fun handleSearchQueryChanged(query: String) {
        setState { copy(searchQuery = query) }
        searchAndFilterParams.value = Pair(query, currentState.activeFilter)
    }

    private fun handleFilterSelected(filter: OrderStatusFilter) {
        setState { copy(activeFilter = filter) }
        searchAndFilterParams.value = Pair(currentState.searchQuery, filter)
    }

    private fun handleToggleSelection(orderId: String) {
        setState {
            val newSelected = if (selectedIds.contains(orderId)) {
                selectedIds - orderId
            } else {
                selectedIds + orderId
            }.toImmutableSet()

            val updatedOrders = orders.map { item ->
                if (item.id == orderId) {
                    item.copy(isSelected = newSelected.contains(item.id))
                } else {
                    item
                }
            }.toImmutableList()

            copy(selectedIds = newSelected, orders = updatedOrders)
        }
    }

    private fun handleClearSelection() {
        setState {
            val updatedOrders = orders.map { it.copy(isSelected = false) }.toImmutableList()
            copy(selectedIds = emptySet<String>().toImmutableSet(), orders = updatedOrders)
        }
    }

    private fun handleSelectAllVisible() {
        setState {
            val allIds = orders.map { it.id }.toImmutableSet()
            val updatedOrders = orders.map { it.copy(isSelected = true) }.toImmutableList()
            copy(selectedIds = allIds, orders = updatedOrders)
        }
    }

    private fun handleBatchApprove() {
        val targets = currentState.selectedIds
        if (targets.isEmpty()) return

        viewModelScope.launch {
            setState { copy(isBatchApproving = true) }
            val result = batchApproveOrdersUseCase(targets)
            result.fold(
                onSuccess = { approvedCount ->
                    setState {
                        copy(
                            isBatchApproving = false,
                            selectedIds = emptySet<String>().toImmutableSet()
                        )
                    }
                    sendEffect(OrderDashboardEffect.TriggerSuccessHaptic)
                    sendEffect(
                        OrderDashboardEffect.ShowSnackbar("Approved $approvedCount order${if (approvedCount > 1) "s" else ""} successfully.")
                    )
                },
                onFailure = { error ->
                    val message = error.message ?: "Batch approval failed."
                    setState { copy(isBatchApproving = false) }
                    sendEffect(OrderDashboardEffect.ShowSnackbar(message))
                }
            )
        }
    }

    private fun handleRetry() {
        setState { copy(isLoading = true, errorMessage = null) }
        handleLoadOrders()
    }

    private fun handleOrderClicked(orderId: String) {
        sendEffect(OrderDashboardEffect.NavigateToOrderDetail(orderId))
    }
}
