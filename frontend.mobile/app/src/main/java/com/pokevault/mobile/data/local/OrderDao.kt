package com.pokevault.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY id DESC")
    suspend fun getOrders(userId: Int): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrders(orders: List<OrderEntity>)

    @Query("DELETE FROM orders WHERE userId = :userId")
    suspend fun clearOrders(userId: Int)
}
