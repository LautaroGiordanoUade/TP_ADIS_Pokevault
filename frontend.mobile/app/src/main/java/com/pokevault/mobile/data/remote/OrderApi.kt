package com.pokevault.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OrderApi {
    @GET("orders/me")
    suspend fun myOrders(): List<OrderDto>

    @POST("orders/me")
    suspend fun createOrder(@Body payload: CreateOrderRequestDto): OrderDto
}
