package com.pokevault.mobile.ui.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.data.repository.ProfileRepository
import com.pokevault.mobile.ui.feature.profile.state.ProfileEffect
import com.pokevault.mobile.ui.feature.profile.state.ProfileEvent
import com.pokevault.mobile.ui.feature.profile.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val screenState = MutableStateFlow(ProfileUiState())
    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState = combine(screenState, profileRepository.profile) { state, profile ->
        state.copy(profile = profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init {
        refresh()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnGoogleLoginClick -> viewModelScope.launch {
                _effects.send(ProfileEffect.RequestGoogleSignIn)
            }
            is ProfileEvent.OnGoogleIdTokenReceived -> login(event.idToken)
            is ProfileEvent.OnLoginFailed -> screenState.update {
                it.copy(isLoading = false, errorMessage = event.message)
            }
            ProfileEvent.OnLogoutClick -> viewModelScope.launch {
                profileRepository.logout()
                screenState.value = ProfileUiState()
            }
            is ProfileEvent.OnPickupClick -> viewModelScope.launch {
                _effects.send(ProfileEffect.NavigateToPickup)
            }
        }
    }

    private fun login(idToken: String) {
        viewModelScope.launch {
            screenState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                profileRepository.loginWithGoogle(idToken)
                profileRepository.getOrders()
            }.onSuccess { orders ->
                screenState.update { it.copy(isLoading = false, orders = orders) }
            }.onFailure { error ->
                screenState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo iniciar sesion",
                    )
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            runCatching {
                profileRepository.refreshProfile()
                profileRepository.getOrders()
            }.onSuccess { orders ->
                screenState.update { it.copy(orders = orders) }
            }
        }
    }
}
