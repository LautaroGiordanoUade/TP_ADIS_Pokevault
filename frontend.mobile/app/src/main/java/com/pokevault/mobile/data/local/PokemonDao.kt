package com.pokevault.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemon_cards ORDER BY name")
    fun observeCards(): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon_cards WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun observeCards(query: String): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon_cards WHERE isFavorite = 1 ORDER BY name")
    fun observeFavorites(): Flow<List<PokemonEntity>>

    @Query("SELECT COUNT(*) FROM pokemon_cards")
    suspend fun countCards(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCards(cards: List<PokemonEntity>)

    @Query("UPDATE pokemon_cards SET isFavorite = :isFavorite WHERE id = :cardId")
    suspend fun setFavorite(cardId: String, isFavorite: Boolean)
}
