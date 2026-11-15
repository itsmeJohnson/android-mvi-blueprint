package com.mvi.blueprint.domain.model

data class Order(
    val id: String,
    val customerName: String,
    val sku: String,
    val productName: String,
    val itemCount: Int,
    val totalAmount: Double,
    val status: OrderStatus,
    val priority: OrderPriority,
    val createdAt: Long,
    val destinationCity: String
)
