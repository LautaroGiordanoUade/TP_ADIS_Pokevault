package com.pokevault.mobile.data.mapper

import com.pokevault.mobile.data.local.PokemonEntity
import com.pokevault.mobile.data.local.OrderEntity
import com.pokevault.mobile.data.remote.OrderDto
import com.pokevault.mobile.data.remote.PokemonDto
import com.pokevault.mobile.data.remote.UserDto
import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.OrderStatus
import com.pokevault.mobile.domain.model.PokemonCard
import com.pokevault.mobile.domain.model.UserProfile

fun PokemonDto.toEntity(isFavorite: Boolean = false): PokemonEntity =
    PokemonEntity(
        id = id,
        externalId = externalId,
        name = name,
        imageUrl = image,
        rarity = rarity,
        price = price,
        description = description,
        types = type.orEmpty().joinToString(separator = "|"),
        setName = setName,
        number = number,
        artist = artist,
        source = source,
        isFavorite = isFavorite,
    )

fun PokemonEntity.toDomain(): PokemonCard =
    PokemonCard(
        id = id,
        externalId = externalId,
        name = name,
        imageUrl = imageUrl,
        rarity = rarity,
        price = price,
        description = description,
        types = types.split("|").filter { it.isNotBlank() },
        setName = setName,
        number = number,
        artist = artist,
        source = source,
        isFavorite = isFavorite,
    )

fun PokemonDto.toDomain(isFavorite: Boolean = false): PokemonCard =
    PokemonCard(
        id = id,
        externalId = externalId,
        name = name,
        imageUrl = image,
        rarity = rarity,
        price = price,
        description = description,
        types = type.orEmpty(),
        setName = setName,
        number = number,
        artist = artist,
        source = source,
        isFavorite = isFavorite,
    )

fun PokemonCard.toEntity(): PokemonEntity =
    PokemonEntity(
        id = id,
        externalId = externalId,
        name = name,
        imageUrl = imageUrl,
        rarity = rarity,
        price = price,
        description = description,
        types = types.joinToString(separator = "|"),
        setName = setName,
        number = number,
        artist = artist,
        source = source,
        isFavorite = isFavorite,
    )

fun UserDto.toDomain(): UserProfile =
    UserProfile(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        balance = 0.0,
        isVip = false,
    )

fun OrderDto.toDomain(): Order {
    val firstItem = items.firstOrNull()
    val title = firstItem?.card?.name ?: "Compra PokeMarket"
    val quantity = items.sumOf { it.quantity }
    val amount = firstItem?.let { it.unitPrice * it.quantity } ?: total
    return Order(
        id = id,
        title = title,
        quantity = quantity,
        amount = amount,
        statusId = statusId,
        status = if (statusId == 2 || status == "delivered") OrderStatus.Delivered else OrderStatus.ReadyForPickup,
        paymentMethod = paymentMethod,
        total = total,
    )
}

fun OrderDto.toEntity(): OrderEntity {
    val order = toDomain()
    return OrderEntity(
        id = id,
        userId = userId,
        title = order.title,
        quantity = order.quantity,
        amount = order.amount,
        statusId = statusId,
        status = if (order.status == OrderStatus.Delivered) "delivered" else "ready_for_pickup",
        paymentMethod = order.paymentMethod,
        total = order.total,
    )
}

fun OrderEntity.toDomain(): Order =
    Order(
        id = id,
        title = title,
        quantity = quantity,
        amount = amount,
        statusId = statusId,
        status = if (statusId == 2 || status == "delivered") OrderStatus.Delivered else OrderStatus.ReadyForPickup,
        paymentMethod = paymentMethod,
        total = total,
    )
