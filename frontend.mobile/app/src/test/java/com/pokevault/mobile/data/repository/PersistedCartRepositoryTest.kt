package com.pokevault.mobile.data.repository

import app.cash.turbine.test
import com.pokevault.mobile.data.local.CartDao
import com.pokevault.mobile.data.local.CartEntity
import com.pokevault.mobile.data.local.CartLocalDataSource
import com.pokevault.mobile.domain.model.PokemonCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PersistedCartRepositoryTest {
    private val cartDao = FakeCartDao()
    private val repository = PersistedCartRepository(CartLocalDataSource(cartDao))

    @Test
    fun `items starts empty`() = runTest {
        repository.items.test {
            assertEquals(emptyList<Any>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add inserts item and adding same card increments quantity`() = runTest {
        val card = pokemonCard(id = 7, name = "Pikachu")

        repository.add(card)
        repository.add(card)

        repository.items.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(card.id, items.single().card.id)
            assertEquals(2, items.single().quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update quantity edits persisted item`() = runTest {
        val card = pokemonCard(id = 10, name = "Charmander")

        repository.add(card)
        repository.updateQuantity(card.id, 4)

        repository.items.test {
            assertEquals(4, awaitItem().single().quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `zero quantity removes item`() = runTest {
        val card = pokemonCard(id = 12, name = "Bulbasaur")

        repository.add(card)
        repository.updateQuantity(card.id, 0)

        repository.items.test {
            assertEquals(emptyList<Any>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remove deletes only selected card`() = runTest {
        val pikachu = pokemonCard(id = 1, name = "Pikachu")
        val bulbasaur = pokemonCard(id = 2, name = "Bulbasaur")

        repository.add(pikachu)
        repository.add(bulbasaur)
        repository.remove(pikachu.id)

        repository.items.test {
            val items = awaitItem()
            assertEquals(listOf(bulbasaur.id), items.map { it.card.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `new repository over same local source reads persisted cart`() = runTest {
        val card = pokemonCard(id = 25, name = "Mew")
        repository.add(card)
        repository.updateQuantity(card.id, 3)

        val repositoryAfterRecreation = PersistedCartRepository(CartLocalDataSource(cartDao))

        repositoryAfterRecreation.items.test {
            val item = awaitItem().single()
            assertEquals(card.id, item.card.id)
            assertEquals(3, item.quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun pokemonCard(id: Int, name: String) = PokemonCard(
        id = id,
        externalId = "external-$id",
        name = name,
        imageUrl = "https://example.com/$id.png",
        rarity = "Rare",
        price = 100.0 + id,
        description = "Test card",
        types = listOf("Electric"),
        setName = "Base",
        number = "$id",
        artist = "Tester",
        source = "unit-test",
    )

    private class FakeCartDao : CartDao {
        private val rows = linkedMapOf<Int, CartEntity>()
        private val state = MutableStateFlow<List<CartEntity>>(emptyList())

        override fun observeItems(): Flow<List<CartEntity>> = state

        override suspend fun getItemById(cardId: Int): CartEntity? = rows[cardId]

        override suspend fun upsert(item: CartEntity) {
            rows[item.id] = item
            emitRows()
        }

        override suspend fun updateQuantity(cardId: Int, quantity: Int) {
            rows[cardId]?.let { rows[cardId] = it.copy(quantity = quantity) }
            emitRows()
        }

        override suspend fun deleteById(cardId: Int) {
            rows.remove(cardId)
            emitRows()
        }

        override suspend fun clear() {
            rows.clear()
            emitRows()
        }

        private fun emitRows() {
            state.value = rows.values.sortedBy { it.name }
        }
    }
}
