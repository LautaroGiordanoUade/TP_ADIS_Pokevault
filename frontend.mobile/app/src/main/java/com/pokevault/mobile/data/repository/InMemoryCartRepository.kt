package com.pokevault.mobile.data.repository

import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.model.PokemonCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryCartRepository @Inject constructor() : CartRepository {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())

    override val items: Flow<List<CartItem>> = _items.asStateFlow()

    override fun add(card: PokemonCard) {
        _items.update { current ->
            val existing = current.firstOrNull { it.card.id == card.id }
            if (existing == null) current + CartItem(card, 1)
            else current.map { if (it.card.id == card.id) it.copy(quantity = it.quantity + 1) else it }
        }
    }

    override fun remove(cardId: Int) {
        _items.update { current -> current.filterNot { it.card.id == cardId } }
    }

    override fun increment(cardId: Int) {
        _items.update { current -> current.map { if (it.card.id == cardId) it.copy(quantity = it.quantity + 1) else it } }
    }

    override fun decrement(cardId: Int) {
        _items.update { current ->
            current.mapNotNull {
                if (it.card.id != cardId) it else it.copy(quantity = it.quantity - 1).takeIf { item -> item.quantity > 0 }
            }
        }
    }

    override fun clear() {
        _items.value = emptyList()
    }
}
