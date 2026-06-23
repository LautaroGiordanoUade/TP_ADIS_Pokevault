package com.pokevault.mobile.ui.feature.onboarding.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Storefront
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.R
import com.pokevault.mobile.data.local.PreferencesDataSource
import com.pokevault.mobile.ui.feature.onboarding.state.OnboardingEffect
import com.pokevault.mobile.ui.feature.onboarding.state.OnboardingEvent
import com.pokevault.mobile.ui.feature.onboarding.state.OnboardingPageUiModel
import com.pokevault.mobile.ui.feature.onboarding.state.OnboardingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            pages = listOf(
                OnboardingPageUiModel(
                    titleRes = R.string.onboarding_page_1_title,
                    descriptionRes = R.string.onboarding_page_1_description,
                    imageVector = Icons.Outlined.Storefront,
                ),
                OnboardingPageUiModel(
                    titleRes = R.string.onboarding_page_2_title,
                    descriptionRes = R.string.onboarding_page_2_description,
                    imageVector = Icons.Outlined.CollectionsBookmark,
                ),
                OnboardingPageUiModel(
                    titleRes = R.string.onboarding_page_3_title,
                    descriptionRes = R.string.onboarding_page_3_description,
                    imageVector = Icons.Outlined.AutoAwesome,
                ),
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.OnBackClick -> goToPreviousPage()
            OnboardingEvent.OnNextClick -> goToNextStep()
            OnboardingEvent.OnSkipClick -> finishOnboarding()
        }
    }

    private fun goToPreviousPage() {
        _uiState.update { state ->
            state.copy(currentPage = (state.currentPage - 1).coerceAtLeast(0))
        }
    }

    private fun goToNextStep() {
        val state = _uiState.value
        if (state.isLastPage) {
            finishOnboarding()
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                currentPage = (currentState.currentPage + 1).coerceAtMost(currentState.pages.lastIndex)
            )
        }
    }

    private fun finishOnboarding() {
        viewModelScope.launch {
            preferencesDataSource.markOnboardingSeen()
            _effects.send(OnboardingEffect.NavigateToHome)
        }
    }
}
