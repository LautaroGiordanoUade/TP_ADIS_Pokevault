package com.pokevault.mobile.ui.feature.search.screen

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pokevault.mobile.R
import com.pokevault.mobile.ui.feature.components.PokemonCardItem
import com.pokevault.mobile.ui.feature.search.state.SearchEffect
import com.pokevault.mobile.ui.feature.search.state.SearchEvent
import com.pokevault.mobile.ui.feature.search.state.SearchFilterOptions
import com.pokevault.mobile.ui.feature.search.state.SearchUiState
import com.pokevault.mobile.ui.feature.search.viewmodel.SearchViewModel
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

@Composable
fun SearchScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onCardClick: (Int) -> Unit,
    onOpenLogin: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SearchEffect.NavigateToLogin -> onOpenLogin()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(SearchEvent.OnRefresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SearchContent(
        state = state,
        contentPadding = contentPadding,
        onCardClick = onCardClick,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun SearchContent(
    state: SearchUiState,
    contentPadding: PaddingValues,
    onCardClick: (Int) -> Unit,
    onEvent: (SearchEvent) -> Unit,
) {
    val gridState = rememberLazyGridState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= state.cards.size - 4
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !state.isLoading && state.cards.isNotEmpty()) {
            onEvent(SearchEvent.LoadNextPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(SearchEvent.OnQueryChange(it)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = { onEvent(SearchEvent.OnToggleFilters) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (state.filtersVisible) MarketOrange else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.filtersVisible) Color.Black else MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.height(56.dp),
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = null)
                Text(stringResource(R.string.search_filters_button), fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        if (state.filtersVisible) {
            Spacer(Modifier.height(14.dp))
            FilterPanel(state, onEvent)
        }

        Spacer(Modifier.height(14.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = contentPadding.calculateBottomPadding() + 80.dp,
                    top = 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(state.cards, key = { _, card -> card.id }) { index, card ->
                    PokemonCardItem(
                        card = card,
                        onFavoriteClick = { onEvent(SearchEvent.OnFavoriteClick(it)) },
                        onAddToCart = { onEvent(SearchEvent.OnAddToCart(it)) },
                        onCardClick = onCardClick
                    )
                }

                if (state.isLoading && state.cards.isNotEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MarketOrange, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            if (state.isLoading && state.cards.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MarketOrange
                )
            }

            if (!state.isLoading && state.cards.isEmpty()) {
                Text(
                    stringResource(R.string.search_no_results),
                    modifier = Modifier.align(Alignment.Center),
                    color = Muted
                )
            }
        }
    }
}

@Composable
private fun FilterPanel(state: SearchUiState, onEvent: (SearchEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterValue(
                label = stringResource(R.string.search_filter_type),
                value = state.selectedType,
                options = state.availableTypes,
                modifier = Modifier.weight(1f),
                onSelected = { onEvent(SearchEvent.OnTypeSelected(it)) },
            )
            FilterValue(
                label = stringResource(R.string.search_filter_rarity),
                value = state.selectedRarity,
                options = state.availableRarities,
                modifier = Modifier.weight(1f),
                onSelected = { onEvent(SearchEvent.OnRaritySelected(it)) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterValue(
                label = stringResource(R.string.search_filter_price),
                value = state.selectedPrice,
                options = SearchFilterOptions.priceOptions,
                modifier = Modifier.weight(1f),
                onSelected = { onEvent(SearchEvent.OnPriceSelected(it)) },
            )
            FilterValue(
                label = stringResource(R.string.search_filter_sort),
                value = state.selectedSort,
                options = SearchFilterOptions.sortOptions,
                modifier = Modifier.weight(1f),
                onSelected = { onEvent(SearchEvent.OnSortSelected(it)) },
            )
        }
    }
}

@Composable
private fun FilterValue(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(value, maxLines = 1, style = MaterialTheme.typography.labelSmall)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.labelSmall) },
                        onClick = {
                            expanded = false
                            onSelected(option)
                        },
                    )
                }
            }
        }
    }
}
