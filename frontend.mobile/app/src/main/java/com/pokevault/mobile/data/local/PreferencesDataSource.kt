package com.pokevault.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pokevault.mobile.domain.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val onboardingSeen: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[Keys.OnboardingSeen] ?: false
    }

    val session: Flow<UserSession> = context.userPreferencesDataStore.data.map { preferences ->
        UserSession(
            token = preferences[Keys.Token],
            userId = preferences[Keys.Id],
            name = preferences[Keys.Name],
            email = preferences[Keys.Email],
            avatarUrl = preferences[Keys.AvatarUrl],
        )
    }

    val profile: Flow<UserProfile?> = context.userPreferencesDataStore.data.map { preferences ->
        val id = preferences[Keys.Id]
        val name = preferences[Keys.Name]
        val email = preferences[Keys.Email]
        if (id == null || name == null || email == null) {
            null
        } else {
            UserProfile(
                id = id,
                name = name,
                email = email,
                avatarUrl = preferences[Keys.AvatarUrl],
                balance = preferences[Keys.Balance] ?: 125_000.00,
                isVip = preferences[Keys.IsVip] ?: true,
            )
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.Id] = profile.id
            preferences[Keys.Name] = profile.name
            preferences[Keys.Email] = profile.email
            profile.avatarUrl?.let { preferences[Keys.AvatarUrl] = it }
            preferences[Keys.Balance] = profile.balance
            preferences[Keys.IsVip] = profile.isVip
        }
    }

    suspend fun saveSession(token: String, profile: UserProfile) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.Token] = token
            preferences[Keys.Id] = profile.id
            preferences[Keys.Name] = profile.name
            preferences[Keys.Email] = profile.email
            profile.avatarUrl?.let { preferences[Keys.AvatarUrl] = it }
            preferences[Keys.Balance] = profile.balance
            preferences[Keys.IsVip] = profile.isVip
        }
    }

    suspend fun markOnboardingSeen() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.OnboardingSeen] = true
        }
    }

    suspend fun clearSession() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.remove(Keys.Token)
            preferences.remove(Keys.Id)
            preferences.remove(Keys.Name)
            preferences.remove(Keys.Email)
            preferences.remove(Keys.AvatarUrl)
            preferences.remove(Keys.Balance)
            preferences.remove(Keys.IsVip)
        }
    }

    private object Keys {
        val Token = stringPreferencesKey("session_token")
        val Id = androidx.datastore.preferences.core.intPreferencesKey("profile_id")
        val Name = stringPreferencesKey("profile_name")
        val Email = stringPreferencesKey("profile_email")
        val AvatarUrl = stringPreferencesKey("profile_avatar_url")
        val Balance = doublePreferencesKey("profile_balance")
        val IsVip = booleanPreferencesKey("profile_is_vip")
        val OnboardingSeen = booleanPreferencesKey("onboarding_seen")
    }
}

data class UserSession(
    val token: String?,
    val userId: Int?,
    val name: String?,
    val email: String?,
    val avatarUrl: String?,
) {
    val isLoggedIn: Boolean = !token.isNullOrBlank()
}
