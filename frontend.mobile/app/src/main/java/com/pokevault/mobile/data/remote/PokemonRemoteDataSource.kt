package com.pokevault.mobile.data.remote

import javax.inject.Inject

class PokemonRemoteDataSource @Inject constructor(
    val api: PokemonApi,
) {
    suspend fun listCards(page: Int, pageSize: Int, name: String?): PokemonPageDto =
        api.listPokemon(page = page, pageSize = pageSize, name = name)

    suspend fun searchCards(name: String, page: Int, pageSize: Int): PokemonPageDto =
        api.searchPokemon(name = name, page = page, pageSize = pageSize)

    suspend fun favoriteCards(userId: Int): List<VaultItemDto> = api.getVault(userId)

    suspend fun addFavorite(userId: Int, pokemonId: Int) {
        api.addToVault(userId, AddVaultItemRequestDto(pokemonId))
    }

    suspend fun removeFavorite(userId: Int, pokemonId: Int) {
        api.removeFromVault(userId, pokemonId)
    }
}
