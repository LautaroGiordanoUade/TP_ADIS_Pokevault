package com.pokevault.mobile.ui.feature.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.data.repository.CartRepository
import com.pokevault.mobile.ui.feature.cart.state.CartEvent
import com.pokevault.mobile.ui.feature.cart.state.CartUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {
    val uiState = cartRepository.items
        .map { CartUiState(items = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.OnIncrement -> cartRepository.increment(event.cardId)
            is CartEvent.OnDecrement -> cartRepository.decrement(event.cardId)
            is CartEvent.OnRemove -> cartRepository.remove(event.cardId)
            CartEvent.OnConfirmPayment -> cartRepository.clear()
            CartEvent.OnExploreCards -> Unit
        }
    }
}
