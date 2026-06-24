package com.pokevault.mobile.ui.feature.onboarding.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingUiStateTest {
    @Test
    fun `empty pages has no current page and is not last page`() {
        val state = OnboardingUiState()

        assertNull(state.currentPageModel)
        assertTrue(state.isFirstPage)
        assertFalse(state.isLastPage)
    }

    @Test
    fun `current page exposes first and last derived flags`() {
        val pages = listOf(page(1), page(2), page(3))

        val first = OnboardingUiState(pages = pages, currentPage = 0)
        val middle = OnboardingUiState(pages = pages, currentPage = 1)
        val last = OnboardingUiState(pages = pages, currentPage = 2)

        assertTrue(first.isFirstPage)
        assertFalse(first.isLastPage)
        assertFalse(middle.isFirstPage)
        assertFalse(middle.isLastPage)
        assertFalse(last.isFirstPage)
        assertTrue(last.isLastPage)
        assertEquals(pages[2], last.currentPageModel)
    }

    private fun page(seed: Int) = OnboardingPageUiModel(
        titleRes = seed,
        descriptionRes = seed + 100,
        imageVector = Icons.Outlined.Storefront,
    )
}
