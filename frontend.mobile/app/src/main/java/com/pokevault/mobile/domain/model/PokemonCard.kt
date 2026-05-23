package com.pokevault.mobile.domain.model

data class PokemonCard(
    val id: String,
    val name: String,
    val imageUrl: String,
    val rarity: String?,
    val price: Double,
    val description: String?,
    val types: List<String>,
    val setName: String?,
    val number: String?,
    val artist: String?,
    val source: String,
    val isFavorite: Boolean = false,
)
