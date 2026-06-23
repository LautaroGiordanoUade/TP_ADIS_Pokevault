package com.pokevault.mobile.ui.feature.cart.state

import com.pokevault.mobile.domain.model.CartItem

data class CartUiState(
    val isLoading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val deliveryAddress: String = "Av. Corrientes 1250, CABA, Argentina",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val editingItem: CartItem? = null,
    val editingQuantityInput: String = "",
) {
    val subtotal: Double = items.sumOf { it.card.price * it.quantity }
    val finalTotal: Double = subtotal
    val totalQuantity: Int = items.sumOf { it.quantity }
}

sealed interface CartEvent {
    data class OnIncrement(val cardId: Int) : CartEvent
    data class OnDecrement(val cardId: Int) : CartEvent
    data class OnRemove(val cardId: Int) : CartEvent
    data class OnEditQuantity(val item: CartItem) : CartEvent
    data class OnQuantityInputChanged(val value: String) : CartEvent
    data object OnSaveEditedQuantity : CartEvent
    data object OnDismissQuantityEditor : CartEvent
    data object OnConfirmPayment : CartEvent
    data object OnExploreCards : CartEvent
}
