package com.pokevault.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VaultApi {
    @GET("vault/me")
    suspend fun myVault(): List<VaultItemDto>

    @POST("vault/me/items")
    suspend fun addItem(@Body payload: AddVaultItemRequestDto)

    @DELETE("vault/me/items/{pokemonId}")
    suspend fun removeItem(@Path("pokemonId") pokemonId: Int)
}
