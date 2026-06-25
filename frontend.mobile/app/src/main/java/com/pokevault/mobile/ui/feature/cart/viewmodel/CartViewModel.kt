package com.pokevault.mobile.ui.feature.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.domain.repository.CartRepository
import com.pokevault.mobile.domain.repository.OrderRepository
import com.pokevault.mobile.ui.feature.cart.state.CartEffect
import com.pokevault.mobile.ui.feature.cart.state.CartEvent
import com.pokevault.mobile.ui.feature.cart.state.CartUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val _effects = Channel<CartEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState = combine(cartRepository.items, submitState) { items, state ->
        CartUiState(
            items = items,
            deliveryAddress = state.deliveryAddress,
            isSubmitting = state.isSubmitting,
            errorMessage = state.errorMessage,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.OnIncrement -> cartRepository.increment(event.cardId)
            is CartEvent.OnDecrement -> cartRepository.decrement(event.cardId)
            is CartEvent.OnRemove -> cartRepository.remove(event.cardId)
            CartEvent.OnConfirmPayment -> confirmPayment()
            CartEvent.OnExploreCards -> Unit
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
                _effects.send(CartEffect.OrderPlaced)
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
}
