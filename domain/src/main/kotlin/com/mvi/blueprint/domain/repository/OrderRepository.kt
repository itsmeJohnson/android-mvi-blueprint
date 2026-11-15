package com.mvi.blueprint.domain.repository

import com.mvi.blueprint.domain.model.Order
import com.mvi.blueprint.domain.model.OrderStatusFilter
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(query: String, filter: OrderStatusFilter): Flow<List<Order>>
    suspend fun getOrderById(orderId: String): Order?
    suspend fun batchApproveOrders(orderIds: Set<String>): Result<Int>
    suspend fun refreshOrders(): Result<Unit>
}
