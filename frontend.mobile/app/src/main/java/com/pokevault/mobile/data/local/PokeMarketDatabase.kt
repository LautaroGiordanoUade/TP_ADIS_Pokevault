package com.pokevault.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PokeMarketDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}
