package com.pokevault.mobile.ui.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.data.local.LanguageManager
import com.pokevault.mobile.data.local.ThemeManager
import com.pokevault.mobile.domain.repository.CartRepository
import com.pokevault.mobile.domain.repository.ProfileRepository
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
    private val cartRepository: CartRepository,
    private val languageManager: LanguageManager,
    private val themeManager: ThemeManager,
) : ViewModel() {
    private val screenState = MutableStateFlow(ProfileUiState())
    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState = combine(
        screenState,
        profileRepository.profile,
        themeManager.isDarkMode
    ) { state, profile, isDarkMode ->
        state.copy(
            profile = profile,
            currentLanguage = languageManager.getCurrentLanguage(),
            isDarkMode = isDarkMode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init {
        refresh()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnRefresh -> refresh()
            ProfileEvent.OnGoogleLoginClick -> viewModelScope.launch {
                screenState.update { it.copy(isLoading = true, errorMessage = null) }
                _effects.send(ProfileEffect.RequestGoogleSignIn)
            }
            is ProfileEvent.OnGoogleIdTokenReceived -> login(event.idToken)
            is ProfileEvent.OnLoginFailed -> screenState.update {
                it.copy(isLoading = false, errorMessage = event.message)
            }
            ProfileEvent.OnLoginCanceled -> screenState.update {
                it.copy(isLoading = false, errorMessage = null)
            }
            ProfileEvent.OnLogoutClick -> viewModelScope.launch {
                profileRepository.logout()
                cartRepository.clear()
                screenState.value = ProfileUiState()
                _effects.send(ProfileEffect.ClearGoogleCredentialState)
            }
            is ProfileEvent.OnPickupClick -> viewModelScope.launch {
                _effects.send(ProfileEffect.NavigateToPickup)
            }
            is ProfileEvent.OnLanguageChanged -> {
                languageManager.setLanguage(event.languageCode)
                screenState.update { it.copy(currentLanguage = event.languageCode) }
            }
            is ProfileEvent.OnDarkModeChanged -> viewModelScope.launch {
                themeManager.setDarkMode(event.enabled)
            }
        }
    }

    private fun login(idToken: String) {
        viewModelScope.launch {
            screenState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                profileRepository.loginWithGoogle(idToken)
            }.onFailure { error ->
                screenState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo iniciar sesion",
                    )
                }
                return@launch
            }
            // Login OK — ahora cargamos pedidos (Room como fallback si no hay red)
            runCatching {
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
            screenState.update { it.copy(errorMessage = null) }
            // Intento silencioso de refrescar perfil desde el servidor (falla OK si no hay red)
            runCatching { profileRepository.refreshProfile() }
            // Siempre cargar pedidos: Room devuelve el caché si no hay conexión
            runCatching {
                profileRepository.getOrders()
            }.onSuccess { orders ->
                screenState.update { it.copy(orders = orders) }
            }.onFailure { error ->
                screenState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo actualizar el historial")
                }
            }
        }
    }
}
