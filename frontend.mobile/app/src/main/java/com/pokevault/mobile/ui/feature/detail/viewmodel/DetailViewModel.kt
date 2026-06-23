package com.pokevault.mobile.ui.feature.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.domain.repository.CartRepository
import com.pokevault.mobile.domain.repository.PokemonRepository
import com.pokevault.mobile.domain.repository.ProfileRepository
import com.pokevault.mobile.ui.feature.detail.state.DetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val cartRepository: CartRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cardId: Int = checkNotNull(savedStateHandle["cardId"])
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<DetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadCard()
        viewModelScope.launch {
            profileRepository.isLoggedIn
                .distinctUntilChanged()
                .collect { isLoggedIn ->
                    if (!isLoggedIn) {
                        _uiState.update { state ->
                            state.copy(card = state.card?.copy(isFavorite = false))
                        }
                    }
                }
        }
    }

    private fun loadCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val card = pokemonRepository.getCard(cardId)
            if (card != null) {
                _uiState.update { it.copy(card = card, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar la carta") }
            }
        }
    }

    fun onFavoriteClick() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            if (!profileRepository.isLoggedIn.first()) {
                _effects.send(DetailEffect.NavigateToLogin)
                return@launch
            }
            pokemonRepository.toggleFavorite(currentCard)
            _uiState.update { state ->
                state.copy(card = state.card?.copy(isFavorite = !currentCard.isFavorite))
            }
        }
    }

    fun onAddToCart() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            cartRepository.add(currentCard)
        }
    }

    fun refresh() {
        loadCard()
    }
}

sealed interface DetailEffect {
    data object NavigateToLogin : DetailEffect
}
