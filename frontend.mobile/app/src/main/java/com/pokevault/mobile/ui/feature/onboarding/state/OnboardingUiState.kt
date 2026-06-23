package com.pokevault.mobile.ui.feature.onboarding.state

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPageUiModel(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val imageVector: ImageVector,
)

data class OnboardingUiState(
    val pages: List<OnboardingPageUiModel> = emptyList(),
    val currentPage: Int = 0,
) {
    val isFirstPage: Boolean = currentPage == 0
    val isLastPage: Boolean = pages.isNotEmpty() && currentPage == pages.lastIndex
    val currentPageModel: OnboardingPageUiModel? = pages.getOrNull(currentPage)
}

data class OnboardingGateUiState(
    val isLoading: Boolean = true,
    val destinationRoute: String? = null,
)

sealed interface OnboardingEvent {
    data object OnNextClick : OnboardingEvent
    data object OnBackClick : OnboardingEvent
    data object OnSkipClick : OnboardingEvent
}

sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
}
