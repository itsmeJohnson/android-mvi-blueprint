package com.mvi.blueprint.domain.usecase

import com.mvi.blueprint.domain.model.Order
import com.mvi.blueprint.domain.repository.OrderRepository

class GetOrderDetailUseCase(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Order? {
        return repository.getOrderById(orderId)
    }
}
