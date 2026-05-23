package com.pokevault.mobile.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PokemonDto(
    val id: String,
    val name: String,
    val image: String,
    val rarity: String?,
    val price: Double,
    val description: String?,
    val type: List<String>?,
    @Json(name = "setName") val setName: String?,
    val number: String?,
    val artist: String?,
    val source: String,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "updatedAt") val updatedAt: String?,
)

@JsonClass(generateAdapter = true)
data class PokemonPageDto(
    val items: List<PokemonDto>,
    val total: Int,
    val page: Int,
    @Json(name = "pageSize") val pageSize: Int,
    @Json(name = "totalPages") val totalPages: Int,
)

@JsonClass(generateAdapter = true)
data class AddVaultItemRequestDto(
    @Json(name = "pokemonId") val pokemonId: String,
)

@JsonClass(generateAdapter = true)
data class VaultItemDto(
    val id: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "pokemonId") val pokemonId: String,
    @Json(name = "addedAt") val addedAt: String,
    val card: PokemonDto?,
)
