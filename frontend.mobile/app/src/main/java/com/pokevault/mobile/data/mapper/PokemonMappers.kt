package com.pokevault.mobile.data.mapper

import com.pokevault.mobile.data.local.PokemonEntity
import com.pokevault.mobile.data.remote.PokemonDto
import com.pokevault.mobile.domain.model.PokemonCard

fun PokemonDto.toEntity(isFavorite: Boolean = false): PokemonEntity =
    PokemonEntity(
        id = id,
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

fun PokemonCard.toEntity(): PokemonEntity =
    PokemonEntity(
        id = id,
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
