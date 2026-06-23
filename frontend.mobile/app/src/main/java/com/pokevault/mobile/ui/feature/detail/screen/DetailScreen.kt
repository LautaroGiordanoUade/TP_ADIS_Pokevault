package com.pokevault.mobile.ui.feature.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.pokevault.mobile.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pokevault.mobile.domain.model.PokemonCard
import com.pokevault.mobile.ui.feature.components.CardArt
import com.pokevault.mobile.ui.feature.components.money
import com.pokevault.mobile.ui.feature.detail.viewmodel.DetailEffect
import com.pokevault.mobile.ui.feature.detail.viewmodel.DetailViewModel
import com.pokevault.mobile.ui.theme.FavoritePink

@Composable
fun DetailScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenLogin: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DetailEffect.NavigateToLogin -> onOpenLogin()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = contentPadding.calculateTopPadding()),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            state.card?.let { card ->
                DetailContent(
                    card = card,
                    contentPadding = contentPadding,
                    onFavoriteClick = viewModel::onFavoriteClick,
                    onAddToCart = viewModel::onAddToCart
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    card: PokemonCard,
    contentPadding: PaddingValues,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Área de la carta con el corazón flotante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            CardArt(
                card = card,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                contentScale = ContentScale.Fit
            )

            // Corazón estilo mockup
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.card_favorite_desc),
                        tint = if (card.isFavorite) FavoritePink else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))

        // Caja de descripción con borde punteado (como en la imagen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = outlineColor,
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .padding(20.dp)
        ) {
            Text(
                text = "\"${card.description ?: stringResource(R.string.detail_fallback_description, card.name)}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = FontStyle.Italic,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(28.dp))

        // Bloque de información: Nombre y Precio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.detail_viewing),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = card.name.uppercase() + (if (card.description?.contains("delta", true) == true) " Δ" else ""),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = card.price.money(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.detail_price_final),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Botón principal de compra naranja
        Button(
            onClick = onAddToCart,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.size(12.dp))
            Text(
                stringResource(R.string.detail_add_to_cart),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                fontSize = 16.sp
            )
        }

        // Espacio para la barra de navegación inferior
        Spacer(Modifier.height(contentPadding.calculateBottomPadding() + 32.dp))
    }
}
