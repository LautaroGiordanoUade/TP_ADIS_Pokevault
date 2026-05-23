package com.pokevault.mobile.ui.feature.search.screen

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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokevault.mobile.ui.feature.components.PokemonCardItem
import com.pokevault.mobile.ui.feature.search.state.SearchEvent
import com.pokevault.mobile.ui.feature.search.state.SearchUiState
import com.pokevault.mobile.ui.feature.search.viewmodel.SearchViewModel
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

@Composable
fun SearchScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun SearchContent(
    state: SearchUiState,
    contentPadding: PaddingValues,
    onEvent: (SearchEvent) -> Unit,
) {
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
                placeholder = { Text("Buscar por nombre, edicion...") },
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
                Text("Filtros", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }
        if (state.filtersVisible) {
            Spacer(Modifier.height(14.dp))
            FilterPanel(state)
        }
        Spacer(Modifier.height(14.dp))
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
                    onFavoriteClick = { onEvent(SearchEvent.OnFavoriteClick(it)) },
                    onAddToCart = { onEvent(SearchEvent.OnAddToCart(it)) },
                )
            }
        }
    }
}

@Composable
private fun FilterPanel(state: SearchUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterValue("TIPO", state.selectedType, Modifier.weight(1f))
            FilterValue("RAREZA / VALOR", state.selectedRarity, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterValue("PRECIO MAXIMO", state.selectedPrice, Modifier.weight(1f))
            FilterValue("ORDENAR POR", state.selectedSort, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
        OutlinedButton(onClick = {}, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(value, maxLines = 1, style = MaterialTheme.typography.labelSmall)
        }
    }
}
