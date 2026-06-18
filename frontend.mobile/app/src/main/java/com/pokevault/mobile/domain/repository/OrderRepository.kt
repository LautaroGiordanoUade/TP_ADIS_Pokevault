package com.pokevault.mobile.domain.repository

import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.model.Order

interface OrderRepository {
    suspend fun createOrder(items: List<CartItem>, deliveryAddress: String): Order
}
