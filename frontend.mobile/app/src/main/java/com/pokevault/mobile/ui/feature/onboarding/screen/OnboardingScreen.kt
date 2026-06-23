package com.pokevault.mobile.ui.feature.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokevault.mobile.R
import com.pokevault.mobile.ui.feature.onboarding.state.OnboardingEffect
import com.pokevault.mobile.ui.feature.onboarding.state.OnboardingEvent
import com.pokevault.mobile.ui.feature.onboarding.viewmodel.OnboardingViewModel
import com.pokevault.mobile.ui.theme.Ink
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

@Composable
fun OnboardingScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is OnboardingEffect.NavigateToHome) {
                onFinished()
            }
        }
    }

    val page = state.currentPageModel ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color.White)
                )
            )
            .padding(contentPadding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = { viewModel.onEvent(OnboardingEvent.OnSkipClick) },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }

            Spacer(Modifier.height(28.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(color = Color(0xFFFFE0B2), shape = CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = page.imageVector,
                            contentDescription = stringResource(page.titleRes),
                            tint = MarketOrange,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(page.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(page.descriptionRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(state.pages.size) { index ->
                    val isSelected = index == state.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (isSelected) 28.dp else 10.dp, height = 10.dp)
                            .background(
                                color = if (isSelected) MarketOrange else Color(0xFFFFE0B2),
                                shape = RoundedCornerShape(999.dp),
                            )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.onEvent(OnboardingEvent.OnBackClick) },
                    enabled = !state.isFirstPage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.onboarding_back))
                }
                Button(
                    onClick = { viewModel.onEvent(OnboardingEvent.OnNextClick) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MarketOrange,
                        contentColor = Color.Black,
                    ),
                ) {
                    Text(
                        text = if (state.isLastPage) {
                            stringResource(R.string.onboarding_finish)
                        } else {
                            stringResource(R.string.onboarding_next)
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
