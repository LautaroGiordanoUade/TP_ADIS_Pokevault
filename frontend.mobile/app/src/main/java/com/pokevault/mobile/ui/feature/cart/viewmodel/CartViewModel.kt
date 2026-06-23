package com.pokevault.mobile.ui.feature.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.domain.repository.CartRepository
import com.pokevault.mobile.domain.repository.OrderRepository
import com.pokevault.mobile.ui.feature.cart.state.CartEvent
import com.pokevault.mobile.ui.feature.cart.state.CartUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {
    private val submitState = MutableStateFlow(CartUiState())
    private val cartItemsState: Flow<CartItemsState> = cartRepository.items
        .map { items -> CartItemsState(items = items, isLoading = false) }
        .onStart { emit(CartItemsState(isLoading = true)) }

    val uiState = combine(cartItemsState, submitState) { collectionState, state ->
        CartUiState(
            isLoading = collectionState.isLoading,
            items = collectionState.items,
            deliveryAddress = state.deliveryAddress,
            isSubmitting = state.isSubmitting,
            errorMessage = state.errorMessage,
            editingItem = state.editingItem,
            editingQuantityInput = state.editingQuantityInput,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.OnIncrement -> viewModelScope.launch { cartRepository.increment(event.cardId) }
            is CartEvent.OnDecrement -> viewModelScope.launch { cartRepository.decrement(event.cardId) }
            is CartEvent.OnRemove -> viewModelScope.launch { cartRepository.remove(event.cardId) }
            is CartEvent.OnEditQuantity -> openQuantityEditor(event.item)
            is CartEvent.OnQuantityInputChanged -> submitState.update {
                it.copy(editingQuantityInput = event.value.filter(Char::isDigit), errorMessage = null)
            }
            CartEvent.OnSaveEditedQuantity -> saveEditedQuantity()
            CartEvent.OnDismissQuantityEditor -> closeQuantityEditor()
            CartEvent.OnConfirmPayment -> confirmPayment()
            CartEvent.OnExploreCards -> Unit
        }
    }

    private fun openQuantityEditor(item: CartItem) {
        submitState.update {
            it.copy(
                editingItem = item,
                editingQuantityInput = item.quantity.toString(),
                errorMessage = null,
            )
        }
    }

    private fun closeQuantityEditor() {
        submitState.update {
            it.copy(
                editingItem = null,
                editingQuantityInput = "",
                errorMessage = null,
            )
        }
    }

    private fun saveEditedQuantity() {
        val state = uiState.value
        val editingItem = state.editingItem ?: return
        val parsedQuantity = state.editingQuantityInput.toIntOrNull()

        if (parsedQuantity == null || parsedQuantity <= 0) {
            submitState.update { it.copy(errorMessage = "Ingresá una cantidad válida mayor a cero") }
            return
        }

        viewModelScope.launch {
            cartRepository.updateQuantity(editingItem.card.id, parsedQuantity)
            closeQuantityEditor()
        }
    }

    private fun confirmPayment() {
        val state = uiState.value
        if (state.items.isEmpty() || state.isSubmitting) return
        viewModelScope.launch {
            submitState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                orderRepository.createOrder(state.items, state.deliveryAddress)
            }.onSuccess {
                cartRepository.clear()
                submitState.update { it.copy(isSubmitting = false) }
            }.onFailure { error ->
                submitState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "No se pudo confirmar la compra",
                    )
                }
            }
        }
    }

    private data class CartItemsState(
        val items: List<CartItem> = emptyList(),
        val isLoading: Boolean = false,
    )
}
