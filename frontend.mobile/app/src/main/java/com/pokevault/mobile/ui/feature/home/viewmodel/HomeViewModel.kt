package com.pokevault.mobile.ui.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.data.repository.CartRepository
import com.pokevault.mobile.data.repository.PokemonRepository
import com.pokevault.mobile.ui.feature.home.state.HomeEffect
import com.pokevault.mobile.ui.feature.home.state.HomeEvent
import com.pokevault.mobile.ui.feature.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(true)
    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)

    val effects = _effects.receiveAsFlow()

    val uiState = combine(
        query,
        query.debounce(250).distinctUntilChanged().flatMapLatest { pokemonRepository.observeCards(it) },
        pokemonRepository.observeFavorites(),
        loading,
    ) { queryValue, cards, favorites, isLoading ->
        HomeUiState(
            isLoading = isLoading,
            query = queryValue,
            cards = cards,
            favorites = favorites,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        refresh()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnRefresh -> refresh()
            is HomeEvent.OnQueryChange -> query.value = event.query
            is HomeEvent.OnFavoriteClick -> viewModelScope.launch { pokemonRepository.toggleFavorite(event.card) }
            is HomeEvent.OnAddToCart -> {
                cartRepository.add(event.card)
                viewModelScope.launch { _effects.send(HomeEffect.ShowSnackbar("Carta agregada al carrito")) }
            }
            is HomeEvent.OnCardClick -> Unit
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            loading.value = true
            runCatching { pokemonRepository.refreshCards(query.value) }
                .onFailure { _effects.send(HomeEffect.ShowSnackbar("No se pudo sincronizar con el backend")) }
            loading.value = false
        }
    }
}
