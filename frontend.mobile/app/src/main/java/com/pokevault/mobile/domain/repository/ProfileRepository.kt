package com.pokevault.mobile.domain.repository

import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profile: Flow<UserProfile?>
    val isLoggedIn: Flow<Boolean>
    suspend fun loginWithGoogle(idToken: String)
    suspend fun logout()
    suspend fun refreshProfile()
    suspend fun getOrders(): List<Order>
}
