package com.pokevault.mobile.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PokemonLocalDataSource @Inject constructor(
    private val dao: PokemonDao,
) {
    fun observeCards(query: String): Flow<List<PokemonEntity>> =
        if (query.isBlank()) dao.observeCards() else dao.observeCards(query.trim())

    fun observeFavorites(): Flow<List<PokemonEntity>> = dao.observeFavorites()

    suspend fun isEmpty(): Boolean = dao.countCards() == 0

    suspend fun saveCards(cards: List<PokemonEntity>) = dao.upsertCards(cards)

    suspend fun setFavorite(cardId: Int, isFavorite: Boolean) {
        dao.setFavorite(cardId, isFavorite)
    }

    suspend fun getCardById(cardId: Int): PokemonEntity? = dao.getCardById(cardId)
}
