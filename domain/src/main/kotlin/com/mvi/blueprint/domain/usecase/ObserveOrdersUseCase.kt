package com.mvi.blueprint.domain.usecase

import com.mvi.blueprint.domain.model.Order
import com.mvi.blueprint.domain.model.OrderStatusFilter
import com.mvi.blueprint.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class ObserveOrdersUseCase(
    private val repository: OrderRepository
) {
    operator fun invoke(query: String, filter: OrderStatusFilter): Flow<List<Order>> {
        return repository.observeOrders(query = query.trim(), filter = filter)
    }
}
