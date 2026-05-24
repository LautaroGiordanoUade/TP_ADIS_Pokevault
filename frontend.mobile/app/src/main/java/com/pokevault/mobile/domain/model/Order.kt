package com.pokevault.mobile.domain.model

data class Order(
    val id: Int,
    val title: String,
    val quantity: Int,
    val amount: Double,
    val statusId: Int,
    val status: OrderStatus,
    val paymentMethod: String,
    val total: Double,
)

enum class OrderStatus {
    ReadyForPickup,
    Delivered,
}
