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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HomeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        onOpenSearch = onOpenSearch,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onOpenSearch: () -> Unit,
    onEvent: (HomeEvent) -> Unit,
) {
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
            Text("CATALOGO OFICIAL", color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(8.dp))
        Text("Adquiere cartas de coleccion en perfectas condiciones y con envio asegurado.", color = Muted)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = { onEvent(HomeEvent.OnQueryChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            placeholder = { Text("Buscar Pikachu, Charizard...") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All Cards", "VMAX / Secret", "Vintage 1999", "Promo").forEach { label ->
                AssistChip(onClick = onOpenSearch, label = { Text(label) })
            }
        }
        Spacer(Modifier.height(16.dp))
        Row {
            Text("COLECCION DE FAVORITOS (${state.favorites.size})", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.weight(1f))
            Text("Tus joyas de coleccion", color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(10.dp))
        if (state.isLoading && state.cards.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = MarketOrange)
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
                    )
                }
            }
        }
        SnackbarHost(snackbarHostState)
    }
}
