package com.pokevault.mobile.ui.feature.search.state

import com.pokevault.mobile.domain.model.PokemonCard

data class SearchUiState(
    val query: String = "",
    val cards: List<PokemonCard> = emptyList(),
    val isLoading: Boolean = false,
    val filtersVisible: Boolean = false,
    val selectedType: String = SearchFilterOptions.ALL_TYPES,
    val selectedRarity: String = SearchFilterOptions.ALL_RARITIES,
    val selectedPrice: String = SearchFilterOptions.ANY_PRICE,
    val selectedSort: String = SearchFilterOptions.NO_SORT,
    val availableTypes: List<String> = listOf(SearchFilterOptions.ALL_TYPES),
    val availableRarities: List<String> = listOf(SearchFilterOptions.ALL_RARITIES),
)

sealed interface SearchEvent {
    data object OnRefresh : SearchEvent
    data class OnQueryChange(val query: String) : SearchEvent
    data object OnToggleFilters : SearchEvent
    data class OnTypeSelected(val value: String) : SearchEvent
    data class OnRaritySelected(val value: String) : SearchEvent
    data class OnPriceSelected(val value: String) : SearchEvent
    data class OnSortSelected(val value: String) : SearchEvent
    data class OnFavoriteClick(val card: PokemonCard) : SearchEvent
    data class OnAddToCart(val card: PokemonCard) : SearchEvent
    data object LoadNextPage : SearchEvent
}

sealed interface SearchEffect {
    data object NavigateToLogin : SearchEffect
}
