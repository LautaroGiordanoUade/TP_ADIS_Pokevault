package com.pokevault.mobile.ui.feature.cart.viewmodel

import app.cash.turbine.test
import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.OrderStatus
import com.pokevault.mobile.domain.model.PokemonCard
import com.pokevault.mobile.domain.repository.CartRepository
import com.pokevault.mobile.domain.repository.OrderRepository
import com.pokevault.mobile.testing.MainDispatcherRule
import com.pokevault.mobile.ui.feature.cart.state.CartEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cartRepository = FakeCartRepository()
    private val orderRepository = FakeOrderRepository()

    @Test
    fun `empty repository exposes empty cart state`() = runTest {
        val viewModel = CartViewModel(cartRepository, orderRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList<CartItem>(), state.items)
            assertEquals(0, state.totalQuantity)
            assertEquals(0.0, state.finalTotal, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `repository items are reflected in totals`() = runTest {
        cartRepository.setItems(
            listOf(
                CartItem(pokemonCard(id = 1, price = 100.0), quantity = 2),
                CartItem(pokemonCard(id = 2, price = 50.0), quantity = 1),
            )
        )
        val viewModel = CartViewModel(cartRepository, orderRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.items.size)
            assertEquals(3, state.totalQuantity)
            assertEquals(250.0, state.finalTotal, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `quantity editor filters non digits and saves valid value`() = runTest {
        val item = CartItem(pokemonCard(id = 8), quantity = 1)
        cartRepository.setItems(listOf(item))
        val viewModel = CartViewModel(cartRepository, orderRepository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(CartEvent.OnEditQuantity(item))
            viewModel.onEvent(CartEvent.OnQuantityInputChanged("4abc2"))
            viewModel.onEvent(CartEvent.OnSaveEditedQuantity)

            assertEquals(8, cartRepository.updatedCardId)
            assertEquals(42, cartRepository.updatedQuantity)
            val state = viewModel.uiState.value
            assertNull(state.editingItem)
            assertEquals("", state.editingQuantityInput)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `quantity editor keeps dialog open for invalid value`() = runTest {
        val item = CartItem(pokemonCard(id = 9), quantity = 2)
        cartRepository.setItems(listOf(item))
        val viewModel = CartViewModel(cartRepository, orderRepository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(CartEvent.OnEditQuantity(item))
            viewModel.onEvent(CartEvent.OnQuantityInputChanged("0"))
            viewModel.onEvent(CartEvent.OnSaveEditedQuantity)

            assertNull(cartRepository.updatedCardId)
            val state = viewModel.uiState.value
            assertEquals(item, state.editingItem)
            assertEquals("0", state.editingQuantityInput)
            assertNotNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remove event delegates to repository`() = runTest {
        val viewModel = CartViewModel(cartRepository, orderRepository)

        viewModel.onEvent(CartEvent.OnRemove(99))

        assertEquals(99, cartRepository.removedCardId)
    }

    @Test
    fun `confirm payment creates order and clears cart`() = runTest {
        val item = CartItem(pokemonCard(id = 3), quantity = 2)
        cartRepository.setItems(listOf(item))
        val viewModel = CartViewModel(cartRepository, orderRepository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(CartEvent.OnConfirmPayment)

            assertEquals(listOf(item), orderRepository.createdItems)
            assertEquals("Av. Corrientes 1250, CABA, Argentina", orderRepository.createdAddress)
            assertEquals(true, cartRepository.clearCalled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun pokemonCard(id: Int, price: Double = 100.0) = PokemonCard(
        id = id,
        externalId = "external-$id",
        name = "Card $id",
        imageUrl = "https://example.com/$id.png",
        rarity = null,
        price = price,
        description = null,
        types = emptyList(),
        setName = null,
        number = null,
        artist = null,
        source = "unit-test",
    )

    private class FakeCartRepository : CartRepository {
        private val state = MutableStateFlow<List<CartItem>>(emptyList())
        override val items: Flow<List<CartItem>> = state

        var updatedCardId: Int? = null
        var updatedQuantity: Int? = null
        var removedCardId: Int? = null
        var clearCalled: Boolean = false

        fun setItems(items: List<CartItem>) {
            state.value = items
        }

        override suspend fun add(card: PokemonCard) = Unit

        override suspend fun remove(cardId: Int) {
            removedCardId = cardId
        }

        override suspend fun updateQuantity(cardId: Int, quantity: Int) {
            updatedCardId = cardId
            updatedQuantity = quantity
            state.value = state.value.map {
                if (it.card.id == cardId) it.copy(quantity = quantity) else it
            }
        }

        override suspend fun increment(cardId: Int) = Unit

        override suspend fun decrement(cardId: Int) = Unit

        override suspend fun clear() {
            clearCalled = true
            state.value = emptyList()
        }
    }

    private class FakeOrderRepository : OrderRepository {
        var createdItems: List<CartItem>? = null
        var createdAddress: String? = null

        override suspend fun createOrder(items: List<CartItem>, deliveryAddress: String): Order {
            createdItems = items
            createdAddress = deliveryAddress
            return Order(
                id = 1,
                title = "Unit test order",
                quantity = items.sumOf { it.quantity },
                amount = items.sumOf { it.card.price * it.quantity },
                statusId = 1,
                status = OrderStatus.ReadyForPickup,
                paymentMethod = "test",
                total = items.sumOf { it.card.price * it.quantity },
            )
        }
    }
}
