package com.pokevault.mobile.data.repository

import com.pokevault.mobile.data.local.PreferencesDataSource
import com.pokevault.mobile.data.local.OrderLocalDataSource
import com.pokevault.mobile.data.mapper.toDomain
import com.pokevault.mobile.data.mapper.toEntity
import com.pokevault.mobile.data.remote.AuthApi
import com.pokevault.mobile.data.remote.GoogleLoginRequestDto
import com.pokevault.mobile.data.remote.OrderApi
import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.UserProfile
import com.pokevault.mobile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val orderLocalDataSource: OrderLocalDataSource,
    private val authApi: AuthApi,
    private val orderApi: OrderApi,
) : ProfileRepository {
    override val profile: Flow<UserProfile?> = preferencesDataSource.profile
    override val isLoggedIn: Flow<Boolean> = preferencesDataSource.session.map { it.isLoggedIn }

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

    override suspend fun getOrders(): List<Order> {
        val userId = preferencesDataSource.profile.first()?.id ?: return emptyList()
        val cachedOrders = orderLocalDataSource.getOrders(userId).map { it.toDomain() }
        return runCatching {
            val remoteOrders = orderApi.myOrders()
            orderLocalDataSource.replaceOrders(userId, remoteOrders.map { it.toEntity() })
            remoteOrders.map { it.toDomain() }
        }.getOrElse {
            cachedOrders
        }
    }
}
