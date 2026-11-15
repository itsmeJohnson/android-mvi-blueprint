package com.mvi.blueprint.data.repository

import com.mvi.blueprint.data.database.OrderDataSeeder
import com.mvi.blueprint.data.database.dao.OrderDao
import com.mvi.blueprint.domain.model.Order
import com.mvi.blueprint.domain.model.OrderStatusFilter
import com.mvi.blueprint.domain.repository.OrderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OrderRepository {

    private var simulateNextNetworkError = false

    override fun observeOrders(query: String, filter: OrderStatusFilter): Flow<List<Order>> {
        return orderDao.observeOrders(
            query = query,
            filter = filter.name
        ).onStart {
            ensureDatabaseSeeded()
        }.map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override suspend fun getOrderById(orderId: String): Order? = withContext(ioDispatcher) {
        orderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun batchApproveOrders(orderIds: Set<String>): Result<Int> = withContext(ioDispatcher) {
        runCatching {
            delay(400) // Simulate enterprise RPC call
            val approvedCount = orderDao.batchApproveOrders(orderIds.toList())
            approvedCount
        }
    }

    override suspend fun refreshOrders(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            delay(600) // Simulate cloud sync latency
            if (simulateNextNetworkError) {
                simulateNextNetworkError = false
                throw IllegalStateException("Connection timed out: Remote Gateway 504. Retry sync.")
            }

            val existingCount = orderDao.getOrderCount()
            if (existingCount == 0) {
                orderDao.insertOrders(OrderDataSeeder.generateInitialOrders())
            }
        }
    }

    private suspend fun ensureDatabaseSeeded() {
        if (orderDao.getOrderCount() == 0) {
            orderDao.insertOrders(OrderDataSeeder.generateInitialOrders())
        }
    }

    fun triggerNetworkErrorOnNextSync() {
        simulateNextNetworkError = true
    }
}
