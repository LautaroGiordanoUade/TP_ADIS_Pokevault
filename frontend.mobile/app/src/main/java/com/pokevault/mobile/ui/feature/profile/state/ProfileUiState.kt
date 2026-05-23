package com.pokevault.mobile.ui.feature.profile.state

import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.UserProfile

data class ProfileUiState(
    val name: String = "Ash Ketchum",
    val email: String = "ash.ketchum@pallet.org",
    val password: String = "secret",
    val profile: UserProfile? = null,
    val orders: List<Order> = emptyList(),
)

sealed interface ProfileEvent {
    data class OnNameChange(val name: String) : ProfileEvent
    data class OnEmailChange(val email: String) : ProfileEvent
    data class OnPasswordChange(val password: String) : ProfileEvent
    data object OnLoginClick : ProfileEvent
    data object OnQuickLoginClick : ProfileEvent
    data class OnPickupClick(val orderId: String) : ProfileEvent
}

sealed interface ProfileEffect {
    data object NavigateToPickup : ProfileEffect
}
