package com.pokevault.mobile.ui.feature.detail.state

import com.pokevault.mobile.domain.model.PokemonCard

data class DetailUiState(
    val card: PokemonCard? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface DetailEvent {
    data object OnFavoriteClick : DetailEvent
    data object OnAddToCart : DetailEvent
    data object OnBackClick : DetailEvent
}
