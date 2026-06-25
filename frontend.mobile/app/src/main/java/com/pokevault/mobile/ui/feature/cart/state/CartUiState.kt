package com.pokevault.mobile.ui.feature.cart.state

import com.pokevault.mobile.domain.model.CartItem

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val deliveryAddress: String = "Av. Corrientes 1250, CABA, Argentina",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val subtotal: Double = items.sumOf { it.card.price * it.quantity }
    val finalTotal: Double = subtotal
    val totalQuantity: Int = items.sumOf { it.quantity }
}

sealed interface CartEvent {
    data class OnIncrement(val cardId: Int) : CartEvent
    data class OnDecrement(val cardId: Int) : CartEvent
    data class OnRemove(val cardId: Int) : CartEvent
    data object OnConfirmPayment : CartEvent
    data object OnExploreCards : CartEvent
}

sealed interface CartEffect {
    /** Emitido una sola vez cuando la compra se confirma exitosamente. */
    data object OrderPlaced : CartEffect
}
