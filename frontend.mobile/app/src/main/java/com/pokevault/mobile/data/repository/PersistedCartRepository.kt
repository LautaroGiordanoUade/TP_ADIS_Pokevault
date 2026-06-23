package com.pokevault.mobile.data.repository

import com.pokevault.mobile.data.local.CartLocalDataSource
import com.pokevault.mobile.data.mapper.toCartEntity
import com.pokevault.mobile.data.mapper.toDomain
import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.model.PokemonCard
import com.pokevault.mobile.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistedCartRepository @Inject constructor(
    private val localDataSource: CartLocalDataSource,
) : CartRepository {
    override val items: Flow<List<CartItem>> = localDataSource.observeItems().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun add(card: PokemonCard) {
        val existingItem = localDataSource.getItemById(card.id)
        val nextQuantity = (existingItem?.quantity ?: 0) + 1
        localDataSource.upsert(card.toCartEntity(quantity = nextQuantity))
    }

    override suspend fun remove(cardId: Int) {
        localDataSource.deleteById(cardId)
    }

    override suspend fun updateQuantity(cardId: Int, quantity: Int) {
        when {
            quantity <= 0 -> localDataSource.deleteById(cardId)
            else -> localDataSource.updateQuantity(cardId, quantity)
        }
    }

    override suspend fun increment(cardId: Int) {
        val currentItem = localDataSource.getItemById(cardId) ?: return
        localDataSource.updateQuantity(cardId, currentItem.quantity + 1)
    }

    override suspend fun decrement(cardId: Int) {
        val currentItem = localDataSource.getItemById(cardId) ?: return
        val nextQuantity = currentItem.quantity - 1
        if (nextQuantity <= 0) {
            localDataSource.deleteById(cardId)
        } else {
            localDataSource.updateQuantity(cardId, nextQuantity)
        }
    }

    override suspend fun clear() {
        localDataSource.clear()
    }
}
