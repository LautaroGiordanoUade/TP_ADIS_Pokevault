package com.pokevault.mobile.data.repository

import com.pokevault.mobile.data.local.PokemonLocalDataSource
import com.pokevault.mobile.data.mapper.toDomain
import com.pokevault.mobile.data.mapper.toEntity
import com.pokevault.mobile.data.remote.PokemonRemoteDataSource
import com.pokevault.mobile.domain.model.PokemonCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultPokemonRepository @Inject constructor(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonLocalDataSource,
) : PokemonRepository {
    override fun observeCards(query: String): Flow<List<PokemonCard>> =
        localDataSource.observeCards(query).map { cards -> cards.map { it.toDomain() } }

    override fun observeFavorites(): Flow<List<PokemonCard>> =
        localDataSource.observeFavorites().map { cards -> cards.map { it.toDomain() } }

    override suspend fun refreshCards(query: String) {
        val remoteCards = runCatching {
            remoteDataSource.listCards(page = 1, pageSize = 60, name = query.ifBlank { null }).items
        }.getOrNull()

        val cards = remoteCards?.map { it.toEntity() } ?: if (localDataSource.isEmpty()) {
            DemoCards.cards.map { it.toEntity() }
        } else {
            emptyList()
        }

        if (cards.isNotEmpty()) {
            localDataSource.saveCards(cards)
        }
    }

    override suspend fun toggleFavorite(card: PokemonCard) {
        val nextValue = !card.isFavorite
        localDataSource.setFavorite(card.id, nextValue)
        runCatching {
            if (nextValue) {
                remoteDataSource.addFavorite(DEMO_USER_ID, card.id)
            } else {
                remoteDataSource.removeFavorite(DEMO_USER_ID, card.id)
            }
        }
    }

    private companion object {
        const val DEMO_USER_ID = "ash-ketchum"
    }
}
