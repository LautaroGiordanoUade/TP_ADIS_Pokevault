package com.pokevault.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val quantity: Int,
    val amount: Double,
    val statusId: Int,
    val status: String,
    val paymentMethod: String,
    val total: Double,
)
