package com.pokevault.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY name ASC")
    fun observeItems(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_items WHERE id = :cardId LIMIT 1")
    suspend fun getItemById(cardId: Int): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :cardId")
    suspend fun updateQuantity(cardId: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :cardId")
    suspend fun deleteById(cardId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
