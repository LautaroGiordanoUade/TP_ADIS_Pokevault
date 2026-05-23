package com.pokevault.mobile.data.repository

import com.pokevault.mobile.data.local.PreferencesDataSource
import com.pokevault.mobile.data.mapper.toDomain
import com.pokevault.mobile.data.remote.AuthApi
import com.pokevault.mobile.data.remote.GoogleLoginRequestDto
import com.pokevault.mobile.data.remote.OrderApi
import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    val profile: Flow<UserProfile?>
    suspend fun loginWithGoogle(idToken: String)
    suspend fun logout()
    suspend fun refreshProfile()
    suspend fun getOrders(): List<Order>
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val authApi: AuthApi,
    private val orderApi: OrderApi,
) : ProfileRepository {
    override val profile: Flow<UserProfile?> = preferencesDataSource.profile

    override suspend fun loginWithGoogle(idToken: String) {
        val response = authApi.loginWithGoogle(GoogleLoginRequestDto(idToken))
        preferencesDataSource.saveSession(response.token, response.user.toDomain())
    }

    override suspend fun logout() {
        runCatching { authApi.logout() }
        preferencesDataSource.clearSession()
    }

    override suspend fun refreshProfile() {
        val token = preferencesDataSource.session.first().token.orEmpty()
        if (token.isBlank()) {
            return
        }
        preferencesDataSource.saveSession(token, authApi.me().toDomain())
    }

    override suspend fun getOrders(): List<Order> = orderApi.myOrders().map { it.toDomain() }
}
