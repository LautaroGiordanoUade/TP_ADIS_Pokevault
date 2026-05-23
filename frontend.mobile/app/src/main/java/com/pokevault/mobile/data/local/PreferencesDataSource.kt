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
            preferences[Keys.Balance] = profile.balance
            preferences[Keys.IsVip] = profile.isVip
        }
    }

    private object Keys {
        val Id = stringPreferencesKey("profile_id")
        val Name = stringPreferencesKey("profile_name")
        val Email = stringPreferencesKey("profile_email")
        val Balance = doublePreferencesKey("profile_balance")
        val IsVip = booleanPreferencesKey("profile_is_vip")
    }
}
