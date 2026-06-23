package com.pokevault.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val id: Int,
    val externalId: String,
    val name: String,
    val imageUrl: String,
    val rarity: String?,
    val price: Double,
    val description: String?,
    val types: String,
    val setName: String?,
    val number: String?,
    val artist: String?,
    val source: String,
    val quantity: Int,
)
