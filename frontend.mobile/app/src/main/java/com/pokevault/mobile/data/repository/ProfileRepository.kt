package com.pokevault.mobile.data.repository

import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.OrderStatus
import com.pokevault.mobile.domain.model.UserProfile
import com.pokevault.mobile.data.local.PreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    val profile: Flow<UserProfile?>
    val orders: Flow<List<Order>>
    suspend fun login(name: String, email: String)
    suspend fun quickLogin()
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
) : ProfileRepository {
    private val _orders = MutableStateFlow(
        listOf(
            Order("PKM-A7X2", "Pikachu Illustrator", 1, 4999.00, OrderStatus.ReadyForPickup, "Visa", 5011.50),
            Order("PKM-B9P1", "Mewtwo EX Full Art", 2, 270.00, OrderStatus.Delivered, "MasterCard", 282.50),
        ),
    )

    override val profile: Flow<UserProfile?> = preferencesDataSource.profile
    override val orders: Flow<List<Order>> = _orders.asStateFlow()

    override suspend fun login(name: String, email: String) {
        preferencesDataSource.saveProfile(UserProfile("ash-ketchum", name, email, 125_000.00, true))
    }

    override suspend fun quickLogin() {
        login("Ash Ketchum", "ash.ketchum@pallet.org")
    }
}
