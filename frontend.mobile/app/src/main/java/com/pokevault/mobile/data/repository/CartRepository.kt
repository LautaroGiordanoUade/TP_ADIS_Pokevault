package com.pokevault.mobile.data.repository

import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.model.PokemonCard
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val items: Flow<List<CartItem>>
    fun add(card: PokemonCard)
    fun remove(cardId: String)
    fun increment(cardId: String)
    fun decrement(cardId: String)
    fun clear()
}
