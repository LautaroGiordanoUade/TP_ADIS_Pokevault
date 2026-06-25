package com.pokevault.mobile.ui.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.domain.model.PokemonCard
import com.pokevault.mobile.domain.repository.CartRepository
import com.pokevault.mobile.domain.repository.PokemonRepository
import com.pokevault.mobile.domain.repository.ProfileRepository
import com.pokevault.mobile.ui.feature.home.state.HomeEffect
import com.pokevault.mobile.ui.feature.home.state.HomeEvent
import com.pokevault.mobile.ui.feature.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val cartRepository: CartRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val content = MutableStateFlow(HomeUiState())
    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)

    val effects = _effects.receiveAsFlow()

    val uiState = combine(content, profileRepository.isLoggedIn) { state, isLoggedIn ->
        val sourceCards = if (isLoggedIn) state.favorites else state.cards
        val filteredCards = sourceCards.filterByQuery(state.query)
        state.copy(
            cards = filteredCards,
            title = "", // Se computa dinÃ¡micamente en la vista (HomeScreen) para localizaciÃ³n
            subtitle = "", // Se computa dinÃ¡micamente en la vista (HomeScreen) para localizaciÃ³n
            isLoggedIn = isLoggedIn,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        refresh()
        viewModelScope.launch {
            profileRepository.isLoggedIn
                .distinctUntilChanged()
                .collect { isLoggedIn ->
                    if (!isLoggedIn) {
                        content.value = HomeUiState(isLoading = true)
                    }
                    refresh()
                }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnRefresh -> refresh()
            is HomeEvent.OnQueryChange -> content.update { it.copy(query = event.query) }
            is HomeEvent.OnFavoriteClick -> viewModelScope.launch {
                if (!profileRepository.isLoggedIn.first()) {
                    _effects.send(HomeEffect.NavigateToLogin)
                    return@launch
                }
                runCatching { pokemonRepository.toggleFavorite(event.card) }
                refresh()
            }
            is HomeEvent.OnAddToCart -> {
                cartRepository.add(event.card)
                viewModelScope.launch { _effects.send(HomeEffect.ShowSnackbar("Carta agregada al carrito")) }
            }
            is HomeEvent.OnCardClick -> Unit
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            content.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { pokemonRepository.listHomeCards() }
                .onSuccess { cards ->
                    content.update {
                        it.copy(
                            isLoading = false,
                            cards = cards,
                            favorites = cards.filter { card -> card.isFavorite },
                        )
                    }
                }
                .onFailure { error ->
                    content.update {
                        it.copy(
                            isLoading = false,
                            cards = emptyList(),
                            favorites = emptyList(),
                            errorMessage = error.message ?: "No se pudo cargar el inicio",
                        )
                    }
                    _effects.send(HomeEffect.ShowSnackbar("No fue posible completar la operacion. Intenta nuevamente."))
                }
        }
    }
}

private fun List<PokemonCard>.filterByQuery(
    query: String,
): List<PokemonCard> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return this
    return filter { card ->
        card.name.contains(normalizedQuery, ignoreCase = true) ||
            card.rarity.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            card.setName.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            card.artist.orEmpty().contains(normalizedQuery, ignoreCase = true)
    }
}
