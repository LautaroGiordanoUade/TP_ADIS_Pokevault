package com.pokevault.mobile.ui.feature.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pokevault.mobile.R
import com.pokevault.mobile.ui.feature.components.LoadErrorState
import com.pokevault.mobile.ui.feature.components.PokemonCardItem
import com.pokevault.mobile.ui.feature.home.state.HomeEffect
import com.pokevault.mobile.ui.feature.home.state.HomeEvent
import com.pokevault.mobile.ui.feature.home.state.HomeUiState
import com.pokevault.mobile.ui.feature.home.viewmodel.HomeViewModel
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

@Composable
fun HomeScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenSearch: () -> Unit,
    onOpenLogin: () -> Unit,
    onCardClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                HomeEffect.NavigateToLogin -> onOpenLogin()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(HomeEvent.OnRefresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        onCardClick = onCardClick,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onCardClick: (Int) -> Unit,
    onEvent: (HomeEvent) -> Unit,
) {
    val title = if (state.isLoggedIn) {
        stringResource(R.string.home_favorites_title, state.cards.size)
    } else {
        stringResource(R.string.home_gengar_title)
    }
    val subtitle = if (state.isLoggedIn) {
        stringResource(R.string.home_favorites_subtitle)
    } else {
        stringResource(R.string.home_gengar_subtitle)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = contentPadding.calculateTopPadding())
        .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildAnnotatedString {
                    append("Poke")
                    withStyle(SpanStyle(color = MarketOrange)) { append("Market") }
                },
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.home_catalog_title), color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.home_catalog_subtitle), color = Muted)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = { onEvent(HomeEvent.OnQueryChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
        )
        Spacer(Modifier.height(16.dp))
        Row {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.weight(1f))
            Text(subtitle, color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(10.dp))
        if (state.isLoading && state.cards.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = MarketOrange)
        } else if (state.errorMessage != null && state.cards.isEmpty()) {
            LoadErrorState(
                title = stringResource(R.string.home_error_loading_title),
                message = stringResource(R.string.home_error_loading_subtitle),
                modifier = Modifier.weight(1f),
            )
        } else if (state.isLoggedIn && state.favorites.isEmpty()) {
            EmptyFavorites(modifier = Modifier.weight(1f))
        } else if (state.cards.isEmpty()) {
            EmptySearchResults(modifier = Modifier.weight(1f))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    top = 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.cards, key = { it.id }) { card ->
                    PokemonCardItem(
                        card = card,
                        onFavoriteClick = { onEvent(HomeEvent.OnFavoriteClick(it)) },
                        onAddToCart = { onEvent(HomeEvent.OnAddToCart(it)) },
                        onCardClick = onCardClick
                    )
                }
            }
        }
        SnackbarHost(snackbarHostState)
    }
}

@Composable
private fun EmptySearchResults(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = Muted)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.home_no_results_title), style = MaterialTheme.typography.labelSmall)
        Text(stringResource(R.string.home_no_results_subtitle), color = Muted)
    }
}

@Composable
private fun EmptyFavorites(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Muted)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.home_no_favorites_title), style = MaterialTheme.typography.labelSmall)
        Text(stringResource(R.string.home_no_favorites_subtitle), color = Muted)
    }
}
