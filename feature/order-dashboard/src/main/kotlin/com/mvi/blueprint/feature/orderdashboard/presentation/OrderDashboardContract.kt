package com.mvi.blueprint.feature.orderdashboard.presentation

import androidx.compose.runtime.Immutable
import com.mvi.core.mvi.UiEffect
import com.mvi.core.mvi.UiIntent
import com.mvi.core.mvi.UiState
import com.mvi.blueprint.domain.model.Order
import com.mvi.blueprint.domain.model.OrderPriority
import com.mvi.blueprint.domain.model.OrderStatus
import com.mvi.blueprint.domain.model.OrderStatusFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Immutable
data class OrderUiModel(
    val id: String,
    val customerName: String,
    val sku: String,
    val productName: String,
    val itemCount: Int,
    val formattedAmount: String,
    val status: OrderStatus,
    val priority: OrderPriority,
    val formattedDate: String,
    val destinationCity: String,
    val isSelected: Boolean
)

fun Order.toUiModel(isSelected: Boolean = false): OrderUiModel {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)

    return OrderUiModel(
        id = id,
        customerName = customerName,
        sku = sku,
        productName = productName,
        itemCount = itemCount,
        formattedAmount = currencyFormatter.format(totalAmount),
        status = status,
        priority = priority,
        formattedDate = dateFormatter.format(Date(createdAt)),
        destinationCity = destinationCity,
        isSelected = isSelected
    )
}

@Immutable
data class OrderDashboardState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val orders: ImmutableList<OrderUiModel> = persistentListOf(),
    val selectedIds: ImmutableSet<String> = persistentSetOf(),
    val searchQuery: String = "",
    val activeFilter: OrderStatusFilter = OrderStatusFilter.ALL,
    val errorMessage: String? = null,
    val isBatchApproving: Boolean = false
) : UiState {
    val selectedCount: Int get() = selectedIds.size
    val hasSelection: Boolean get() = selectedIds.isNotEmpty()
    val isEmpty: Boolean get() = !isLoading && orders.isEmpty()
}

sealed interface OrderDashboardIntent : UiIntent {
    data object LoadOrders : OrderDashboardIntent
    data class SearchQueryChanged(val query: String) : OrderDashboardIntent
    data class FilterStatusSelected(val status: OrderStatusFilter) : OrderDashboardIntent
    data class ToggleOrderSelection(val orderId: String) : OrderDashboardIntent
    data object ClearSelection : OrderDashboardIntent
    data object SelectAllVisible : OrderDashboardIntent
    data object BatchApproveSelected : OrderDashboardIntent
    data object RetryFailedSync : OrderDashboardIntent
    data class OrderClicked(val orderId: String) : OrderDashboardIntent
    data object DismissError : OrderDashboardIntent
}

sealed interface OrderDashboardEffect : UiEffect {
    data class ShowSnackbar(val message: String) : OrderDashboardEffect
    data class NavigateToOrderDetail(val orderId: String) : OrderDashboardEffect
    data object TriggerSuccessHaptic : OrderDashboardEffect
}
