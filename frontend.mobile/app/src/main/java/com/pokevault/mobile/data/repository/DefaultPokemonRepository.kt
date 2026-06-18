package com.pokevault.mobile.data.repository

import com.pokevault.mobile.data.local.PokemonLocalDataSource
import com.pokevault.mobile.data.local.PreferencesDataSource
import com.pokevault.mobile.data.mapper.toDomain
import com.pokevault.mobile.data.mapper.toEntity
import com.pokevault.mobile.data.remote.AddVaultItemRequestDto
import com.pokevault.mobile.data.remote.PokemonRemoteDataSource
import com.pokevault.mobile.data.remote.VaultApi
import com.pokevault.mobile.domain.model.PokemonCard
import com.pokevault.mobile.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// TODO Data layer & Arquitectura MVVM:
// 1. Inyección de dependencias directas de API: La clase DefaultPokemonRepository depende directamente de VaultApi (cliente de Retrofit).
//    Para cumplir con el principio de abstracción, se debe crear un VaultRemoteDataSource en:
//    data/datasource/remote/VaultRemoteDataSource.kt
//    y hacer que el repositorio interactúe con este origen de datos en lugar de la API directamente.
// 2. Acoplamiento de responsabilidades de Sesión: Se inyecta PreferencesDataSource para comprobar si el usuario está logueado en métodos como toggleFavorite.
//    Sería mejor abstraer esta lógica delegando el estado de sesión a un AuthRepository o a un caso de uso (UseCase),
//    evitando que el repositorio de Pokémon conozca detalles de persistencia de usuario y disminuyendo el acoplamiento.
@Singleton
class DefaultPokemonRepository @Inject constructor(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonLocalDataSource,
    private val vaultApi: VaultApi,
    private val preferencesDataSource: PreferencesDataSource,
) : PokemonRepository {
    private val favorites = MutableStateFlow<List<PokemonCard>>(emptyList())

    override fun observeCards(query: String): Flow<List<PokemonCard>> =
        localDataSource.observeCards(query).map { entities -> entities.map { it.toDomain() } }

    override fun observeFavorites(): Flow<List<PokemonCard>> = favorites

    override suspend fun refreshCards(query: String) {
        val response = remoteDataSource.listCards(
            page = 1,
            pageSize = 15,
            name = query.ifBlank { null },
        )
        val entities = response.items.map { it.toEntity() }
        if (entities.isNotEmpty()) {
            localDataSource.saveCards(entities)
        }
    }

    override suspend fun toggleFavorite(card: PokemonCard) {
        if (!preferencesDataSource.session.first().isLoggedIn) return
        if (card.isFavorite) {
            vaultApi.removeItem(card.id)
        } else {
            vaultApi.addItem(AddVaultItemRequestDto(card.id))
        }
        refreshFavorites()
    }

    override suspend fun listCards(query: String, page: Int, pageSize: Int): List<PokemonCard> {
        val favoriteIds = if (preferencesDataSource.session.first().isLoggedIn) {
            refreshFavorites().map { it.id }.toSet()
        } else {
            emptySet()
        }
        return remoteDataSource.listCards(
            page = page,
            pageSize = pageSize,
            name = query.ifBlank { null },
        ).items.map { it.toDomain(isFavorite = it.id in favoriteIds) }
    }

    override suspend fun listHomeCards(): List<PokemonCard> {
        return if (preferencesDataSource.session.first().isLoggedIn) {
            refreshFavorites()
        } else {
            favorites.value = emptyList()
            listCards(query = "gengar", page = 1, pageSize = 3)
        }
    }

    override suspend fun refreshFavorites(): List<PokemonCard> {
        if (!preferencesDataSource.session.first().isLoggedIn) {
            favorites.value = emptyList()
            return emptyList()
        }
        val cards = vaultApi.myVault().mapNotNull { it.card?.toDomain(isFavorite = true) }
        favorites.value = cards
        return cards
    }

    override suspend fun getCard(cardId: Int): PokemonCard? {
        val isLoggedIn = preferencesDataSource.session.first().isLoggedIn
        val favoriteIds = if (isLoggedIn) refreshFavorites().map { it.id }.toSet() else emptySet()
        val localCard = localDataSource.getCardById(cardId)?.toDomain()?.let { card ->
            card.copy(isFavorite = card.id in favoriteIds)
        }
        if (localCard != null) return localCard

        return runCatching {
            remoteDataSource.api.getPokemon(cardId).toDomain(isFavorite = cardId in favoriteIds)
        }.getOrNull()
    }
}
