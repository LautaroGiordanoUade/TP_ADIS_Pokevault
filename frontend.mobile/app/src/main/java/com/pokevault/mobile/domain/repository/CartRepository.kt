package com.pokevault.mobile.domain.repository

import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.model.PokemonCard
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val items: Flow<List<CartItem>>
    suspend fun add(card: PokemonCard)
    suspend fun remove(cardId: Int)
    suspend fun updateQuantity(cardId: Int, quantity: Int)
    suspend fun increment(cardId: Int)
    suspend fun decrement(cardId: Int)
    suspend fun clear()
}
