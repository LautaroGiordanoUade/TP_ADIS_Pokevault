package com.pokevault.mobile.ui.feature.search.state

import com.pokevault.mobile.domain.model.PokemonCard

data class SearchUiState(
    val query: String = "",
    val cards: List<PokemonCard> = emptyList(),
    val isLoading: Boolean = false,
    val filtersVisible: Boolean = false,
    val selectedType: String = "Todos los tipos",
    val selectedRarity: String = "Todas las rarezas",
    val selectedPrice: String = "Cualquier precio",
    val selectedSort: String = "Mayor precio primero",
)

sealed interface SearchEvent {
    data object OnRefresh : SearchEvent
    data class OnQueryChange(val query: String) : SearchEvent
    data object OnToggleFilters : SearchEvent
    data class OnFavoriteClick(val card: PokemonCard) : SearchEvent
    data class OnAddToCart(val card: PokemonCard) : SearchEvent
    data object LoadNextPage : SearchEvent
}

sealed interface SearchEffect {
    data object NavigateToLogin : SearchEffect
}
