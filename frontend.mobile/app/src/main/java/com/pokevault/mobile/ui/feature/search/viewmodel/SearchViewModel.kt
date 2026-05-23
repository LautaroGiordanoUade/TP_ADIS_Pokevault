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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentPage = 1
    private var isLastPage = false

    init {
        viewModelScope.launch {
            search(query = "", page = 1)
        }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChange -> {
                _uiState.update { it.copy(query = event.query) }
                resetAndSearch(event.query)
            }
            SearchEvent.OnToggleFilters -> _uiState.update { it.copy(filtersVisible = !it.filtersVisible) }
            is SearchEvent.OnFavoriteClick -> viewModelScope.launch {
                pokemonRepository.toggleFavorite(event.card)
                _uiState.update { state ->
                    state.copy(cards = state.cards.map {
                        if (it.id == event.card.id) it.copy(isFavorite = !it.isFavorite) else it
                    })
                }
            }
            is SearchEvent.OnAddToCart -> cartRepository.add(event.card)
            SearchEvent.LoadNextPage -> loadNextPage()
        }
    }

    private fun resetAndSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            currentPage = 1
            isLastPage = false
            search(query, currentPage)
        }
    }

    private suspend fun search(query: String, page: Int) {
        if (isLastPage && page > 1) return

        _uiState.update { it.copy(isLoading = true) }
        try {
            val results = pokemonRepository.listCards(query = query, page = page, pageSize = 15)
            _uiState.update {
                it.copy(
                    cards = if (page == 1) results else it.cards + results,
                    isLoading = false
                )
            }
            isLastPage = results.isEmpty()
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadNextPage() {
        if (!isLastPage && !_uiState.value.isLoading) {
            viewModelScope.launch {
                currentPage++
                search(_uiState.value.query, currentPage)
            }
        }
    }
}
