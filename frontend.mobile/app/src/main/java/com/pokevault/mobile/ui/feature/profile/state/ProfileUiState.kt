package com.pokevault.mobile.ui.feature.profile.state

import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.UserProfile

data class ProfileUiState(
    val profile: UserProfile? = null,
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ProfileEvent {
    data object OnGoogleLoginClick : ProfileEvent
    data class OnGoogleIdTokenReceived(val idToken: String) : ProfileEvent
    data class OnLoginFailed(val message: String) : ProfileEvent
    data object OnLogoutClick : ProfileEvent
    data class OnPickupClick(val orderId: Int) : ProfileEvent
}

sealed interface ProfileEffect {
    data object RequestGoogleSignIn : ProfileEffect
    data object NavigateToPickup : ProfileEffect
}
