package com.pokevault.mobile.data.local

import javax.inject.Inject

class OrderLocalDataSource @Inject constructor(
    private val dao: OrderDao,
) {
    suspend fun getOrders(userId: Int): List<OrderEntity> = dao.getOrders(userId)

    suspend fun replaceOrders(userId: Int, orders: List<OrderEntity>) {
        dao.clearOrders(userId)
        dao.upsertOrders(orders)
    }
}
