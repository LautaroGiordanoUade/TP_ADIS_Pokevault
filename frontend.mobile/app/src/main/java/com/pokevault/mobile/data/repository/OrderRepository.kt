package com.pokevault.mobile.data.repository

import com.pokevault.mobile.data.mapper.toDomain
import com.pokevault.mobile.data.remote.CreateOrderItemRequestDto
import com.pokevault.mobile.data.remote.CreateOrderRequestDto
import com.pokevault.mobile.data.remote.OrderApi
import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.model.Order
import javax.inject.Inject
import javax.inject.Singleton

interface OrderRepository {
    suspend fun createOrder(items: List<CartItem>, deliveryAddress: String): Order
}

@Singleton
class DefaultOrderRepository @Inject constructor(
    private val orderApi: OrderApi,
) : OrderRepository {
    override suspend fun createOrder(items: List<CartItem>, deliveryAddress: String): Order {
        return orderApi.createOrder(
            CreateOrderRequestDto(
                items = items.map {
                    CreateOrderItemRequestDto(
                        pokemonId = it.card.id,
                        quantity = it.quantity,
                    )
                },
                deliveryAddress = deliveryAddress,
                paymentMethod = "Google Pay",
            ),
        ).toDomain()
    }
}
