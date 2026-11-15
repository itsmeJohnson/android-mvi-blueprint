package com.mvi.blueprint.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mvi.blueprint.domain.model.Order
import com.mvi.blueprint.domain.model.OrderPriority
import com.mvi.blueprint.domain.model.OrderStatus

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["status"]),
        Index(value = ["priority"]),
        Index(value = ["customerName"]),
        Index(value = ["sku"])
    ]
)
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val customerName: String,
    val sku: String,
    val productName: String,
    val itemCount: Int,
    val totalAmount: Double,
    val status: String,
    val priority: String,
    val createdAt: Long,
    val destinationCity: String
) {
    fun toDomain(): Order {
        return Order(
            id = id,
            customerName = customerName,
            sku = sku,
            productName = productName,
            itemCount = itemCount,
            totalAmount = totalAmount,
            status = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.PENDING),
            priority = runCatching { OrderPriority.valueOf(priority) }.getOrDefault(OrderPriority.NORMAL),
            createdAt = createdAt,
            destinationCity = destinationCity
        )
    }

    companion object {
        fun fromDomain(domain: Order): OrderEntity {
            return OrderEntity(
                id = domain.id,
                customerName = domain.customerName,
                sku = domain.sku,
                productName = domain.productName,
                itemCount = domain.itemCount,
                totalAmount = domain.totalAmount,
                status = domain.status.name,
                priority = domain.priority.name,
                createdAt = domain.createdAt,
                destinationCity = domain.destinationCity
            )
        }
    }
}
