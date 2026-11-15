package com.mvi.blueprint.domain.usecase

import com.mvi.blueprint.domain.repository.OrderRepository

class RefreshOrdersUseCase(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshOrders()
    }
}
