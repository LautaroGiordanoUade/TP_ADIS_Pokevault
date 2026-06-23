package com.pokevault.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class, OrderEntity::class, CartEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class PokeMarketDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
}
