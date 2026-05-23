package com.pokevault.mobile.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PokemonDto(
    val id: Int,
    @Json(name = "externalId") val externalId: String,
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
    @Json(name = "pokemonId") val pokemonId: Int,
)

@JsonClass(generateAdapter = true)
data class VaultItemDto(
    val id: Int,
    @Json(name = "userId") val userId: Int,
    @Json(name = "pokemonId") val pokemonId: Int,
    @Json(name = "addedAt") val addedAt: String,
    val card: PokemonDto?,
)

@JsonClass(generateAdapter = true)
data class GoogleLoginRequestDto(
    @Json(name = "idToken") val idToken: String,
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int,
    val email: String,
    val name: String,
    @Json(name = "avatarUrl") val avatarUrl: String?,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "updatedAt") val updatedAt: String?,
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    val token: String,
    val user: UserDto,
)

@JsonClass(generateAdapter = true)
data class CreateOrderItemRequestDto(
    @Json(name = "pokemonId") val pokemonId: Int,
    val quantity: Int,
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequestDto(
    val items: List<CreateOrderItemRequestDto>,
    @Json(name = "deliveryAddress") val deliveryAddress: String,
    @Json(name = "paymentMethod") val paymentMethod: String,
)

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    val id: Int,
    @Json(name = "pokemonId") val pokemonId: Int,
    val quantity: Int,
    @Json(name = "unitPrice") val unitPrice: Double,
    val card: PokemonDto?,
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: Int,
    @Json(name = "userId") val userId: Int,
    @Json(name = "deliveryAddress") val deliveryAddress: String,
    @Json(name = "paymentMethod") val paymentMethod: String,
    val status: String,
    val subtotal: Double,
    val shipping: Double,
    val total: Double,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "updatedAt") val updatedAt: String?,
    val items: List<OrderItemDto>,
)
