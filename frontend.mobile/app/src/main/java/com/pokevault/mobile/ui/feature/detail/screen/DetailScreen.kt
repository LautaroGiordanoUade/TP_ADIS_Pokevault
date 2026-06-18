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
import androidx.compose.ui.graphics.Color
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
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

// TODO Jetpack Compose & Arquitectura MVVM:
// 1. Cadenas de texto hardcodeadas: Cadenas de texto tales como "ESTÁS VIENDO:", "PRECIO FINAL" y "AGREGAR AL CARRITO"
//    están codificadas directamente. Deben moverse al archivo de recursos strings.xml.
// 2. Colores fijos: Se definen colores directamente en el Composable como Color(0xFFFF2F68) para el corazón de favoritos,
//    y Color.Black.copy(...) para el sombreado o bordes. Esto rompe la consistencia estética del tema oscuro/claro de Material 3.
//    Se deben definir estos colores en ui/theme/Theme.kt o ui/theme/Color.kt y consumirlos mediante MaterialTheme.colorScheme.
// 3. Lógica de Ciclo de Vida: El uso de DisposableEffect para añadir/remover el LifecycleEventObserver y gatillar
//    'viewModel.refresh()' en el evento ON_RESUME es correcto, pero se podría encapsular en un efecto secundario
//    reutilizable o manejar a través del repositorio reactivo si los flujos fuesen completamente calientes y observables.
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
                text = "\"${card.description ?: stringResource(R.string.detail_fallback_description, card.name)}\"",
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
                    stringResource(R.string.detail_viewing),
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
                    stringResource(R.string.detail_price_final),
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
