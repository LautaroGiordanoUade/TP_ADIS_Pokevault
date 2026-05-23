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
    private val form = MutableStateFlow(ProfileUiState())
    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState = combine(form, profileRepository.profile, profileRepository.orders) { formState, profile, orders ->
        formState.copy(profile = profile, orders = orders)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.OnNameChange -> form.update { it.copy(name = event.name) }
            is ProfileEvent.OnEmailChange -> form.update { it.copy(email = event.email) }
            is ProfileEvent.OnPasswordChange -> form.update { it.copy(password = event.password) }
            ProfileEvent.OnLoginClick -> viewModelScope.launch {
                profileRepository.login(uiState.value.name, uiState.value.email)
            }
            ProfileEvent.OnQuickLoginClick -> viewModelScope.launch { profileRepository.quickLogin() }
            is ProfileEvent.OnPickupClick -> viewModelScope.launch { _effects.send(ProfileEffect.NavigateToPickup) }
        }
    }
}
