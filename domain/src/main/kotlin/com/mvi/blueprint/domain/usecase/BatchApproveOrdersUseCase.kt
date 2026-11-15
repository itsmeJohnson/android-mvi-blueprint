package com.mvi.blueprint.domain.usecase

import com.mvi.blueprint.domain.repository.OrderRepository

class BatchApproveOrdersUseCase(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderIds: Set<String>): Result<Int> {
        if (orderIds.isEmpty()) {
            return Result.success(0)
        }
        return repository.batchApproveOrders(orderIds)
    }
}
