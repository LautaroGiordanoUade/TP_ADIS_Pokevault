package com.pokevault.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApi {
    @GET("pokemon")
    suspend fun listPokemon(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("name") name: String? = null,
    ): PokemonPageDto

    @GET("pokemon/search")
    suspend fun searchPokemon(
        @Query("name") name: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): PokemonPageDto

    @GET("pokemon/{pokemonId}")
    suspend fun getPokemon(@Path("pokemonId") pokemonId: Int): PokemonDto

    @GET("pokemon/vault/{userId}")
    suspend fun getVault(@Path("userId") userId: Int): List<VaultItemDto>

    @POST("pokemon/vault/{userId}/add")
    suspend fun addToVault(
        @Path("userId") userId: Int,
        @Body payload: AddVaultItemRequestDto,
    )

    @DELETE("pokemon/vault/{userId}/remove/{pokemonId}")
    suspend fun removeFromVault(
        @Path("userId") userId: Int,
        @Path("pokemonId") pokemonId: Int,
    )
}
