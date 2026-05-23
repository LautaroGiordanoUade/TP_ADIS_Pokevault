package com.pokevault.mobile.data.remote

import javax.inject.Inject

class PokemonRemoteDataSource @Inject constructor(
    private val api: PokemonApi,
) {
    suspend fun listCards(page: Int, pageSize: Int, name: String?): PokemonPageDto =
        api.listPokemon(page = page, pageSize = pageSize, name = name)

    suspend fun searchCards(name: String, page: Int, pageSize: Int): PokemonPageDto =
        api.searchPokemon(name = name, page = page, pageSize = pageSize)

    suspend fun favoriteCards(userId: String): List<VaultItemDto> = api.getVault(userId)

    suspend fun addFavorite(userId: String, pokemonId: String) {
        api.addToVault(userId, AddVaultItemRequestDto(pokemonId))
    }

    suspend fun removeFavorite(userId: String, pokemonId: String) {
        api.removeFromVault(userId, pokemonId)
    }
}
