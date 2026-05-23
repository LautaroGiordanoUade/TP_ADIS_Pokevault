package com.pokevault.mobile.ui.feature.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.data.repository.CartRepository
import com.pokevault.mobile.data.repository.PokemonRepository
import com.pokevault.mobile.ui.feature.search.state.SearchEvent
import com.pokevault.mobile.ui.feature.search.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val filtersVisible = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    val uiState = combine(
        query,
        filtersVisible,
        query.debounce(250).distinctUntilChanged().flatMapLatest { search -> pokemonRepository.observeCards(search) },
    ) { queryValue, visible, cards ->
            SearchUiState(
                query = queryValue,
                cards = cards.sortedByDescending { it.price },
                filtersVisible = visible,
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    init {
        viewModelScope.launch { pokemonRepository.refreshCards() }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChange -> query.value = event.query
            SearchEvent.OnToggleFilters -> filtersVisible.update { !it }
            is SearchEvent.OnFavoriteClick -> viewModelScope.launch { pokemonRepository.toggleFavorite(event.card) }
            is SearchEvent.OnAddToCart -> cartRepository.add(event.card)
        }
    }
}
