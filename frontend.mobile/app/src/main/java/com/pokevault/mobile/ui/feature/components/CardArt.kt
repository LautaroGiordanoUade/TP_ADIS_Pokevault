package com.pokevault.mobile.ui.feature.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pokevault.mobile.domain.model.PokemonCard

@Composable
fun CardArt(
    card: PokemonCard,
    modifier: Modifier = Modifier,
) {
    val colors = gradientFor(card.name)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(colors)),
    ) {
        if (card.imageUrl.isNotBlank()) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = if (card.rarity?.contains("PSA", ignoreCase = true) == true) card.rarity else "PSA 10",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black, RoundedCornerShape(4.dp))
                .size(width = 42.dp, height = 18.dp),
        )
        Text(
            text = "+",
            color = Color.White.copy(alpha = 0.32f),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private fun gradientFor(name: String): List<Color> = when {
    name.contains("Charizard", true) -> listOf(Color(0xFFFF2D00), Color(0xFFFF8A00), Color(0xFFE70012))
    name.contains("Blastoise", true) -> listOf(Color(0xFF4E4BFF), Color(0xFF3A3DFF), Color(0xFF0086B8))
    name.contains("Venusaur", true) -> listOf(Color(0xFF02B653), Color(0xFF00883F), Color(0xFF04C46A))
    name.contains("Gengar", true) -> listOf(Color(0xFF3D0874), Color(0xFF651B7D), Color(0xFFC0006F))
    name.contains("Pikachu", true) -> listOf(Color(0xFFFFC400), Color(0xFFFF9800), Color(0xFFE57800))
    name.contains("Umbreon", true) -> listOf(Color(0xFF2D014E), Color(0xFF3F116B), Color(0xFF24246E))
    name.contains("Rayquaza", true) -> listOf(Color(0xFF008060), Color(0xFF006E67), Color(0xFF61472A))
    else -> listOf(Color(0xFF444BFF), Color(0xFF8B20A8), Color(0xFFFF9800))
}
