package com.pokevault.mobile.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSource @Inject constructor(
    private val cartDao: CartDao,
) {
    fun observeItems(): Flow<List<CartEntity>> = cartDao.observeItems()

    suspend fun getItemById(cardId: Int): CartEntity? = cartDao.getItemById(cardId)

    suspend fun upsert(item: CartEntity) {
        cartDao.upsert(item)
    }

    suspend fun updateQuantity(cardId: Int, quantity: Int) {
        cartDao.updateQuantity(cardId, quantity)
    }

    suspend fun deleteById(cardId: Int) {
        cartDao.deleteById(cardId)
    }

    suspend fun clear() {
        cartDao.clear()
    }
}
