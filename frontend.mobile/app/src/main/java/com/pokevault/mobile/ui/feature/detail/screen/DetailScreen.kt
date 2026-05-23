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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokevault.mobile.domain.model.PokemonCard
import com.pokevault.mobile.ui.feature.components.CardArt
import com.pokevault.mobile.ui.feature.components.money
import com.pokevault.mobile.ui.feature.detail.viewmodel.DetailViewModel
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

@Composable
fun DetailScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = contentPadding.calculateTopPadding()),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = MarketOrange)
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
                .background(Color.White)
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
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (card.isFavorite) Color(0xFFFF2F68) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))
        Spacer(Modifier.height(20.dp))

        // Caja de descripción con borde punteado (como en la imagen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.15f),
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
                text = "\"${card.description ?: "Rare delta species ${card.name} of dual element. Delta Charge enables quick lightning..."}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.85f),
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
                    "ESTÁS VIENDO:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
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
                    color = Color.Black
                )
                Text(
                    "PRECIO FINAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
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
                containerColor = MarketOrange,
                contentColor = Color.Black
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
                "AGREGAR AL CARRITO",
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                fontSize = 16.sp
            )
        }

        // Espacio para la barra de navegación inferior
        Spacer(Modifier.height(contentPadding.calculateBottomPadding() + 32.dp))
    }
}
