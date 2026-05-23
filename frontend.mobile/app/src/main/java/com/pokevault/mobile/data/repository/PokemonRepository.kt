package com.pokevault.mobile.data.repository

import com.pokevault.mobile.domain.model.PokemonCard
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun observeCards(query: String = ""): Flow<List<PokemonCard>>
    fun observeFavorites(): Flow<List<PokemonCard>>
    suspend fun refreshCards(query: String = "")
    suspend fun toggleFavorite(card: PokemonCard)
    suspend fun listCards(query: String = "", page: Int = 1, pageSize: Int = 15): List<PokemonCard>
    suspend fun listHomeCards(): List<PokemonCard>
    suspend fun refreshFavorites(): List<PokemonCard>
    suspend fun getCard(cardId: Int): PokemonCard?
}
