package com.pokevault.mobile.ui.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.data.local.PreferencesDataSource
import com.pokevault.mobile.ui.feature.onboarding.state.OnboardingGateUiState
import com.pokevault.mobile.ui.navigation.PokeMarketDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingGateViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingGateUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val onboardingSeen = preferencesDataSource.onboardingSeen.first()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    destinationRoute = if (onboardingSeen) {
                        PokeMarketDestination.Home.route
                    } else {
                        PokeMarketDestination.Onboarding.route
                    },
                )
            }
        }
    }
}
