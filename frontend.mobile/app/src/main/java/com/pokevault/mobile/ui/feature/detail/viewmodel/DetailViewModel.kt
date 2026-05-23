package com.pokevault.mobile.ui.feature.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.data.repository.CartRepository
import com.pokevault.mobile.data.repository.PokemonRepository
import com.pokevault.mobile.ui.feature.detail.state.DetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cardId: Int = checkNotNull(savedStateHandle["cardId"])
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCard()
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
            pokemonRepository.toggleFavorite(currentCard)
            _uiState.update { state ->
                state.copy(card = state.card?.copy(isFavorite = !currentCard.isFavorite))
            }
        }
    }

    fun onAddToCart() {
        val currentCard = _uiState.value.card ?: return
        cartRepository.add(currentCard)
    }
}
