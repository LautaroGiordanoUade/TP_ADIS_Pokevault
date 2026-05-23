package com.pokevault.mobile.ui.feature.home.state

import com.pokevault.mobile.domain.model.PokemonCard

data class HomeUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val cards: List<PokemonCard> = emptyList(),
    val favorites: List<PokemonCard> = emptyList(),
    val errorMessage: String? = null,
    val title: String = "GENGAR DESTACADOS",
    val subtitle: String = "Inicia sesion para guardar favoritos",
    val isLoggedIn: Boolean = false,
)

sealed interface HomeEvent {
    data object OnRefresh : HomeEvent
    data class OnQueryChange(val query: String) : HomeEvent
    data class OnFavoriteClick(val card: PokemonCard) : HomeEvent
    data class OnAddToCart(val card: PokemonCard) : HomeEvent
    data class OnCardClick(val cardId: Int) : HomeEvent
}

sealed interface HomeEffect {
    data class ShowSnackbar(val message: String) : HomeEffect
}
